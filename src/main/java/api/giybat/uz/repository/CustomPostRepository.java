package api.giybat.uz.repository;

import api.giybat.uz.dto.FilterResultDTO;
import api.giybat.uz.dto.post.PostAdminFilterDTO;
import api.giybat.uz.dto.post.PostFilterDTO;
import api.giybat.uz.entity.PostEntity;
import api.giybat.uz.mapper.PostDetailMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomPostRepository {

    private final EntityManager entityManager;

    public FilterResultDTO<PostEntity> filter(PostFilterDTO filter, int page, int size) {
        StringBuilder queryBuilder = new StringBuilder(" where p.visible = true ");
        Map<String, Object> params = new HashMap<>();

        if (filter.getQuery() != null) {
            queryBuilder.append(" and lower(p.title) like :query ");
            params.put("query", "%" + filter.getQuery().toLowerCase() + "%");
        }
        if (filter.getExceptId() != null) {
            queryBuilder.append(" and p.id != :exceptId ");
            params.put("exceptId", filter.getExceptId());
        }


        StringBuilder selectBuilder = new StringBuilder("Select p From PostEntity p ")
                .append(queryBuilder)
                .append(" order by p.createdDate desc ");
        StringBuilder countBuilder = new StringBuilder("Select count(p) From PostEntity p ")
                .append(queryBuilder);

        // select
        Query selectQuery = entityManager.createQuery(selectBuilder.toString());
        selectQuery.setFirstResult((page) * size); // offset 50
        selectQuery.setMaxResults(size); // limit 30
        params.forEach(selectQuery::setParameter);
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            selectQuery.setParameter(entry.getKey(), entry.getValue());
//        }
        List<PostEntity> entityList = selectQuery.getResultList();

        // count
        Query countQuery = entityManager.createQuery(countBuilder.toString());
        params.forEach(countQuery::setParameter);
        Long totalCount = (Long) countQuery.getSingleResult();

        return new FilterResultDTO<>(entityList, totalCount);
    }

    public FilterResultDTO<Object[]> filter(PostAdminFilterDTO filter, int page, int size) {
        StringBuilder queryBuilder = new StringBuilder(" where p.visible = true ");
        Map<String, Object> params = new HashMap<>();

        if (filter.getProfileQuery() != null && !filter.getProfileQuery().isBlank()) {
            queryBuilder.append(" and (lower(pr.name) like :profileQuery or lower(pr.username) like :profileQuery) ");
            params.put("profileQuery", "%" + filter.getProfileQuery().toLowerCase() + "%");
        }
        if (filter.getPostQuery() != null && !filter.getPostQuery().isBlank()) {
            queryBuilder.append(" and (lower(p.title) like :postQuery or p.id = :postId ) ");
            params.put("postQuery", "%" + filter.getPostQuery().toLowerCase() + "%");
            params.put("postId", filter.getPostQuery().toLowerCase());
        }


        StringBuilder selectBuilder = new StringBuilder("Select p.id as postId, p.title as postTitle, p.photoId as postPhotoId, p.createdDate as postCreatedDate, " +
                " pr.id as profileId, pr.name as profileName, pr.username as profileUsername " +
                " From PostEntity p ")
                .append(" inner join p.profile as pr ")
                .append(queryBuilder)
                .append(" order by p.createdDate desc ");
        StringBuilder countBuilder = new StringBuilder("Select count(p) From PostEntity p inner join p.profile as pr ")
                .append(queryBuilder);

        // select
        Query selectQuery = entityManager.createQuery(selectBuilder.toString());
        selectQuery.setFirstResult((page) * size); // offset 50
        selectQuery.setMaxResults(size); // limit 30
        params.forEach(selectQuery::setParameter);
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            selectQuery.setParameter(entry.getKey(), entry.getValue());
//        }
        List<Object[]> entityList = selectQuery.getResultList();

        // count
        Query countQuery = entityManager.createQuery(countBuilder.toString());
        params.forEach(countQuery::setParameter);
        Long totalCount = (Long) countQuery.getSingleResult();

        return new FilterResultDTO<>(entityList, totalCount);
    }
}
