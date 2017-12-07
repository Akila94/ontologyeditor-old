package hello.controller;

import hello.service.OntologyService;
import hello.util.Init;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

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
        OntologyService info = new OntologyService();
        model.addAttribute("module", "home");
        model.addAttribute("axiomInfo",info.processAxioms(init.getOntology()));
        model.addAttribute("name",info.getOntologyName(init.getOntology()));
        model.addAttribute("version",info.getOntologyVersion(init.getOntology()));
        model.addAttribute("description",info.getDescription(init.getOntology()));
        model.addAttribute("contributors",info.getContributors(init.getOntology()));
        return "home";
    }


}
