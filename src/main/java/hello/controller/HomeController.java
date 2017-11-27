package hello.controller;

import hello.OWLObjectVisitor;
import hello.service.EditClass;
import hello.service.OntologyInfo;
import hello.util.Init;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class HomeController {

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

        Init init = new Init();
        OntologyInfo info = new OntologyInfo();
        model.addAttribute("module", "home");
        model.addAttribute("axiomInfo",info.processAxioms(init.getOntology()));
        model.addAttribute("name",info.getOntlogyName(init.getOntology()));
        model.addAttribute("version",info.getOntologyVersion(init.getOntology()));
        model.addAttribute("description",info.getDescription(init.getOntology()));
        model.addAttribute("contributors",info.getContributors(init.getOntology()));
        return "home";
    }


}
