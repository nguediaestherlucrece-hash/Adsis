package com.ISI.Appel_classe.security.util;

import com.ISI.Appel_classe.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT d'authentification
 *
 * Ce filtre est appelé POUR CHAQUE REQUÊTE HTTP entrant dans l'application.
 * Il est responsable de :
 * 1. Extraire le token JWT du header Authorization
 * 2. Valider le token JWT
 * 3. Charger l'utilisateur depuis la base de données
 * 4. Créer et configurer l'authentification Spring Security
 * 5. Stocker l'authentification dans le SecurityContext
 *
 * OncePerRequestFilter garantit que le doFilterInternal() est appelé une seule fois par requête
 *
 * Annotations :
 * - @Slf4j : Fournit un logger pour les messages de debug/erreur
 *
 * Flux :
 * CLIENT REQUEST
 *   ↓
 * JwtAuthFilter.doFilterInternal() ← Ce filtre est exécuté
 *   ├─ Extrait le token du header "Authorization: Bearer xxx"
 *   ├─ Valide le token avec JwtService
 *   ├─ Charge l'utilisateur via UserDetailsService
 *   ├─ Crée une authentification Spring Security
 *   └─ Stocke dans SecurityContextHolder (accessible en toute l'app)
 *   ↓
 * filterChain.doFilter() ← Continue vers le controller
 *   ↓
 * @RestController reçoit la requête avec l'authentification
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Service JWT pour la validation des tokens
     *
     * Injecté par Spring via @Autowired
     * Utilisé pour valider et extraire les données du token
     */
    @Autowired
    private JwtService jwtService;

    /**
     * Service pour charger les détails utilisateur
     *
     * Injecté par Spring via @Autowired
     * Implémentation : CustomUserDetailsService
     * Utilisé pour récupérer l'utilisateur depuis la BD via son email
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Méthode principale du filtre
     *
     * Appelée une fois par requête HTTP
     * Effectue l'authentification JWT si un token valide est présent
     *
     * Flux :
     * 1. TRY : Essayer d'authentifier l'utilisateur
     * 2. Extraire le token du header
     * 3. Valider le token
     * 4. Si valide : charger l'utilisateur et créer l'authentification
     * 5. CATCH : Si erreur, continuer sans authentification
     * 6. Appeler filterChain.doFilter() pour continuer le traitement
     *
     * @param request La requête HTTP entrant
     * @param response La réponse HTTP sortant
     * @param filterChain La chaîne de filtres à exécuter après
     * @throws ServletException Si erreur servlet
     * @throws IOException Si erreur I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // ÉTAPE 1 : Extraire le token du header Authorization
            String jwt = extractJwtFromRequest(request);

            // ÉTAPE 2 : Vérifier que le token existe ET est valide
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {

                // ÉTAPE 3 : Extraire l'email du token
                // C'est le "subject" du JWT
                String userEmail = jwtService.getEmailFromToken(jwt);

                // ÉTAPE 4 : Charger les détails complets de l'utilisateur depuis la BD
                // Utilise CustomUserDetailsService qui recherche en BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // ÉTAPE 5 : Créer un objet Authentication Spring Security
                // Ce token contient :
                // - Principal : l'utilisateur (UserDetails)
                // - Credentials : null (pas besoin du mot de passe en JWT)
                // - Authorities : les rôles/permissions de l'utilisateur
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // ÉTAPE 6 : Ajouter les détails de la requête au token d'authentification
                // Cela ajoute des info comme l'IP, le user-agent, etc.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ÉTAPE 7 : Stocker l'authentification dans le SecurityContext
                // Ce contexte est accessible partout dans l'application
                // Tous les contrôleurs peuvent récupérer cet utilisateur authentifié
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Log pour debug
                log.debug("Authentification JWT réussie pour : {}", userEmail);
            }
        } catch (Exception ex) {
            // Si une erreur se produit, enregistrer le log mais continuer quand même
            // Les routes publiques n'ont pas besoin d'authentification
            // Les routes protégées rejetteront la requête plus tard (autorisation échouée)
            log.error("Impossible de configurer l'authentification de l'utilisateur: {}", ex.getMessage());
        }

        // ÉTAPE 8 : Continuer le traitement normal
        // Passe la requête au prochain filtre de la chaîne
        // Et finalement au contrôleur
        filterChain.doFilter(request, response);
    }

    /**
     * Extraire le token JWT du header Authorization
     *
     * Format du Header :
     * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ...
     *
     * Cette méthode sépare "Bearer " du token réel
     *
     * Processus :
     * 1. Récupère le header "Authorization"
     * 2. Vérifie qu'il commence par "Bearer "
     * 3. Extrait la partie après "Bearer " (7 caractères)
     * 4. Retourne le token, ou null s'il n'est pas présent/valide
     *
     * @param request La requête HTTP
     * @return Le token JWT, ou null s'il n'existe pas ou format invalide
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Récupère la valeur complète du header "Authorization"
        // Par exemple : "Bearer eyJhbGciOiJIUzUxMiJ9..."
        String bearerToken = request.getHeader("Authorization");

        // Vérifie que le token existe ET commence par "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extrait la partie après "Bearer "
            // En supprimant les 7 premiers caractères ("Bearer ")
            return bearerToken.substring(7);
        }

        // Si le header n'existe pas ou format invalide, retourne null
        return null;
    }
}
