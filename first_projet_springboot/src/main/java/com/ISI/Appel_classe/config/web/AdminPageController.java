package com.ISI.Appel_classe.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur MVC pour les pages du module Admin.
 * Ne fait que renvoyer un nom de template Thymeleaf — aucune logique métier.
 */
@Controller
@RequestMapping("/admin")
public class AdminPageController {
    @GetMapping("/tableaudebord")       public String dashboard() { return "admin/tableaudebord"; }
    @GetMapping("/gestionutilisateur")  public String staff()     { return "admin/gestionutilisateur"; }
    @GetMapping("/gestionacces")        public String acces()     { return "admin/gestionacces"; }
    @GetMapping("/cycles")              public String cycles()    { return "admin/cycles"; }
}
