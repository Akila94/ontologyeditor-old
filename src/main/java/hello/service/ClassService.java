package hello.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import static hello.util.UtilMethods.checkConsistency;
import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/16/2017.
 */
@Service
public class ClassService {

    private TreeNode classTree = null;



    public ClassService() {
    }


    public TreeNode printHierarchy(OWLClass clazz) throws OWLException {
        printHierarchy(Init.getReasoner(Variables.STRUCTURAL), clazz, 0);
        for (OWLClass cl : Init.getOntology().getClassesInSignature()) {
            if (!Init.getReasoner(Variables.STRUCTURAL).isSatisfiable(cl)) {
                //  System.out.println("XXX: " + cl.getIRI().toString());
            }
        }
        return classTree;
     }
    private void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level)
            throws OWLException {
        if (reasoner.isSatisfiable(clazz)) {

            if(classTree == null){
                classTree = new TreeNode(clazz.getIRI().getShortForm());
            }

            for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (reasoner.isSatisfiable(child)) {
                    searchTree(clazz.getIRI().getShortForm(),classTree).addChild(child.getIRI().getShortForm());
                    if (!child.equals(clazz)) {
                        printHierarchy(reasoner, child, level + 1);
                    }
                }
            }
        }
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


    public String addClass(String className) throws OWLOntologyStorageException, OWLOntologyCreationException {

        OWLEntity entity = Init.getFactory().getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI, className));
        OWLAxiom declare = Init.getFactory().getOWLDeclarationAxiom(entity);

        return UtilMethods.addAxiom(declare);
    }

    public void deleteClass(OWLClass owlClass) throws OWLOntologyStorageException, OWLOntologyCreationException
    {

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
        UtilMethods.removedInstances = Init.getReasoner(Variables.Pellet).getInstances(owlClass,true).getFlattened();


        UtilMethods.removedAnnotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(owlClass, Init.getOntology());

        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
    }

    public String addSubClass(Pattern ptrn) throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLAxiom axiom = null;
        OWLClass childC = Init.getFactory().getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI,ptrn.getCurrentClass() ));

        //a is sub class of b
        if(ptrn.getPatternType().equals("sp1")){
            OWLClass parentC = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI, ptrn.getClassList().get(0)));
            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC, parentC);

        }else if(ptrn.getPatternType().equals("sp2")){
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLClass individual = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(0)));
            OWLObjectSomeValuesFrom someValuesFrom= Init.getFactory().getOWLObjectSomeValuesFrom(property,individual);
            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC,someValuesFrom);
        }else if(ptrn.getPatternType().equals("sp3")){
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+s)));
            }

            OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);

            OWLObjectAllValuesFrom allValuesFrom =Init.getFactory().getOWLObjectAllValuesFrom(property,unionOf);
            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC,allValuesFrom);
        }else if(ptrn.getPatternType().equals("sp4")){
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLIndividual individual = Init.getFactory().getOWLNamedIndividual(IRI.create(Variables.baseIRI+ptrn.getIndividuals().get(0)));
            OWLObjectHasValue hasValue= Init.getFactory().getOWLObjectHasValue(property,individual);
            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC,hasValue);
        }else if(ptrn.getPatternType().equals("sp5")){
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);
            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC,unionOf);
        }else if(ptrn.getPatternType().equals("sp6")){
            OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+ptrn.getdProperties().get(0)));
            OWLLiteral literal = Init.getFactory().getOWLLiteral(ptrn.getLiterals().get(0));
            OWLDataHasValue hasValue = Init.getFactory().getOWLDataHasValue(property,literal);

            axiom = Init.getFactory().getOWLSubClassOfAxiom(childC,hasValue);
        }

        return UtilMethods.addAxiom(axiom);
    }

    public String addEqClass(Pattern ptrn) throws OWLOntologyCreationException, OWLOntologyStorageException {
        AddAxiom addAxiom;
        OWLAxiom axiom= null;
        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getCurrentClass()));
        OWLClass parent=getParent(current);
        if(ptrn.getPatternType().equals("eq1")){  // a equivalent b and dP has value literal

            OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+ptrn.getdProperties().get(0)));
            OWLLiteral literal = Init.getFactory().getOWLLiteral(ptrn.getLiterals().get(0));
            OWLDataHasValue hasValue = Init.getFactory().getOWLDataHasValue(property,literal);
            OWLClassExpression intersectionOf = hasValue;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,hasValue);
            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq2")){// a eq to pC and a or b or c or ...
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);
            OWLClassExpression intersectionOf = unionOf;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,unionOf);

            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq3")){ //a is eq to pc and (not b) and (not c)
            Set<OWLClassExpression> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(Init.getFactory().getOWLObjectComplementOf(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+s))));
            }
            if(parent!=null){
                owlClasses.add(parent);
            }

            OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(owlClasses);
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq4")){ // a eq to pc and p only (d or e or ...)
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = Init.getFactory().getOWLObjectUnionOf(owlClasses);
            OWLObjectAllValuesFrom allValuesFrom = Init.getFactory().getOWLObjectAllValuesFrom(property,unionOf);
            OWLClassExpression intersectionOf = allValuesFrom;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,allValuesFrom);
            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq5")){ // pc and op cardinality value
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLObjectCardinalityRestriction cardinality;
            if(ptrn.getCardinalityType().equals("min")){
                cardinality = Init.getFactory().getOWLObjectMinCardinality(ptrn.getCardinality(),property);
            }else if(ptrn.getCardinalityType().equals("max")){
                cardinality = Init.getFactory().getOWLObjectMaxCardinality(ptrn.getCardinality(),property);
            }else{
                cardinality = Init.getFactory().getOWLObjectExactCardinality(ptrn.getCardinality(),property);
            }
            OWLClassExpression intersectionOf = cardinality;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,cardinality);
            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq6")){// a eq to pc and op has value individual
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLIndividual individual = Init.getFactory().getOWLNamedIndividual(IRI.create(Variables.baseIRI+ptrn.getIndividuals().get(0)));
            OWLObjectHasValue hasValue =Init.getFactory().getOWLObjectHasValue(property,individual);
            OWLClassExpression intersectionOf = hasValue;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,hasValue);
            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq7")){// a eq t pc and (not b some c) and (not d some e) and ...

            Set<OWLClassExpression> owlClasses = new HashSet<>();
            for(int i = 0;i<ptrn.getoProperties().size();i++){
                OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(i)));
                OWLClass clz = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(i)));
                OWLObjectSomeValuesFrom someValuesFrom = Init.getFactory().getOWLObjectSomeValuesFrom(property,clz);
                owlClasses.add(Init.getFactory().getOWLObjectComplementOf(someValuesFrom));
            }
            if(parent!=null){
                owlClasses.add(parent);
            }

            OWLObjectIntersectionOf intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(owlClasses);
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq8")){ // a eq to op some d
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLClass clz = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(0)));
            OWLObjectSomeValuesFrom someValuesFrom = Init.getFactory().getOWLObjectSomeValuesFrom(property,clz);
            OWLClassExpression intersectionOf = someValuesFrom;
            if(parent!=null){
                intersectionOf = Init.getFactory().getOWLObjectIntersectionOf(parent,someValuesFrom);
            }
            axiom = Init.getFactory().getOWLEquivalentClassesAxiom(current,intersectionOf);
        }

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

    public String addOrRemoveDisjointClass(Pattern ptrn, int addOrRemove) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getCurrentClass()));
        OWLClass dis = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(0)));
        OWLAxiom dja = Init.getFactory().getOWLDisjointClassesAxiom(current,dis);
        if(addOrRemove==1){
            return UtilMethods.addAxiom(dja);
        }else{
            return UtilMethods.removeAxiom(dja);
        }

    }

//1=add
    public String addOrRemoveDomainOf(Pattern ptrn,int addOrRemove) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getCurrentClass()));
        OWLAxiom axiom;
        if(new ObjectPropertyService().getAllOProperties().contains(ptrn.getoProperties().get(0))){
            OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            axiom = Init.getFactory().getOWLObjectPropertyDomainAxiom(property,current);
        }else{
            OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            axiom = Init.getFactory().getOWLDataPropertyDomainAxiom(property,current);
        }
        if(addOrRemove==1){
            return UtilMethods.addAxiom(axiom);
        }else{
            return UtilMethods.removeAxiom(axiom);
        }


    }

    public String addOrremoveRangeOf(Pattern ptrn, int addOrRemove) throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLClass current = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+ptrn.getCurrentClass()));
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
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
