-- OTP hardening + JWT invalidation support.
--
-- Two problems are fixed here:
--   1. The otps table was keyed on email alone, so the same address registered
--      under two roles (a student who also freelances, for example) would
--      overwrite its own OTP. Scoping to (email, role) fixes that.
--   2. There was no way to invalidate a token after a password change, so a
--      stolen JWT stayed usable until it expired. token_version gives every
--      account a counter that can be bumped to revoke all issued tokens.
--
-- V0.1 already creates the role and verified_at columns; IF NOT EXISTS keeps this
-- migration safe on databases that were created before those columns existed.

-- Existing rows are five-minute password-reset codes with no role attribution.
-- Clearing them lets role be made NOT NULL without inventing a backfill value.
DELETE FROM otps;

ALTER TABLE otps ADD COLUMN IF NOT EXISTS role VARCHAR(50);
ALTER TABLE otps ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE otps ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;

ALTER TABLE otps ALTER COLUMN role SET NOT NULL;

-- One live OTP per account per role.
DROP INDEX IF EXISTS idx_otps_email;
CREATE UNIQUE INDEX IF NOT EXISTS ux_otps_email_role ON otps(email, role);
CREATE INDEX IF NOT EXISTS idx_otps_expiry_verified ON otps(expiry_time, verified);

-- Token version per role table. Incremented on every password change so that
-- tokens minted before the change stop validating.
ALTER TABLE students      ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE admin         ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE college_staff ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE office_staff  ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE team_lead     ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE freelancer    ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
