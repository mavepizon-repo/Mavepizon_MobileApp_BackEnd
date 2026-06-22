package com.example.MpApp.config;

import com.example.MpApp.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public UserDetailsService userDetailsService(
            CustomUserDetailsService customUserDetailsService) {
        return customUserDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");

        configuration.addAllowedMethod("*");

        configuration.addAllowedHeader("*");

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider provider) {

        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider provider) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // ================= PUBLIC =================

                        .requestMatchers(
                                "/api/admin/register",
                                "/api/admin/login",

                                "/api/teamlead/login",
                                "/api/officestaff/login",

                                "/api/student/register",
                                "/api/student/login",


                                "/api/student/forgot-password/send-otp",
                                "/api/student/forgot-password/verify-otp",
                                "/api/student/forgot-password/reset",
                                "/api/student/forgot-password/**",

                                "/api/collegestaff/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // ================= ADMIN =================

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // ================= TEAM LEAD =================

                        .requestMatchers("/api/teamlead/**")
                        .hasRole("TEAM_LEAD")

                        // ================= TELECALLING =================

                        .requestMatchers(
                                "/api/officestaff/telecalling/**"
                        )
                        .hasRole("OFFICE_STAFF")

                        .requestMatchers(
                                "/api/trainer/**"
                        )
                        .hasRole("OFFICE_STAFF")

                        // ================= OFFICE STAFF =================

                        .requestMatchers("/api/officestaff/**")
                        .hasRole("OFFICE_STAFF")

                        // ================= COLLEGE STAFF =================

                        .requestMatchers("/api/collegestaff/**")
                        .hasRole("COLLEGE_STAFF")

                        // ================= COURSE =================

                        .requestMatchers(
                                "/api/course/create",
                                "/api/course/update/**",
                                "/api/course/delete/**"
                        ).hasRole("TEAM_LEAD")

                        .requestMatchers(
                                "/api/offered-course/create",
                                "/api/offered-course/update/**",
                                "/api/offered-course/delete/**"
                        ).hasRole("TEAM_LEAD")

                        .requestMatchers(
                                "/api/course/get-all",
                                "/api/course/get/**",
                                "/api/offered-course/get-all",
                                "/api/offered-course/get/**"
                        )
                        .hasAnyRole(
                                "STUDENT",
                                "TEAM_LEAD",
                                "ADMIN",
                                "COLLEGE_STAFF"
                        )

                        // ================= STUDENT =================

                        .requestMatchers("/api/student/**")
                        .hasRole("STUDENT")

                        // ================= STUDENT COURSE =================

                        .requestMatchers(
                                "/api/student-course/register",
                                "/api/student-course/my-courses",
                                "/api/student-course/my-registrations"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/student-course/get-all",
                                "/api/student-course/get/**",
                                "/api/student-course/delete/**"
                        )
                        .hasAnyRole(
                                "TEAM_LEAD",
                                "ADMIN"
                        )

                        // ================= CASH PAYMENT =================

                        .requestMatchers(
                                "/api/cash-payment/create",
                                "/api/cash-payment/my-payments"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                "/api/cash-payment/get-all",
                                "/api/cash-payment/get/**",
                                "/api/cash-payment/status/**",
                                "/api/cash-payment/staff/**",
                                "/api/cash-payment/approve/**",
                                "/api/cash-payment/reject/**"
                        )
                        .hasAnyRole(
                                "OFFICE_STAFF",
                                "ADMIN"
                        )

                        .anyRequest()
                        .authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(provider)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}