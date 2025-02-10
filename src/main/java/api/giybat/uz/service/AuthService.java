package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.auth.AuthDTO;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.dto.sms.SmsResendDTO;
import api.giybat.uz.dto.sms.SmsVerificationDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ProfileRoleService profileRoleService;
    private final EmailSendingService emailSendingService;
    private final ProfileService profileService;
    private final ProfileRoleRepository profileRoleRepository;
    private final ResourceBundleService bundleService;
    private final SmsSendService smsSendService;
    private final SmsHistoryService smsHistoryService;
    private final EmailHistoryService emailHistoryService;
    private final AttachService attachService;

    public AppResponse<String> registration(RegistrationDTO dto, AppLanguage lang) {

        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isPresent()) {
            ProfileEntity profile = optional.get();
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRoleService.deleteRoles(profile.getId());
                profileRepository.delete(profile);
                // send sms/email
            } else {
                log.warn("Profile already exists with username {}", dto.getUsername());
                throw new AppBadException(bundleService.getMessage("email.phone.exists", lang));
            }
        }

        ProfileEntity entity = new ProfileEntity();
        entity.setName(dto.getName());
        entity.setUsername(dto.getUsername());
        entity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        entity.setStatus(GeneralStatus.IN_REGISTRATION);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setVisible(true);
        profileRepository.save(entity);

        profileRoleService.create(entity.getId(), ProfileRole.ROLE_USER);

        // send
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendRegistrationSms(dto.getUsername(), lang);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailSendingService.sendRegistrationEmail(entity.getUsername(), entity.getId(), lang);
        }

        return new AppResponse<>(bundleService.getMessage("email.confirm.send", lang));
    }

    public AppResponse<String> registrationEmailVerification(String token, AppLanguage lang) {
        try {
            Integer profileId = JwtUtil.decodeRegVerToken(token);
            ProfileEntity profile = profileService.getById(profileId);
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRepository.changeStatus(profileId, GeneralStatus.ACTIVE);
                return new AppResponse<>(bundleService.getMessage("verification.finished", lang));
            }

        } catch (JwtException e) {
        }
        log.warn("Registration email verification failed {}", token);
        throw new AppBadException(bundleService.getMessage("verification.failed", lang));
    }

    public ProfileDTO login(AuthDTO dto, AppLanguage lang) {

        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            log.warn("Username or password wrong {}", dto.getUsername());
            throw new AppBadException(bundleService.getMessage("username.password.wrong", lang));
        }

        ProfileEntity profile = optional.get();
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), profile.getPassword())) {
            throw new AppBadException(bundleService.getMessage("username.password.wrong", lang));
        }

        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            log.warn("Wrong status {}", dto.getUsername());
            throw new AppBadException(bundleService.getMessage("status.wrong", lang));
        }
        return getLogInResponse(profile);
    }

    public ProfileDTO registrationSmsVerification(SmsVerificationDTO dto, AppLanguage lang) {
        // 998931051739
        // 12345

        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (optional.isEmpty()) {
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        // code check
        smsHistoryService.check(dto.getPhone(), dto.getCode(), lang);

        // ACTIVE
        profileRepository.changeStatus(profile.getId(), GeneralStatus.ACTIVE);
        return getLogInResponse(profile);
    }

    public AppResponse<String> registrationSmsVerificationResend(SmsResendDTO dto, AppLanguage lang) {

        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getPhone());
        if (optional.isEmpty()) {
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        smsSendService.sendRegistrationSms(dto.getPhone(), lang);
        return new AppResponse<>(bundleService.getMessage("sms.resend", lang));
    }

    public AppResponse<String> resetPassword(@Valid ResetPasswordDTO dto, AppLanguage lang) {

        // check
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(bundleService.getMessage("profile.not.found", lang));
        }

        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(bundleService.getMessage("status.wrong", lang));
        }

        // send
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendResetPasswordSms(dto.getUsername(), lang);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailSendingService.sendResetPasswordEmail(dto.getUsername(), lang);
        }

        String responseMessage = bundleService.getMessage("reset.password.response", lang);
        return new AppResponse<>(String.format(responseMessage,dto.getUsername()));
    }

    public AppResponse<String> resetPasswordConfirm(@Valid ResetPasswordConfirmDTO dto, AppLanguage lang) {

        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(bundleService.getMessage("status.wrong", lang));
        }

        // check
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsHistoryService.check(dto.getUsername(), dto.getConfirmCode(), lang);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            emailHistoryService.check(dto.getUsername(), dto.getConfirmCode(), lang);
        }
        // update
        profileRepository.updatePassword(profile.getId(), bCryptPasswordEncoder.encode(dto.getPassword()));
        // return
        return new AppResponse<>(bundleService.getMessage("reset.password.success", lang));
    }

    public ProfileDTO getLogInResponse(ProfileEntity profile) {
        ProfileDTO response = new ProfileDTO();
        response.setName(profile.getName());
        response.setUsername(profile.getUsername());
        response.setRoleList(profileRoleRepository.getAllRolesListByProfileId(profile.getId()));
        response.setJwt(JwtUtil.encode(profile.getUsername(), profile.getId(), response.getRoleList()));
        response.setPhoto(attachService.attachDTO(profile.getPhotoId()));
        return response;
    }
}
