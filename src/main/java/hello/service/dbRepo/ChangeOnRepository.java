package hello.service.dbRepo;

import hello.bean.mode.ChangeOn;
import org.springframework.data.repository.CrudRepository;


public interface ChangeOnRepository extends CrudRepository<ChangeOn, Long> {
    ChangeOn findChangeOnById(Integer id);
}