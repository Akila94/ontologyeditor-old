package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.DataPropertyService;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.util.List;

@Controller
public class DataPropertyController {
    @RequestMapping("/dataPropertyDetail/{property}")
    public String getDataPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        DataPropertyService service = new DataPropertyService();

        if(property==null){
            model.addAttribute("module", "topDataProperty");
        }
        model.addAttribute("module", "dPView");
        model.addAttribute("pattern", new Pattern());
        model.addAttribute("isFunctional",service.isFunctional(property));
        model.addAttribute("disjointDP",service.getDisjointDProperties(property));
        model.addAttribute("domainDP",service.getDPDomains(property));
        model.addAttribute("rangeDP",service.getDPRanges(property));
        session.setAttribute("currentDP",property);
        TreeNode tree = service.printHierarchy(Init.getManager().getOWLDataFactory().getOWLTopDataProperty());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        return "dataPropertyDetail";
    }


    @PostMapping("/addNewDProperty")
    public ResponseEntity<?> addObjectProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService service = new DataPropertyService();
        String result;
        if(pattern.getoProperties().get(0).equals("topDataProperty")){
            result = service.addDProperty(pattern.getCurrentClass());
        }else{
            result = service.addSubDProperty(pattern.getCurrentClass(),pattern.getoProperties().get(0));
        }
        if(pattern.getClassList()!=null && !pattern.getClassList().isEmpty()){
            if(pattern.getClassList().get(0).equals("F")){
                service.addFunctionalDProperty(pattern.getCurrentClass());
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDProperty")
    public ResponseEntity<?> removeDataProperty(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result = new DataPropertyService().removeDProperty((String) session.getAttribute("currentDP"));
        session.setAttribute("currentDP","topDataProperty");
        return ResponseEntity.ok(result);
    }


    @RequestMapping("/getDataProperties")
    public ResponseEntity<?> getDPList(){
        List<String> prs = new DataPropertyService().getAllDProperties();
        return ResponseEntity.ok(prs);
    }

    @PostMapping("/editDCharacteristics")
    public ResponseEntity<?> editDPCharacteristics(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String prop = (String)session.getAttribute("currentDP");
        DataPropertyService service = new DataPropertyService();
        String result;
        if(service.isFunctional(prop)){
            result = service.removeFunctionalDProperty(prop);
        }else{
            result = service.addFunctionalDProperty(prop);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisDProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        return ResponseEntity.ok(new DataPropertyService().getDisjointDProperties((String) session.getAttribute("currentDP")));
    }
    @GetMapping("/getNonDisDProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        return ResponseEntity.ok(new DataPropertyService().getNonDisjointDProperties((String) session.getAttribute("currentDP")));
    }

    @PostMapping("/addDisDProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService oPService = new DataPropertyService();
        String result= null;
        for(String s:pattern.getoProperties()){
            result = oPService.addDisDProperty((String) session.getAttribute("currentDP"),s);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisDProperty")
    public ResponseEntity<?> removeDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),pattern.getoProperties().get(0));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result = dPService.addDPDomain((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyDomain")
    public ResponseEntity<?> removeOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result = dPService.removeDPDomain((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result= dPService.addDPRange((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyRange")
    public ResponseEntity<?> removeOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result= dPService.removeDPRange((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDataTypes")
    public ResponseEntity<?> getDataTypes(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        return ResponseEntity.ok(dPService.getDataTypes());
    }
}
