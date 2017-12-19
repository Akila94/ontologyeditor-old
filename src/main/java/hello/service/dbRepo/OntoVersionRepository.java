package hello.service.dbRepo;

import hello.bean.mode.OntoVersion;
import hello.bean.mode.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;


public interface OntoVersionRepository extends CrudRepository<OntoVersion, Long> {
    OntoVersion findOntoVersionById(Integer id);
    OntoVersion findOntoVersionByCurrentEquals(boolean i);
    OntoVersion findOntoVersionByMainVersionAndSubVersionAndChangeVersion(int main, int sub, int change);
    Set<OntoVersion> findOntoVersionByUsers(Set<User> users);
    OntoVersion findOntoVersionByMainVersionAndSubVersionAndAndChangeVersion(int mainVersion,int subVersion, int changeVersion);
    Set<OntoVersion> findOntoVersionsByPrior(int id);
}
