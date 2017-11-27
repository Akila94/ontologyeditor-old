package hello.util;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


/**
 * Created by Lotus on 9/9/2017.
 */
public class Init {
    private final static OWLOntologyManager manager= OWLManager.createOWLOntologyManager();
    private static OWLOntology ontology ;
    private static OWLReasonerFactory owlReasonerFactory;
    private static OWLReasoner reasoner;
    private final static OWLDataFactory factory= OWLManager.getOWLDataFactory();


    static {
        try{
            UtilMethods untiles = new UtilMethods();
            if(ontology==null) {
                ontology = untiles.loadOntology(manager, Variables.ontoPath);
            }
            if(owlReasonerFactory==null) {
                owlReasonerFactory = new StructuralReasonerFactory();
            }
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    public static OWLReasoner getReasoner() {
        return reasoner;
    }

    public static void setReasoner(OWLReasoner r) {
        reasoner = r;
    }

    public static OWLDataFactory getFactory() {
        return factory;
    }

    public static OWLOntologyManager getManager() {
        return manager;
    }


    public static OWLOntology getOntology() {
        return ontology;
    }

    public static void setOntology(OWLOntology o) {
        ontology = o;
    }

    public static OWLReasonerFactory getOwlReasonerFactory(String type) {
        if(type==Variables.STRUCTURAL){
            owlReasonerFactory = new StructuralReasonerFactory();
        } else if(type==Variables.Pellet){
            owlReasonerFactory = new PelletReasonerFactory();
        }
        return owlReasonerFactory;
    }

    public static OWLReasoner getReasoner(String type) {
        reasoner = getOwlReasonerFactory(type).createNonBufferingReasoner(ontology);
        return reasoner;
    }
}
