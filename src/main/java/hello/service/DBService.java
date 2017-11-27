package hello.service;

import hello.bean.MainChangeCol;
import hello.bean.MainChanges;
import hello.bean.mode.*;
import hello.service.dbRepo.*;
import hello.util.UtilMethods;
import hello.util.Variables;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DBService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private OntoChangeRepository ontoChangeRepository;
    @Autowired
    private OntoVersionRepository ontoVersionRepository;
    @Autowired
    private ChangeTypeRepository changeTypeRepository;
    @Autowired
    private ChangeOnRepository changeOnRepository;
    @Autowired
    private ChangeInstancesRepository changeInstancesRepository;
    @Autowired
    private ChangeDesRepository changeDesRepository;
    @Autowired
    private ChangeAnnotationRepository changeAnnotationRepository;

    public void addOrRemoveClass(String clz, String author, String des,int t){
        String cType;
        if(t==1){
            cType="Class Added: ";
        }else{
           cType="Class Removed: ";
        }
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(t);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(29);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(1);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ontoChangeRepository.save(ontoChange);
        if(UtilMethods.axiomsQueue!=null){
            for(OWLAxiom axiom:UtilMethods.axiomsQueue){
                ChangeDes changeDes = new ChangeDes();
                changeDes.setOntoChange(ontoChange);
                changeDes.setDescription(cType+UtilMethods.manchesterExplainer(axiom));
                changeDes.setObject(UtilMethods.toByts(axiom));
                changeDesRepository.save(changeDes);

            }
        }

        if(UtilMethods.removedInstances!=null){
            for(OWLNamedIndividual i: UtilMethods.removedInstances){
                ChangeInstances instances= new ChangeInstances();
                instances.setOntoChange(ontoChange);
                instances.setObject(UtilMethods.toByts(i));
                instances.setDescription(i.getIRI().getShortForm()+ " "+cType);

                changeInstancesRepository.save(instances);
            }
        }

        if(UtilMethods.removedAnnotations!=null) {
            for (OWLAnnotation annotation : UtilMethods.removedAnnotations) {
                System.out.println(annotation);
                ChangeAnnotation changeAnnotation = new ChangeAnnotation();
                changeAnnotation.setOntoChange(ontoChange);
                changeAnnotation.setObject(UtilMethods.toByts(annotation));
                changeAnnotation.setAnnKey(annotation.getProperty().toString());
                changeAnnotation.setAnnValue(annotation.getValue().asLiteral().asSet().iterator().next().getLiteral());
                changeAnnotationRepository.save(changeAnnotation);
            }
        }

    }

    public void addObjectProperty(String prop, String author, String des){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(1);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(2);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(1);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(prop);
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));


    }


    public List<MainChangeCol> getAllChanges(){
        MainChanges changes = new MainChanges();
        List<MainChangeCol> mainChangeCols = new ArrayList<>();

        Set<OntoChange> ontoChanges =  ontoChangeRepository.findAll();

        for(OntoChange ontoChange:ontoChanges){
            MainChangeCol cols = new MainChangeCol();
            cols.setId(ontoChange.getId());
            cols.setChangeType(ontoChange.getChangeType().getDescription());
            cols.setDescription(ontoChange.getDescription());
            cols.setAuthor(ontoChange.getUser().getName());
            cols.setVersion(ontoChange.getOntoVersion().getName());
            cols.setTime(ontoChange.getTimestamp().toString());
            cols.setChangeOn(ontoChange.getConcept());
            mainChangeCols.add(cols);
        }
        changes.setRowCount(ontoChanges.size());
        changes.setRows(mainChangeCols);
        return mainChangeCols;
    }

    public File getOntologyByVersion(int main,int sub, int change){
        OntoVersion version = ontoVersionRepository.findOntoVersionByMainVersionAndSubVersionAndChangeVersion(main,sub,change);
        ClassLoader classLoader = getClass().getClassLoader();
        System.out.println(version.getLocation());
        return new File(classLoader.getResource(version.getLocation()).getFile());
    }
}
