package lk.oracene.hardware_management_api.config;

import lk.oracene.hardware_management_api.security.JwtAuthenticationEntryPoint;
import lk.oracene.hardware_management_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Auth (change-password, logout) — any authenticated user
                        .requestMatchers("/api/v1/auth/**").authenticated()

                        // Users — ADMIN only
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // Categories & Products — read: all roles, write: ADMIN + MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**", "/api/v1/products/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/v1/categories/**", "/api/v1/products/**")
                                .hasAnyRole("ADMIN", "MANAGER")

                        // Customers — read: all roles, write: ADMIN + MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/v1/customers/**")
                                .hasAnyRole("ADMIN", "MANAGER")

                        // Sales — CASHIER can create; all roles can read
                        .requestMatchers(HttpMethod.GET, "/api/v1/sales/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/v1/sales/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")

                        // Customer Payments — CASHIER can create; all roles can read
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer-payments/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers("/api/v1/customer-payments/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")

                        // Print & Printer Settings — all roles
                        .requestMatchers("/api/v1/print/**", "/api/v1/printer/**")
                                .hasAnyRole("ADMIN", "MANAGER", "CASHIER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
