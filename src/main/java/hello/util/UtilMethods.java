package hello.util;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import hello.bean.TreeNode;
import hello.service.DBService;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Lotus on 8/20/2017.
 */
public class UtilMethods {

    public static int consistent;

    public static List<OWLAxiom> axiomsQueue;
    public static Set<OWLNamedIndividual> removedInstances;
    public static Set<OWLAnnotation> removedAnnotations;

    public OWLOntology loadOntology(OWLOntologyManager manager, String fileName) throws OWLOntologyCreationException {
      //  ClassLoader classLoader = getClass().getClassLoader();
      //  File file = new File(classLoader.getResource(fileName).getFile());
        return manager.loadOntologyFromOntologyDocument(new File(Variables.ontoPath));
    }

    public OWLOntology loadOntology(OWLOntologyManager manager,int main, int sub, int change) throws OWLOntologyCreationException {
      //  ClassLoader classLoader = getClass().getClassLoader();
       // File file = new File(classLoader.getResource("ontoDir/SLN_Ontology_"+main+"."+sub+"."+change).getFile());
        return manager.loadOntologyFromOntologyDocument(new File(Variables.baseOntoPath+main+"."+sub+"."+change+".owl"));
    }
    public static TreeNode searchTree(String className, TreeNode node){
        if (node.getText().equals(className)){
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

    public static byte[] toByts(Object o){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes=null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            bytes = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes){
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Object o=null;
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o=in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return o;
    }

    public static String manchesterExplainer(OWLAxiom axiomToExplain) {
        OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        return  renderer.render(axiomToExplain);

    }

    public static String removeAxiom(OWLAxiom axiom) throws OWLOntologyCreationException, OWLOntologyStorageException {

        RemoveAxiom removeAxiom = new RemoveAxiom(Init.getOntology(),axiom);
        Init.getManager().applyChange(removeAxiom);
        String reason = checkConsistency(Init.getOntology(), Init.getManager());
        if (UtilMethods.consistent == 0) {
            AddAxiom addAxiom = new AddAxiom(Init.getOntology(),axiom);
            Init.getManager().applyChange(addAxiom);
        }else{
            axiomsQueue = new ArrayList<>();
            axiomsQueue.add(axiom);
        }
        Init.getManager().saveOntology(Init.getOntology());
        return reason;
    }

    public static String addAxiom(OWLAxiom axiom) throws OWLOntologyCreationException, OWLOntologyStorageException {
        AddAxiom addAxiom = new AddAxiom(Init.getOntology(),axiom);
        Init.getManager().applyChange(addAxiom);
        String reason = checkConsistency(Init.getOntology(), Init.getManager());
        if (UtilMethods.consistent == 0) {
            RemoveAxiom removeAxiom = new RemoveAxiom(Init.getOntology(),axiom);;
            Init.getManager().applyChange(removeAxiom);
        }else{
            axiomsQueue = new ArrayList<>();
            axiomsQueue.add(axiom);
        }
        Init.getManager().saveOntology(Init.getOntology());
        return reason;
    }
}
