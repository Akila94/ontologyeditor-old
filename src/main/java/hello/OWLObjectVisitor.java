package hello;

import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

/**
 * Created by Lotus on 9/12/2017.
 */
public class OWLObjectVisitor extends OWLObjectVisitorAdapter {

    // private static final Logger logger = LoggerFactory.getLogger(OWLObjectVisitor.class);

    @Override
    public void visit(OWLDataMaxCardinality desc) {
        super.visit(desc);
        System.out.println("Max Cardinality =["+desc.getCardinality()+"]");
    }

    @Override
    public void visit(OWLDataMinCardinality desc) {
        super.visit(desc);
        System.out.println("Min Cardinality =["+desc.getCardinality()+"]");
    }

    @Override
    public void visit(OWLDataExactCardinality desc){
        super.visit(desc);
        System.out.println("Min Cardinality =["+desc.getCardinality()+"]");
    }

}