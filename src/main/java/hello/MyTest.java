package hello;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.clarkparsia.owlapiv3.OWL;
import hello.util.Variables;
import jdk.nashorn.internal.runtime.ParserException;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import java.util.*;
import java.util.regex.*;

import static hello.util.UtilMethods.checkConsistency;
import static hello.util.UtilMethods.loadOntology;

/**
 * Created by Lotus on 8/21/2017.
 */
public class MyTest {
    private static OWLOntologyManager manager;
    private static OWLOntology ontology;
    private static OWLReasonerFactory owlReasonerFactory;
    private static OWLReasoner reasoner;
    private static OWLDataFactory dataFactory;

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        manager = OWLManager.createOWLOntologyManager();
        ontology = loadOntology(manager);
        owlReasonerFactory = new StructuralReasonerFactory();
        dataFactory = OWLManager.getOWLDataFactory();
        reasoner = owlReasonerFactory.createNonBufferingReasoner(ontology);

        System.out.println("-----------------Test running-----------------");
        processAxioms(ontology);
        List<String> strings = new ArrayList<>();
        for(OWLClass c:ontology.getClassesInSignature()){
            Set<OWLEquivalentClassesAxiom> axioms = ontology.getEquivalentClassesAxioms(c);
            for(OWLAxiom a: axioms){

                String s = explain(ontology,manager,owlReasonerFactory,reasoner,a);
                String rel = "(\\w+)\\s⊑\\s(\\w+)$";
               // String rel2 = "(\\w+)\\s⊑\\s∃\\s(\\w+)\\.\\w+$";
                String rel2 = "(\\w+)\\s⊑\\s∃\\s(\\w+).\\{(\\w+)\\}";

                Pattern p = Pattern.compile(rel,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                strings.add(s);
                Matcher m = p.matcher(s);
                if (m.find())
                {


                }else{
                    Pattern p2 = Pattern.compile(rel2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

                    Matcher m2 = p2.matcher(s);
                    if(!m2.find()){

                    }

                }
               //
            }
        }
        System.out.println(strings.size());
        for(String s:strings){
            System.out.println(s);
        }


        OWLClass entity = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(Variables.baseIRI + "VegetarianPizza"));
//        for(OWLClass c:ontology.getClassesInSignature()){
//            Set<OWLEquivalentClassesAxiom> sx = ontology.getEquivalentClassesAxioms(c);
//
//            List<IEqClassPattern> set = getEquivalentAxioms(c);
////            for(IEqClassPattern p: set){
////                System.out.println(entity.getIRI().getShortForm()+" "+p.toString());
////            }
//            for(OWLAxiom a:sx){
//                System.out.println(explain(ontology,manager,owlReasonerFactory,reasoner,a));
//
//            }
//
//        }

        //subClassPAllValueFrom




    }





    private static void printClasses(Set<OWLClass> classes) {
        System.out.println("ALL CLASSES (" + classes.size() + ")");
        for (OWLClass c : classes) {
            System.out.println(c.toString());
        }
        System.out.println("-----------------------------------");
    }

    private static void printLogicalAxioms(Set<OWLLogicalAxiom> logicalAxioms) {
        System.out.println("ALL LOGICAL AXIOMS (" + logicalAxioms + ")");

    }

    private static void printAxioms(Set<OWLAxiom> axioms) {

        Set<OWLAxiom> axIndividual = new HashSet<OWLAxiom>();
        Set<OWLAxiom> axDataProperty = new HashSet<OWLAxiom>();
        Set<OWLAxiom> axObjectProperty = new HashSet<OWLAxiom>();
        Set<OWLAxiom> axClass = new HashSet<OWLAxiom>();
        Set<OWLAxiom> axOther = new HashSet<OWLAxiom>();

        for (OWLAxiom a : axioms) {
            a.getSignature();
            if ((a instanceof OWLClassAxiom)) {
                axClass.add(a);
            } else if (a instanceof OWLDataPropertyAxiom) {
                axDataProperty.add(a);
            } else if (a instanceof OWLObjectPropertyAxiom) {
                axDataProperty.add(a);
            } else if (a instanceof OWLIndividualAxiom) {
                axIndividual.add(a);
            } else
                axOther.add(a);
        }

        System.out.println("ALL AXIOMS (" + axioms.size() + ")");
        for (OWLAxiom ax : axIndividual) {
            String line;
            line = ax.toString() + " TYPE: Individual";
            System.out.println(line);
        }
        for (OWLAxiom ax : axDataProperty) {
            String line;
            line = ax.toString() + " TYPE: DataProperty";
            System.out.println(line);
        }
        for (OWLAxiom ax : axObjectProperty) {
            String line;
            line = ax.toString() + " TYPE: ObjectProperty";
            System.out.println(line);
        }
        for (OWLAxiom ax : axClass) {
            String line;
            line = ax.toString() + " TYPE: Class";
            System.out.println(line);
        }
        for (OWLAxiom ax : axOther) {
            String line;
            line = ax.toString() + " TYPE: Other";
            System.out.println(line);
        }
        System.out.println("-----------------------------------");

    }


    public static String removeSubClass(String parent, String child) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = loadOntology(man);
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLClass entity = df.getOWLEntity(EntityType.CLASS, IRI.create("http://lumii.lv/ontologies/pizza.owl#", parent));
        OWLClass entity2 = df.getOWLEntity(EntityType.CLASS, IRI.create("http://lumii.lv/ontologies/pizza.owl#", child));
        OWLAxiom axiom = df.getOWLSubClassOfAxiom(entity, entity2);
        RemoveAxiom removeAxiom = new RemoveAxiom(ontology, axiom);

        man.applyChange(removeAxiom);

        String reason = checkConsistency(ontology, man);


        man.saveOntology(ontology);
        System.out.println(reason);
        return reason;
    }

    public static void deleteClass(String classToDelete) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        OWLOntology ontology = loadOntology(man);

        OWLClass remove = man.getOWLDataFactory().getOWLClass(IRI.create("http://lumii.lv/ontologies/pizza.owl#", classToDelete));
        Set<OWLAxiom> toRemove = new HashSet<>();

        for (OWLAxiom select : ontology.getAxioms()) {

            if (select.getSignature().contains(remove)) {
                toRemove.add(select);

            }
        }

        man.removeAxioms(ontology, toRemove);

        man.saveOntology(ontology);

    }

    private static void renameURI(OWLEntity entityToRename, IRI newNameIRI, OWLOntologyManager mngr) throws OWLOntologyStorageException {
        if (!mngr.contains(newNameIRI)) {
            OWLEntityRenamer owlEntityRenamer = new OWLEntityRenamer(mngr, mngr.getOntologies());
            if (newNameIRI == null) {
                return;
            }
            final List<OWLOntologyChange> changes;
            changes = owlEntityRenamer.changeIRI(entityToRename.getIRI(), newNameIRI);
            for (OWLOntologyChange ch : changes) {
                System.out.println(ch.getChangeRecord());
            }
            mngr.applyChanges(changes);
            for (OWLOntology on : mngr.getOntologies()) {
                mngr.saveOntology(on);
            }
        }


    }

    //////////////


    public static String explain(OWLOntology ontology, OWLOntologyManager manager, OWLReasonerFactory reasonerFactory, OWLReasoner reasoner, OWLAxiom axiomToExplain) {

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
        return  renderer.render(axiom);

    }

  //  private static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

//    private static void printIndented(Tree<OWLAxiom> node, String indent) {
//        OWLAxiom axiom = node.getUserObject();
//        System.out.println(DLtoenglish(renderer.render(axiom)));
//        if (!node.isLeaf()) {
//            for (Tree<OWLAxiom> child : node.getChildren()) {
//                printIndented(child, indent + "    ");
//            }
//        }
//    }

    public Set<OWLClass> getEquivalentClasses(OWLClassExpression classExpression, OWLOntology ontology)
            throws ParserException {
//        if (classExpressionString.trim().length() == 0) {
//            return Collections.emptySet();
//        }
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(classExpression);
        Set<OWLClass> result;
        if (classExpression.isAnonymous()) {
            result = equivalentClasses.getEntities();
        } else {
            result = equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
        }
        return result;
    }


    private static void processAxioms(OWLOntology ontology){
        OWLObjectVisitor objectVisitor = new OWLObjectVisitor();
        if(ontology!=null)
        {
            Set<OWLAxiom> axiomSet = ontology.getAxioms();
            HashMap<String,Integer> axiomsMap = new HashMap<String, Integer>();
            if(axiomSet!=null && axiomSet.size()>0)
            {
                Iterator<OWLAxiom> setIter = axiomSet.iterator();
                OWLAxiom axiom = null;
                while(setIter.hasNext()){
                    axiom = setIter.next();
                    if(axiomsMap.containsKey(axiom.getAxiomType().getName())){
                        axiomsMap.put(axiom.getAxiomType().getName(),axiomsMap.get(axiom.getAxiomType().getName())+1);
                    }
                    else{
                        axiomsMap.put(axiom.getAxiomType().getName(),1);
                    }
                    axiom.accept(objectVisitor);
                }
                System.out.println("-------------- Axiom Info for Ontology =["+ontology+"]");
                System.out.println("No.of Axiom Types =["+axiomsMap.size()+"]");
                Iterator<String> mapIter = axiomsMap.keySet().iterator();
                String axiomType = null;
                while(mapIter.hasNext()){
                    axiomType = mapIter.next();
                    System.out.println("Axiom Type =["+axiomType+"] No.of Axioms =["+axiomsMap.get(axiomType)+"]");
                }
                System.out.println("-------------- ------------------------------------------");
            }
        }

       // System.out.println(new OWLObjectVisitor().);

    }



    public static OWLClass getParent(OWLClass clz) {
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
}
