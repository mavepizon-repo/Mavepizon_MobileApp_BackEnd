package com.example.MpApp;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the real OTP migration files against a real PostgreSQL engine.
 *
 * <p>Purpose is narrow and deliberate: the Flyway scripts are the one part of
 * this change that cannot be unit tested, and a typo in them is a failed
 * deploy. A throwaway cluster is used so the developer's own database is never
 * touched.
 *
 * <p>Note on scope: this project has never been bootstrappable from its
 * migrations -- no script creates the domain tables (students, admin, ...), and
 * V2/V3 already alter tables that no migration creates. So the stubs below
 * stand in for the tables that exist in the real database. That is the
 * situation V4 will meet in production, and it is the situation under test.
 */
public class OtpMigrationScriptIT {

    private static final String[] DOMAIN_TABLES = {
            "students", "admin", "college_staff", "office_staff", "team_lead", "freelancer"
    };

    private static EmbeddedPostgres postgres;
    private static Connection connection;

    @BeforeAll
    static void setUp() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl("postgres", "postgres"), "postgres", "postgres");

        // Stand-ins for the tables the real database already has.
        try (Statement s = connection.createStatement()) {
            for (String t : DOMAIN_TABLES) {
                s.execute("CREATE TABLE " + t + " (id BIGINT PRIMARY KEY)");
            }
        }

        applyScript("db/migration/V0.1__create_otps.sql");
        // V1 creates this index; V4 must drop it without erroring if present.
        try (Statement s = connection.createStatement()) {
            s.execute("CREATE INDEX IF NOT EXISTS idx_otps_email ON otps(email)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_otps_expiry_time ON otps(expiry_time)");
        }
        applyScript("db/migration/V4__otp_hardening_and_token_version.sql");
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (postgres != null) {
            postgres.close();
        }
    }

    private static void applyScript(String resource) throws IOException, SQLException {
        try (InputStream in = OtpMigrationScriptIT.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(in).as("migration %s must be on the classpath", resource).isNotNull();
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement s = connection.createStatement()) {
                s.execute(sql);
            }
        }
    }

    private static List<String> columnsOf(String table) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, table, null)) {
            while (rs.next()) {
                names.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return names;
    }

    @Test
    @DisplayName("V4 runs cleanly and leaves every column OtpEntity maps")
    void otpsTableHasEveryMappedColumn() throws SQLException {
        assertThat(columnsOf("otps")).contains(
                "id", "email", "role", "otp_code", "expiry_time",
                "verification_attempts", "last_sent_at", "verified", "verified_at");
    }

    @Test
    @DisplayName("V4 adds token_version to all six role tables")
    void everyRoleTableGetsTokenVersion() throws SQLException {
        for (String t : DOMAIN_TABLES) {
            assertThat(columnsOf(t)).as("%s", t).contains("token_version");
        }
    }

    @Test
    @DisplayName("role is NOT NULL and verified defaults to false")
    void roleBecomesMandatory() throws SQLException {
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT is_nullable, column_default FROM information_schema.columns "
                             + "WHERE table_name='otps' AND column_name='role'")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("is_nullable")).isEqualTo("NO");
        }
        try (Statement s = connection.createStatement()) {
            assertThatThrownBy(() -> s.execute(
                    "INSERT INTO otps (email, otp_code, expiry_time) VALUES ('a@b.com','x',now())"))
                    .isInstanceOf(SQLException.class)
                    .satisfies(e -> assertThat(((SQLException) e).getSQLState()).isEqualTo("23502"));
        }
    }

    @Test
    @DisplayName("one live OTP per account per role")
    void uniqueIndexOnEmailAndRole() throws Exception {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("INSERT INTO otps (email, role, otp_code, expiry_time) "
                    + "VALUES ('dual@example.com','STUDENT','x', now() + interval '5 min')");
            s.executeUpdate("INSERT INTO otps (email, role, otp_code, expiry_time) "
                    + "VALUES ('dual@example.com','ADMIN','y', now() + interval '5 min')");
        }

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM otps WHERE email='dual@example.com'")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(2);
        }

        try (Statement s = connection.createStatement()) {
            assertThatThrownBy(() -> s.executeUpdate(
                    "INSERT INTO otps (email, role, otp_code, expiry_time) "
                            + "VALUES ('dual@example.com','STUDENT','z', now() + interval '5 min')"))
                    .isInstanceOf(SQLException.class)
                    .satisfies(e -> assertThat(((SQLException) e).getSQLState()).startsWith("23"));
        }
    }

    @Test
    @DisplayName("V4 drops the old email-only index and leaves a role-aware one")
    void emailOnlyIndexIsReplaced() throws Exception {
        List<String> indexes = new ArrayList<>();
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT indexname FROM pg_indexes WHERE tablename='otps'")) {
            while (rs.next()) {
                indexes.add(rs.getString(1));
            }
        }
        assertThat(indexes).contains("ux_otps_email_role");
        assertThat(indexes).doesNotContain("idx_otps_email");
    }

    @Test
    @DisplayName("the cleanup sweep keeps a freshly verified code but drops a stale one")
    void cleanupSweepSparesFreshVerifiedRows() throws Exception {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("DELETE FROM otps");
            s.executeUpdate("INSERT INTO otps (email, role, otp_code, expiry_time, verified, verified_at) "
                    + "VALUES ('keep@example.com','STUDENT','x', now() + interval '5 min', true, now())");
            s.executeUpdate("INSERT INTO otps (email, role, otp_code, expiry_time, verified, verified_at) "
                    + "VALUES ('old@example.com','STUDENT','y', now() - interval '5 min', true, now() - interval '3 hours')");
        }

        // Same predicate as OtpRepository.deleteStale, expressed in SQL because
        // JPQL only runs through Hibernate in the application.
        int deleted;
        try (var ps = connection.prepareStatement(
                "DELETE FROM otps WHERE expiry_time < now() "
                        + "OR (verified = true AND verified_at < now() - interval '1 hour')")) {
            deleted = ps.executeUpdate();
        }
        assertThat(deleted).isEqualTo(1);

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT email FROM otps ORDER BY email")) {
            List<String> remaining = new ArrayList<>();
            while (rs.next()) {
                remaining.add(rs.getString(1));
            }
            // The unexpired, recently verified code is still there for the reset step.
            assertThat(remaining).containsExactly("keep@example.com");
        }
    }
}
