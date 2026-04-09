package com.ISI.Appel_classe.security.service;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service personnalisé pour charger les détails utilisateur
 *
 * Cette classe implémente l'interface UserDetailsService de Spring Security
 * qui est utilisée par le système d'authentification pour :
 * 1. Charger l'utilisateur depuis la BD lors du login
 * 2. Charger l'utilisateur lors de la validation du token JWT
 *
 * Interface UserDetailsService :
 * - Contient une seule méthode : loadUserByUsername(String)
 * - Spring utilise cette méthode pour récupérer l'utilisateur
 *
 * Annotations :
 * - @Service : Enregistre cette classe comme bean de service dans Spring
 * - @Slf4j : Fournit un logger pour enregistrer les événements
 *
 * Flow d'utilisation :
 * 1. Lors du login (AuthenticationManager.authenticate)
 *    → Appelle loadUserByUsername(email)
 *    → SpringSecurity compare le password reçu avec le hash en BD
 *
 * 2. Lors de la validation du JWT (JwtAuthFilter)
 *    → Appelle loadUserByUsername(email extrait du token)
 *    → Récupère l'utilisateur et ses rôles/permissions
 *    → Crée l'Authentication avec les authorities
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository pour accéder aux utilisateurs en base de données
     *
     * Injecté par Spring via @Autowired
     * Utilisé pour rechercher l'utilisateur par email
     */
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Charger les détails utilisateur par email (username)
     *
     * Cette méthode est le point d'entrée du système d'authentification Spring Security
     * Elle est appelée dans deux contextes :
     *
     * 1. Authentification login :
     *    AuthenticationManager.authenticate()
     *    → AuthenticationProvider.authenticate()
     *    → UserDetailsService.loadUserByUsername(email)
     *    → Spring Security compare le password
     *
     * 2. Validation JWT :
     *    JwtAuthFilter.doFilterInternal()
     *    → Extrait email du token
     *    → AuthFilter appelle loadUserByUsername(email)
     *    → Récupère l'utilisateur complet avec ses rôles
     *
     * Processus :
     * 1. Recherche l'utilisateur en BD par email
     * 2. Si trouvé : le retourne (l'utilisateur implémente UserDetails)
     * 3. Si pas trouvé : lance UsernameNotFoundException
     *
     * Important :
     * - Le paramètre username est en réalité l'email (pas le nom d'utilisateur)
     * - Utilisateur implémente l'interface UserDetails
     * - Utilisateur implémente getAuthorities() pour retourner les rôles/permissions
     *
     * @param email L'email de l'utilisateur à charger (nommé "username" par interface)
     * @return L'objet UserDetails (en réalité un Utilisateur qui implémente UserDetails)
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur avec l'email: {}", email);

        // ÉTAPE 1 : Rechercher l'utilisateur par email
        // findByEmail retourne un Optional<Utilisateur>
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);

        if (utilisateur == null) {
            log.error("Utilisateur non trouvé avec l'email: {}", email);
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }

        // ÉTAPE 2 : Log de succès
        log.debug("Utilisateur trouvé: {}", email);

        // ÉTAPE 3 : Retourner l'utilisateur
        // Note : Utilisateur implémente UserDetails, donc il peut être retourné tel quel
        // Spring Security utilisera les méthodes de UserDetails pour :
        // - getUsername() → l'email
        // - getPassword() → le hash du mot de passe
        // - getAuthorities() → les rôles et permissions
        // - isEnabled(), isAccountNonLocked(), etc. → les statuts
        return utilisateur;
    }
}
