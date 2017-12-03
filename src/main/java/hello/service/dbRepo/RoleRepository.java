package hello.service.dbRepo;

import hello.bean.mode.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findRoleById(Integer id);
}