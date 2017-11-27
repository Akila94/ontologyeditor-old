package hello.service;

import hello.bean.TreeNode;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.codehaus.groovy.ast.Variable;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/20/2017.
 */
public class ObjectPropertyService {

    private TreeNode objectPropertyTree = null;

    public ObjectPropertyService() {
    }

    public TreeNode printHierarchy() throws OWLException {
        OWLObjectProperty property = Init.getFactory().getOWLTopObjectProperty();
        OWLReasoner reasoner = Init.getOwlReasonerFactory(Variables.Pellet).createNonBufferingReasoner(Init.getOntology());
        printHierarchy(reasoner, property, 0);
        for (OWLClass pr : Init.getOntology().getClassesInSignature()) {
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
            if(!child.asOWLObjectProperty().getIRI().getShortForm().equals("bottomObjectProperty")) {
                searchTree(property.asOWLObjectProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLObjectProperty().getIRI().getShortForm());
            }
            if (!child.equals(property)) {
                printHierarchy(reasoner, child.asOWLObjectProperty(), level + 1);
            }
        }
    }

    public String addProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLDeclarationAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String addSubProperty(String p, String pa) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLObjectProperty parent = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+pa));
        OWLSubObjectPropertyOfAxiom sub = Init.getFactory().getOWLSubObjectPropertyOfAxiom(property,parent);
        return UtilMethods.addAxiom(sub);
    }

    public String addFunctionalProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addInverseFunctionalProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addTransitiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addSymetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addAsymetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addReflexiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addIreflexiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String removeFunctionalProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeInverseFunctionalProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeTransitiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeSymetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeAsymetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeReflexiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeIreflexiveProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }

    public String getInverseProperty(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getInverseObjectPropertyAxioms(property).iterator().next().getSecondProperty().getNamedProperty().getIRI().getShortForm();
    }

    public String addInverseProperty(String prop, String iProp) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty iProperty = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+iProp));
        OWLAxiom axiom = Init.getFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);

        return UtilMethods.addAxiom(axiom);

    }

    public String removeInverseProperty(String prop, String iProp) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty iProperty = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+iProp));
        OWLAxiom axiom = Init.getFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);

        return UtilMethods.removeAxiom(axiom);
    }

    public boolean isFunctional(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getFunctionalObjectPropertyAxioms(property) != null;
    }

    public boolean isInverseFunctional(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getInverseFunctionalObjectPropertyAxioms(property) != null;
    }

    public boolean isSymmetric(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getSymmetricObjectPropertyAxioms(property) != null;
    }

    public boolean isAsymmetric(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getAsymmetricObjectPropertyAxioms(property) != null;
    }

    public boolean isTransitive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getTransitiveObjectPropertyAxioms(property) != null;
    }

    public boolean isReflexive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getReflexiveObjectPropertyAxioms(property) != null;
    }

    public boolean isIrreflexive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return Init.getOntology().getIrreflexiveObjectPropertyAxioms(property) != null;
    }
}
