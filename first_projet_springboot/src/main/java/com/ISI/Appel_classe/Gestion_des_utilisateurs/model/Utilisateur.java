package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_utilisateur", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    private String password;

    private String matricule; // Identifiant unique commun à tous les types d'utilisateurs

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "premiere_connexion")
    private Boolean premiereConnexion = false; // Par défaut false (0), deviendra true (1) après connexion

    // Relation ManyToMany avec Role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "utilisateur_roles", joinColumns = @JoinColumn(name = "utilisateur_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Permissions retirées spécifiquement pour cet utilisateur
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Set<UserPermissionOverride> permissionOverrides = new HashSet<>();

    // Implémentation de UserDetails pour Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 1. Collecter les noms des permissions retirées
        Set<String> removedNames = permissionOverrides.stream()
                .map(o -> o.getPermission().getName())
                .collect(Collectors.toSet());
        // 2. Permissions des rôles moins les permissions retirées
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .filter(name -> !removedNames.contains(name))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // 🔹 Méthodes de logique métier
    public void seConnecter() {
        // TODO: logique de connexion
    }

    public void seDeconnecter() {
        // TODO: logique de déconnexion
    }

    public void modifierCompte() {
        // TODO: logique de modification
    }
}
