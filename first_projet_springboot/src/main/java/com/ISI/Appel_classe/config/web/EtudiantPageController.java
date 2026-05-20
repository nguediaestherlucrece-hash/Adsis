package com.ISI.Appel_classe.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/etudiant")
public class EtudiantPageController {

    @GetMapping("/dashboard")     public String dashboard()      { return "etudiant/dashboard";    }
    @GetMapping("/emploi")        public String emploiDuTemps()  { return "etudiant/emploi";       }
    @GetMapping("/absences")      public String absences()       { return "etudiant/absences";     }
    @GetMapping("/justifications")public String justifications() { return "etudiant/justificationS"; }
    @GetMapping("/appel")         public String appel()          { return "etudiant/appeletu";     }

    /** Le profil est partagé mais on passe sidebarRole='ETUDIANT'
     *  pour que shared/profil.html affiche la bonne sidebar. */
    @GetMapping("/profil")
    public String profil(org.springframework.ui.Model model) {
        model.addAttribute("sidebarRole", "ETUDIANT");
        return "shared/profil";
    }

}