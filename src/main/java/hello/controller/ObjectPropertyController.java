package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.service.ObjectPropertyService;
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
public class ObjectPropertyController {
    @RequestMapping("/objectPropertyDetail/{property}")
    public String getObjectPropertyHierarchy(@PathVariable String property, Model model, HttpSession session) throws OWLException {
        if(property==null){
            model.addAttribute("module", "topObjectProperty");
        }

        ObjectPropertyService oPService = new ObjectPropertyService();

        model.addAttribute("module", "oPView");
        model.addAttribute("oPInverse",oPService.getInverseProperty(property));
        model.addAttribute("disjointOP",oPService.getDisjointProperties(property));
        model.addAttribute("domainOP",oPService.getDomains(property));
        model.addAttribute("rangeOP",oPService.getRanges(property));
        model.addAttribute("pattern", new Pattern());
        session.setAttribute("currentOP",property);

        TreeNode tree = oPService.printHierarchy();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "objectPropertyDetail";
    }

    @RequestMapping("/getOPHierarchy")
    public ResponseEntity<?> getObjectPropertyHierarchy() throws OWLException {
        TreeNode tree = new ObjectPropertyService().printHierarchy();
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
        List<String> prs = new ObjectPropertyService().getAllOProperties();
        return ResponseEntity.ok(prs);
    }
    @RequestMapping("/getOPChars/{property}")
    public ResponseEntity<?> getOPCharacteristics(@PathVariable String property) throws OWLException {
        return ResponseEntity.ok(new ObjectPropertyService().getOPCharacteristics(property));
    }

    @PostMapping("/addNewOProperty")
    public ResponseEntity<?> addObjectProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
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
            result = objectPropertyService.addOProperty(pattern.getCurrentClass());
        }else{
            result = objectPropertyService.addSubOProperty(pattern.getCurrentClass(),pattern.getoProperties().get(0));
        }

        for(String s:pattern.getClassList()){
            if(s.equals("F")){
                result = objectPropertyService.addFunctionalProperty(pattern.getCurrentClass());
            }else if(s.equals("IF")){
                result = objectPropertyService.addInverseFunctionalProperty(pattern.getCurrentClass());
            }else if(s.equals("T")){
                result = objectPropertyService.addTransitiveProperty(pattern.getCurrentClass());
            }else if(s.equals("S")){
                result = objectPropertyService.addSymmetricProperty(pattern.getCurrentClass());
            }else if(s.equals("AS")){
                result = objectPropertyService.addAsymmetricProperty(pattern.getCurrentClass());
            }else if(s.equals("R")){
                result = objectPropertyService.addReflexiveProperty(pattern.getCurrentClass());
            }else{
                result = objectPropertyService.addIreflexiveProperty(pattern.getCurrentClass());
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/editCharacteristics")
    public ResponseEntity<?> editOPCharacteristics(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        System.out.println(pattern.getCurrentClass());
        ObjectPropertyService ops = new ObjectPropertyService();
        String result = null;
        String prop = (String)session.getAttribute("currentOP");
        if(pattern.getCurrentClass().equals("F")){
            if(ops.isFunctional(prop)){
                result = ops.removeFunctionalProperty(prop);
            }else{
                result = ops.addFunctionalProperty(prop);

            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("IF")){
            if(ops.isInverseFunctional(prop)){
                result = ops.removeInverseFunctionalProperty(prop);
            }else{
                result = ops.addInverseFunctionalProperty(prop);

            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("T")){
            if(ops.isTransitive(prop)){
                result = ops.removeTransitiveProperty(prop);
            }else{
                result = ops.addTransitiveProperty(prop);
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("S")){
            if(ops.isSymmetric(prop)){
                result = ops.removeSymetricProperty(prop);
            }else{
                result = ops.addSymmetricProperty(prop);
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("AS")){
            if(ops.isAsymmetric(prop)){
                result = ops.removeAsymetricProperty(prop);
            }else{
                result = ops.addAsymmetricProperty(prop);
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("R")){
            if(ops.isReflexive(prop)){
                result = ops.removeReflexiveProperty(prop);
            }else{
                result = ops.addReflexiveProperty(prop);
            }
            return  ResponseEntity.ok(result);
        }
        if(pattern.getCurrentClass().equals("IR")){
            if(ops.isIrreflexive(prop)){
                result = ops.removeIreflexiveProperty(prop);
            }else{
                result = ops.addIreflexiveProperty(prop);
            }
            return  ResponseEntity.ok(result);
        }
        return  ResponseEntity.ok(result);
    }

    @GetMapping("removeOProperty")
    public ResponseEntity<?> removeOProperty(HttpSession session) throws OWLOntologyStorageException, OWLOntologyCreationException {
        String result = new ObjectPropertyService().removeOProperty((String) session.getAttribute("currentOP"));
        session.setAttribute("currentOP","topObjectProperty");
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addIOProperty")
    public ResponseEntity<?> addIObjectProperty(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        System.out.println(pattern);
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result = oPService.addInverseProperty((String) session.getAttribute("currentOP"),pattern.getoProperties().get(0));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeIOProperty")
    public ResponseEntity<?> addIObjectProperty(Model model, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result = oPService.removeInverseProperty((String) session.getAttribute("currentOP"));
        return ResponseEntity.ok(result);
    }


    @PostMapping("/addOPropertyRange")
    public ResponseEntity<?> addOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result= oPService.addRange((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeOPropertyRange")
    public ResponseEntity<?> removeOPropertyRange(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result= oPService.removeRange((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addOPropertyDomain")
    public ResponseEntity<?> addOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result = oPService.addDomain((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeOPropertyDomain")
    public ResponseEntity<?> removeOPropertyDomain(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result = oPService.removeDomain((String) session.getAttribute("currentOP"),pattern.getClassList().get(0));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getDisOProperties")
    public ResponseEntity<?> getDisOProperty(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        return ResponseEntity.ok(new ObjectPropertyService().getDisjointProperties((String) session.getAttribute("currentOP")));
    }
    @GetMapping("/getNonDisOProperties")
    public ResponseEntity<?> getNonDisOProperty(HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        return ResponseEntity.ok(new ObjectPropertyService().getNonDisjointProperties((String) session.getAttribute("currentOP")));
    }

    @PostMapping("/addDisOProperty")
    public ResponseEntity<?> addDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result= null;
        for(String s:pattern.getoProperties()){
            result = oPService.addDisOProperty((String) session.getAttribute("currentOP"),s);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisOProperty")
    public ResponseEntity<?> removeDisObjectProperty(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ObjectPropertyService oPService = new ObjectPropertyService();
        String result = oPService.removeDisOProperty((String) session.getAttribute("currentOP"),pattern.getoProperties().get(0));
        return ResponseEntity.ok(result);
    }

}
