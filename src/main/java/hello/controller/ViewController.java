package hello.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.*;
import hello.service.DBService;
import hello.service.EditClass;
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
public class ViewController {

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


        EditClass editClass = new EditClass();
        String result;

        if(pattern.getClassList().get(0).equals("Thing")){
            result =  editClass.addClass(pattern.getCurrentClass());
        }else{
            pattern.setPatternType("sp1");
            result = editClass.addSubClass(pattern);
        }
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(),user.getUsername(),pattern.getDescription(),1);
        }
        return ResponseEntity.ok(result);
    }
    @RequestMapping(value = "/removeClass", method = RequestMethod.GET)
    public String removeClass(Model model,HttpSession session) throws OWLException, JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();

        String toDeleteClass = (String) session.getAttribute("currentClass");
        EditClass editClass = new EditClass();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+toDeleteClass));
        classList.remove(toDeleteClass);
        editClass.deleteClass(clz);
        session.setAttribute("currentClass","Thing");

        dbService.addOrRemoveClass(toDeleteClass, user.getUsername(),toDeleteClass+" Class removed",2);

        return "redirect:/classDetail/Thing";
    }


    @RequestMapping(value = "/range/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> getRanges( @PathVariable String name, Model model) throws OWLException, JsonProcessingException {
        EditClass editClass = new EditClass();
        OWLObjectProperty p = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+name));
        return  ResponseEntity.ok(editClass.getRange(p));
    }


    @RequestMapping(value = "/getNonDisjoint/{claz}", method = RequestMethod.GET)
    public ResponseEntity<?> getNonDisjoints(@PathVariable String claz){
        EditClass editClass = new EditClass();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        List<String> disjoints = editClass.getDisjointAxioms(clz);
        List<String> nonDis = classList;
        nonDis.removeAll(disjoints);
        Collections.sort(nonDis);
        return  ResponseEntity.ok(nonDis);
    }

    @RequestMapping(value = "/classDetail/{claz}", method = RequestMethod.GET)
    public String viewClass( @PathVariable String claz, Model model,HttpSession session) throws OWLException, JsonProcessingException {
        session.setAttribute("currentClass",claz);
        TreeNode tree = new EditClass().printHierarchy(Init.getManager().getOWLDataFactory().getOWLThing());
        ObjectMapper mapper = new ObjectMapper();
        if(classList==null){
            classList = new EditClass().getAllClasses();
            oPropertyList = new EditClass().getAllObjectProperties();
            dPropertyList = new EditClass().getAllDataProperties();
            individuals = new EditClass().getAllIndividuals();
        }

        EditClass editClass = new EditClass();
        OWLClass clz  = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+claz));
        subClasses =editClass.getSubClassOfAxioms(clz);
        eqClasses = editClass.getEquivalentClassAxioms(clz);
        model.addAttribute("subClasses",subClasses);
        String jsonInString = mapper.writeValueAsString(tree);
        model.addAttribute("tree", jsonInString);
        model.addAttribute("module", "view");
        model.addAttribute("eqClasses",eqClasses);
        model.addAttribute("djClasses",editClass.getDisjointAxioms(clz));
        model.addAttribute("domainOf",editClass.getDoaminOf(clz));
        model.addAttribute("rangeOf",editClass.getRangeOf(clz));
        model.addAttribute("dTypes",dTypes);
        model.addAttribute("toDelete",new ClassAxiom());
        model.addAttribute("pattern",new Pattern());
        return "classDetail";
    }

    @RequestMapping(value = "/getClassList", method = RequestMethod.GET)
    public ResponseEntity<?> getClassList(){
        return  ResponseEntity.ok(new EditClass().getAllClasses());
    }

    @RequestMapping(value = "/getDataProperties", method = RequestMethod.GET)
    public ResponseEntity<?> getDataPropertyList(){
        return  ResponseEntity.ok(dPropertyList);
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

//    public @ResponseBody
//    List<OClass> getTags(@RequestParam String term) throws OWLOntologyCreationException {
//
//        return simulateSearchResult(term);
//    }
//
//    private List<OClass> simulateSearchResult(String tagName) {
//
//        List<OClass> result = new ArrayList<>();
//
//        for (int i=0;i<classList.size();i++) {
//            if (classList.get(i).contains(tagName)) {
//                result.add(new OClass(i,classList.get(i)));
//            }
//        }
//        return result;
//    }


    @PostMapping("/classDetail")
    public ResponseEntity<?> addAxiom(@ModelAttribute Pattern ptrn, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result = null;
        EditClass editClass = new EditClass();
        if(ptrn.getPatternType().contains("sp")){
            result = editClass.addSubClass(ptrn);
        }else{
            result = editClass.addEqClass(ptrn);
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(ptrn.getCurrentClass(), user.getUsername(),"Axiom added: "+UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addDisjoint")
    public ResponseEntity<?> addDisjointClass(@ModelAttribute Pattern pattern,Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addorRemoveDisjointClass(pattern,1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Disjoint Classes Added: " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDisjointAxiom")
    public ResponseEntity<?> removeDisjoint(@ModelAttribute Pattern pattern, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addorRemoveDisjointClass(pattern,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Disjoint Classes Removed " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),2);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addDomainOf")
    public ResponseEntity<?> addDomainOf(@ModelAttribute Pattern pattern,Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addOrRemoveDomainOf(pattern,1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Domain of Axiom Added: " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeDomainOf")
    public ResponseEntity<?> removeDomainOf(@ModelAttribute Pattern pattern, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addOrRemoveDomainOf(pattern,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Domain of Axiom Removed: " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),2);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeRangeOf")
    public ResponseEntity<?> removeRangeOf(@ModelAttribute Pattern pattern, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addOrremoveRangeOf(pattern,0);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Range of Axiom Removed: " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),2);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/addRangeOf")
    public ResponseEntity<?> addRangeOf(@ModelAttribute Pattern pattern, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        EditClass editClass =new EditClass();
        String result = editClass.addOrremoveRangeOf(pattern,1);
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass(pattern.getCurrentClass(), user.getUsername(),"Range of Axiom Added: " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),1);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/removeAxiom")
    public ResponseEntity<?> removeAxiom(@ModelAttribute ClassAxiom axiom, Errors errors,HttpSession session) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String des;
        EditClass editClass =new EditClass();
//        if(errors.hasErrors()){
//            System.out.println(errors);
//        }
        String result = null;
//        System.out.println(axiom.getAxiom());
//        System.out.println(axiom.getOwlAxiom());
        if(axiom.getAxiomType().equals("sub")){
            for(ClassAxiom a:subClasses){
                if(a.getId()==axiom.getId()){
                    result = UtilMethods.removeAxiom(a.getOwlAxiom());
                }
            }
            des = "Sub-Class Axiom Removed:";
        }else{
            for(ClassAxiom a:eqClasses){
                if(a.getId()==axiom.getId()){
                    result = UtilMethods.removeAxiom(a.getOwlAxiom());
                }
            }
            des = "Equivalent-Class Axiom Removed:";
        }
        if(UtilMethods.consistent==1){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.User user = (User) auth.getPrincipal();
            dbService.addOrRemoveClass((String) session.getAttribute("currentClass"),user.getUsername(),des+ " " + UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)),2);
        }
        return ResponseEntity.ok(result);
    }



}
