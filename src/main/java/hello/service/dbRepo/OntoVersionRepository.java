package hello.service.dbRepo;

import hello.bean.mode.OntoVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;


public interface OntoVersionRepository extends CrudRepository<OntoVersion, Long> {
    OntoVersion findOntoVersionById(Integer id);
    Set<OntoVersion> findOntoVersionsByCurrentEquals(Integer i);
    OntoVersion findOntoVersionByMainVersionAndSubVersionAndChangeVersion(int main, int sub, int change);
}
