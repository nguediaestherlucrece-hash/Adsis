package com.ISI.Appel_classe.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur MVC pour les pages du module Assistant.
 * Ne fait que renvoyer un nom de template Thymeleaf — aucune logique métier.
 */
@Controller
@RequestMapping("/assistant")
public class AssistantPageController {
    @GetMapping("/tableaudebord")    public String dashboard() { return "assistant/tableaudebord"; }
    @GetMapping("/matiere")          public String matiere()   { return "assistant/matiere"; }
    @GetMapping("/gestionetudiant")  public String etudiant()  { return "assistant/gestionetudiant"; }
    @GetMapping("/emploidutemps")    public String emploi()    { return "assistant/emploidutemps"; }
    @GetMapping("/justificatifs")    public String justificatifs() { return "assistant/justificatifs"; }
    @GetMapping("/appel")            public String appel()         { return "assistant/appel"; }
}
