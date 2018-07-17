package hello.controller;

import hello.service.DBService;
import hello.service.OntologyService;
import hello.util.Init;
import hello.util.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {
    @Autowired
    DBService dbService;

    @RequestMapping("/")
    public String greeting(Model model, HttpSession session) {
        if(session.getAttribute("currentClass")==null){
            session.setAttribute("currentClass","Thing");
        }
        if(session.getAttribute("currentDP")==null){
            session.setAttribute("currentDP","topDataProperty");
        }
        if(session.getAttribute("currentOP")==null){
            session.setAttribute("currentOP","topObjectProperty");
        }

        if(Variables.version==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
            Variables.ontoPath=Variables.version.getLocation();
            session.setAttribute("versionSet",true);
        }


        OntologyService info = new OntologyService();
        model.addAttribute("module", "home");
        model.addAttribute("axiomInfo",info.processAxioms(Init.getOntology()));
        model.addAttribute("name",info.getOntologyName(Init.getOntology()));
        model.addAttribute("version",info.getOntologyVersion(Init.getOntology()));
        model.addAttribute("description",info.getDescription(Init.getOntology()));
        model.addAttribute("contributors",info.getContributors(Init.getOntology()));



        return "home";
    }


}
