package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.DBService;
import hello.service.ObjectPropertyService;
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
public class ObjectPropertyController {

    private final DBService dbService;
    private final ObjectPropertyService objectPropertyService;

    public ObjectPropertyController(DBService dbService, ObjectPropertyService objectPropertyService) {
        this.dbService = dbService;
        this.objectPropertyService = objectPropertyService;
    }

    @RequestMapping("/objectPropertyDetail/{property}")
    public String getObjectPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException {

        if(Variables.version==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            Variables.version = dbService.getUserCurrentVersion(user.getUsername());

            Variables.ontoPath=Variables.version.getLocation();
            session.setAttribute("versionSet",true);
        }

        if(property==null){
            model.addAttribute("currentOP", "topObjectProperty");
        }else{
            session.setAttribute("currentOP",property);
        }

        

        model.addAttribute("module", "oPView");
        model.addAttribute("oPInverse",objectPropertyService.getInverseProperty(property));
        model.addAttribute("disjointOP",objectPropertyService.getDisjointProperties(property));
        model.addAttribute("domainOP",objectPropertyService.getDomains(property));
        model.addAttribute("rangeOP",objectPropertyService.getRanges(property));
        model.addAttribute("pattern", new Pattern());
        model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());
        TreeNode tree = objectPropertyService.printHierarchy(Init.getManager().getOWLDataFactory().getOWLTopObjectProperty());
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
            tree=null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "objectPropertyDetail";
    }

    @RequestMapping("/getOPHierarchy")
    public ResponseEntity<?> getObjectPropertyHierarchy() throws OWLException {
        TreeNode tree = objectPropertyService.printHierarchy(Init.getManager().getOWLDataFactory().getOWLTopObjectProperty());
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = null;
        try {
            jsonInString = mapper.writeValueAsString(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(jsonInString);
    }

    @RequestMapping("/getObjectProperties")
    public ResponseEntity<?> getOPList(){
        List<String> prs = objectPropertyService.getAllOProperties();
        return ResponseEntity.ok(prs);
    }
    @RequestMapping("/getOPChars/{property}")
    public ResponseEntity<?> getOPCharacteristics(@PathVariable String property) throws OWLException {
        return ResponseEntity.ok(objectPropertyService.getOPCharacteristics(property));
    }

    @PostMapping("/addNewOProperty")
    public ResponseEntity<?> addObjectProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws Exception {
        String result;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();


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
        if(pattern.getoProperties().get(0).equals("topObjectProperty")){
            result = objectPropertyService.addOProperty(pattern.getCurrentClass());

            if(UtilMethods.consistent==1){
                session.setAttribute("currentOP",pattern.getCurrentClass());
                dbService.addObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
            }
        }else{
            result = objectPropertyService.addSubOProperty(pattern.getCurrentClass(),pattern.getoProperties().get(0));
            if(UtilMethods.consistent==1){
                session.setAttribute("currentOP",pattern.getCurrentClass());
                dbService.addObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
            }
        }

        for(String s:pattern.getClassList()){
            if(s.equals("F")){
                result = objectPropertyService.addFunctionalProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else if(s.equals("IF")){
                result = objectPropertyService.addInverseFunctionalProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else if(s.equals("T")){
                result = objectPropertyService.addTransitiveProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else if(s.equals("S")){
                result = objectPropertyService.addSymmetricProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else if(s.equals("AS")){
                result = objectPropertyService.addAsymmetricProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else if(s.equals("R")){
                result = objectPropertyService.addReflexiveProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }else{
                result = objectPropertyService.addIreflexiveProperty(pattern.getCurrentClass());
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
                }
            }

            updateVersion(session,pattern,dbService,"currentOP");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/editCharacteristics")
    public ResponseEntity<?> editOPCharacteristics(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        ObjectPropertyService ops = objectPropertyService;
        String result = null;
        String prop = (String)session.getAttribute("currentOP");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();

        if(pattern.getCurrentClass().equals("F")){
            if(ops.isFunctional(prop)){
                result = ops.removeFunctionalProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addFunctionalProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }

            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("IF")){
            if(ops.isInverseFunctional(prop)){
                result = ops.removeInverseFunctionalProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addInverseFunctionalProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }

            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("T")){
            if(ops.isTransitive(prop)){
                result = ops.removeTransitiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addTransitiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("S")){
            if(ops.isSymmetric(prop)){
                result = ops.removeSymetricProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addSymmetricProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("AS")){
            if(ops.isAsymmetric(prop)){
                result = ops.removeAsymetricProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addAsymmetricProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("R")){
            if(ops.isReflexive(prop)){
                result = ops.removeReflexiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addReflexiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("IR")){
            if(ops.isIrreflexive(prop)){
                result = ops.removeIreflexiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.removeAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }else{
                result = ops.addIreflexiveProperty(prop);
                if(UtilMethods.consistent==1){
                    dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),1);
                }
            }
            return  ResponseEntity.ok(result);
        }
        return  ResponseEntity.ok(result);
    }

    @GetMapping("/removeOProperty")
    public ResponseEntity<?> removeOProperty(@ModelAttribute Pattern pattern,HttpSession session) throws Exception {

        createVersion(dbService);
        String result = objectPropertyService.removeOProperty((String) session.getAttribute("currentOP"));

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeObjectProperty((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }
        session.setAttribute("currentOP","topObjectProperty");
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addIOProperty")
    public ResponseEntity<?> addIObjectProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws Exception {

        
        String result = objectPropertyService.addInverseProperty((String) session.getAttribute("currentOP"),pattern.getoProperties().get(0));

        updateVersion(session,pattern,dbService,"currentOP");

        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeIOProperty")
    public ResponseEntity<?> addIObjectProperty(Model model, HttpSession session) throws Exception {
        createVersion(dbService);
        
        String result = objectPropertyService.removeInverseProperty((String) session.getAttribute("currentOP"));

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentOP"),user.getUsername(),UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/addOPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        updateVersion(session,pattern,dbService,"currentOP");
        String result= objectPropertyService.addRange((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeOPropertyRange/{property}")
    public ResponseEntity<?> removeOPropertyRange(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        createVersion(dbService);
        String result= objectPropertyService.removeRange((String) session.getAttribute("currentOP"),property);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addOPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        String result = objectPropertyService.addDomain((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));

        updateVersion(session,pattern,dbService,"currentOP");

        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeOPropertyDomain/{property}")
    public ResponseEntity<?> removeOPropertyDomain(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        createVersion(dbService);
        String result = objectPropertyService.removeDomain((String) session.getAttribute("currentOP"),property);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentOP"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisOProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(objectPropertyService.getDisjointProperties((String) session.getAttribute("currentOP")));
    }
    @GetMapping("/getNonDisOProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws Exception {
        return ResponseEntity.ok(objectPropertyService.getNonDisjointProperties((String) session.getAttribute("currentOP")));
    }

    @PostMapping("/addDisOProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        String result= null;
        for(String s:pattern.getoProperties()){
            result = objectPropertyService.addDisOProperty((String) session.getAttribute("currentOP"),s);

        }

        if(UtilMethods.consistent==1){
            for(String s:pattern.getoProperties()){
                result = objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s);

            }
            createVersion(dbService);

            for(String s:pattern.getoProperties()){
                result = objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),s);

            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentOP"), user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisOProperty/{property}")
    public ResponseEntity<?> removeDisObjectProperty(@PathVariable String property,@ModelAttribute Pattern pattern, HttpSession session) throws Exception {
        
        String result = objectPropertyService.removeDisOProperty((String) session.getAttribute("currentOP"),property);

        updateVersion(session,pattern,dbService,"currentOP");

        return ResponseEntity.ok(result);
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
