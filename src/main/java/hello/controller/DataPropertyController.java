package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.DBService;
import hello.service.DataPropertyService;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.util.List;

@Controller
public class DataPropertyController {

    @Autowired
    DBService dbService;
    @RequestMapping("/dataPropertyDetail/{property}")
    public String getDataPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        if(Variables.version==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            Variables.version = dbService.getUserCurrentVersion(user.getUsername());

            Variables.ontoPath=Variables.version.getLocation();
            session.setAttribute("versionSet",true);
        }



        DataPropertyService service = new DataPropertyService();

        if(property==null){
            model.addAttribute("currentDP", "topDataProperty");
        }else{

            session.setAttribute("currentDP",property);
        }
        model.addAttribute("module", "dPView");
        model.addAttribute("pattern", new Pattern());
        model.addAttribute("isFunctional",service.isFunctional(property));
        model.addAttribute("disjointDP",service.getDisjointDProperties(property));
        model.addAttribute("domainDP",service.getDPDomains(property));
        model.addAttribute("rangeDP",service.getDPRanges(property));
        TreeNode tree = service.printHierarchy(Init.getManager().getOWLDataFactory().getOWLTopDataProperty());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        tree=null;
        model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());



        return "dataPropertyDetail";
    }


    @PostMapping("/addNewDProperty")
    public ResponseEntity<?> addDataProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
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
        session.setAttribute("currentDP",pattern.getCurrentClass());
        new ClassController().updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDProperty")
    public ResponseEntity<?> removeDataProperty(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        new ClassController().createVersion(dbService);
        String result = new DataPropertyService().removeDProperty((String) session.getAttribute("currentDP"));

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeDataProperty((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),1);
        }

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

            if(UtilMethods.consistent==1){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
                dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set non functional",1);
            }
        }else{
            result = service.addFunctionalDProperty(prop);
            if(UtilMethods.consistent==1){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
                dbService.addAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set functional",1);
            }
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
        DataPropertyService dPService = new DataPropertyService();
        String result= null;
        for(String s:pattern.getoProperties()){
            result = dPService.addDisDProperty((String) session.getAttribute("currentDP"),s);
        }

        if(UtilMethods.consistent==1){
            for(String s:pattern.getoProperties()){
                result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),s);

            }
            new ClassController().createVersion(dbService);

            for(String s:pattern.getoProperties()){
                result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),s);

            }


            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisDProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),property);

        new ClassController().updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result = dPService.addDPDomain((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));

        new ClassController().updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyDomain/{property}")
    public ResponseEntity<?> removeDPropertyDomain(@PathVariable String property,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();

        new ClassController().createVersion(dbService);
        String result = dPService.removeDPDomain((String) session.getAttribute("currentDP"),property);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        String result= dPService.addDPRange((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));

        new ClassController().updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();

        new ClassController().createVersion(dbService);
        String result= dPService.removeDPRange((String) session.getAttribute("currentDP"),property);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDataTypes")
    public ResponseEntity<?> getDataTypes(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        DataPropertyService dPService = new DataPropertyService();
        return ResponseEntity.ok(dPService.getDataTypes());
    }
}
