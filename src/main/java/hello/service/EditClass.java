package hello.service;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import hello.bean.Pattern;
import hello.bean.ClassAxiom;
import hello.bean.TreeNode;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import java.util.*;

import static hello.util.UtilMethods.*;

/**
 * Created by Lotus on 8/16/2017.
 */

public class EditClass {

    private final OWLReasonerFactory reasonerFactory;
    private final OWLOntology ontology;
    private final OWLReasoner reasoner;
    private TreeNode classTree = null;

    public EditClass(OWLReasonerFactory reasonerFactory, OWLOntology ontology) {
        this.reasonerFactory = reasonerFactory;
        this.ontology = ontology;
        this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

    }

    public TreeNode printHierarchy(OWLClass clazz) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, clazz, 0);
        for (OWLClass cl : ontology.getClassesInSignature()) {
            if (!reasoner.isSatisfiable(cl)) {
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

    public List<String> getAllClasses(){
        List<String> classList = new ArrayList<>();


        Set<OWLClass> classes = ontology.getClassesInSignature();
        for(OWLClass owlClass:classes){
            classList.add(owlClass.getIRI().getShortForm());
        }
        classList.remove("Thing");
        Collections.sort(classList);
        return classList;
    }
    public List<String> getAllObjectProperties(){
        List<String> propertyList = new ArrayList<>();


        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
        for(OWLObjectProperty p:properties){
            propertyList.add(p.getIRI().getShortForm());
        }
        Collections.sort(propertyList);
        return propertyList;
    }
    public List<String> getAllDataProperties(){
        List<String> propertyList = new ArrayList<>();
        Set<OWLDataProperty> properties = ontology.getDataPropertiesInSignature();
        for(OWLDataProperty p:properties){
            propertyList.add(p.getIRI().getShortForm());
        }
        Collections.sort(propertyList);
        return propertyList;
    }
    public List<String> getAllIndividuals(){
        List<String> individuals = new ArrayList<>();
        Set<OWLNamedIndividual> properties = ontology.getIndividualsInSignature();
        for(OWLNamedIndividual i:properties){
            individuals.add(i.getIRI().getShortForm());
        }
        Collections.sort(individuals);
        return individuals;
    }
    public List<String> getAllDataTypes(){
        List<String> dTypes = new ArrayList<>();
        Set<OWLDatatype> properties = ontology.getDatatypesInSignature();
        for(OWLDatatype t:properties){
            dTypes.add(t.getIRI().getShortForm());
        }
        Collections.sort(dTypes);
        return dTypes;
    }

    public String addClass(String className) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = loadOntology(man);
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLEntity entity = df.getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI, className));
        OWLAxiom declare = df.getOWLDeclarationAxiom(entity);

        man.addAxiom(ontology, declare);

        String reason = checkConsistency(ontology, man);

        if (UtilMethods.consistent == 0) {
            man.removeAxiom(ontology, declare);
        }
        man.saveOntology(ontology);
        System.out.println(reason);
        return reason;
    }



    public String addSubClass(Pattern ptrn) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = loadOntology(man);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        AddAxiom addAxiom;
        OWLAxiom axiom = null;


        OWLClass childC = df.getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI,ptrn.getCurrentClass() ));

        //a is sub class of b
        if(ptrn.getPatternType().equals("sp1")){
            if(ptrn.getClassList().get(0).equals("Thing")){
                addClass(ptrn.getClassList().get(0));
            }else{
                OWLClass parentC = df.getOWLClass(IRI.create(Variables.baseIRI, ptrn.getClassList().get(0)));
                axiom = df.getOWLSubClassOfAxiom(childC, parentC);
            }

        }else if(ptrn.getPatternType().equals("sp2")){
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLClass individual = df.getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(0)));
            OWLObjectSomeValuesFrom someValuesFrom= df.getOWLObjectSomeValuesFrom(property,individual);
            axiom = df.getOWLSubClassOfAxiom(childC,someValuesFrom);
        }else if(ptrn.getPatternType().equals("sp3")){
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(df.getOWLClass(IRI.create(Variables.baseIRI+s)));
            }

            OWLObjectUnionOf unionOf = df.getOWLObjectUnionOf(owlClasses);

            OWLObjectAllValuesFrom allValuesFrom =df.getOWLObjectAllValuesFrom(property,unionOf);
            axiom = df.getOWLSubClassOfAxiom(childC,allValuesFrom);
        }else if(ptrn.getPatternType().equals("sp4")){
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLIndividual individual = df.getOWLNamedIndividual(IRI.create(Variables.baseIRI+ptrn.getIndividuals().get(0)));
            OWLObjectHasValue hasValue= df.getOWLObjectHasValue(property,individual);
            axiom = df.getOWLSubClassOfAxiom(childC,hasValue);
        }else if(ptrn.getPatternType().equals("sp5")){
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(df.getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = df.getOWLObjectUnionOf(owlClasses);
            axiom = df.getOWLSubClassOfAxiom(childC,unionOf);
        }else if(ptrn.getPatternType().equals("sp6")){
            OWLDataProperty property = df.getOWLDataProperty(IRI.create(Variables.baseIRI+ptrn.getdProperties().get(0)));
            OWLLiteral literal = df.getOWLLiteral(ptrn.getLiterals().get(0));
            OWLDataHasValue hasValue = df.getOWLDataHasValue(property,literal);

            axiom = df.getOWLSubClassOfAxiom(childC,hasValue);
        }


        addAxiom = new AddAxiom(ontology, axiom);
        man.applyChange(addAxiom);

        String reason = checkConsistency(ontology, man);

        if (UtilMethods.consistent == 0) {
            RemoveAxiom removeAxiom = new RemoveAxiom(ontology,axiom);
            man.applyChange(removeAxiom);
        }
        man.saveOntology(ontology);
        System.out.println(reason);
        return reason;
    }

    public String addEqClass(Pattern ptrn) throws OWLOntologyCreationException, OWLOntologyStorageException {
        AddAxiom addAxiom;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = loadOntology(man);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLAxiom axiom= null;
        OWLClass current = df.getOWLClass(IRI.create(Variables.baseIRI+ptrn.getCurrentClass()));
        OWLClass parent=getParent(current);
        if(ptrn.getPatternType().equals("eq1")){  // a equivalent b and dP has value literal

            OWLDataProperty property = df.getOWLDataProperty(IRI.create(Variables.baseIRI+ptrn.getdProperties().get(0)));
            OWLLiteral literal = df.getOWLLiteral(ptrn.getLiterals().get(0));
            OWLDataHasValue hasValue = df.getOWLDataHasValue(property,literal);
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,hasValue);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq2")){// a eq to pC and a or b or c or ...
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(df.getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = df.getOWLObjectUnionOf(owlClasses);
            if(parent.getIRI().getShortForm().equals("Thing")){
                OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,unionOf);
                axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);
            }else{
                axiom = df.getOWLEquivalentClassesAxiom(current,unionOf);
            }
        }else if(ptrn.getPatternType().equals("eq3")){ //a is eq to pc and (not b) and (not c)
            Set<OWLClassExpression> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(df.getOWLObjectComplementOf(df.getOWLClass(IRI.create(Variables.baseIRI+s))));
            }
            owlClasses.add(parent);
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(owlClasses);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq4")){ // a eq to pc and p only (d or e or ...)
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            Set<OWLClass> owlClasses = new HashSet<>();
            for(String s:ptrn.getClassList()){
                owlClasses.add(df.getOWLClass(IRI.create(Variables.baseIRI+s)));
            }
            OWLObjectUnionOf unionOf = df.getOWLObjectUnionOf(owlClasses);
            OWLObjectAllValuesFrom allValuesFrom = df.getOWLObjectAllValuesFrom(property,unionOf);
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,allValuesFrom);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq5")){ // pc and op cardinality value
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLObjectCardinalityRestriction cardinality;
            if(ptrn.getCardinalityType().equals("min")){
                cardinality = df.getOWLObjectMinCardinality(ptrn.getCardinality(),property);
            }else if(ptrn.getCardinalityType().equals("max")){
                cardinality = df.getOWLObjectMaxCardinality(ptrn.getCardinality(),property);
            }else{
                cardinality = df.getOWLObjectExactCardinality(ptrn.getCardinality(),property);
            }
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,cardinality);

            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq6")){// a eq to pc and op has value individual
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLIndividual individual = df.getOWLNamedIndividual(IRI.create(Variables.baseIRI+ptrn.getIndividuals().get(0)));
            OWLObjectHasValue hasValue =df.getOWLObjectHasValue(property,individual);
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,hasValue);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);

        }else if(ptrn.getPatternType().equals("eq7")){// a eq t pc and (not b some c) and (not d some e) and ...

            Set<OWLClassExpression> owlClasses = new HashSet<>();
            for(int i = 0;i<ptrn.getoProperties().size();i++){
                OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(i)));
                OWLClass clz = df.getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(i)));
                OWLObjectSomeValuesFrom someValuesFrom = df.getOWLObjectSomeValuesFrom(property,clz);
                owlClasses.add(df.getOWLObjectComplementOf(someValuesFrom));
            }
            owlClasses.add(parent);

            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(owlClasses);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);
        }else if(ptrn.getPatternType().equals("eq8")){ // a eq to op some d
            OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(Variables.baseIRI+ptrn.getoProperties().get(0)));
            OWLClass clz = df.getOWLClass(IRI.create(Variables.baseIRI+ptrn.getClassList().get(0)));
            OWLObjectSomeValuesFrom someValuesFrom = df.getOWLObjectSomeValuesFrom(property,clz);
            OWLObjectIntersectionOf intersectionOf = df.getOWLObjectIntersectionOf(parent,someValuesFrom);
            axiom = df.getOWLEquivalentClassesAxiom(current,intersectionOf);
        }

        System.out.println(axiom);
        addAxiom = new AddAxiom(ontology, axiom);
        man.applyChange(addAxiom);

        String reason = checkConsistency(ontology, man);

        if (UtilMethods.consistent == 0) {
            RemoveAxiom removeAxiom = new RemoveAxiom(ontology,axiom);
            man.applyChange(removeAxiom);
        }
        man.saveOntology(ontology);
        System.out.println(reason);
        return reason;
    }

//    public List<ISubClassPattern> getSubClassAxioms(OWLClass clz) {
//        Set<OWLSubClassOfAxiom> suclss = ontology.getSubClassAxiomsForSubClass(clz);
//        List<ISubClassPattern> patterns = new ArrayList<>();
//
//        for (OWLSubClassOfAxiom subClassOfAxiom : suclss) {
//            OWLClassExpression expression = subClassOfAxiom.getSuperClass();
//            Set<OWLObjectProperty> ex3 = expression.getObjectPropertiesInSignature();
//            if(expression.getClassExpressionType() == ClassExpressionType.OWL_CLASS){
//                patterns.add(new SubClassPClass(clz.getIRI().getShortForm(),expression.asOWLClass().getIRI().getShortForm()));
//
//            }else if (expression.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
//                Set<OWLClassExpression> expression2 = expression.getNestedClassExpressions();
//                if (expression2.iterator().next().getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
//                    patterns.add(new SubClassPAllValuesFrom(clz.getIRI().getShortForm(),ex3.iterator().next().getIRI().getShortForm(),getUnionClasses(expression)));
//                }else{
//                   List<String> list =  new ArrayList<>();
//                   list.add(expression2.iterator().next().asOWLClass().getIRI().getShortForm());
//                   patterns.add(new SubClassPAllValuesFrom(clz.getIRI().getShortForm(),ex3.iterator().next().getIRI().getShortForm(),list));
//                }
//            }else if(expression.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM){
//                Set<OWLClassExpression> expression2 = expression.getNestedClassExpressions();
//                patterns.add(new SubClassPSomeValuesFrom(clz.getIRI().getShortForm(),ex3.iterator().next().getIRI().getShortForm(),expression2.iterator().next().getClassesInSignature().iterator().next().getIRI().getShortForm()));
//            }else if(expression.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF){
//                patterns.add(new SubClassPUnionOf(clz.getIRI().getShortForm(),getUnionClasses(expression)));
//            }else if(expression.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_VALUE){
//                Set<OWLClassExpression> expression2 = expression.getNestedClassExpressions();
//                patterns.add(new SubClassPObjectHasValue(clz.getIRI().getShortForm(),ex3.iterator().next().getIRI().getShortForm(),expression2.iterator().next().getIndividualsInSignature().iterator().next().getIRI().getShortForm()));
//
//            }else if(expression.getClassExpressionType() == ClassExpressionType.DATA_HAS_VALUE) {
//                Set<OWLClassExpression> expression2 = expression.getNestedClassExpressions();
//                patterns.add(new SubClassPDataHasValue(clz.getIRI().getShortForm(),ex3.iterator().next().getIRI().getShortForm(),expression2.iterator().next().getDatatypesInSignature().iterator().next().getIRI().getShortForm()));
//            }
//
//        }
//        return patterns;
//    }


    public OWLClass getParent(OWLClass clz) {
        Set<OWLClass> classes = ontology.getClassesInSignature();
        OWLClass parent=null;
        for(OWLClass c:classes){

            for (OWLClass child : reasoner.getSubClasses(c, true).getFlattened()) {
                if (reasoner.isSatisfiable(child)) {
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
        Set<OWLEquivalentClassesAxiom> sx = ontology.getEquivalentClassesAxioms(clz);
        int i=0;
        for(OWLAxiom a:sx){
            ClassAxiom ptrn = new ClassAxiom();
            ptrn.setAxiom(explain(ontology,ontology.getOWLOntologyManager(),reasonerFactory,reasoner,a));
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
        Set<OWLSubClassOfAxiom> sx = ontology.getSubClassAxiomsForSubClass(clz);
        int i=0;
        for(OWLAxiom a:sx){
            ClassAxiom ptrn = new ClassAxiom();
            ptrn.setAxiom(explain(ontology,ontology.getOWLOntologyManager(),reasonerFactory,reasoner,a));
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
        Set<OWLDisjointClassesAxiom> sx = ontology.getDisjointClassesAxioms(clz);
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

    public String removeAxiom(OWLAxiom axiom) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String result="success";
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = loadOntology(man);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        RemoveAxiom removeAxiom = new RemoveAxiom(ontology,axiom);
        man.applyChange(removeAxiom);
        man.saveOntology(ontology);
        return result;
    }

    public List<String> getDoaminOf(OWLClass clz){
        List<String> domainOf = new ArrayList<>();
        for(OWLObjectProperty p: ontology.getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyDomainAxiom> da = ontology.getObjectPropertyDomainAxioms(p);
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
        for(OWLObjectProperty p: ontology.getObjectPropertiesInSignature()){
            Set<OWLObjectPropertyRangeAxiom> da = ontology.getObjectPropertyRangeAxioms(p);
            for(OWLObjectPropertyRangeAxiom a:da){
                if(a.getClassesInSignature().iterator().next().equals(clz)){
                    rangeOf.add(a.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
                }
            }
        }
        return rangeOf;
    }



    public String explain(OWLOntology ontology, OWLOntologyManager manager, OWLReasonerFactory reasonerFactory, OWLReasoner reasoner, OWLAxiom axiomToExplain) {

        DefaultExplanationGenerator explanationGenerator = new DefaultExplanationGenerator(manager, reasonerFactory, ontology, reasoner, new SilentExplanationProgressMonitor());
        Set<OWLAxiom> explanation = explanationGenerator.getExplanation(axiomToExplain);
        ExplanationOrderer deo = null;
        if(axiomToExplain.getAxiomType().equals(AxiomType.SUBCLASS_OF)){
            deo = new hello.ExplanationOrdererImpl(manager);
        }else{
            deo = new ExplanationOrdererImpl(manager);
        }

        ExplanationTree explanationTree = deo.getOrderedExplanation(axiomToExplain, explanation);
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
        OWLAxiom axiom = explanationTree.getUserObject();
        return  DLtoenglish(renderer.render(axiom));

    }

    public String DLtoenglish(String exp){
        String eng = exp.replace("≡","equivalent with")
                .replace("⊓","and")
                .replace("⊔","or")
                .replace("¬","not ")
                .replace("⊑","is sub class of ")
                .replace("≥","at least")
                .replace("≤","at most")
                .replace("=","exact")
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
