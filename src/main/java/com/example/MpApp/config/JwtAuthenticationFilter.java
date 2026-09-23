package com.example.MpApp.config;

import com.example.MpApp.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Lazy
    private CustomUserDetailsService customUserDetailsService;

    // Only authentication endpoints that must be reachable without a JWT.
    // Password-change endpoints are intentionally NOT public.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PUBLIC_EXACT_URLS = List.of(
            "/api/admin/register",
            "/api/admin/login",
            "/api/freelancer/login",
            "/api/teamlead/login",
            "/api/officestaff/login",
            "/api/student/register",
            "/api/student/login",
            "/api/collegestaff/login"
    );

    private static final List<String> PUBLIC_PREFIX_URLS = List.of(
            "/api/admin/forgot-password/",
            "/api/teamlead/forgot-password/",
            "/api/officestaff/forgot-password/",
            "/api/student/forgot-password/",
            "/api/collegestaff/forgot-password/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        String path = request.getRequestURI();

        boolean publicRoute = PUBLIC_EXACT_URLS.contains(path)
                || PUBLIC_PREFIX_URLS.stream().anyMatch(path::startsWith);

        if (publicRoute || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
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
            String role = jwtService.extractRole(token);

            if (email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Bind the token to the role issued at login. This prevents an email
                // that exists in multiple user tables from gaining all of those roles.
                String requiredAuthority = "ROLE_" + role;
                boolean roleMatches = userDetails.getAuthorities().stream()
                        .anyMatch(a -> requiredAuthority.equals(a.getAuthority()));
                boolean accountActive = customUserDetailsService.isAccountActive(email, role);

                if (roleMatches && accountActive && jwtService.isTokenValid(token, userDetails)) {
                    GrantedAuthority issuedAuthority = new SimpleGrantedAuthority(requiredAuthority);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, List.of(issuedAuthority));

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    writeError(response, request, "INVALID_TOKEN", "Invalid Token");
                    return;
                }
            } else if (email != null && role == null) {
                writeError(response, request, "INVALID_TOKEN", "Invalid Token");
                return;
            }
        } catch (Exception e) {
            writeError(response, request, "INVALID_TOKEN", "Invalid or Expired Token");
            return;
        }

        filterChain.doFilter(request, response);


    }
    private void writeError(HttpServletResponse response, HttpServletRequest request, String errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("errorCode", errorCode);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), body);
    }

}