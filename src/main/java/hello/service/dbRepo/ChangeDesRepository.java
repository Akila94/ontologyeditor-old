package hello.service.dbRepo;

import hello.bean.mode.ChangeDes;
import org.springframework.data.repository.CrudRepository;

public interface ChangeDesRepository extends CrudRepository<ChangeDes, Long> {
    ChangeDes findChangeDesById(Integer id);
}
