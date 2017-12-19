package hello.bean;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.ArrayList;
import java.util.List;

public class ChangeKeeper {
    private List<OWLAxiomChange> changeQueue;
    private List<OWLIndividual> individuals;
    private List<OWLAnnotation> annotations;

    public ChangeKeeper() {
    }

    public ChangeKeeper(List<OWLAxiomChange> changeQueue, List<OWLIndividual> individuals, List<OWLAnnotation> annotations) {
        this.changeQueue = changeQueue;
        this.individuals = individuals;
        this.annotations = annotations;
    }

    public List<OWLAxiomChange> getChangeQueue() {
        return changeQueue;
    }

    public void setChangeQueue(List<OWLAxiomChange> changeQueue) {
        this.changeQueue = changeQueue;
    }

    public List<OWLIndividual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<OWLIndividual> individuals) {
        this.individuals = individuals;
    }

    public List<OWLAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<OWLAnnotation> annotations) {
        this.annotations = annotations;
    }


}
