package hello.service;

import hello.bean.TreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/20/2017.
 */
public class DataPropertyHierarchy {

    private final OWLReasonerFactory reasonerFactory;
    private final OWLOntology ontology;
    private TreeNode objectPropertyTree = null;

    public DataPropertyHierarchy(OWLReasonerFactory reasonerFactory, OWLOntology ontology) {
        this.reasonerFactory = reasonerFactory;
        this.ontology = ontology;
    }

    public TreeNode printHierarchy(OWLDataProperty property) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, property, 0);
        for (OWLClass pr : ontology.getClassesInSignature()) {
            if (!reasoner.isSatisfiable(pr)) {
                System.out.println("XXX: " + pr.getIRI().toString());
            }
        }
        return objectPropertyTree;
    }

    private void printHierarchy(OWLReasoner reasoner, OWLDataProperty property, int level)
            throws OWLException {

        if(objectPropertyTree == null){
            objectPropertyTree = new TreeNode(property.getIRI().getShortForm());
        }

        for (OWLDataPropertyExpression child : reasoner.getSubDataProperties(property, true).getFlattened()) {
            if(!child.asOWLDataProperty().getIRI().getShortForm().equals("bottomDataProperty")){
                searchTree(property.asOWLDataProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLDataProperty().getIRI().getShortForm());
            }

            if (!child.equals(property)) {
                printHierarchy(reasoner, child.asOWLDataProperty(), level + 1);
            }
        }
    }
}
