package api.giybat.uz.repository;

import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.GeneralStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer> {

    Optional<ProfileEntity> findByUsernameAndVisibleTrue(String username);

    Optional<ProfileEntity> findByIdAndVisibleTrue(Integer id);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set status=?2 where id = ?1")
    void changeStatus(Integer id, GeneralStatus status);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set password=?2 where id = ?1")
    void updatePassword(Integer id, String password);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set name=?2 where id = ?1")
    void updateDetail(Integer id, String name);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set tempUsername=?2 where id = ?1")
    void updateTempUsername(Integer id, String tempUsername);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set username=?2 where id = ?1")
    void updateUsername(Integer id, String username);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set photoId=?2 where id = ?1")
    void updatePhoto(Integer id, String photoId);
}
