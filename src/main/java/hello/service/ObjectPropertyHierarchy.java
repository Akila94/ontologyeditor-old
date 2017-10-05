package hello.service;

import hello.bean.TreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/20/2017.
 */
public class ObjectPropertyHierarchy {

    private final OWLReasonerFactory reasonerFactory;
    private final OWLOntology ontology;
    private TreeNode objectPropertyTree = null;

    public ObjectPropertyHierarchy(OWLReasonerFactory reasonerFactory, OWLOntology ontology) {
        this.reasonerFactory = reasonerFactory;
        this.ontology = ontology;
    }

    public TreeNode printHierarchy(OWLObjectProperty property) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, property, 0);
        for (OWLClass pr : ontology.getClassesInSignature()) {
            if (!reasoner.isSatisfiable(pr)) {
                System.out.println("XXX: " + pr.getIRI().toString());
            }
        }
        return objectPropertyTree;
    }

    private void printHierarchy(OWLReasoner reasoner, OWLObjectProperty property, int level)
            throws OWLException {

        if(objectPropertyTree == null){
            objectPropertyTree = new TreeNode(property.getIRI().getShortForm());
        }

        for (OWLObjectPropertyExpression child : reasoner.getSubObjectProperties(property, true).getFlattened()) {
            searchTree(property.asOWLObjectProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLObjectProperty().getIRI().getShortForm());
            if (!child.equals(property)) {
                printHierarchy(reasoner, child.asOWLObjectProperty(), level + 1);
            }
        }
    }
}
