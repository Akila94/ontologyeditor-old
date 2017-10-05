package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.*;
import hello.service.DataPropertyHierarchy;
import hello.service.EditClass;
import hello.service.ObjectPropertyHierarchy;
import hello.util.Init;
import hello.util.Variables;
import org.semanticweb.owlapi.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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

    List<ClassAxiom> subClasses;
    List<ClassAxiom> eqClasses;

    @RequestMapping("/view")
    public String getClassHierarchy(Model model) throws OWLException {
        Init init = new Init();
        TreeNode tree = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).printHierarchy(init.getManager().getOWLDataFactory().getOWLThing());
        ObjectMapper mapper = new ObjectMapper();
        classList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllClasses();
        oPropertyList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllObjectProperties();
        dPropertyList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllDataProperties();
        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
            model.addAttribute("module", "view");
            model.addAttribute("classList",classList );
            model.addAttribute("newClass", new NewClass());
            model.addAttribute("oClass",new OClass());
            //mapper.writerWithDefaultPrettyPrinter().writeValue(new File("result.json"), carFleet);//Prettified JSON
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "classHierarchy";
    }

    @RequestMapping("/getObjectPropertyHierarchy")
    public String getObjectPropertyHierarchy(Model model) throws OWLException {
        Init init = new Init();
        TreeNode tree = new ObjectPropertyHierarchy(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology()).printHierarchy(init.getManager().getOWLDataFactory().getOWLTopObjectProperty());
        ObjectMapper mapper = new ObjectMapper();

        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "objectPropertyHierarchy";
    }
    @RequestMapping("/getDataPropertyHierarchy")
    public String getDataPropertyHierarchy(Model model) throws OWLException {

        Init init = new Init();
        TreeNode tree = new DataPropertyHierarchy(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology()).printHierarchy(init.getManager().getOWLDataFactory().getOWLTopDataProperty());
        ObjectMapper mapper = new ObjectMapper();

        try {
            String jsonInString = mapper.writeValueAsString(tree);
            model.addAttribute("tree", jsonInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "dataPropertyHierarchy";
    }

    @PostMapping("/view")
    public ResponseEntity<?> addClass(@ModelAttribute NewClass newClass, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {

        Init init = new Init();
        EditClass editClass = new EditClass(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology());
        Date date = new Date();
        newClass.setTime(date);
        newClass.setAuthor("testAuthor");
        String result;
        if(newClass.getParent().equals("Thing")){
            result = editClass.addClass(newClass.getName());
        }else{
            Pattern ptrn = new Pattern();
            List<String> list = new ArrayList<>();
            list.add(newClass.getParent());
            ptrn.setCurrentClass(newClass.getName());
            ptrn.setClassList(list);
            result = editClass.addSubClass(ptrn);
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/classDetail")
    public String viewClass(@ModelAttribute OClass oClass,Model model, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {
        model.addAttribute("oClass",oClass);

        Init init = new Init();
        EditClass editClass = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology());
        OWLClass clz  = init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+oClass.getClassName()));


        classList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllClasses();
        oPropertyList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllObjectProperties();
        dPropertyList = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllDataProperties();
        individuals = new EditClass(init.getOwlReasonerFactory(Variables.STRUCTURAL),init.getOntology()).getAllIndividuals();
        subClasses =editClass.getSubClassOfAxioms(clz);
        eqClasses = editClass.getEquivalentClassAxioms(clz);
        model.addAttribute("subClasses",subClasses);


        model.addAttribute("eqClasses",eqClasses);
        model.addAttribute("djClasses",editClass.getDisjointAxioms(clz));
        model.addAttribute("domainOf",editClass.getDoaminOf(clz));
        model.addAttribute("rangeOf",editClass.getRangeOf(clz));
        model.addAttribute("classList",classList);
        model.addAttribute("oPropertyList",oPropertyList);
        model.addAttribute("dPropertyList",dPropertyList);
        model.addAttribute("individuals",individuals);
        model.addAttribute("dTypes",dTypes);
        model.addAttribute("toDelete",new ClassAxiom());

        model.addAttribute("pattern",new Pattern());
        return "classDetail";
    }


    @RequestMapping(value = "/getClassList", method = RequestMethod.GET)
    public @ResponseBody
    List<OClass> getTags(@RequestParam String term) throws OWLOntologyCreationException {

        return simulateSearchResult(term);
    }

    private List<OClass> simulateSearchResult(String tagName) {

        List<OClass> result = new ArrayList<>();

        for (int i=0;i<classList.size();i++) {
            if (classList.get(i).contains(tagName)) {
                result.add(new OClass(i,classList.get(i)));
            }
        }
        return result;
    }


    @PostMapping("/classDetail")
    public ResponseEntity<?> addAxiom(@ModelAttribute Pattern ptrn, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {
        Init init = new Init();
        String result = null;
        EditClass editClass = new EditClass(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology());
        if(ptrn.getPatternType().contains("sp")){
            result = editClass.addSubClass(ptrn);
        }else{
            result = editClass.addEqClass(ptrn);
        }

        //If error, just return a 400 bad request, along with the error message
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/removeAxiom")
    public ResponseEntity<?> removeAxiom(@ModelAttribute ClassAxiom axiom, Errors errors) throws OWLOntologyCreationException, OWLOntologyStorageException {
        Init init = new Init();
        EditClass editClass =new EditClass(init.getOwlReasonerFactory(Variables.Pellet),init.getOntology());
        if(errors.hasErrors()){
            System.out.println(errors);
        }
        String result = null;
        System.out.println(axiom.getAxiom());
        System.out.println(axiom.getOwlAxiom());
        if(axiom.getAxiomType().equals("sub")){
            for(ClassAxiom a:subClasses){
                if(a.getId()==axiom.getId()){
                    result = editClass.removeAxiom(a.getOwlAxiom());
                }
            }
        }else{
            for(ClassAxiom a:eqClasses){
                if(a.getId()==axiom.getId()){
                    result = editClass.removeAxiom(a.getOwlAxiom());
                }
            }
        }

        return ResponseEntity.ok(result);
    }

//    public void parseClassExpression(
//            @Nonnull String classExpressionString) {
//        Init init =new Init();
//        Set<OWLOntology> importsClosure = init.getOntology().getImportsClosure();
//        BidirectionalShortFormProvider bidiShortFormProvider =new BidirectionalShortFormProviderAdapter(init.getManager(), importsClosure, new SimpleShortFormProvider());
//
//        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
//        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
//        parser.setOWLEntityChecker(entityChecker);
//        parser.setStringToParse(classExpressionString);
//        OWLAxiom axiom=parser.parseAxiom();
//        System.out.println(axiom);
//       // return parser.parseClassExpression();
//    }

}
