package hello.service.dbRepo;

import hello.bean.mode.ChangeInstances;
import hello.bean.mode.OntoChange;
import org.springframework.data.repository.CrudRepository;

public interface ChangeInstancesRepository extends CrudRepository<ChangeInstances, Long> {
    ChangeInstances findChangeInstancesById(Integer id);
}
