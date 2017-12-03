package hello.service.dbRepo;

import hello.bean.mode.ChangeAnnotation;
import org.springframework.data.repository.CrudRepository;

public interface ChangeAnnotationRepository extends CrudRepository<ChangeAnnotation, Long> {
    ChangeAnnotation findChangeAnnotationById(Integer id);
}
