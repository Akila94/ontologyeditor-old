package hello.service;

import hello.util.UtilMethods;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static hello.util.UtilMethods.checkConsistency;

/**
 * Created by Lotus on 8/22/2017.
 */

public class EditOntology {
    public static String addClass(String categoryName) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("file:///home/admin/onto.owl");


        OWLOntology ontology = man.loadOntology(ontologyIRI);
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLEntity entity = df.getOWLEntity(EntityType.CLASS, IRI.create("http://www.sln4mop.org/ontologies/2014/SLN_Ontology#", categoryName));
        OWLAxiom declare = df.getOWLDeclarationAxiom(entity);

        man.addAxiom(ontology, declare);

        String reason = checkConsistency(ontology, man);

        if (UtilMethods.consistent == 0) {
            man.removeAxiom(ontology, declare);
        }
        man.saveOntology(ontology);
        return reason;
    }
}
