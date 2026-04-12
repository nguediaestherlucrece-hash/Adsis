package com.ISI.Appel_classe.security.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.security.dto.AuthRequest;
import com.ISI.Appel_classe.security.dto.AuthResponse;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.UtilisateurService;
import com.ISI.Appel_classe.security.dto.RefreshTokenRequest;
import com.ISI.Appel_classe.security.dto.ChangePasswordRequest;
import com.ISI.Appel_classe.security.service.JwtService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Endpoint de login - Authentification avec email et mot de passe
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        // Log pour tracer qui essaie de se connecter
        log.info("Tentative de connexion pour l'utilisateur: {}", authRequest.getEmail());

        try {
            // ÉTAPE 1 : Authentifier l'utilisateur avec email et mot de passe
            //
            // AuthenticationManager :
            // - Cherche l'utilisateur par email via CustomUserDetailsService
            // - Compare le password reçu avec le hash BCrypt en BD
            // - Retourne un objet Authentication si match
            // - Lève AuthenticationException si pas de match
            Authentication authentication = authenticationManager.authenticate(
                    // Crée un token non-authentifié avec email et password
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),      // username
                            authRequest.getPassword()     // credentials
                    )
            );

            // ÉTAPE 2 : Stocker l'authentification dans le SecurityContext
            // Cela la rend disponible dans la requête actuelle
            // (Utile pour certaines opérations internes)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Authentification réussie pour: {}", authRequest.getEmail());

            // ÉTAPE 3 : Récupérer les informations complètes de l'utilisateur depuis la BD
            // Optional.orElseThrow() → Si pas trouvé en BD, lance ResourceNotFoundException
            // Note: On devrait toujours trouver l'utilisateur ici car AuthenticationManager l'a validé
            Utilisateur utilisateur = utilisateurRepository.findByEmail(authRequest.getEmail());
            if (utilisateur == null) {
                throw new ResourceNotFoundException("Utilisateur non trouvé");
            }

            // ÉTAPE 4 : Générer les tokens JWT

            // Access Token :
            // - Durée : 1 heure
            // - Utilisé pour accéder aux ressources protégées
            // - À inclure dans le header de chaque requête : Authorization: Bearer <token>
            String accessToken = jwtService.generateAccessToken(authentication);

            // Refresh Token :
            // - Durée : 7 jours
            // - Utilisé pour obtenir un nouveau access token
            // - À conserver en toute sécurité côté client
            String refreshToken = jwtService.generateRefreshToken(authRequest.getEmail());

            // ÉTAPE 5 : Construire et retourner la réponse
            // Collecter les rôles
            List<String> roles = utilisateur.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            // Collecter les permissions effectives (exclure les ROLE_ qui ne sont pas des permissions métier)
            List<String> permissions = utilisateur.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> !a.startsWith("ROLE_")) // Garder seulement les permissions métier
                    .collect(Collectors.toList());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600000L) // 1 heure en millisecondes
                    .id(utilisateur.getId())
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

            log.info("Token généré avec succès pour: {}", authRequest.getEmail());

            // ResponseEntity.ok() retourne une 200 OK avec l'AuthResponse en JSON
            return ResponseEntity.ok(authResponse);

        } catch (Exception ex) {
            // Si une erreur se produit pendant l'authentification
            // (email introuvable, password incorrect, etc.)
            log.error("Erreur lors de la connexion: {}", ex.getMessage());

            // Lance une BadRequestException qui sera traitée par GlobalExceptionHandler
            // Retournera une 400 Bad Request avec message d'erreur
            throw new BadRequestException("Email ou mot de passe incorrect");
        }
    }

    /**
     * Endpoint pour rafraîchir le token d'accès
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Demande de rafraîchissement de token");

        try {
            // ÉTAPE 1 : Valider que le refresh token existe et n'est pas expiré
            if (!jwtService.validateToken(request.getRefreshToken())) {
                // Si le token n'est pas valide (expiré, signature invalide, etc.)
                throw new BadRequestException("Refresh token invalide ou expiré");
            }

            // ÉTAPE 2 : Extraire l'email du refresh token
            // On récupère le subject (email) du token sans besoin de se reconnecter
            String email = jwtService.getEmailFromToken(request.getRefreshToken());

            // ÉTAPE 3 : Récupérer l'utilisateur depuis la BD
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            if (utilisateur == null) {
                throw new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email);
            }

            // ÉTAPE 4 : Générer un nouveau access token
            // Note: On passe l'email directement (pas d'objet Authentication)
            String accessToken = jwtService.generateAccessToken(email);

            // Optionnel : Générer aussi un nouveau refresh token
            // (pour une sécurité renforcée, rotation du refresh token)
            String newRefreshToken = jwtService.generateRefreshToken(email);

            // ÉTAPE 5 : Construire et retourner la réponse
            // Collecter les rôles et permissions pour le refresh aussi
            List<String> roles = utilisateur.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            List<String> permissions = utilisateur.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> !a.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(3600000L)
                    .id(utilisateur.getId())
                    .email(utilisateur.getEmail())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .roles(roles)
                    .permissions(permissions)
                    .build();

            log.info("Token rafraîchi avec succès pour: {}", email);
            return ResponseEntity.ok(authResponse);

        } catch (BadRequestException ex) {
            // Refresh token expiré ou invalide
            log.warn("Erreur lors du rafraîchissement du token: {}", ex.getMessage());

            // Retourne 401 Unauthorized
            // Le client doit se reconnecter via /login
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Endpoint de déconnexion
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Vider le SecurityContext
            // Cela supprime l'authentification de la requête actuelle côté serveur
            SecurityContextHolder.clearContext();

            log.info("Déconnexion réussie");

            // Message de confirmation
            return ResponseEntity.ok("Déconnexion réussie");

        } catch (Exception ex) {
            // Erreur lors du nettoyage du contexte
            log.error("Erreur lors de la déconnexion: {}", ex.getMessage());

            // Retourne 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la déconnexion");
        }
    }

    /**
     * Endpoint pour valider que l'authentification actuelle est valide
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        // Récupère l'authentification stockée dans le SecurityContext
        // Si pas d'authentification, retourne null
        // Si authentification existe, elle a été validée par JwtAuthFilter
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifie que l'authentification existe ET est validée
        if (authentication != null && authentication.isAuthenticated()) {
            // Authentification valide et non expirée
            return ResponseEntity.ok("Token valide");
        }

        // Pas d'authentification valide
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token invalide ou expiré");
    }

    /**
     * Endpoint pour changer le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Vérifier que l'utilisateur est authentifié
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié");
        }

        try {
            // Dans ce JWT configuré, getName() renvoie l'email
            String email = authentication.getName();
            
            // Appel au service
            utilisateurService.changePassword(email, request.getAncienMotDePasse(), request.getNouveauMotDePasse());
            
            log.info("Mot de passe modifié avec succès pour l'utilisateur: {}", email);
            return ResponseEntity.ok().body("{\"message\": \"Mot de passe modifié avec succès\"}");
            
        } catch (BadRequestException ex) {
            log.warn("Erreur de changement de mot de passe: {}", ex.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            log.error("Erreur serveur lors du changement de mot de passe", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erreur lors du changement de mot de passe\"}");
        }
    }
}

