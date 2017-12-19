package hello.service.dbRepo;

import hello.bean.mode.OntoChange;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface OntoChangeRepository extends CrudRepository<OntoChange, Long> {
    OntoChange findOntoChangeById(Integer id);
    Set<OntoChange> findAll();
    OntoChange findTop1ByOrderByIdDesc();
    void removeById(Integer id);
}