package hello.service;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
/**
 * Created by Lotus on 9/5/2017.
 */
public class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

    private final Set<OWLClass> processedClasses;
    private final Set<OWLOntology> onts;

    private final Set<OWLObjectSomeValuesFrom> someValues;
    private final Set<OWLObjectAllValuesFrom> allValues;

    private final Set<OWLObjectPropertyExpression> restrictedProperties;

    public RestrictionVisitor(Set<OWLOntology> onts) {
        processedClasses = new HashSet<>();
        restrictedProperties = new HashSet<>();
        someValues = new HashSet<>();
        allValues = new HashSet<>();
        this.onts = onts;
    }

    @Override
    public void visit(OWLClass ce) {
        if (!processedClasses.contains(ce)) {
            // If we are processing inherited restrictions then we
            // recursively visit named supers. Note that we need to keep
            // track of the classes that we have processed so that we don't
            // get caught out by cycles in the taxonomy
            processedClasses.add(ce);
            for (OWLOntology ont : onts) {
                for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(ce)) {
                    System.out.println(ax);
                    ax.getSuperClass().accept(this);
                }
            }
        }
    }

    @Override
    public void visit(@NotNull OWLObjectAllValuesFrom values) {
        allValues.add(values);
    }

    @Override
    public void visit(@NotNull OWLObjectSomeValuesFrom ce) {
        // This method gets called when a class expression is an existential
        // (someValuesFrom) restriction and it asks us to visit it
        someValues.add(ce);
        restrictedProperties.add(ce.getProperty());
    }

    public Set<OWLObjectPropertyExpression> getRestrictedProperties() {
        return restrictedProperties;
    }

    /**
     * @return the someValues
     */
    public Set<OWLObjectSomeValuesFrom> getSomeValues() {
        return someValues;
    }

    /**
     * @return the allValues
     */
    public Set<OWLObjectAllValuesFrom> getAllValues() {
        return allValues;
    }

}
