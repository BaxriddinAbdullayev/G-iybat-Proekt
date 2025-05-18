package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.CodeConfirmDTO;
import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfileFilterDTO;
import api.giybat.uz.dto.profile.ProfilePasswordUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUsernameUpdateDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.entity.ProfileRoleEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.mapper.ProfileDetailMapper;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import api.giybat.uz.util.SpringSecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ResourceBundleService bundleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SmsSendService smsSendService;
    private final EmailSendingService emailSendingService;
    private final SmsHistoryService smsHistoryService;
    private final EmailHistoryService emailHistoryService;
    private final ProfileRoleRepository profileRoleRepository;
    private final AttachService attachService;


    public AppResponse<String> updateDetail(ProfileDetailUpdateDTO dto, AppLanguage lang) {
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateDetail(profileId, dto.getName());
        return new AppResponse<>(bundleService.getMessage("profile.detail.update.success", lang));
    }

    public AppResponse<String> updatePassword(ProfilePasswordUpdateDTO dto, AppLanguage lang) {
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profile = getById(profileId);
        if (!bCryptPasswordEncoder.matches(dto.getCurrentPswd(), profile.getPassword())) {
            throw new AppBadException(bundleService.getMessage("wrong.password", lang));
        }

        profileRepository.updatePassword(profileId, bCryptPasswordEncoder.encode(dto.getNewPswd()));
        return new AppResponse<>(bundleService.getMessage("profile.password.update.success", lang));
    }

    public AppResponse<String> updateUsername(ProfileUsernameUpdateDTO dto, AppLanguage lang) {
        // check
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isPresent()) {
            throw new AppBadException(bundleService.getMessage("email.phone.exists", lang));
        }

        // send
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendUsernameChangeConfirmSms(dto.getUsername(), lang);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailSendingService.sendChangeUsernameEmail(dto.getUsername(), lang);
        }
        //save
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateTempUsername(profileId, dto.getUsername());
        String responseText = bundleService.getMessage("reset.password.response", lang);
        return new AppResponse<>(String.format(responseText, dto.getUsername()));
    }


    public AppResponse<String> updateUsernameConfirm(CodeConfirmDTO dto, AppLanguage lang) {
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profile = getById(profileId);
        String tempUsername = profile.getTempUsername();

        // check
        if (PhoneUtil.isPhone(tempUsername)) {
            smsHistoryService.check(tempUsername, dto.getCode(), lang);
        } else if (EmailUtil.isEmail(tempUsername)) {
            emailHistoryService.check(tempUsername, dto.getCode(), lang);
        }

        // update username
        profileRepository.updateUsername(profileId, tempUsername);

        // return response
        List<ProfileRole> roles = profileRoleRepository.getAllRolesListByProfileId(profile.getId());
        String jwt = JwtUtil.encode(tempUsername, profile.getId(), roles);
        return new AppResponse<>(jwt, bundleService.getMessage("change.username.success", lang));
    }

    public AppResponse<String> updatePhoto(String photoId, AppLanguage lang) {
        Integer profileId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profile = getById(profileId);
        profileRepository.updatePhoto(profileId, photoId);

        if (profile.getPhotoId() != null && !profile.getPhotoId().equals(photoId)) {
            attachService.delete(profile.getPhotoId()); // delete old image
        }
        return new AppResponse<>(bundleService.getMessage("profile.photo.update.success", lang));
    }

    public ProfileEntity getById(int id) {
        return profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> {
            throw new AppBadException("Profile not found");
        });
    }

    public PageImpl<ProfileDTO> filter(@Valid ProfileFilterDTO dto, int page, int size, AppLanguage lang) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ProfileDetailMapper> filterResult = null;
        if (dto.getQuery() == null) {
            filterResult = profileRepository.filter(pageRequest);
        } else {
            filterResult = profileRepository.filter("%" + dto.getQuery().toLowerCase() + "%", pageRequest);
        }

        List<ProfileDTO> resultList = filterResult.stream().map(this::toDTO).toList();
        return new PageImpl<>(resultList, pageRequest, filterResult.getTotalElements());
    }

    public AppResponse<String> changeStatus(Integer id, GeneralStatus status, AppLanguage lang) {
        profileRepository.changeStatus(id, status);
        return new AppResponse<>(bundleService.getMessage("profile.detail.update.success", lang));
    }

    public AppResponse<String> delete(Integer id, AppLanguage lang) {
        profileRepository.delete(id);
        return new AppResponse<>(bundleService.getMessage("profile.delete.success", lang));
    }

    public ProfileDTO toDTO(ProfileEntity entity) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUsername(entity.getUsername());

        if (entity.getRoleList() != null) {
            List<ProfileRole> roleList = entity.getRoleList().stream().map(ProfileRoleEntity::getRoles).toList();
            dto.setRoleList(roleList);
        }
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setPhoto(attachService.attachDTO(entity.getPhotoId()));
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public ProfileDTO toDTO(ProfileDetailMapper mapper) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(mapper.getId());
        dto.setName(mapper.getName());
        dto.setUsername(mapper.getUsername());

        if (mapper.getRoles() != null) {
            List<ProfileRole> roleList = Arrays.stream(mapper.getRoles().split(","))
                    .map(roleName -> ProfileRole.valueOf(roleName))
                    .toList();
            dto.setRoleList(roleList);
        }
        dto.setCreatedDate(mapper.getCreatedDate());
        dto.setPhoto(attachService.attachDTO(mapper.getPhotoId()));
        dto.setStatus(mapper.getStatus());
        dto.setPostCount(mapper.getPostCount());
        return dto;
    }
}
