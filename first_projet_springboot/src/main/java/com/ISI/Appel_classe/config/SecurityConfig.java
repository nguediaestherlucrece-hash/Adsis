package com.ISI.Appel_classe.config;

import com.ISI.Appel_classe.security.util.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // ========== ÉTAPE 1 : CONFIGURATION CORS ==========
        http.cors(cors -> cors.configurationSource(request -> {

                    // Créer une configuration CORS personnalisée
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();

                    corsConfig.setAllowedOrigins(java.util.List.of("*"));

                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));

                    return corsConfig;
                }))

                // ========== ÉTAPE 2 : DÉSACTIVER CSRF ==========
                .csrf(csrf -> csrf.disable())

                // ========== ÉTAPE 3 : CONFIGURATION DE SESSION ==========

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ========== ÉTAPE 4 : RÈGLES D'AUTORISATION ==========
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques - pas d'authentification requise
                        // Endpoints d'authentification : login, refresh, logout, validate
                        .requestMatchers("/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/auth/validate").permitAll()
                        // Pages Thymeleaf (l'auth est gérée côté JS/JWT)
                        .requestMatchers(
                                "/etudiant/**",
                                "/enseignant/**",
                                "/admin/**",
                                "/assistant/**",
                                "/shared/**",
                                "/css/**", "/js/**", "/assets/**",
                                "/uploads/**",
                                "/manifest.json", "/sw.js", "/icons/**",
                                "/favicon.ico", "/login"
                        ).permitAll()

                        // Routes protégées par rôle/permission - authentification JWT requise
                        // Ces routes ne peuvent être appelées que si l'utilisateur a un token JWT valide
                        .requestMatchers("/api/utilisateurs/**",
                                "/api/etudiants/**",
                                "/api/etudiant/**",
                                "/api/enseignants/**",
                                "/api/assistants-pedagogiques/**",
                                "/api/surveillants/**",
                                "/api/administrateurs/**",
                                "/api/cycles/**",
                                "/api/filieres/**",
                                "/api/niveaux/**",
                                "/api/specialites/**",
                                "/api/classes/**",
                                "/api/annees-academiques/**",
                                "/api/cours-ue/**",
                                "/api/semestres/**",
                                "/api/emplois-du-temps/**",
                                "/api/seances/**",
                                "/api/appels/**",
                                "/api/surveillant/**",
                                "/api/appels-etudiant/**",
                                "/api/justificatifs/**").authenticated()

                        // Autres requêtes
                        // Par défaut : authentification requise
                        .anyRequest().authenticated()
                )

                // ========== ÉTAPE 5 : AJOUTER LE FILTRE JWT ==========
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                // ========== ÉTAPE 6 : GESTION DES ERREURS D'AUTHENTIFICATION ==========
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Format JSON pour l'erreur
                            response.setContentType("application/json");
                            // Code HTTP 401 Unauthorized
                            response.setStatus(401);
                            // Message d'erreur en JSON
                            response.getWriter().write("{\"error\": \"Authentification requise\"}");
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            // Code HTTP 403 Forbidden
                            response.setStatus(403);
                            // Message d'erreur en JSON
                            response.getWriter().write("{\"error\": \"Accès refusé\"}");
                        })
                );

        // Retourner la chaîne de filtres configurée
        return http.build();
    }
}
