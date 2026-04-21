package com.ISI.Appel_classe.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur MVC pour les pages du module Enseignant.
 * Ne fait que renvoyer un nom de template Thymeleaf — aucune logique métier.
 */
@Controller
@RequestMapping("/enseignant")
public class EnseignantPageController {

    @GetMapping("/dashboard")  public String dashboard() { return "enseignant/dashboard"; }
    @GetMapping("/emploi")     public String emploi()    { return "enseignant/emploi"; }
    @GetMapping("/appel")      public String appel()     { return "enseignant/appel"; }
}
