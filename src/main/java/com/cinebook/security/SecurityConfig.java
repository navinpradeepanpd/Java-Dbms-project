package com.cinebook.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * Spring Security Configuration for CineBook 2.0.
 * Enforces mandatory authentication across all customer pages and REST endpoints,
 * BCrypt password hashing, session invalidation on logout, and role-based access control.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for simplicity in single-host embedded REST/HTML app
            .authorizeHttpRequests(auth -> auth
                // Public assets and authentication endpoints
                .requestMatchers(
                    "/login.html",
                    "/register.html",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/videos/**",
                    "/favicon.ico",
                    "/api/auth/**",
                    "/api/tmdb/**",
                    "/api/tickets/verify"
                ).permitAll()
                // Admin specific endpoints
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // All other HTML pages and REST APIs require authenticated session
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":true,\"message\":\"Login successful\",\"redirectUrl\":\"/index.html\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Invalid username/phone or password\"}");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":true,\"message\":\"Logged out successfully\",\"redirectUrl\":\"/login.html\"}");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // For HTML requests redirect to /login.html, for API requests return 401
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    },
                    request -> request.getRequestURI().startsWith("/api/")
                )
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login.html"),
                    request -> !request.getRequestURI().startsWith("/api/")
                )
            )
            .sessionManagement(session -> session
                .maximumSessions(2)
            );

        return http.build();
    }
}
