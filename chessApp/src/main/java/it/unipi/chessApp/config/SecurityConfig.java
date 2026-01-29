package it.unipi.chessApp.config;

import it.unipi.chessApp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Public user endpoints
                        .requestMatchers(HttpMethod.GET, "/users", "/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                        
                        // Public game endpoints
                        .requestMatchers(HttpMethod.GET, "/games", "/games/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/games/live/*/status").permitAll()
                        
                        // Public club endpoints
                        .requestMatchers(HttpMethod.GET, "/clubs", "/clubs/*").permitAll()
                        
                        // Public tournament endpoints
                        .requestMatchers(HttpMethod.GET, "/tournaments", "/tournaments/active", "/tournaments/*", "/tournaments/*/*").permitAll()
                        
                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.DELETE, "/games/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/tournaments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clubs/**").hasRole("ADMIN")
                        .requestMatchers("/users/promote").hasRole("ADMIN")
                        
                        // All other requests require authentication (including /games/stats/**)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
