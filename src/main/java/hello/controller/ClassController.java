package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.*;
import hello.service.DBService;
import hello.service.ClassService;
import hello.service.DataPropertyService;
import hello.service.ObjectPropertyService;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.model.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lotus on 8/20/2017.
 */
@Controller
public class ClassController {

    private List<String> classList;
    private List<String> oPropertyList;
    private List<String> dPropertyList;
    private List<String> individuals;
    private List<String> dTypes;

    @Autowired
    DBService dbService;

    List<ClassAxiom> subClasses;
    List<ClassAxiom> eqClasses;
    private String currentClass;



    @PostMapping("/addNewClass")
    public ResponseEntity<?> addClass(@ModelAttribute Pattern pattern, Errors errors, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {


        ClassService classService = new ClassService();
        String result;

        if(pattern.getClassList().get(0).equals("Thing")){
            result =  classService.addClass(pattern.getCurrentClass());
        }else{
            pattern.setPatternType("o1");
            result = classService.addClassAxiom(pattern,0);
        }
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addNewClass(pattern.getCurrentClass(),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public ResponseEntity<?> removeClass(@ModelAttribute Pattern pattern, HttpSession session) throws OWLException, JsonProcessingException {


        String toDeleteClass = (String) session.getAttribute("currentClass");
        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+toDeleteClass));
        classList.remove(toDeleteClass);
        String result = classService.deleteClass(clz);
        session.setAttribute("currentClass","Thing");
        if(UtilMethods.consistent==1) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeClass(toDeleteClass, user.getUsername(), pattern.getDescription(), 1);
        }

        return  ResponseEntity.ok(result);
    }


    @RequestMapping(value = "/range/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges( @PathVariable String name, Model model) throws OWLException, JsonProcessingException {
        ClassService classService = new ClassService();
        OWLObjectProperty p = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+name));
        return  ResponseEntity.ok(classService.getRange(p));
    }


    @RequestMapping(value = "/getNonDisjoint/{claz}", method = RequestMethod.GET)
    public ResponseEntity<?> getNonDisjoints(@PathVariable String claz){
        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        List<String> disjoints = classService.getDisjointAxioms(clz);
        List<String> nonDis = classList;
        nonDis.removeAll(disjoints);
        Collections.sort(nonDis);
        return  ResponseEntity.ok(nonDis);
    }

    @RequestMapping(value = "/classDetail/{claz}", method = RequestMethod.GET)
    public String viewClass( @PathVariable String claz, Model model,HttpSession session) throws OWLException, JsonProcessingException {
        session.setAttribute("currentClass",claz);
        TreeNode tree = new ClassService().printHierarchy(Init.getManager().getOWLDataFactory().getOWLThing());
        ObjectMapper mapper = new ObjectMapper();
        if(classList==null){
            classList = new ClassService().getAllClasses();
            oPropertyList = new ObjectPropertyService().getAllOProperties();
            dPropertyList = new DataPropertyService().getAllDProperties();
            individuals = new ClassService().getAllIndividuals();
        }

        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        subClasses = classService.getSubClassOfAxioms(clz);
        eqClasses = classService.getEquivalentClassAxioms(clz);
        model.addAttribute("subClasses",subClasses);
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        model.addAttribute("module", "view");
        model.addAttribute("eqClasses",eqClasses);
        model.addAttribute("djClasses", classService.getDisjointAxioms(clz));
        model.addAttribute("domainOf", classService.getDomainOf(clz));
        model.addAttribute("rangeOf", classService.getRangeOf(clz));
        model.addAttribute("dTypes",dTypes);
        model.addAttribute("toDelete",new ClassAxiom());
        model.addAttribute("pattern",new Pattern());
        return "classDetail";
    }

    @RequestMapping(value = "/getClassList", method = RequestMethod.GET)
    public ResponseEntity<?> getClassList(){
        return  ResponseEntity.ok(new ClassService().getAllClasses());
    }

    @RequestMapping(value = "/getDataProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDataPropertyList(){
        return  ResponseEntity.ok(new DataPropertyService().getAllDProperties());
    }
    @RequestMapping(value = "/getInstances", method = RequestMethod.GET)
    public ResponseEntity<?> getInstances(){
        return  ResponseEntity.ok(individuals);
    }
    @RequestMapping(value = "/getDomainOfProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDomainOfProperties(){
        List<String> domainOfProps = new ArrayList<>();
        domainOfProps.addAll(oPropertyList);
        domainOfProps.addAll(dPropertyList);
        return  ResponseEntity.ok(domainOfProps);
    }

    @PostMapping("/addDisjoint")
    public ResponseEntity<?> addDisjointClass(@ModelAttribute Pattern pattern,Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom(pattern.getCurrentClass(), user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisjointAxiom/{clz}")
    public ResponseEntity<?> removeDisjoint(@PathVariable String clz, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDisjointClass((String) session.getAttribute("currentClass"),clz,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute Pattern pattern,Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDomainOf(pattern.getCurrentClass(),pattern.getoProperties().get(0),1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom(pattern.getCurrentClass(), user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDomainOf/{property}")
    public ResponseEntity<?> removeDomainOf(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDomainOf((String) session.getAttribute("currentClass"),property,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeRangeOf/{property}")
    public ResponseEntity<?> removeRangeOf(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveRangeOf((String) session.getAttribute("currentClass"),property,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute Pattern pattern, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveRangeOf(pattern.getCurrentClass(),pattern.getoProperties().get(0),1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom(pattern.getCurrentClass(), user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }



    @GetMapping("/removeSubClassOfAxiom/{id}")
    public ResponseEntity<?> removeSubClassAxiom(@PathVariable int id,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result=null;
        for(ClassAxiom a:subClasses){
            if(a.getId()==id){
                result = UtilMethods.removeAxiom(a.getOwlAxiom());
            }
        }

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeEqClassOfAxiom/{id}")
    public ResponseEntity<?> removeEqClassAxiom(@PathVariable int id,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result=null;
        for(ClassAxiom a:eqClasses){
            if(a.getId()==id){
                result = UtilMethods.removeAxiom(a.getOwlAxiom());
            }
        }

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/addSubClassAxiom")
    public ResponseEntity<?> addSubClassAxioms(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        pattern.setCurrentClass((String) session.getAttribute("currentClass"));
        String result = new ClassService().addClassAxiom(pattern,0);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/addEqClassAxiom")
    public ResponseEntity<?> addEqClassAxioms(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        pattern.setCurrentClass((String) session.getAttribute("currentClass"));
        String result = new ClassService().addClassAxiom(pattern,1);

        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),1);
        }

        return ResponseEntity.ok(result);
    }

}
