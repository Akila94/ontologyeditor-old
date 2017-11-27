package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.ObjectPropertyService;
import hello.util.Init;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class ObjectPropertyController {
    @RequestMapping("/objectPropertyDetail/{property}")
    public String getObjectPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException {
        if(property==null){
            model.addAttribute("module", "topObjectProperty");
        }
        model.addAttribute("module", "oPView");
        model.addAttribute("pattern", new Pattern());
        session.setAttribute("currentOP",property);
        Init init = new Init();
        TreeNode tree = new ObjectPropertyService().printHierarchy();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "objectPropertyDetail";
    }

    @PostMapping("/addNewOProperty")
    public ResponseEntity<?> addObjectPropery(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result;
        if(pattern.getClassList().contains("S") && pattern.getClassList().contains("AS")){
            result = "a property can't be symmetric and asymmetric at the same time";
            return ResponseEntity.ok(result);
        }
        if(pattern.getClassList().contains("R") && pattern.getClassList().contains("IR")){
            result = "a property can't be Reflexive and Ireflexive at the same time";
            return ResponseEntity.ok(result);
        }
        if((pattern.getClassList().contains("F") ||pattern.getClassList().contains("IF")) && pattern.getClassList().contains("T")){
            result = "if property is functional or inverse functional then it can't be a transitive";
            return ResponseEntity.ok(result);
        }
        if(pattern.getClassList().contains("T") && pattern.getClassList().contains("AS")){
            result = "a property can't be Transitive and Asymmetric at the same time";
            return ResponseEntity.ok(result);
        }
        if(pattern.getClassList().contains("T") && pattern.getClassList().contains("IR")){
            result = "property can't be Transitive and Ireflexive at the same time";
            return ResponseEntity.ok(result);
        }
        if(pattern.getClassList().contains("As") && pattern.getClassList().contains("R")){
            result = "property can't be Asymmetric and Reflexive at the same time";
            return ResponseEntity.ok(result);
        }
        ObjectPropertyService objectPropertyService = new ObjectPropertyService();
        if(pattern.getoProperties().get(0).equals("topObjectProperty")){
            result = objectPropertyService.addProperty(pattern.getCurrentClass());
        }else{
            result = objectPropertyService.addSubProperty(pattern.getCurrentClass(),pattern.getoProperties().get(0));
        }
        for(String s:pattern.getClassList()){
            if(s.equals("F")){
                result = objectPropertyService.addFunctionalProperty(pattern.getCurrentClass());
            }else if(s.equals("IF")){
                result = objectPropertyService.addInverseFunctionalProperty(pattern.getCurrentClass());
            }else if(s.equals("T")){
                result = objectPropertyService.addTransitiveProperty(pattern.getCurrentClass());
            }else if(s.equals("S")){
                result = objectPropertyService.addSymetricProperty(pattern.getCurrentClass());
            }else if(s.equals("AS")){
                result = objectPropertyService.addAsymetricProperty(pattern.getCurrentClass());
            }else if(s.equals("R")){
                result = objectPropertyService.addReflexiveProperty(pattern.getCurrentClass());
            }else{
                result = objectPropertyService.addIreflexiveProperty(pattern.getCurrentClass());
            }
        }

        return ResponseEntity.ok(result);
    }
}
