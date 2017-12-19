package hello;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.google.common.base.Optional;
import hello.bean.mode.OntoVersion;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import jdk.nashorn.internal.runtime.ParserException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import java.io.IOException;
import java.util.*;

import static hello.util.UtilMethods.checkConsistency;

/**
 * Created by Lotus on 8/21/2017.
 */
public class MyTest {
    private static OWLOntologyManager manager;
    private static OWLOntology ontology;
    private static OWLReasonerFactory owlReasonerFactory;
    private static OWLReasoner reasoner;
    private static OWLDataFactory dataFactory;

    public static void main(String[] args) throws OWLException, IOException {
        manager = OWLManager.createOWLOntologyManager();
        UtilMethods utils = new UtilMethods();
        ontology = utils.loadOntology(manager,Variables.baseIRI);
        owlReasonerFactory = new StructuralReasonerFactory();
        dataFactory = OWLManager.getOWLDataFactory();
        reasoner = owlReasonerFactory.createNonBufferingReasoner(ontology);
        System.out.println("-----------------Test running-----------------");
       // System.out.println( isFunctional("hasApplicationMethodForControlMethodEvent"));
        Set<OWLObjectProperty> propertySet = Init.getOntology().getObjectPropertiesInSignature();
        System.out.println(propertySet);

        OntoVersion ontoVersion= new OntoVersion();
        ontoVersion.setMainVersion(1);
        ontoVersion.setSubVersion(1);
        ontoVersion.setChangeVersion(0);
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



    public static void parseClassExpression(
            String classExpressionString) {
        Init init =new Init();
        Set<OWLOntology> importsClosure = init.getOntology().getImportsClosure();
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(
                new BidirectionalShortFormProviderAdapter(manager, importsClosure,
                        new SimpleShortFormProvider()));
        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
        parser.setDefaultOntology(init.getOntology());
        parser.setOWLEntityChecker(entityChecker);
        parser.setStringToParse(classExpressionString);


        OWLClassExpression axiom=parser.parseClassExpression();
        System.out.println(axiom);
        // return parser.parseClassExpression();
    }
}
