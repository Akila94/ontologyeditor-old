package hello.util;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import hello.bean.TreeNode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by Lotus on 8/20/2017.
 */
public class UtilMethods {

    public static int consistent;

    public static OWLOntology loadOntology(OWLOntologyManager manager) throws OWLOntologyCreationException {
        return manager.loadOntologyFromOntologyDocument(new File(Variables.ontologyFilePath));
    }
    public static TreeNode searchTree(String className, TreeNode node){
        if (node.getName().equals(className)){
            return node;
        }
        List<?> children = node.getChildren();
        TreeNode res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            res = searchTree(className, (TreeNode) children.get(i));
        }
        return res;
    }

    public static String checkConsistency (OWLOntology ontology, OWLOntologyManager man) throws OWLOntologyCreationException
    {
        OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        String answer;

        if(reasoner.isConsistent())
        {
            if(reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size()>0)
            {
                StringBuilder builder = new StringBuilder();
                Set<OWLClass> clzes = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
                for(OWLClass s:clzes){
                    builder.append(s.getIRI().getShortForm()+", ");
                }
                answer = "Merged ontology FAILED the consistency test, Unsatisfiable classes detected: " + builder.toString() ;
                consistent=0;
            }
            else
            {
                answer = "Merged ontology PASSED the consistency test ";
                consistent=1;
            }
        }
        else
        {
            answer = "Merged ontology FAILED the consistency test";
            consistent=0;
        }
        reasoner.dispose();
        return(answer);
    }


}
