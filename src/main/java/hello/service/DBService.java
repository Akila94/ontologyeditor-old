package hello.service;

import hello.bean.MainChangeCol;
import hello.bean.DetailChange;
import hello.bean.TreeNode;
import hello.bean.VersionTable;
import hello.bean.mode.*;
import hello.service.dbRepo.*;
import hello.util.UtilMethods;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
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

    private TreeNode versionTree;

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
        List<MainChangeCol> mainChangeCols = new ArrayList<>();

        Set<OntoChange> ontoChanges =  ontoChangeRepository.findAll();

        for(OntoChange ontoChange:ontoChanges){
            MainChangeCol cols = new MainChangeCol();
            cols.setId(ontoChange.getId());
            cols.setChangeType(ontoChange.getChangeType().getDescription());
            cols.setDescription(ontoChange.getDescription());
            cols.setAuthor(ontoChange.getUser().getName());
            cols.setVersion(ontoChange.getOntoVersion().getMainVersion()+". "+ontoChange.getOntoVersion().getSubVersion()+". "+ontoChange.getOntoVersion().getChangeVersion());
            cols.setTime(ontoChange.getTimestamp().toString());
            cols.setConcept(ontoChange.getConcept());
            cols.setChangeOn(ontoChange.getChangeAxiom());
            mainChangeCols.add(cols);
        }
        return mainChangeCols;
    }

    public List<DetailChange> getDetailChanges(int ontoChangeId){
        OntoChange ontoChange = ontoChangeRepository.findOntoChangeById(ontoChangeId);
        Set<ChangeDes> changeDes = changeDesRepository.findChangeDesByOntoChange(ontoChange);

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeDes cd:changeDes){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setDescription(cd.getDescription());
            changes.add(dc);
        }

        System.out.println(changes);
        return changes;
    }


    public String getOntologyByVersion(int main,int sub, int change){
        OntoVersion version = ontoVersionRepository.findOntoVersionByMainVersionAndSubVersionAndChangeVersion(main,sub,change);
        System.out.println(version.getLocation());
        return version.getLocation();
    }

    public OntoVersion addVersion(int main, int sub, int change,String location,String name, int prior,boolean active){
        OntoVersion ontoVersion= new OntoVersion();
        ontoVersion.setMainVersion(main);
        ontoVersion.setSubVersion(sub);
        ontoVersion.setChangeVersion(change);
        ontoVersion.setLocation(location);
        ontoVersion.setCurrent(active);
        ontoVersion.setName(name);
        ontoVersion.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ontoVersion.setPrior(prior);
        ontoVersionRepository.save(ontoVersion);
        return ontoVersionRepository.findOntoVersionByMainVersionAndSubVersionAndAndChangeVersion(main,sub,change);
    }

    public void updateSub(int vId){
        OntoVersion ontoVersion=ontoVersionRepository.findOntoVersionById(vId);
        ontoVersion.setSubVersion(ontoVersion.getSubVersion()+1);
        ontoVersionRepository.save(ontoVersion);

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

    public void removeLastRecord(){
    OntoChange ontoChange= ontoChangeRepository.findTop1ByOrderByIdDesc();
    changeDesRepository.removeByOntoChange(ontoChange);
    changeAnnotationRepository.removeByOntoChange(ontoChange);
    changeInstancesRepository.removeByOntoChange(ontoChange);
    ontoChangeRepository.removeById(ontoChange.getId());
    }

    public List<DetailChange> getInstanceChanges(int id) {
        OntoChange ontoChange = ontoChangeRepository.findOntoChangeById(id);
        Set<ChangeInstances> changeInstances = changeInstancesRepository.findChangeInstancesByOntoChange(ontoChange);

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeInstances cd:changeInstances){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setDescription(cd.getDescription());
            changes.add(dc);
        }
        System.out.println(changes);
        return changes;
    }

    public List<DetailChange> getAnnotationChanges(int id) {
        OntoChange ontoChange = ontoChangeRepository.findOntoChangeById(id);
        Set<ChangeAnnotation> changeDes = changeAnnotationRepository.findChangeAnnotationByOntoChange(ontoChange);

        List<DetailChange> changes = new ArrayList<>();
        for(ChangeAnnotation cd:changeDes){
            DetailChange dc = new DetailChange();
            dc.setId(cd.getId());
            dc.setChangeId(cd.getOntoChange().getId());
            dc.setKey(cd.getAnnKey());
            dc.setDescription(cd.getAnnValue());
            changes.add(dc);
        }

        System.out.println(changes);
        return changes;
    }

    public OntoVersion getUserCurrentVersion(String user){
//        Set<User> users = new HashSet<>();
//        users.add(userRepository.findUserByName(user));
//        Set<OntoVersion> versions = ontoVersionRepository.findOntoVersionByUsers(users);
//        for(OntoVersion o : versions){
//            if(o.getCurrent()){
//                return o;
//            }
//        }
        return ontoVersionRepository.findOntoVersionByCurrentEquals(true);
    }

    public OntoVersion changeVersion(int id){
        return ontoVersionRepository.findOntoVersionById(id);
    }
    public void versionUserAssign(String user,int version){

        OntoVersion ontoVersion = ontoVersionRepository.findOntoVersionById(version);
        Set<User> users = ontoVersion.getUsers();
        boolean i=true;
        for(User u:users){
            if(u.getName().equals(user)){
                i = false;
            }
        }
        if(i){
            users.add(userRepository.findUserByName(user));
        }

        ontoVersion.setUsers(users);
        ontoVersionRepository.save(ontoVersion);
    }

    public void versionUserAssign(int oldV,int newV){
        OntoVersion ontoVersion = ontoVersionRepository.findOntoVersionById(oldV);

        OntoVersion newOntoVersion = ontoVersionRepository.findOntoVersionById(newV);
        Set<User> users = ontoVersion.getUsers();
        newOntoVersion.setUsers(users);
        ontoVersion.setUsers(null);
        ontoVersionRepository.save(ontoVersion);
        ontoVersionRepository.save(newOntoVersion);
    }

    public void setInactiveVersion(int id){
        OntoVersion version = ontoVersionRepository.findOntoVersionById(id);
        version.setCurrent(false);
        ontoVersionRepository.save(version);
    }

    public int getMaxVersionNumber(){
        Iterable<OntoVersion> ontoVersions = ontoVersionRepository.findAll();
        int max=0;
        for(OntoVersion ov:ontoVersions){
            if(ov.getMainVersion()>max){
                max = ov.getMainVersion();
            }
        }
        return max;
    }

    public List<VersionTable> getAllVersions(){
        Iterable<OntoVersion> versions = ontoVersionRepository.findAll();
        List<VersionTable> table = new ArrayList<>();
        for(OntoVersion v:versions){
            if(v.getPrior()==0){
                continue;
            }
            VersionTable row = new VersionTable();
            row.setId(v.getId());
            row.setNumber(v.getMainVersion()+"."+v.getSubVersion()+"."+v.getChangeVersion());
            row.setDescription(v.getName());
            OntoVersion pV = ontoVersionRepository.findOntoVersionById(v.getPrior());
            row.setPrior(String.valueOf(pV.getMainVersion()+"."+pV.getSubVersion()+"."+pV.getChangeVersion()));

            row.setTime(v.getTimestamp().toString());
            table.add(row);
            System.out.println(v.getId());
        }
        return table;
    }

    public TreeNode printVersionTree(){
        OntoVersion root  =ontoVersionRepository.findOntoVersionById(1);
        versionTree = new TreeNode(root.getMainVersion()+"."+root.getSubVersion()+"."+root.getChangeVersion());
        printVersionTree(root, 0);
        return versionTree;
    }

    private void printVersionTree(OntoVersion parent, int level){
        Set<OntoVersion> cVersions = ontoVersionRepository.findOntoVersionsByPrior(parent.getId());
        if(!cVersions.isEmpty()){
            TreeNode pN =searchTree(parent.getMainVersion()+"."+parent.getSubVersion()+"."+parent.getChangeVersion(),versionTree);
            for(OntoVersion o: cVersions){
                pN.addChild(o.getMainVersion()+"."+o.getSubVersion()+"."+o.getChangeVersion());
                printVersionTree(o, level+1);
            }
        }
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
}
