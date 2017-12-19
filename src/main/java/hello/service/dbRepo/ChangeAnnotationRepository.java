package hello.service.dbRepo;

import hello.bean.mode.ChangeAnnotation;
import hello.bean.mode.ChangeDes;
import hello.bean.mode.OntoChange;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ChangeAnnotationRepository extends CrudRepository<ChangeAnnotation, Long> {
    ChangeAnnotation findChangeAnnotationById(Integer id);
    Set<ChangeAnnotation> findChangeAnnotationByOntoChange(OntoChange ontoChange);
    void removeByOntoChange(OntoChange ontoChange);
}
