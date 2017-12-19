package hello.service;

import hello.bean.ChangeKeeper;
import hello.bean.Pattern;
import hello.bean.TreeNode;
import hello.util.Init;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;

import static hello.util.UtilMethods.searchTree;

/**
 * Created by Lotus on 8/20/2017.
 */
public class DataPropertyService {

    private TreeNode objectPropertyTree = null;

    public DataPropertyService() {
    }

    public TreeNode printHierarchy(OWLDataProperty property) throws OWLException {
        System.out.println(property);
        OWLReasoner reasoner = Init.getOwlReasonerFactory(Variables.STRUCTURAL).createNonBufferingReasoner(Init.getOntology());
        printHierarchy(reasoner, property, 0);
        for (OWLClass pr : Init.getOntology().getClassesInSignature()) {
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
            if(!child.asOWLDataProperty().equals(Init.getFactory().getOWLBottomDataProperty())){
                searchTree(property.asOWLDataProperty().getIRI().getShortForm(), objectPropertyTree).addChild(child.asOWLDataProperty().getIRI().getShortForm());
            }

            if (!child.equals(property)) {
                printHierarchy(reasoner, child.asOWLDataProperty(), level + 1);
            }
        }
    }

    public String addDProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLDeclarationAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String addSubDProperty(String p, String pa) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+p));
        OWLDataProperty parent = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+pa));
        OWLSubDataPropertyOfAxiom sub = Init.getFactory().getOWLSubDataPropertyOfAxiom(property,parent);
        return UtilMethods.addAxiom(sub);
    }

    public String removeDProperty(String p) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+p));
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
            if(UtilMethods.manchesterExplainer(a).contains("DataProperty:")){
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
        UtilMethods.checkConsistency(Init.getOntology());
        return "Property Deleted";
    }

    public List<String> getAllDProperties(){
        Set<OWLDataProperty> propertySet = Init.getOntology().getDataPropertiesInSignature();
        List<String> properties = new ArrayList<>();
        for(OWLDataProperty p: propertySet){
            properties.add(p.getIRI().getShortForm());
        }
        Collections.sort(properties);
        return properties;
    }

    public String addFunctionalDProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalDataPropertyAxiom(property);
        return UtilMethods.addAxiom(declare);
    }

    public String removeFunctionalDProperty(String p) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+p));
        OWLAxiom declare = Init.getFactory().getOWLFunctionalDataPropertyAxiom(property);
        return UtilMethods.removeAxiom(declare);
    }

    public boolean isFunctional(String prop){
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        return !Init.getOntology().getFunctionalDataPropertyAxioms(property).isEmpty();

    }

    public List<String> getDisjointDProperties(String prop){
        List<String> disjoints = new ArrayList<>();
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLDisjointDataPropertiesAxiom> disOP = Init.getOntology().getDisjointDataPropertiesAxioms(property);
        for(OWLDisjointDataPropertiesAxiom p: disOP){
            for(OWLDataProperty pr:p.getDataPropertiesInSignature()){
                String sf = pr.getIRI().getShortForm();
                if(!sf.equals(prop)){
                    disjoints.add(sf);
                }
            }
        }
        Collections.sort(disjoints);
        disjoints.remove(prop);
        return disjoints;
    }

    public List<String> getNonDisjointDProperties(String prop){
        List<String> all = getAllDProperties();
        List<String> dis =getDisjointDProperties(prop);
        all.removeAll(dis);
        return all;
    }

    public String addDisDProperty(String prop,String dis) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLDataProperty disP = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+dis));
        OWLDisjointDataPropertiesAxiom axiom = Init.getFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeDisDProperty(String prop,String dis) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLDataProperty disP = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+dis));
        OWLDisjointDataPropertiesAxiom axiom = Init.getFactory().getOWLDisjointDataPropertiesAxiom(property,disP);
        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getDPDomains(String prop){
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLDataPropertyDomainAxiom> domainAxioms = Init.getOntology().getDataPropertyDomainAxioms(property);

        List<String> domains = new ArrayList<>();
        for(OWLDataPropertyDomainAxiom a: domainAxioms){
            domains.add(a.getClassesInSignature().iterator().next().getIRI().getShortForm());
        }
        return domains;
    }

    public String addDPDomain(String prop,String doamin) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass d = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+doamin));
        OWLDataPropertyDomainAxiom axiom = Init.getFactory().getOWLDataPropertyDomainAxiom(property,d);

        return UtilMethods.addAxiom(axiom);
    }

    public String removeDPDomain(String prop,String doamin) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLClass d = Init.getFactory().getOWLClass(IRI.create(Variables.baseIRI+doamin));
        OWLDataPropertyDomainAxiom axiom = Init.getFactory().getOWLDataPropertyDomainAxiom(property,d);

        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getDPRanges(String prop){
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        Set<OWLDataPropertyRangeAxiom> domainAxioms = Init.getOntology().getDataPropertyRangeAxioms(property);

        List<String> ranges = new ArrayList<>();
        for(OWLDataPropertyRangeAxiom a: domainAxioms){
            ranges.add(a.getDatatypesInSignature().iterator().next().getIRI().getShortForm());
        }
        return ranges;
    }

    public String addDPRange(String prop,String range) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLDatatype r = Init.getFactory().getOWLDatatype(IRI.create(Variables.baseIRI+range));
        OWLDataPropertyRangeAxiom axiom = Init.getFactory().getOWLDataPropertyRangeAxiom(property,r);
        return UtilMethods.addAxiom(axiom);
    }

    public String removeDPRange(String prop,String range) throws OWLOntologyCreationException, OWLOntologyStorageException {
        String mRange=null;
        if(range.equals("Literal")){
            mRange="rdfs:Literal";
        }else if(range.equals("XMLLiteral") ||range.equals("PlainLiteral")){
            mRange="rdf:"+range;
        }else if(range.equals("real") || range.equals("rational")){
            mRange="owl:"+range;
        }else{
            mRange="xsd:"+range;
        }
        OWLDataProperty property = Init.getFactory().getOWLDataProperty(IRI.create(Variables.baseIRI+prop));
        OWLDatatype r = Init.getFactory().getOWLDatatype(IRI.create(Variables.baseIRI+mRange));
        OWLDataPropertyRangeAxiom axiom = Init.getFactory().getOWLDataPropertyRangeAxiom(property,r);
        return UtilMethods.removeAxiom(axiom);
    }

    public List<String> getDataTypes(){
        List<String> dTypes  = new ArrayList<>();
        dTypes.add("owl:rational");
        dTypes.add("owl:real");
        dTypes.add("rdf:PlainLiteral");
        dTypes.add("rdf:XMLLiteral");
        dTypes.add("rdfs:Literal");
        dTypes.add("xsd:boolean");
        dTypes.add("xsd:dateTime");
        dTypes.add("xsd:dateTimeStamp");
        dTypes.add("xsd:decimal");
        dTypes.add("xsd:int");
        dTypes.add("xsd:float");
        dTypes.add("xsd:language");
        dTypes.add("xsd:Name");
        dTypes.add("xsd:negativeInteger");
        dTypes.add("xsd:nonNegativeInteger");
        dTypes.add("xsd:nonPositiveInteger");
        dTypes.add("xsd:positiveInteger");
        dTypes.add("xsd:string");
        dTypes.add("xsd:string");

        return dTypes;

    }
}
