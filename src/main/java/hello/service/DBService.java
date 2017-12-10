package hello.service;

import hello.bean.MainChangeCol;
import hello.bean.MainChanges;
import hello.bean.mode.*;
import hello.service.dbRepo.*;
import hello.util.UtilMethods;
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

    public void removeClass(String clz, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(2);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(1);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setChangeAxiom("Declaration "+ clz);
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ontoChangeRepository.save(ontoChange);
        saveAllAxioms(ontoChange);

    }

    public void addNewClass(String clz, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(1);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(1);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setChangeAxiom(UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)));
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ontoChangeRepository.save(ontoChange);

        saveAllAxioms(ontoChange);
    }

    public void addObjectProperty(String prop, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(1);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(2);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(prop);
        ontoChange.setChangeAxiom(UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)));
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ontoChangeRepository.save(ontoChange);

        saveAllAxioms(ontoChange);


    }

    public void removeObjectProperty(String clz, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(2);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(2);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setChangeAxiom("Declaration "+ clz);
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ontoChangeRepository.save(ontoChange);
        saveAllAxioms(ontoChange);

    }

    public void addDataProperty(String prop, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(1);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(3);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(prop);
        ontoChange.setChangeAxiom(UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)));
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ontoChangeRepository.save(ontoChange);
        saveAllAxioms(ontoChange);


    }

    public void removeDataProperty(String clz, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(2);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(3);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setChangeAxiom("Declaration "+ clz);
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ontoChangeRepository.save(ontoChange);
        saveAllAxioms(ontoChange);

    }

    public void addAxiom(String prop, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(1);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(4);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(prop);
        ontoChange.setChangeAxiom(UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)));
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ontoChangeRepository.save(ontoChange);

        saveAllAxioms(ontoChange);

    }

    public void removeAxiom(String clz, String author, String des,int vId){
        User u = userRepository.findUserByName(author);
        ChangeType type = changeTypeRepository.findChangeTypeById(2);
        ChangeOn changeOn = changeOnRepository.findChangeOnById(4);
        OntoVersion version = ontoVersionRepository.findOntoVersionById(vId);

        OntoChange ontoChange = new OntoChange();
        ontoChange.setUser(u);
        ontoChange.setChangeType(type);
        ontoChange.setChangeOn(changeOn);
        ontoChange.setOntoVersion(version);
        ontoChange.setDescription(des);
        ontoChange.setConcept(clz);
        ontoChange.setChangeAxiom(UtilMethods.manchesterExplainer(UtilMethods.axiomsQueue.get(0)));
        ontoChange.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ontoChangeRepository.save(ontoChange);
        saveAllAxioms(ontoChange);

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
            cols.setConcept(ontoChange.getConcept());
            cols.setChangeOn(ontoChange.getChangeAxiom());
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

    private void saveAllAxioms(OntoChange ontoChange){
        if(UtilMethods.axiomsQueue!=null){
            for(OWLAxiom axiom:UtilMethods.axiomsQueue){
                ChangeDes changeDes = new ChangeDes();
                changeDes.setOntoChange(ontoChange);
                changeDes.setDescription(UtilMethods.manchesterExplainer(axiom));
                changeDes.setObject(UtilMethods.toByts(axiom));
                changeDesRepository.save(changeDes);

            }
        }

        if(UtilMethods.removedInstances!=null){
            for(OWLNamedIndividual i: UtilMethods.removedInstances){
                ChangeInstances instances= new ChangeInstances();
                instances.setOntoChange(ontoChange);
                instances.setObject(UtilMethods.toByts(i));
                instances.setDescription(i.getIRI().getShortForm());

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
}
