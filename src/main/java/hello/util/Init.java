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

import static hello.util.UtilMethods.loadOntology;

/**
 * Created by Lotus on 9/9/2017.
 */
public class Init {
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLReasonerFactory owlReasonerFactory;
    private OWLReasoner reasoner;
    private OWLDataFactory factory;

    public Init() {
        manager = OWLManager.createOWLOntologyManager();
        this.factory = OWLManager.getOWLDataFactory();
        try{
            ontology = loadOntology(manager);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public void setReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

    public void setFactory(OWLDataFactory factory) {
        this.factory = factory;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLReasonerFactory getOwlReasonerFactory(String type) {
        if(type==Variables.STRUCTURAL){
            owlReasonerFactory = new StructuralReasonerFactory();
        } else if(type==Variables.Pellet){
            owlReasonerFactory = new PelletReasonerFactory();
        }
        return owlReasonerFactory;
    }

    public OWLReasoner getReasoner(String type) {
        reasoner = this.getOwlReasonerFactory(type).createNonBufferingReasoner(ontology);
        return reasoner;
    }
}
