package com.ISI.Appel_classe.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service pour envoyer des emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    /**
     * Construit le contenu de l'email avec les informations de connexion
     */
    private String buildEmailContent(String nom, String prenom, String email, String password) {
        return String.format(
                "Bonjour %s %s,\n\n" +
                        "Bienvenue dans l'application Appel Classe!\n\n" +
                        "Vos identifiants de connexion sont les suivants:\n\n" +
                        "Email: %s\n" +
                        "Mot de passe: %s\n\n" +
                        "Veuillez conserver ces informations en lieu sûr.\n" +
                        "Nous vous recommandons de changer votre mot de passe à votre première connexion.\n\n",
                prenom, nom, email, password
        );
    }

    /**
     * Envoie un email avec les informations de connexion
     */
    @Async
    public void sendPasswordEmail(String email, String nom, String prenom, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("landngou@gmail.com");
            message.setTo(email);
            message.setSubject("Votre mot de passe de connexion à l'application ADSIS");

            String htmlContent = buildEmailContent(nom, prenom, email, password);
            message.setText(htmlContent);

            mailSender.send(message);
            log.info("Email envoyé avec succès à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à: {}", email, e);
            throw new RuntimeException("Impossible d'envoyer l'email: " + e.getMessage());
        }
    }


}
