package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.CodeConfirmDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfilePasswordUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUsernameUpdateDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import api.giybat.uz.util.SpringSecurityUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

        if(profile.getPhotoId() != null && !profile.getPhotoId().equals(photoId)){
            attachService.delete(profile.getPhotoId()); // delete old image
        }
        return new AppResponse<>(bundleService.getMessage("profile.photo.update.success", lang));
    }

    public ProfileEntity getById(int id) {
        return profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> {
            throw new AppBadException("Profile not found");
        });
    }
}
