package com.isi.Appel_classe.models;


import java.io.*;
import java.util.*;

/**
 *
 */
public class Notification {

    /**
     *
     */
    public string idNotification;
    /**
     *
     */
    public string message;
    /**
     *
     */
    public string destinataire;

    /**
     * Default constructor
     */
    public Notification() {
    }

    /**
     *
     */
    public string type(alerte absence, rappel, validation justificatif);

    /**
     *
     */
    public string statut(envoyée, en attente);


    /**
     *
     */
    public void envoyer() {
        // TODO implement here
    }

    /**
     *
     */
    public void marquerCommeLue() {
        // TODO implement here
    }

}