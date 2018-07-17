package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.DBService;
import hello.service.DataPropertyService;
import hello.service.OntologyService;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
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

    private final DBService dbService;
    private final DataPropertyService dataPropertyService;

    public DataPropertyController(DBService dbService, DataPropertyService dataPropertyService) {
        this.dbService = dbService;
        this.dataPropertyService = dataPropertyService;
    }

    @RequestMapping("/dataPropertyDetail/{property}")
    public String getDataPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException, JsonProcessingException {
        if(Variables.version==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            Variables.version = dbService.getUserCurrentVersion(user.getUsername());

            Variables.ontoPath=Variables.version.getLocation();
            session.setAttribute("versionSet",true);
        }



        if(property==null){
            model.addAttribute("currentDP", "topDataProperty");
        }else{

            session.setAttribute("currentDP",property);
        }
        model.addAttribute("module", "dPView");
        model.addAttribute("pattern", new Pattern());
        model.addAttribute("isFunctional",dataPropertyService.isFunctional(property));
        model.addAttribute("disjointDP",dataPropertyService.getDisjointDProperties(property));
        model.addAttribute("domainDP",dataPropertyService.getDPDomains(property));
        model.addAttribute("rangeDP",dataPropertyService.getDPRanges(property));
        TreeNode tree = dataPropertyService.printHierarchy(Init.getManager().getOWLDataFactory().getOWLTopDataProperty());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        tree=null;
        model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());



        return "dataPropertyDetail";
    }


    @PostMapping("/addNewDProperty")
    public ResponseEntity<?> addDataProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws Exception {

        String result;
        if(pattern.getoProperties().get(0).equals("topDataProperty")){
            result = dataPropertyService.addDProperty(pattern.getCurrentClass());
        }else{
            result = dataPropertyService.addSubDProperty(pattern.getCurrentClass(),pattern.getoProperties().get(0));
        }
        if(pattern.getClassList()!=null && !pattern.getClassList().isEmpty()){
            if(pattern.getClassList().get(0).equals("F")){
                dataPropertyService.addFunctionalDProperty(pattern.getCurrentClass());

            }
        }
        session.setAttribute("currentDP",pattern.getCurrentClass());
        updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDProperty")
    public ResponseEntity<?> removeDataProperty(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {

        createVersion(dbService);
        String result = dataPropertyService.removeDProperty((String) session.getAttribute("currentDP"));

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
        List<String> prs = dataPropertyService.getAllDProperties();
        return ResponseEntity.ok(prs);
    }

    @PostMapping("/editDCharacteristics")
    public ResponseEntity<?> editDPCharacteristics(HttpSession session) throws Exception {
        String prop = (String)session.getAttribute("currentDP");

        String result;
        if(dataPropertyService.isFunctional(prop)){
            result = dataPropertyService.removeFunctionalDProperty(prop);

            if(UtilMethods.consistent==1){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
                dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set non functional",1);
            }
        }else{
            result = dataPropertyService.addFunctionalDProperty(prop);
            if(UtilMethods.consistent==1){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
                dbService.addAxiom((String) session.getAttribute("currentDP"),user.getUsername(),"set functional",1);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisDProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(dataPropertyService.getDisjointDProperties((String) session.getAttribute("currentDP")));
    }
    @GetMapping("/getNonDisDProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(dataPropertyService.getNonDisjointDProperties((String) session.getAttribute("currentDP")));
    }

    @PostMapping("/addDisDProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;
        String result= null;
        for(String s:pattern.getoProperties()){
            result = dPService.addDisDProperty((String) session.getAttribute("currentDP"),s);
        }

        if(UtilMethods.consistent==1){
            for(String s:pattern.getoProperties()){
                result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),s);

            }
            createVersion(dbService);

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
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property,@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;
        String result = dPService.removeDisDProperty((String) session.getAttribute("currentDP"),property);

        updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;
        String result = dPService.addDPDomain((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));

        updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyDomain/{property}")
    public ResponseEntity<?> removeDPropertyDomain(@PathVariable String property,@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;

        createVersion(dbService);
        String result = dPService.removeDPDomain((String) session.getAttribute("currentDP"),property);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;
        String result= dPService.addDPRange((String) session.getAttribute("currentDP"),pattern.getClassList().get(0));

        updateVersion(session,pattern,dbService,"currentDP");

        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeDPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;

        createVersion(dbService);
        String result= dPService.removeDPRange((String) session.getAttribute("currentDP"),property);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentDP"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDataTypes")
    public ResponseEntity<?> getDataTypes(HttpSession session) throws Exception {
        DataPropertyService dPService = dataPropertyService;
        return ResponseEntity.ok(dPService.getDataTypes());
    }

    public void createVersion(DBService dbServ) throws Exception {
        int maxV = dbServ.getMaxVersionNumber();
        Variables.ontoPath = Variables.baseOntoPath+(maxV+1)+".0.0.owl";
        UtilMethods.renameFile(Variables.version.getLocation(),Variables.ontoPath);
        Init.getManager().removeOntology(Init.getOntology());
        Init.setOntology(new UtilMethods().loadOntology(Init.getManager(),Variables.ontoPath));
        OntologyService ontologyService = new OntologyService();
        // ontologyService.addPriorVersion(Variables.version);
        // ontologyService.addBackwardInCompatibleWith(preOnto);
        if(Variables.version.getCurrent()){
            dbServ.setInactiveVersion(Variables.version.getId());
            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),true);
        }else{
            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),false);
        }


        ontologyService.addVersionInfo(Variables.version);
    }

    public void updateVersion(HttpSession session,Pattern pattern,DBService dbSer,String att) throws OWLOntologyStorageException {

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbSer.addAxiom((String) session.getAttribute(att),user.getUsername(),pattern.getDescription(),Variables.version.getId());
            dbSer.updateSub( Variables.version.getId());
            Variables.version.setSubVersion(Variables.version.getSubVersion()+1);
            new OntologyService().addVersionInfo(Variables.version);

        }

    }
}
