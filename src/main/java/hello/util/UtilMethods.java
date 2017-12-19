package hello.util;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SingleExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import com.clarkparsia.pellet.IncrementalChangeTracker;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.io.Files;
import hello.bean.ChangeKeeper;
import hello.bean.TreeNode;
import hello.service.DBService;
import org.apache.commons.io.FileUtils;
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
import java.util.*;

/**
 * Created by Lotus on 8/20/2017.
 */
public class UtilMethods {

    public static int consistent;

    public static List<OWLAxiom> axiomsQueue;
    public static Set<OWLNamedIndividual> removedInstances;
    public static Set<OWLAnnotation> removedAnnotations;

    public static List<ChangeKeeper> changeQueue = new ArrayList<>();

    public OWLOntology loadOntology(OWLOntologyManager manager, String fileName) throws OWLOntologyCreationException {
        System.out.println(Variables.ontoPath);
        return manager.loadOntologyFromOntologyDocument(new File(fileName));
    }

    public static void renameFile(String oldName, String newName) {
        try {
            File file = new File(newName);
            if (file.createNewFile()) {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }
            FileWriter writer = new FileWriter(file);
            writer.write("");
            writer.close();

            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(new File(oldName));
                os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                assert is != null;
                is.close();
                assert os != null;
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OWLOntology loadOntology(OWLOntologyManager manager, int main, int sub, int change) throws OWLOntologyCreationException {
        //  ClassLoader classLoader = getClass().getClassLoader();
        // File file = new File(classLoader.getResource("ontoDir/SLN_Ontology_"+main+"."+sub+"."+change).getFile());
        return manager.loadOntologyFromOntologyDocument(new File(Variables.baseOntoPath + main + "." + sub + "." + change + ".owl"));
    }

    public static TreeNode searchTree(String className, TreeNode node) {
        if (node.getText().equals(className)) {
            return node;
        }
        List<?> children = node.getChildren();
        TreeNode res = null;
        for (int i = 0; res == null && i < children.size(); i++) {
            res = searchTree(className, (TreeNode) children.get(i));
        }
        return res;
    }

    public static String checkConsistency(OWLOntology ontology) throws OWLOntologyCreationException {

        String answer;

        Init.getPelletReasoner().refresh();
        if (Init.getPelletReasoner().isConsistent()) {
            if (Init.getPelletReasoner().getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0) {
                StringBuilder builder = new StringBuilder();
                Set<OWLClass> clzes = Init.getReasoner().getUnsatisfiableClasses().getEntitiesMinusBottom();
                for (OWLClass s : clzes) {
                    builder.append(s.getIRI().getShortForm() + ", ");
                }
                answer = "Merged ontology FAILED the consistency test, Unsatisfiable classes detected: " + builder.toString();
                consistent = 0;
            } else {
                answer = "Merged ontology PASSED the consistency test ";
                consistent = 1;
            }
        } else {
            answer = "Merged ontology FAILED the consistency test";
            consistent = 0;
        }

        Init.getPelletReasoner().flush();
        return (answer);

    }

    public static byte[] toByts(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;
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

    public static Object toObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Object o = null;
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();

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
        return renderer.render(axiomToExplain);

    }

    public static String removeAxiom(OWLAxiom axiom) throws OWLOntologyCreationException, OWLOntologyStorageException {

        RemoveAxiom removeAxiom = new RemoveAxiom(Init.getOntology(), axiom);
        Init.getManager().applyChange(removeAxiom);
        String reason = checkConsistency(Init.getOntology());
        if (consistent == 0) {
            AddAxiom addAxiom = new AddAxiom(Init.getOntology(), axiom);
            Init.getManager().applyChange(addAxiom);
        } else {
            axiomsQueue = new ArrayList<>();
            axiomsQueue.add(axiom);

            ChangeKeeper changeKeeper = new ChangeKeeper();
            List<OWLAxiomChange> changes = new ArrayList<>();
            changes.add(new AddAxiom(Init.getOntology(), axiom));
            changeKeeper.setChangeQueue(changes);
            changeQueue.add(changeKeeper);

        }
        Init.getManager().saveOntology(Init.getOntology());
        return reason;
    }

    public static String addAxiom(OWLAxiom axiom) throws OWLOntologyCreationException, OWLOntologyStorageException {
        System.out.println("add come");
        AddAxiom addAxiom = new AddAxiom(Init.getOntology(), axiom);
        Init.getManager().applyChange(addAxiom);
        System.out.println("add applied");
        String reason = checkConsistency(Init.getOntology());
        System.out.println("add checked");
        if (consistent == 0) {
            RemoveAxiom removeAxiom = new RemoveAxiom(Init.getOntology(), axiom);
            Init.getManager().applyChange(removeAxiom);
        } else {
            axiomsQueue = new ArrayList<>();
            axiomsQueue.add(axiom);

            ChangeKeeper changeKeeper = new ChangeKeeper();
            List<OWLAxiomChange> changes = new ArrayList<>();
            changes.add(new RemoveAxiom(Init.getOntology(), axiom));
            changeKeeper.setChangeQueue(changes);
            changeQueue.add(changeKeeper);

        }
        System.out.println("add save");
        Init.getManager().saveOntology(Init.getOntology());
        System.out.println("add out");
        return reason;
    }
}
