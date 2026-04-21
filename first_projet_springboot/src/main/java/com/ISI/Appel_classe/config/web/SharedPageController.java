package com.ISI.Appel_classe.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur MVC pour les pages partagées entre modules.
 * Ne fait que renvoyer un nom de template Thymeleaf — aucune logique métier.
 */
@Controller
public class SharedPageController {
    @GetMapping("/login")            public String login()  { return "shared/login"; }
    @GetMapping("/shared/profil")  public String profil() { return "shared/profil"; }
}
