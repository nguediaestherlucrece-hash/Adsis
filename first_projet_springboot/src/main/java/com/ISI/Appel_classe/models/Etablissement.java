package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "etablissement")
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEtablissement;
    private String nomEtablissement;
    private String telephone;
    private String emailContact;
    private String anneeAcademiqueCourante;

    // 🔹 Constructeurs
    public Etablissement() {
    }

    public Etablissement(String idEtablissement, String nomEtablissement, String telephone, 
                        String emailContact, String anneeAcademiqueCourante) {
        this.idEtablissement = idEtablissement;
        this.nomEtablissement = nomEtablissement;
        this.telephone = telephone;
        this.emailContact = emailContact;
        this.anneeAcademiqueCourante = anneeAcademiqueCourante;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdEtablissement() {
        return idEtablissement;
    }

    public void setIdEtablissement(String idEtablissement) {
        this.idEtablissement = idEtablissement;
    }

    public String getNomEtablissement() {
        return nomEtablissement;
    }

    public void setNomEtablissement(String nomEtablissement) {
        this.nomEtablissement = nomEtablissement;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }

    public String getAnneeAcademiqueCourante() {
        return anneeAcademiqueCourante;
    }

    public void setAnneeAcademiqueCourante(String anneeAcademiqueCourante) {
        this.anneeAcademiqueCourante = anneeAcademiqueCourante;
    }

    // 🔹 Méthodes de logique métier
    public void creerClasse() {
        // TODO: logique de création de classe
    }

    public void gererEmployes() {
        // TODO: logique de gestion des employés
    }

    public void configurerAnneeAcademique() {
        // TODO: logique de configuration de l'année académique
    }
}