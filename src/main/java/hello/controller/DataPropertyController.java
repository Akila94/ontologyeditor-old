package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.TreeNode;
import hello.service.DataPropertyHierarchy;
import hello.util.Init;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class DataPropertyController {
    @RequestMapping("/dataPropertyDetail/{property}")
    public String getDataPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        if(property==null){
            model.addAttribute("module", "topDataProperty");
        }
        model.addAttribute("module", "dPView");
        session.setAttribute("currentDP",property);
        Init init = new Init();
        TreeNode tree = new DataPropertyHierarchy(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology()).printHierarchy(init.getManager().getOWLDataFactory().getOWLTopDataProperty());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        return "dataPropertyDetail";
    }
}
