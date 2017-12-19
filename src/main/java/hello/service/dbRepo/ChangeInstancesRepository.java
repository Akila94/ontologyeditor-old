package hello.service.dbRepo;

import hello.bean.mode.ChangeInstances;
import hello.bean.mode.OntoChange;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ChangeInstancesRepository extends CrudRepository<ChangeInstances, Long> {
    ChangeInstances findChangeInstancesById(Integer id);
    Set<ChangeInstances> findChangeInstancesByOntoChange(OntoChange ontoChange);
    void removeByOntoChange(OntoChange ontoChange);
}
