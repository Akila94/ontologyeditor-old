package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import hello.bean.*;
import hello.service.*;
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
import java.util.Set;

/**
 * Created by Lotus on 8/20/2017.
 */
@Controller
public class ClassController {

    private static final Set<String> PATTERN_TYPES = ImmutableSet.of("o1", "o2", "o3", "o4", "o6");


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


        updateVersion(session,pattern,dbService,"currentClass");
        return ResponseEntity.ok(result);
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public ResponseEntity<?> removeClass(@ModelAttribute Pattern pattern, HttpSession session) throws OWLException, JsonProcessingException {
        createVersion(dbService);

        String toDeleteClass = (String) session.getAttribute("currentClass");
        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+toDeleteClass));
        String result = classService.deleteClass(clz);

        session.setAttribute("currentClass","Thing");

        if(UtilMethods.consistent==1) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeClass(toDeleteClass, user.getUsername(), pattern.getDescription(), Variables.version.getId());
        }

        return  ResponseEntity.ok(result);
    }


    @RequestMapping(value = "/range/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges( @PathVariable String name, Model model) throws OWLException, JsonProcessingException {
        ClassService classService = new ClassService();
        OWLObjectProperty p = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+name));
        return  ResponseEntity.ok(classService.getRange(p));
    }

    @RequestMapping(value = "/undo", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges(Model model) throws OWLException, JsonProcessingException {
        ChangeKeeper changes= UtilMethods.changeQueue.get(UtilMethods.changeQueue.size()-1);
        List<OWLAxiomChange> cq = changes.getChangeQueue();
        for(OWLAxiomChange c:cq){
            Init.getManager().applyChange(c);
        }
        Init.getManager().saveOntology(Init.getOntology());
        UtilMethods.changeQueue.remove(changes);

        dbService.removeLastRecord();
        return  ResponseEntity.ok("Undo Success!");
    }


    @RequestMapping(value = "/getNonDisjoint/{claz}", method = RequestMethod.GET)
    public ResponseEntity<?> getNonDisjoints(@PathVariable String claz){
        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        List<String> disjoints = classService.getDisjointAxioms(clz);
        List<String> nonDis = classService.getAllClasses();
        nonDis.removeAll(disjoints);
        Collections.sort(nonDis);
        return  ResponseEntity.ok(nonDis);
    }

    @RequestMapping(value = "/classDetail/{claz}", method = RequestMethod.GET)
    public String viewClass( @PathVariable String claz, Model model,HttpSession session) throws OWLException, JsonProcessingException {

        if(Variables.version==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            Variables.version = dbService.getUserCurrentVersion(user.getUsername());
            Variables.ontoPath=Variables.version.getLocation();
            session.setAttribute("versionSet",true);
        }

        session.setAttribute("currentClass",claz);
        TreeNode tree = new ClassService().printHierarchy(Init.getManager().getOWLDataFactory().getOWLThing());
        ObjectMapper mapper = new ObjectMapper();

        ClassService classService = new ClassService();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        subClasses = classService.getSubClassOfAxioms(clz);
        eqClasses = classService.getEquivalentClassAxioms(clz);
        model.addAttribute("subClasses",subClasses);
        String jsonInString = mapper.writeValueAsString(tree);
        tree=null;
        model.addAttribute("tree", jsonInString);
        model.addAttribute("module", "view");
        model.addAttribute("eqClasses",eqClasses);
        model.addAttribute("djClasses", classService.getDisjointAxioms(clz));
        model.addAttribute("domainOf", classService.getDomainOf(clz));
        model.addAttribute("rangeOf", classService.getRangeOf(clz));
        model.addAttribute("toDelete",new ClassAxiom());
        model.addAttribute("pattern",new Pattern());
        model.addAttribute("undo",!UtilMethods.changeQueue.isEmpty());

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
        return  ResponseEntity.ok(new ClassService().getAllIndividuals());
    }
    @RequestMapping(value = "/getDomainOfProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDomainOfProperties(){
        ClassService classService = new ClassService();
        List<String> domainOfProps = new ArrayList<>();
        domainOfProps.addAll(new ObjectPropertyService().getAllOProperties());
        domainOfProps.addAll(new DataPropertyService().getAllDProperties());
        return  ResponseEntity.ok(domainOfProps);
    }

    @PostMapping("/addDisjoint")
    public ResponseEntity<?> addDisjointClass(@ModelAttribute Pattern pattern,Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),1);
        if(UtilMethods.consistent==1) {
            classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),0);
            createVersion(dbService);

        }

        String result = classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        dbService.addAxiom(pattern.getCurrentClass(), user.getUsername(),pattern.getDescription(),Variables.version.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDisjointAxiom/{clz}")
    public ResponseEntity<?> removeDisjoint(@PathVariable String clz, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDisjointClass((String) session.getAttribute("currentClass"),clz,0);

        updateVersion(session,pattern,dbService,"currentClass");
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute Pattern pattern,Errors errors,HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveDomainOf(pattern.getCurrentClass(),pattern.getoProperties().get(0),1);

        updateVersion(session,pattern,dbService,"currentClass");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/removeDomainOf/{property}")
    public ResponseEntity<?> removeDomainOf(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),0);
        if(UtilMethods.consistent==1) {
            classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),1);
            createVersion(dbService);

        }
        String result = classService.addOrRemoveDomainOf((String) session.getAttribute("currentClass"),property,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }


        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeRangeOf/{property}")
    public ResponseEntity<?> removeRangeOf(@PathVariable String property, @ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),0);
        if(UtilMethods.consistent==1) {
            classService.addOrRemoveDisjointClass(pattern.getCurrentClass(),pattern.getClassList().get(0),1);
            createVersion(dbService);

        }

        String result = classService.addOrRemoveRangeOf((String) session.getAttribute("currentClass"),property,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }


        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute Pattern pattern, Errors errors,HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        ClassService classService =new ClassService();
        String result = classService.addOrRemoveRangeOf(pattern.getCurrentClass(),pattern.getoProperties().get(0),Variables.version.getId());

        updateVersion(session,pattern,dbService,"currentClass");
        return ResponseEntity.ok(result);
    }



    @GetMapping("/removeSubClassOfAxiom/{id}")
    public ResponseEntity<?> removeSubClassAxiom(@PathVariable int id,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        createVersion(dbService);

        String result=null;
        for(ClassAxiom a:subClasses){
            if(a.getId()==id){
                result = UtilMethods.removeAxiom(a.getOwlAxiom());
            }
        }


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),Variables.version.getId());



        return ResponseEntity.ok(result);
    }
    @GetMapping("/removeEqClassOfAxiom/{id}")
    public ResponseEntity<?> removeEqClassAxiom(@PathVariable int id,@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        createVersion(dbService);

        String result=null;
        for(ClassAxiom a:eqClasses){
            if(a.getId()==id){
                result = UtilMethods.removeAxiom(a.getOwlAxiom());
            }

        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        dbService.removeAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),Variables.version.getId());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/addSubClassAxiom")
    public ResponseEntity<?> addSubClassAxioms(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        System.out.println("come");
        pattern.setCurrentClass((String) session.getAttribute("currentClass"));
        ClassService classService=new ClassService();
        if(PATTERN_TYPES.contains(pattern.getPatternType()) || (pattern.getPatternType().equals("o7") &&pattern
                .getCardinalityType().equals("min"))){

            updateVersion(session,pattern,dbService,"currentClass");
        } else{
            System.out.println("come2");
            classService.addClassAxiom(pattern,0);
            if(UtilMethods.consistent==1){
                UtilMethods.removeAxiom(UtilMethods.axiomsQueue.get(0));
                createVersion(dbService);
            }
        }
        System.out.println(pattern.toString());
        String result = classService.addClassAxiom(pattern,0);
        System.out.println(result);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addAxiom((String) session.getAttribute("currentClass"),user.getUsername(),pattern.getDescription(),Variables.version.getId());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addEqClassAxiom")
    public ResponseEntity<?> addEqClassAxioms(@ModelAttribute Pattern pattern, HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {

        if(PATTERN_TYPES.contains(pattern.getPatternType()) || (pattern.getPatternType().equals("o7") &&pattern
                .getCardinalityType().equals("min"))){
            updateVersion(session,pattern,dbService,"currentClass");
        } else{
            new ClassService().addClassAxiom(pattern,1);
            if(UtilMethods.consistent==1){
                UtilMethods.removeAxiom(UtilMethods.axiomsQueue.get(0));
                createVersion(dbService);
            }
        }


        pattern.setCurrentClass((String) session.getAttribute("currentClass"));
        String result = new ClassService().addClassAxiom(pattern,1);
        updateVersion(session,pattern,dbService,"currentClass");


        return ResponseEntity.ok(result);
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

    public void createVersion(DBService dbServ) throws OWLOntologyCreationException, OWLOntologyStorageException {
        int maxV = dbServ.getMaxVersionNumber();
        Variables.ontoPath = Variables.baseOntoPath+(maxV+1)+".0.0.owl";
        UtilMethods.renameFile(Variables.version.getLocation(),Variables.ontoPath);
        OWLOntology preOnto = Init.getOntology();
        Init.getManager().removeOntology(Init.getOntology());
        Init.setOntology(new UtilMethods().loadOntology(Init.getManager(),Variables.ontoPath));
        OntologyService ontologyService = new OntologyService();
        ontologyService.addPriorVersion(Variables.version);
        ontologyService.addBackwardInCompatibleWith(preOnto);
        if(Variables.version.getCurrent()){
            dbServ.setInactiveVersion(Variables.version.getId());
            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),true);
        }else{
            Variables.version = dbServ.addVersion(maxV+1,0,0,Variables.ontoPath,"sln_onto",Variables.version.getId(),false);
        }


        ontologyService.addVersionInfo(Variables.version);
    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
        return user.getUsername();
    }
}