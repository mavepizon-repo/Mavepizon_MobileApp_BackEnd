package com.example.MpApp.config;

import com.example.MpApp.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Lazy
    private CustomUserDetailsService customUserDetailsService;

    // Fully synced with SecurityConfig public routes
    private static final List<String> PUBLIC_URLS = List.of(
            "/api/admin/register",
            "/api/admin/login",
            "/api/admin/change-password",

            "/api/teamlead/login",
            "/api/teamlead/change-password",

            "/api/officestaff/login",
            "/api/officestaff/change-password",

            "/api/student/register",
            "/api/student/login",
            "/api/student/change-password",

            "/api/collegestaff/login",
            "/api/collegestaff/change-password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        /*
        ============================================================
        SKIP PUBLIC ROUTES & ANY FORGOT-PASSWORD OTP SUB-PATHS
        ============================================================
        */
        if (PUBLIC_URLS.contains(path) || path.contains("/forgot-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
        ============================
        GET TOKEN
        ============================
        */
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or Expired Token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}