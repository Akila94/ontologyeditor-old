package hello.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.bean.ChangeKeeper;
import hello.bean.ClassAxiom;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Lotus on 8/16/2017.
 */
@Service
public class ClassService {

    private TreeNode classTree = null;
    private String treeString = null;

    private final DBService dbService;

    public ClassService(DBService dbService) {
        this.dbService = dbService;
        Variables.version = dbService.getUserCurrentVersion("admin");
        Variables.ontoPath=Variables.version.getLocation();
        try {
            printHierarchy(Init.getOntology().getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    public void printHierarchy(OWLClass clazz) throws OWLException {
        if (Init.getReasoner(Variables.STRUCTURAL).isSatisfiable(clazz)) {
            if(classTree == null){
                classTree = new TreeNode(clazz.getIRI().getShortForm());
            }

            for (OWLClass child : Init.getReasoner(Variables.STRUCTURAL).getSubClasses(clazz, true).getFlattened()) {
                if (Init.getReasoner(Variables.STRUCTURAL).isSatisfiable(child)) {
                    UtilMethods.searchTree(clazz.getIRI().getShortForm(),classTree).addChild(child.getIRI().getShortForm());
                    if (!child.equals(clazz)) {
                        printHierarchy(child);
                    }
                }
            }
        }
    }

    public String getClassTree(boolean build) throws JsonProcessingException, OWLException {
        if(build){
            printHierarchy(Init.getOntology().getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        }
        if(treeString==null|| build){
            ObjectMapper mapper = new ObjectMapper();
            treeString = mapper.writeValueAsString(classTree);
        }
        return treeString;
    }

    List<OWLClass> clzes;
    private List<OWLClass> printHierarchy(OWLReasoner reasoner, OWLClass clazz)
            throws OWLException {

        if (reasoner.isSatisfiable(clazz)) {
            clzes.add(clazz);

            for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (reasoner.isSatisfiable(child)) {
                    if (!child.equals(clazz)) {
                        printHierarchy(reasoner, child);
                    }
                }
            }
        }
        return  clzes;
    }

    public List<String> getAllClasses(){
        List<String> classList = new ArrayList<>();


        Set<OWLClass> classes = Init.getOntology().getClassesInSignature();
        for(OWLClass owlClass:classes){
            classList.add(owlClass.getIRI().getShortForm());
        }
        classList.remove("Thing");
        Collections.sort(classList);
        return classList;
    }

    public List<String> getAllIndividuals(){
        List<String> individuals = new ArrayList<>();
        Set<OWLNamedIndividual> properties = Init.getOntology().getIndividualsInSignature();
        for(OWLNamedIndividual i:properties){
            individuals.add(i.getIRI().getShortForm());
        }
        Collections.sort(individuals);
        return individuals;
    }
    public List<String> getAllDataTypes(){
        List<String> dTypes = new ArrayList<>();
        Set<OWLDatatype> properties = Init.getOntology().getDatatypesInSignature();
        for(OWLDatatype t:properties){
            dTypes.add(t.getIRI().getShortForm());
        }
        Collections.sort(dTypes);
        return dTypes;
    }


    public String addClass(String className) throws Exception {

        OWLEntity entity = Init.getFactory().getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI, className));
        OWLAxiom declare = Init.getFactory().getOWLDeclarationAxiom(entity);
        return UtilMethods.addAxiom(declare);
    }

    public String deleteClass(OWLClass owlClass) throws Exception {

        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : Init.getOntology().getAxioms())
        {
            if(select.getSignature().contains(owlClass))
            {
                toRemove.add(select);
            }
        }

        UtilMethods.axiomsQueue = new ArrayList<>();
        UtilMethods.axiomsQueue.addAll(toRemove);
        int index=0;
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            if(UtilMethods.manchesterExplainer(a).contains( "Class:")){
                Collections.swap(UtilMethods.axiomsQueue,UtilMethods.axiomsQueue.indexOf(a),index);
                index++;
            }
        }

        ChangeKeeper changeKeeper = new ChangeKeeper();
        List<OWLAxiomChange> changes = new ArrayList<>();

        changeKeeper.setChangeQueue(changes);
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            changes.add(new AddAxiom(Init.getOntology(),a));
        }
        UtilMethods.changeQueue.add(changeKeeper);


        UtilMethods.removedInstances = Init.getReasoner(Variables.Pellet).getInstances(owlClass,true).getFlattened();
        UtilMethods.removedAnnotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(owlClass, Init.getOntology());

        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
        UtilMethods.checkConsistency();
        return "Class Deleted";
    }

    public String addClassAxiom(Pattern ptrn, int cOrEq) throws Exception {

        OWLAxiom axiom ;
        OWLClass childC = Init.getFactory().getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI,ptrn.getCurrentClass() ));
        OWLClass parent=getParent(childC);

        switch (ptrn.getPatternType()) {
            case "o1":
                OWLClass parentC = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI, ptrn.getClassList().get(0)));

                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, parentC);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, parentC);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, parentC);
                }

                break;
            case "o2": {
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : ptrn.getClassList()) {
                    owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI + s)));
                }
                OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);

                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, unionOf);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, unionOf);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, unionOf);
                }
                break;
            }
            case "o3": {
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : ptrn.getClassList()) {
                    owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI + s)));
                }
                OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(owlClasses);


                if (cOrEq == 1) {
                    if (parent != null) {
                        owlClasses.add(parent);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, parent);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);

                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, intersectionOf);
                }
                System.out.println(axiom);
                break;
            }
            case "o4": {
                OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI + ptrn.getoProperties().get(0)));
                OWLClass owlClass = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI + ptrn.getClassList().get(0)));
                OWLObjectSomeValuesFrom someValuesFrom = Init.getFactory().getOWLObjectSomeValuesFrom(property, owlClass);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, someValuesFrom);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, someValuesFrom);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, someValuesFrom);
                }

                break;
            }
            case "o5": {
                OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI + ptrn.getoProperties().get(0)));
                Set<OWLClass> owlClasses = new HashSet<>();
                for (String s : ptrn.getClassList()) {
                    owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI + s)));
                }
                OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);

                OWLObjectAllValuesFrom allValuesFrom = Init.getFactory().getOWLObjectAllValuesFrom(property, unionOf);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, allValuesFrom);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, allValuesFrom);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, allValuesFrom);
                }
                break;
            }
            case "o6": {
                OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI + ptrn.getdProperties().get(0)));
                OWLDatatype dataType = Init.getFactory().getOWLDatatype(IRI.create(Variables.baseIRI + ptrn.getdTypes().get(0)));
                OWLLiteral literal = Init.getFactory().getOWLLiteral(ptrn.getLiterals().get(0), dataType);
                OWLDataHasValue hasValue = Init.getFactory().getOWLDataHasValue(property, literal);


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, hasValue);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, hasValue);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, hasValue);
                }
                break;
            }
            default: {
                System.out.println("come");
                OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI + ptrn.getoProperties().get(0)));
                OWLObjectCardinalityRestriction cardinality;
                if (ptrn.getCardinalityType().equals("min")) {
                    cardinality = Init.getFactory().getOWLObjectMinCardinality(ptrn.getCardinality(), property);
                } else if (ptrn.getCardinalityType().equals("max")) {
                    cardinality = Init.getFactory().getOWLObjectMaxCardinality(ptrn.getCardinality(), property);
                } else {
                    cardinality = Init.getFactory().getOWLObjectExactCardinality(ptrn.getCardinality(), property);
                }


                if (cOrEq == 1) {
                    if (parent != null) {
                        OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent, cardinality);
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, intersectionOf);
                    } else {
                        axiom = Init.getFactory().getOWLEquivalentClassesAxiom(childC, cardinality);
                    }
                }else{
                    axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, cardinality);
                }
                break;
            }
        }
        System.out.println(axiom);
        return UtilMethods.addAxiom(axiom);
    }


    public OWLClass getParent(OWLClass clz) {
        Set<OWLClass> classes = Init.getOntology().getClassesInSignature();
        OWLClass parent=null;
        for(OWLClass c:classes){

            for (OWLClass child : Init.getReasoner(Variables.STRUCTURAL).getSubClasses(c, true).getFlattened()) {
                if (Init.getReasoner(Variables.STRUCTURAL).isSatisfiable(child)) {
                    if(child.equals(clz)){
                        parent = c;
                    }
                }
            }
        }
        return parent;
    }

    public List<ClassAxiom> getEquivalentClassAxioms(OWLClass clz){
        List<ClassAxiom> ptrns = new ArrayList<>();
        Set<OWLEquivalentClassesAxiom> sx = Init.getOntology().getEquivalentClassesAxioms(clz);
        int i=0;
        for(OWLAxiom a:sx){
            ClassAxiom ptrn = new ClassAxiom();
            ptrn.setAxiom(explain(Init.getOntology(),Init.getManager(),Init.getOwlReasonerFactory(Variables.Pellet),Init.getReasoner(Variables.Pellet),a));
            ptrn.setOwlAxiom(a);
            ptrn.setAxiomType("eq");
            ptrn.setId(i);
            ptrns.add(ptrn);
            i++;
        }
        return ptrns;
    }

    public List<ClassAxiom> getSubClassOfAxioms(OWLClass clz){
        List<ClassAxiom> ptrns = new ArrayList<>();
        Set<OWLSubClassOfAxiom> sx = Init.getOntology().getSubClassAxiomsForSubClass(clz);
        int i=0;
        for(OWLAxiom a:sx){
            ClassAxiom ptrn = new ClassAxiom();
            ptrn.setAxiom(explain(Init.getOntology(),Init.getManager(),Init.getOwlReasonerFactory(Variables.Pellet),Init.getReasoner(Variables.Pellet),a));
            ptrn.setOwlAxiom(a);
            ptrn.setAxiomType("sub");
            ptrn.setId(i);
            ptrns.add(ptrn);
            i++;
        }
        return ptrns;
    }


    public List<String> getDisjointAxioms(OWLClass clz){
        List<String> axioms = new ArrayList<>();
        Set<OWLDisjointClassesAxiom> sx = Init.getOntology().getDisjointClassesAxioms(clz);
        for(OWLDisjointClassesAxiom a:sx){
            Set<OWLClass> djc = a.getClassesInSignature();
            for(OWLClass c:djc){
                if(!c.equals(clz)){
                    axioms.add(c.getIRI().getShortForm());
                }
            }

        }
        return axioms;
    }


    public List<String> getDomainOf(OWLClass clz){
        List<String> domainOf = new ArrayList<>();
        for(OWLObjectProperty p: Init.getOntology().getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyDomainAxiom> da = Init.getOntology().getObjectPropertyDomainAxioms(p);
            for(OWLObjectPropertyDomainAxiom a:da){
                if(a.getClassesInSignature().iterator().next().equals(clz)){
                    domainOf.add(a.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
                }
            }
        }
        return domainOf;
    }

    public List<String> getRangeOf(OWLClass clz){
        List<String> rangeOf = new ArrayList<>();
        for(OWLObjectProperty p: Init.getOntology().getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyRangeAxiom> da = Init.getOntology().getObjectPropertyRangeAxioms(p);
            for(OWLObjectPropertyRangeAxiom a:da){
                if(a.getClassesInSignature().iterator().next().equals(clz)){
                    rangeOf.add(a.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
                }
            }
        }
        return rangeOf;
    }

    public List<String> getRange(OWLObjectProperty p) throws OWLException, JsonProcessingException {
        List<String> rangeOf = new ArrayList<>();
        Init init = new Init();
        Set<OWLObjectPropertyRangeAxiom> da = Init.getOntology().getObjectPropertyRangeAxioms(p);
        for(OWLObjectPropertyRangeAxiom a:da){
            OWLClass clz = a.getClassesInSignature().iterator().next();
            clzes = new ArrayList<>();
            List<OWLClass> clzes = printHierarchy(init.getReasoner(Variables.STRUCTURAL),clz);
            for(OWLClass c:clzes){
                rangeOf.add(c.getIRI().getShortForm());
//                Set<OWLNamedIndividual> nodes = init.getReasoner(Variables.STRUCTURAL).getInstances(c,true).getFlattened();
//                for(OWLNamedIndividual i:nodes){
//                    rangeOf.add(i.getIRI().getShortForm());
//                }
            }
        }
        return rangeOf;
    }

    public String addOrRemoveDisjointClass(String clz, String dClz, int addOrRemove) throws Exception {
        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+clz));
        OWLClass dis = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+dClz));
        OWLAxiom dja = Init.getFactory().getOWLDisjointClassesAxiom(current,dis);
        if(addOrRemove==1){
            return UtilMethods.addAxiom(dja);
        }else{
            return UtilMethods.removeAxiom(dja);
        }

    }

//1=add
    public String addOrRemoveDomainOf(String clz, String prop, int addOrRemove) throws Exception {
        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+clz));
        OWLAxiom axiom;
        if(new ObjectPropertyService().getAllOProperties().contains(prop)){
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
            axiom = Init.getFactory().getOWLObjectPropertyDomainAxiom(property,current);
        }else{
            OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
            axiom = Init.getFactory().getOWLDataPropertyDomainAxiom(property,current);
        }
        if(addOrRemove==1){
            return UtilMethods.addAxiom(axiom);
        }else{
            return UtilMethods.removeAxiom(axiom);
        }


    }

    public String addOrRemoveRangeOf(String clz, String prop, int addOrRemove) throws Exception {

        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+clz));
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLAxiom axiom= Init.getFactory().getOWLObjectPropertyRangeAxiom(property,current);

        if(addOrRemove==0){
            return UtilMethods.removeAxiom(axiom);
        }else{
           return UtilMethods.addAxiom(axiom);
        }
    }


    public String explain(OWLOntology ontology, OWLOntologyManager manager, OWLReasonerFactory reasonerFactory, OWLReasoner reasoner, OWLAxiom axiomToExplain) {

       // DefaultExplanationGenerator explanationGenerator = new DefaultExplanationGenerator(manager, reasonerFactory, ontology, reasoner, new SilentExplanationProgressMonitor());
       // Set<OWLAxiom> explanation = explanationGenerator.getExplanation(axiomToExplain);
//        Set<OWLAxiom> explanation = new HashSet<>();
//        explanation.add(axiomToExplain);
//        ExplanationOrderer deo = null;
//        if(axiomToExplain.getAxiomType().equals(AxiomType.SUBCLASS_OF)){
//            deo = new hello.ExplanationOrdererImpl(manager);
//        }else{
//            deo = new ExplanationOrdererImpl(manager);
//        }

       // ExplanationTree explanationTree = deo.getOrderedExplanation(axiomToExplain, explanation);
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
     //   OWLAxiom axiom = explanationTree.getUserObject();
        return  DLtoenglish(renderer.render(axiomToExplain));
    }


    public String DLtoenglish(String exp){
        String eng = exp.replace("≡","is equivalent to")
                .replace("⊓","and")
                .replace("⊔","or")
                .replace("¬","not ")
                .replace("⊑","is a sub class of ")
                .replace("≥","at least")
                .replace("≤","at most")
                .replace("=","exact");


        int lastIndex = 0;
        while(lastIndex != -1){
            if(eng.contains("∃ ")){
                eng = eng.replaceFirst("∃ ","")
                        .replaceFirst("\\."," has some ");
            }
            if(eng.contains("∀ ")){
                eng = eng.replaceFirst("∀ ","")
                        .replaceFirst("\\."," has only ");
            }

            lastIndex = eng.indexOf("∃ ",lastIndex);

            if(lastIndex != -1){
                lastIndex += "∃ ".length();
            }
        }
        eng = eng.replace("has some {","has value {").replace("⊤","").replaceAll("\\.","");
        return eng;
    }

}
