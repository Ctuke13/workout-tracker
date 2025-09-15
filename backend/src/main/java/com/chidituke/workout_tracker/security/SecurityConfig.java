package com.chidituke.workout_tracker.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",    // React default
                "http://localhost:5173",    // Vite default
                "http://127.0.0.1:3000",    // Alternative localhost
                "http://127.0.0.1:5173"     // Alternative localhost
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count", "X-Page-Count"));

        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // ===================================================================
                    // 🌍 PUBLIC ACCESS - No authentication required
                    // ===================================================================
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/test/**").permitAll();
                    auth.requestMatchers("/api/health").permitAll();
                    auth.requestMatchers("/api/health/**").permitAll();
                    auth.requestMatchers("/api/ping").permitAll();
                    auth.requestMatchers("/api/health").permitAll();
                    auth.requestMatchers("/error").permitAll();
                    auth.requestMatchers("/api/public/**").permitAll();
                    auth.requestMatchers("/api/subscriptions/test/**").permitAll();
                    auth.requestMatchers("/api/users/professionals").permitAll();

                    // 🌍 PUBLIC EXERCISE LIBRARY - Browse, search, view exercises
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises").permitAll();                    // Browse/search exercises
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/popular").permitAll();            // Popular exercises
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/filters").permitAll();            // Available filters
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/goals").permitAll();              // Fitness goals
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/public/**").permitAll();          // Frontend-specific endpoints
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/type/**").permitAll();            // Exercises by type
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/{id}").permitAll();               // Individual exercise details

                    // 🌍 PUBLIC FAVORITES - Publicly accessible favorites data
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/favorites/trending").permitAll();      // Trending favorites
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/favorites/most-popular").permitAll();  // Most favorited

                    // ===================================================================
                    // 🌍 PUBLIC WORKOUT PLAN LIBRARY - Browse, search, view workout plans
                    // ===================================================================
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans").permitAll();                // Browse all public workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/popular").permitAll();        // Popular workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/trending").permitAll();       // Trending workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/statistics").permitAll();     // Workout plan statistics
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/category/**").permitAll();    // Plans by category
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/difficulty/**").permitAll();  // Plans by difficulty
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/search").permitAll();         // Search workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/highly-rated").permitAll();   // Highly rated plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/{id}").permitAll();           // Individual workout plan details

                    // ===================================================================
                    // 🔐 USER AUTHENTICATED - Requires login
                    // ===================================================================

                    // 🔐 USER-SPECIFIC EXERCISE OPERATIONS - New ExerciseUserController routes
                    auth.requestMatchers("/api/exercises/user/**").authenticated();                         // All user-specific operations

                    // 🔐 LEGACY USER EXERCISE ROUTES - Will be removed after migration
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/*/rate").authenticated();        // Rate exercises
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/*/record-usage").authenticated(); // Record usage
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/recommended").authenticated();     // Personal recommendations
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/workout-plan").authenticated();   // Create workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/insights").authenticated();        // User insights

                    // 🔐 USER FAVORITES - Personal favorites management
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/favorites").authenticated();           // Get user favorites
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/favorites/*/toggle").authenticated(); // Toggle favorite
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/favorites/ids").authenticated();       // Get favorite IDs
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/favorites/check").authenticated();    // Check multiple
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/favorites/bulk/**").authenticated();  // Bulk operations
                    auth.requestMatchers(HttpMethod.DELETE, "/api/exercises/favorites/**").authenticated();     // Delete operations

                    // 🔐 CALENDAR & WORKOUT TRACKING
                    auth.requestMatchers("/api/calendar/**").authenticated();                               // All calendar operations

                    // 🔐 AUTHENTICATED WORKOUT PLAN FEATURES - User-specific workout plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/accessible").authenticated();  // User's accessible plans based on subscription
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/my/**").authenticated();       // User's created plans
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/subscription-limits").authenticated(); // User's subscription limits
                    auth.requestMatchers(HttpMethod.POST, "/api/workout-plans/*/rate").authenticated();     // Rate workout plans
                    auth.requestMatchers(HttpMethod.POST, "/api/workout-plans/*/record-usage").authenticated(); // Record usage
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/recommended").authenticated(); // Personal recommendations

                    // ===================================================================
                    // 💼 PROFESSIONAL ROLE - Content creation
                    // ===================================================================
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises").hasRole("PROFESSIONAL");       // Create exercises
                    auth.requestMatchers(HttpMethod.PUT, "/api/exercises/**").hasRole("PROFESSIONAL");     // Update exercises

                    auth.requestMatchers(HttpMethod.POST, "/api/workout-plans").hasRole("PROFESSIONAL");   // Create workout plans
                    auth.requestMatchers(HttpMethod.PUT, "/api/workout-plans/**").hasRole("PROFESSIONAL"); // Update workout plans

                    // ===================================================================
                    // 🔒 ADMIN ROLE - Management operations
                    // ===================================================================
                    auth.requestMatchers("/api/exercises/admin/**").hasAnyRole("ADMIN", "PROFESSIONAL");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/exercises/**").hasRole("ADMIN");         // Delete exercises
                    auth.requestMatchers(HttpMethod.POST, "/api/exercises/bulk-action").hasRole("ADMIN");  // Bulk operations
                    auth.requestMatchers(HttpMethod.GET, "/api/exercises/analytics").hasRole("ADMIN");     // Analytics

                    auth.requestMatchers(HttpMethod.DELETE, "/api/workout-plans/**").hasRole("ADMIN");     // Delete workout plans
                    auth.requestMatchers(HttpMethod.POST, "/api/workout-plans/bulk-action").hasRole("ADMIN"); // Bulk operations
                    auth.requestMatchers(HttpMethod.GET, "/api/workout-plans/analytics").hasRole("ADMIN"); // Analytics

                    // ===================================================================
                    // 🔐 OTHER AUTHENTICATED ENDPOINTS
                    // ===================================================================
                    auth.requestMatchers("/api/workouts/**").authenticated();
                    auth.requestMatchers("/api/users/**").authenticated();

                    // All other requests require authentication
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}