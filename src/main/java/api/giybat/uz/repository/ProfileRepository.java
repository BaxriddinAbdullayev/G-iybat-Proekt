package api.giybat.uz.repository;

import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.mapper.ProfileDetailMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer>, PagingAndSortingRepository<ProfileEntity, Integer> {

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

    @Query(value = """
            select p.id as id, p.name as name, p.username as username, p.photo_id as photoId, p.status as status, p.created_date as createdDate,
            (select count(post) from post as post where post.profile_id = p.id) as postCount,
            (select string_agg(roles, ',') from profile_role as pr where pr.profile_id = p.id) as roles
            from profile as p where visible = true order by p.created_date desc 
            """,
            countQuery = """
                    select count(*) from profile where visible = true
                    """,
            nativeQuery = true)
    Page<ProfileDetailMapper> filter(PageRequest pageRequest);

    @Query(value = """
            select p.id as id, p.name as name, p.username as username, p.photo_id as photoId, p.status as status, p.created_date as createdDate,
            (select count(post) from post as post where post.profile_id = p.id) as postCount,
            (select string_agg(roles, ',') from profile_role as pr where pr.profile_id = p.id) as roles
            from profile as p where (lower(p.username) like ?1 or lower(p.name) like ?1) and visible = true order by p.created_date desc 
            """,
            countQuery = """
                    select count(*) from profile as p where (lower(p.username) like ?1 or lower(p.name) like ?1) and visible = true
                    """,
            nativeQuery = true)
    Page<ProfileDetailMapper> filter(String query, PageRequest pageRequest);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set visible = false where id = ?1")
    void delete(Integer id);
}
