package hello.bean;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by Lotus on 9/23/2017.
 */
public class ClassAxiom {
    int id;
    private String axiom;
    private OWLAxiom owlAxiom;
    private String axiomType;

    public String getAxiom() {
        return axiom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAxiom(String axiom) {
        this.axiom = axiom;
    }

    public OWLAxiom getOwlAxiom() {
        return owlAxiom;
    }

    public void setOwlAxiom(OWLAxiom owlAxiom) {
        this.owlAxiom = owlAxiom;
    }

    public String getAxiomType() {
        return axiomType;
    }

    public void setAxiomType(String axiomType) {
        this.axiomType = axiomType;
    }

    public ClassAxiom() {

    }
}
