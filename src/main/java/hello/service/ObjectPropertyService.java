package hello.service;

import hello.bean.ChangeKeeper;
import hello.bean.TreeNode;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.codehaus.groovy.ast.Variable;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/20/2017.
 */

@Service
public class ObjectPropertyService {

    private TreeNode objectPropertyTree = null;

    public ObjectPropertyService() {
    }

    public TreeNode printHierarchy(OWLObjectProperty property) throws OWLException {
        OWLReasoner reasoner = Init.getOwlReasonerFactory(Variables.STRUCTURAL).createNonBufferingReasoner(Init.getOntology());
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
            if(!child.isAnonymous()){
            if(!child.asOWLObjectProperty().equals(Init.getFactory().getOWLBottomObjectProperty())) {
                searchTree(property.asOWLObjectProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLObjectProperty().getIRI().getShortForm());
            }
            }
            if (!child.equals(property) &&!child.isAnonymous()) {
                printHierarchy(reasoner, child.asOWLObjectProperty(), level + 1);
            }
        }
    }

    public String addOProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLDeclarationAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String removeOProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        Set<OWLAxiom> toRemove = new HashSet<>();
        for (OWLAxiom select : Init.getOntology().getAxioms())
        {
            if(select.getSignature().contains(property))
            {
                toRemove.add(select);
            }
        }

        UtilMethods.axiomsQueue = new ArrayList<>();
        UtilMethods.axiomsQueue.addAll(toRemove);

        int index = 0;
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            if(UtilMethods.manchesterExplainer(a).contains("ObjectProperty:")){
                Collections.swap(UtilMethods.axiomsQueue,UtilMethods.axiomsQueue.indexOf(a),index);
                index++;
            }
        }

        ChangeKeeper changeKeeper = new ChangeKeeper();
        List<OWLAxiomChange> changes = new ArrayList<>();

        changeKeeper.setChangeQueue(changes);
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            changes.add(new AddAxiom(Init.getOntology(),a));
        }
        UtilMethods.changeQueue.add(changeKeeper);


        UtilMethods.removedAnnotations = (Set<OWLAnnotation>) EntitySearcher.getAnnotations(property, Init.getOntology());

        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
        UtilMethods.checkConsistency();
        return "Property Deleted";
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

    public String addSubOProperty(String p, String pa) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLObjectProperty parent = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+pa));
        OWLSubObjectPropertyOfAxiom sub = Init.getFactory().getOWLSubObjectPropertyOfAxiom(property,parent);
        return UtilMethods.addAxiom(sub);
    }

    public String addFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addInverseFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addTransitiveProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addSymmetricProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addAsymmetricProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addReflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }
    public String addIreflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String removeFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeInverseFunctionalProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeTransitiveProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLTransitiveObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeSymetricProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLSymmetricObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeAsymetricProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeReflexiveProperty(String p) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLReflexiveObjectPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }
    public String removeIreflexiveProperty(String p) throws Exception {
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

    public String addInverseProperty(String prop, String iProp) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty iProperty = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+iProp));
        OWLAxiom axiom = Init.getFactory().getOWLInverseObjectPropertiesAxiom(property, iProperty);

        return UtilMethods.addAxiom(axiom);

    }

    public String removeInverseProperty(String prop) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLInverseObjectPropertiesAxiom> toRemove = Init.getOntology().getInverseObjectPropertyAxioms(property);

        UtilMethods.axiomsQueue = new ArrayList<>();
        UtilMethods.axiomsQueue.addAll(toRemove);

        int index = 0;
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            if(UtilMethods.manchesterExplainer(a).contains("ObjectProperty:")){
                Collections.swap(UtilMethods.axiomsQueue,UtilMethods.axiomsQueue.indexOf(a),index);
                index++;
            }
        }

        ChangeKeeper changeKeeper = new ChangeKeeper();
        List<OWLAxiomChange> changes = new ArrayList<>();

        changeKeeper.setChangeQueue(changes);
        for(OWLAxiom a:UtilMethods.axiomsQueue){
            changes.add(new AddAxiom(Init.getOntology(),a));
        }
        UtilMethods.changeQueue.add(changeKeeper);


        Init.getManager().removeAxioms(Init.getOntology(), toRemove);
        Init.getManager().saveOntology(Init.getOntology());
        return UtilMethods.checkConsistency();
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

    public String addDisOProperty(String prop,String dis) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLObjectProperty disP = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+dis));
        OWLDisjointObjectPropertiesAxiom axiom = Init.getFactory().getOWLDisjointObjectPropertiesAxiom(property,disP);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeDisOProperty(String prop,String dis) throws Exception {
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

    public String addDomain(String prop,String doamin) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass d = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+doamin));
        OWLObjectPropertyDomainAxiom axiom = Init.getFactory().getOWLObjectPropertyDomainAxiom(property,d);

        return UtilMethods.addAxiom(axiom);
    }

    public String removeDomain(String prop,String doamin) throws Exception {
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

    public String addRange(String prop,String range) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass r = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+range));
        OWLObjectPropertyRangeAxiom axiom = Init.getFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeRange(String prop,String range) throws Exception {
        OWLObjectProperty property = Init.getFactory().getOWLObjectProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass r = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+range));
        OWLObjectPropertyRangeAxiom axiom = Init.getFactory().getOWLObjectPropertyRangeAxiom(property,r);
        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getOPCharacteristics(String property){
        List<String> propertyTypes = new ArrayList<>();
        if(isFunctional(property)){
            propertyTypes.add("F");
        }
        if(isInverseFunctional(property)){
            propertyTypes.add("IF");
        }
        if(isTransitive(property)){
            propertyTypes.add("T");
        }
        if(isSymmetric(property)){
            propertyTypes.add("S");
        }if(isAsymmetric(property)){
            propertyTypes.add("AS");
        }
        if(isReflexive(property)){
            propertyTypes.add("R");
        }if(isIrreflexive(property)){
            propertyTypes.add("IR");
        }
        return propertyTypes;
    }
}
