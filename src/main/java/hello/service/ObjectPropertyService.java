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

import java.util.*;

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

    public String removeProperty(String p) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : Init.getOntology().getAxioms())
        {
            if(select.getSignature().contains(property))
            {
                toRemove.add(select);
            }
        }
        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
        OWLDeclarationAxiom axiom = Init.getFactory().getOWLDeclarationAxiom(property);
//        UtilMethods.removeAxiom(axiom);
        return "PASSED: Property Deleted";
    }

    public List<String> getAllOProperties(){
         Set<OWLObjectProperty> propertySet = Init.getOntology().getObjectPropertiesInSignature();
         List<String> properties = new ArrayList<>();
         for(OWLObjectProperty p: propertySet){
             properties.add(p.getIRI().getShortForm());
         }
        Collections.sort(properties);
         return properties;
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
    public String addSymmetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addAsymmetricProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
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
        if(Init.getOntology().getInverseObjectPropertyAxioms(property).isEmpty()){
            return null;
        }
        String i = Init.getOntology().getInverseObjectPropertyAxioms(property).iterator().next().getFirstProperty().getNamedProperty().getIRI().getShortForm();
        if(i.equals(prop)){
            return Init.getOntology().getInverseObjectPropertyAxioms(property).iterator().next().getSecondProperty().getNamedProperty().getIRI().getShortForm();
        }
        return i;
    }

    public String addInverseProperty(String prop, String iProp) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty iProperty = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+iProp));
        OWLAxiom axiom = Init.getFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);

        return UtilMethods.addAxiom(axiom);

    }

    public String removeInverseProperty(String prop) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLInverseObjectPropertiesAxiom> toRemove = Init.getOntology().getInverseObjectPropertyAxioms(property);
        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
        return "PASSED";
    }

    public boolean isFunctional(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getFunctionalObjectPropertyAxioms(property).isEmpty();

    }

    public boolean isInverseFunctional(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getInverseFunctionalObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isSymmetric(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getSymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isAsymmetric(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getAsymmetricObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isTransitive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getTransitiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isReflexive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getReflexiveObjectPropertyAxioms(property).isEmpty();
    }

    public boolean isIrreflexive(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getIrreflexiveObjectPropertyAxioms(property).isEmpty();
    }

    public List<String> getDisjointProperties(String prop){
        List<String> disjoints = new ArrayList<>();
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLDisjointObjectPropertiesAxiom> disOP = Init.getOntology().getDisjointObjectPropertiesAxioms(property);
        for(OWLDisjointObjectPropertiesAxiom p: disOP){
            disjoints.add(p.getObjectPropertiesInSignature().iterator().next().getIRI().getShortForm());
        }
        Collections.sort(disjoints);
        return disjoints;
    }

    public List<String> getNonDisjointProperties(String prop){
        List<String> all = getAllOProperties();
        List<String> dis =getDisjointProperties(prop);
        all.removeAll(dis);
        return all;
    }

    public String addDisOProperty(String prop,String dis) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty disP = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+dis));
        OWLDisjointObjectPropertiesAxiom axiom = Init.getFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeDisOProperty(String prop,String dis) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty disP = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+dis));
        OWLDisjointObjectPropertiesAxiom axiom = Init.getFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getDomains(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLObjectPropertyDomainAxiom> domainAxioms = Init.getOntology().getObjectPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLObjectPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public String addDomain(String prop,String doamin) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass d = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+doamin));
        OWLObjectPropertyDomainAxiom axiom = Init.getFactory().getOWLObjectPropertyDomainAxiom(property,d);

        return UtilMethods.addAxiom(axiom);
    }

    public String removeDomain(String prop,String doamin) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass d = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+doamin));
        OWLObjectPropertyDomainAxiom axiom = Init.getFactory().getOWLObjectPropertyDomainAxiom(property,d);

        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getRanges(String prop){
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLObjectPropertyRangeAxiom> domainAxioms = Init.getOntology().getObjectPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLObjectPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public String addRange(String prop,String range) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass r = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+range));
        OWLObjectPropertyRangeAxiom axiom = Init.getFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeRange(String prop,String range) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass r = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+range));
        OWLObjectPropertyRangeAxiom axiom = Init.getFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return UtilMethods.removeAxiom(axiom);
    }
}
