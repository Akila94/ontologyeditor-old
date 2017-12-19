package hello.service.dbRepo;

import hello.bean.mode.ChangeDes;
import hello.bean.mode.OntoChange;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ChangeDesRepository extends CrudRepository<ChangeDes, Long> {
    ChangeDes findChangeDesById(Integer id);
    Set<ChangeDes> findChangeDesByOntoChange(OntoChange ontoChange);
    void removeByOntoChange(OntoChange ontoChange);
}
