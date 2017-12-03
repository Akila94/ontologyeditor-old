package hello.service.dbRepo;

import hello.bean.mode.ChangeType;
import org.springframework.data.repository.CrudRepository;

public interface ChangeTypeRepository extends CrudRepository<ChangeType, Long> {
    ChangeType findChangeTypeById(Integer id);
}