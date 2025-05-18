package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.FilterResultDTO;
import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.post.*;
import api.giybat.uz.entity.PostEntity;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.mapper.PostDetailMapper;
import api.giybat.uz.repository.CustomPostRepository;
import api.giybat.uz.repository.PostRepository;
import api.giybat.uz.util.SpringSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AttachService attachService;
    private final CustomPostRepository customPostRepository;

    public PostDTO create(PostCreateDTO dto) {
        PostEntity entity = new PostEntity();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setPhotoId(dto.getPhoto().getId());
        entity.setVisible(true);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setProfileId(SpringSecurityUtil.getCurrentUserId());
        postRepository.save(entity);
        return toInfoDTO(entity);
    }

    public Page<PostDTO> getProfilePostList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        
        Page<PostEntity> result = postRepository.getAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(profileId, pageRequest);
        List<PostDTO> dtoList = result.getContent().stream()
                .map(this::toInfoDTO)
                .toList();

        return new PageImpl<PostDTO>(dtoList, pageRequest, result.getTotalElements());
    }

    public PostDTO getById(String id) {
        PostEntity entity = get(id);
        return toDTO(entity);
    }

    public PostDTO update(String id, PostCreateDTO dto) {
        PostEntity entity = get(id);
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        if (!SpringSecurityUtil.hasRole(ProfileRole.ROLE_ADMIN) && !entity.getProfileId().equals(profileId)) {
            throw new AppBadException("You do not have permission to update this post");
        }

        String deletePhotoId = null;
        if (!dto.getPhoto().getId().equals(entity.getPhotoId())) {
            deletePhotoId = entity.getPhotoId();
        }

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setPhotoId(dto.getPhoto().getId());
        postRepository.save(entity);
        if (deletePhotoId != null) {
            attachService.delete(deletePhotoId);
        }
        return toInfoDTO(entity);
    }

    public AppResponse<String> delete(String id) {
        PostEntity entity = get(id);
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        if (!SpringSecurityUtil.hasRole(ProfileRole.ROLE_ADMIN) && !entity.getProfileId().equals(profileId)) {
            throw new AppBadException("You do not have permission to update this post");
        }
        postRepository.delete(id);
        return new AppResponse<>("Success");
    }

    public PageImpl<PostDTO> filter(PostFilterDTO filterDTO, int page, int size) {
        FilterResultDTO<PostEntity> resultDTO = customPostRepository.filter(filterDTO, page, size);
        List<PostDTO> dtoList = resultDTO.getList().stream().map(this::toInfoDTO).toList();
        return new PageImpl<>(dtoList, PageRequest.of(page,size), resultDTO.getTotalCount());
    }

    public List<PostDTO> getSimilarPostList(SimilarPostListDTO dto) {
        List<PostEntity> postEntityList = postRepository.getSimilarPostList(dto.getExceptId());
        List<PostDTO> dtoList = postEntityList.stream()
                .map(this::toInfoDTO).toList();
        return dtoList;
    }

    public PageImpl<PostDTO> adminFilter(PostAdminFilterDTO dto, int page, int size) {
        FilterResultDTO<Object[]> resultDTO = customPostRepository.filter(dto, page, size);

        List<PostDTO> dtoList = resultDTO.getList().stream()
                .map(this::toDTO).toList();
        return new PageImpl<>(dtoList, PageRequest.of(page,size), resultDTO.getTotalCount());
    }

    public PostDTO toDTO(PostEntity entity) {
        PostDTO dto = new PostDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setPhoto(attachService.attachDTO(entity.getPhotoId()));
        return dto;
    }

    public PostDTO toDTO(Object[] obj) {
        PostDTO post = new PostDTO();
        post.setId((String) obj[0]);
        post.setTitle((String) obj[1]);
        if(obj[2] != null) {
            post.setPhoto(attachService.attachDTO((String) obj[2]));
        }
        post.setCreatedDate((LocalDateTime) obj[3]);

        ProfileDTO profile = new ProfileDTO();
        profile.setId((Integer) obj[4]);
        profile.setName((String) obj[5]);
        profile.setUsername((String) obj[6]);
        post.setProfile(profile);
        return post;
    }

    public PostDTO toInfoDTO(PostEntity entity) {
        PostDTO dto = new PostDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setPhoto(attachService.attachDTO(entity.getPhotoId()));
        return dto;
    }

    public PostEntity get(String id) {
        return postRepository.findById(id).orElseThrow(() -> {
            throw new AppBadException("Post not found: " + id);
        });
    }
}
