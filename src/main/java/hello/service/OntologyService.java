package hello.service;

import hello.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

/**
 * Created by Lotus on 10/6/2017.
 */
public class OntologyService {

    public List<String> processAxioms(OWLOntology ontology){
        List<String> strings = new ArrayList<>();
        OWLObjectVisitor objectVisitor = new OWLObjectVisitor();
        if(ontology!=null)
        {
            Set<OWLAxiom> axiomSet = ontology.getAxioms();
            HashMap<String,Integer> axiomsMap = new HashMap<>();
            if(axiomSet!=null && axiomSet.size()>0)
            {
                Iterator<OWLAxiom> axiomIterator = axiomSet.iterator();
                OWLAxiom axiom;
                while(axiomIterator.hasNext()){
                    axiom = axiomIterator.next();
                    if(axiomsMap.containsKey(axiom.getAxiomType().getName())){
                        axiomsMap.put(axiom.getAxiomType().getName(),axiomsMap.get(axiom.getAxiomType().getName())+1);
                    }
                    else{
                        axiomsMap.put(axiom.getAxiomType().getName(),1);
                    }
                    axiom.accept(objectVisitor);
                }
                strings.add("No.of Axiom Types =["+axiomsMap.size()+"]");
                Iterator<String> stringIterator = axiomsMap.keySet().iterator();
                String axiomType;
                while(stringIterator.hasNext()){
                    axiomType = stringIterator.next();
                    strings.add(axiomType +"\t\t\t"+"["+axiomsMap.get(axiomType)+"]");
                }
            }
        }

        return strings;

    }

    public String getOntologyName(OWLOntology owlOntology){
        String ontoName = null;
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("label")||a.getProperty().getIRI().getShortForm().equals("title")){
                ontoName=a.getValue().toString();
                break;
            }
        }
        if(ontoName.isEmpty()){
            ontoName = owlOntology.getOntologyID().getOntologyIRI().asSet().iterator().next().getShortForm();
        }
        return ontoName;
    }

    public String getOntologyVersion(OWLOntology owlOntology){
        return owlOntology.getOntologyID().getVersionIRI().asSet().iterator().next().getShortForm();
    }

    public List<String> getDescription(OWLOntology owlOntology){
        List<String> des = new ArrayList<>();
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("comment")||a.getProperty().getIRI().getShortForm().equals("description")){
                des.add(a.getValue().toString());
            }
        }
        return  des;
    }

    public List<String> getContributors(OWLOntology owlOntology){
        List<String> con = new ArrayList<>();
        Set<OWLAnnotation> annotations = owlOntology.getAnnotations();
        for(OWLAnnotation a:annotations){
            if(a.getProperty().getIRI().getShortForm().equals("contributor")||a.getProperty().getIRI().getShortForm().equals("author")){
                con.add(a.getValue().toString());
            }
        }
        return  con;
    }
}
