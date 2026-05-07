package com.ISI.Appel_classe.config;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.*;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.*;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║               ADSIS — Seed Users Initializer (Comptes de secours)           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  Crée et RECRÉE automatiquement au démarrage les comptes "maître" pour      ║
 * ║  chaque rôle. Ces comptes sont indestructibles : si supprimés en base,      ║
 * ║  ils seront restaurés au prochain redémarrage du backend.                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  COMPTES CRÉÉS                                                              ║
 * ║  ─────────────────────────────────────────────────────────────────────────  ║
 * ║  🔑 Admin        admin@adsis.sn          / Admin@2025!                      ║
 * ║  📚 Enseignant   enseignant@adsis.sn     / Enseignant@2025!                 ║
 * ║  🗂 Assistant    assistant@adsis.sn      / Assistant@2025!                  ║
 * ║  👁 Surveillant  surveillant@adsis.sn    / Surveillant@2025!                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedUsersInitializer {

    // ─── Repositories ───────────────────────────────────────────────────────────
    private final UtilisateurRepository       utilisateurRepository;
    private final AdministrateurRepository    administrateurRepository;
    private final EnseignantRepository        enseignantRepository;
    private final AssistantPedagogiqueRepository assistantRepository;
    private final SurveillantRepository       surveillantRepository;
    private final RoleRepository              roleRepository;
    private final EtudiantRepository           etudiantRepository;

    // ─── Encodeur de mots de passe (BCrypt injecté par Spring Security) ─────────
    private final PasswordEncoder passwordEncoder;

    // ════════════════════════════════════════════════════════════════════════════
    //  DONNÉES FIXES DES COMPTES MAÎTRES
    //  ⚠️  Ne pas modifier ces valeurs sans communiquer le changement à l'équipe.
    // ════════════════════════════════════════════════════════════════════════════

    // ── Compte Administrateur ────────────────────────────────────────────────────
    private static final String ADMIN_EMAIL       = "admin@adsis.sn";
    private static final String ADMIN_PASSWORD    = "Admin@2025!";
    private static final String ADMIN_NOM         = "ADSIS";
    private static final String ADMIN_PRENOM      = "Admin";
    private static final String ADMIN_MATRICULE   = "ADM-000";

    // ── Compte Enseignant ────────────────────────────────────────────────────────
    private static final String ENS_EMAIL         = "enseignant@adsis.sn";
    private static final String ENS_PASSWORD      = "Enseignant@2025!";
    private static final String ENS_NOM           = "Dupont";
    private static final String ENS_PRENOM        = "Jean";
    private static final String ENS_MATRICULE     = "ENS-000";

    // ── Compte Assistant Pédagogique ─────────────────────────────────────────────
    private static final String ASS_EMAIL         = "assistant@adsis.sn";
    private static final String ASS_PASSWORD      = "Assistant@2025!";
    private static final String ASS_NOM           = "Martin";
    private static final String ASS_PRENOM        = "Sophie";
    private static final String ASS_MATRICULE     = "ASS-000";

    // ── Compte Surveillant ───────────────────────────────────────────────────────
    private static final String SUR_EMAIL         = "surveillant@adsis.sn";
    private static final String SUR_PASSWORD      = "Surveillant@2025!";
    private static final String SUR_NOM           = "Ndiaye";
    private static final String SUR_PRENOM        = "Omar";
    private static final String SUR_MATRICULE     = "SUR-000";
    // ── Compte Étudiant ─────────────────────────────────────────────────────────
    private static final String ETU_EMAIL         = "etudiant@adsis.sn";
    private static final String ETU_PASSWORD      = "Etudiant@2025!";
    private static final String ETU_NOM           = "Diop";
    private static final String ETU_PRENOM        = "Aminata";
    private static final String ETU_MATRICULE     = "ETU-000";

    // ─── Noms des rôles (doivent correspondre à ce qu'initializeDefaultRoles crée) ──
    private static final String ROLE_ADMIN        = "ADMIN";
    private static final String ROLE_ENSEIGNANT   = "ENSEIGNANT";
    private static final String ROLE_ASSISTANT    = "ASSISTANT_PEDAGOGIQUE";
    private static final String ROLE_SURVEILLANT  = "SURVEILLANT";
    private static final String ROLE_ETUDIANT     = "ETUDIANT";

    // ════════════════════════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE — appelée par DataInitializer après les rôles/permissions
    // ════════════════════════════════════════════════════════════════════════════

    @Transactional
    public void seedDefaultUsers() {
        log.info("🔑 Vérification des comptes maîtres ADSIS...");

        seedAdministrateur();
        seedEnseignant();
        seedAssistant();
        seedSurveillant();
        seedEtudiant();

        log.info("✅ Comptes maîtres vérifiés et prêts.");
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  ADMINISTRATEUR
    // ════════════════════════════════════════════════════════════════════════════

    private void seedAdministrateur() {
        if (utilisateurRepository.existsByEmail(ADMIN_EMAIL)) {
            log.debug("   ↪ Admin '{}' déjà présent en base, skip.", ADMIN_EMAIL);
            return;
        }

        Role roleAdmin = roleRepository.findByName(ROLE_ADMIN).orElseGet(() -> {
            log.warn("   ⚠ Rôle '{}' introuvable, création à la volée.", ROLE_ADMIN);
            Role r = new Role();
            r.setName(ROLE_ADMIN);
            r.setDescription("Administrateur système");
            return roleRepository.save(r);
        });

        Administrateur admin = new Administrateur();
        admin.setNom(ADMIN_NOM);
        admin.setPrenom(ADMIN_PRENOM);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setMatricule(ADMIN_MATRICULE);
        admin.setStatut(Statut.ACTIF);
        admin.setEnabled(true);
        admin.setAccountNonLocked(true);
        admin.setPremiereConnexion(false);
        admin.setRoles(Set.of(roleAdmin));

        administrateurRepository.save(admin);
        log.info("   ✔ Compte Admin créé  → email: {}  mot de passe: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  ENSEIGNANT
    // ════════════════════════════════════════════════════════════════════════════

    private void seedEnseignant() {
        if (utilisateurRepository.existsByEmail(ENS_EMAIL)) {
            log.debug("   ↪ Enseignant '{}' déjà présent en base, skip.", ENS_EMAIL);
            return;
        }

        Role roleEns = roleRepository.findByName(ROLE_ENSEIGNANT).orElseGet(() -> {
            log.warn("   ⚠ Rôle '{}' introuvable, création à la volée.", ROLE_ENSEIGNANT);
            Role r = new Role();
            r.setName(ROLE_ENSEIGNANT);
            r.setDescription("Enseignant");
            return roleRepository.save(r);
        });

        Enseignant enseignant = new Enseignant();
        enseignant.setNom(ENS_NOM);
        enseignant.setPrenom(ENS_PRENOM);
        enseignant.setEmail(ENS_EMAIL);
        enseignant.setPassword(passwordEncoder.encode(ENS_PASSWORD));
        enseignant.setMatricule(ENS_MATRICULE);
        enseignant.setStatut(Statut.ACTIF);
        enseignant.setEnabled(true);
        enseignant.setAccountNonLocked(true);
        enseignant.setPremiereConnexion(false);
        enseignant.setRoles(Set.of(roleEns));

        enseignantRepository.save(enseignant);
        log.info("   ✔ Compte Enseignant créé  → email: {}  mot de passe: {}", ENS_EMAIL, ENS_PASSWORD);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  ASSISTANT PÉDAGOGIQUE
    // ════════════════════════════════════════════════════════════════════════════

    private void seedAssistant() {
        if (utilisateurRepository.existsByEmail(ASS_EMAIL)) {
            log.debug("   ↪ Assistant '{}' déjà présent en base, skip.", ASS_EMAIL);
            return;
        }

        Role roleAss = roleRepository.findByName(ROLE_ASSISTANT).orElseGet(() -> {
            log.warn("   ⚠ Rôle '{}' introuvable, création à la volée.", ROLE_ASSISTANT);
            Role r = new Role();
            r.setName(ROLE_ASSISTANT);
            r.setDescription("Assistant Pédagogique");
            return roleRepository.save(r);
        });

        AssistantPedagogique assistant = new AssistantPedagogique();
        assistant.setNom(ASS_NOM);
        assistant.setPrenom(ASS_PRENOM);
        assistant.setEmail(ASS_EMAIL);
        assistant.setPassword(passwordEncoder.encode(ASS_PASSWORD));
        assistant.setMatricule(ASS_MATRICULE);
        assistant.setStatut(Statut.ACTIF);
        assistant.setEnabled(true);
        assistant.setAccountNonLocked(true);
        assistant.setPremiereConnexion(false);
        assistant.setRoles(Set.of(roleAss));
        // filiere = null → pas de filière par défaut (peut être assignée via l'interface admin)

        assistantRepository.save(assistant);
        log.info("   ✔ Compte Assistant créé   → email: {}  mot de passe: {}", ASS_EMAIL, ASS_PASSWORD);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  SURVEILLANT
    // ════════════════════════════════════════════════════════════════════════════

    private void seedSurveillant() {
        if (utilisateurRepository.existsByEmail(SUR_EMAIL)) {
            log.debug("   ↪ Surveillant '{}' déjà présent en base, skip.", SUR_EMAIL);
            return;
        }

        Role roleSur = roleRepository.findByName(ROLE_SURVEILLANT).orElseGet(() -> {
            log.warn("   ⚠ Rôle '{}' introuvable, création à la volée.", ROLE_SURVEILLANT);
            Role r = new Role();
            r.setName(ROLE_SURVEILLANT);
            r.setDescription("Surveillant");
            return roleRepository.save(r);
        });

        Surveillant surveillant = new Surveillant();
        surveillant.setNom(SUR_NOM);
        surveillant.setPrenom(SUR_PRENOM);
        surveillant.setEmail(SUR_EMAIL);
        surveillant.setPassword(passwordEncoder.encode(SUR_PASSWORD));
        surveillant.setMatricule(SUR_MATRICULE);
        surveillant.setStatut(Statut.ACTIF);
        surveillant.setEnabled(true);
        surveillant.setAccountNonLocked(true);
        surveillant.setPremiereConnexion(false);
        surveillant.setRoles(Set.of(roleSur));
        // filiere = null → peut être assignée via l'interface admin

        surveillantRepository.save(surveillant);
        log.info("   ✔ Compte Surveillant créé  → email: {}  mot de passe: {}", SUR_EMAIL, SUR_PASSWORD);
    }
    //etudiant  
    private void seedEtudiant() {
    if (utilisateurRepository.existsByEmail(ETU_EMAIL)) {
        log.debug("   ↪ Étudiant '{}' déjà présent en base, skip.", ETU_EMAIL);
        return;
    }

    Role roleEtu = roleRepository.findByName(ROLE_ETUDIANT).orElseGet(() -> {
        log.warn("   ⚠ Rôle '{}' introuvable, création à la volée.", ROLE_ETUDIANT);
        Role r = new Role();
        r.setName(ROLE_ETUDIANT);
        r.setDescription("Étudiant");
        return roleRepository.save(r);
    });

    Etudiant etudiant = new Etudiant();
    etudiant.setNom(ETU_NOM);
    etudiant.setPrenom(ETU_PRENOM);
    etudiant.setEmail(ETU_EMAIL);
    etudiant.setPassword(passwordEncoder.encode(ETU_PASSWORD));
    etudiant.setMatricule(ETU_MATRICULE);
    etudiant.setStatut(Statut.ACTIF);
    etudiant.setEnabled(true);
    etudiant.setAccountNonLocked(true);
    etudiant.setPremiereConnexion(false);
    etudiant.setRoles(Set.of(roleEtu));
    // Optionnel : affecter une filière, une classe, etc.
    // etudiant.setFiliere(...);

    etudiantRepository.save(etudiant);
    log.info("   ✔ Compte Étudiant créé  → email: {}  mot de passe: {}", ETU_EMAIL, ETU_PASSWORD);
}
}
