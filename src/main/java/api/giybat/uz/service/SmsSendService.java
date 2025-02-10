package api.giybat.uz.service;

import api.giybat.uz.dto.sms.SmsAuthDTO;
import api.giybat.uz.dto.sms.SmsAuthResponseDTO;
import api.giybat.uz.dto.sms.SmsRequestDTO;
import api.giybat.uz.dto.sms.SmsSendResponseDTO;
import api.giybat.uz.entity.SmsProviderTokenHolderEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.SmsProviderTokenHolderRepository;
import api.giybat.uz.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsSendService {

    private final RestTemplate restTemplate;
    private final SmsProviderTokenHolderRepository smsProviderTokenHolderRepository;
    private final SmsHistoryService smsHistoryService;
    private final ResourceBundleService bundleService;

    @Value("${eskiz.url}")
    private String smsURL;
    @Value("${eskiz.login}")
    private String accountLogin;
    @Value("${eskiz.password}")
    private String accountPassword;
    private Integer smsLimit = 3;

    public void sendRegistrationSms(String phoneNumber, AppLanguage lang) {
        String code = RandomUtil.getRandomSmsCode();
        System.out.println(code);
        // test uchun ishlatilyapdi
        String message = bundleService.getMessage("sms.registration.confirm.code",lang);
//        String message = "Ro'yxatdan o'tish uchun tasdiqlash codi (code) : %s";
//        message = String.format(message,code);
        sendSms(phoneNumber, message, code, SmsType.REGISTRATION);
    }

    public void sendResetPasswordSms(String phoneNumber, AppLanguage lang) {
        String code = RandomUtil.getRandomSmsCode();
        System.out.println(code);
        // test uchun ishlatilyapdi
        String message = bundleService.getMessage("sms.reset.password.confirm",lang);
//        String message = "Ro'yxatdan o'tish uchun tasdiqlash codi (code) : %s";
//        message = String.format(message,code);
        sendSms(phoneNumber, message, code, SmsType.RESET_PASSWORD);
    }

    public void sendUsernameChangeConfirmSms(String phoneNumber, AppLanguage lang) {
        String code = RandomUtil.getRandomSmsCode();
        System.out.println(code);
        // test uchun ishlatilyapdi
        String message = bundleService.getMessage("sms.change.username.confirm",lang);
//        String message = "Ro'yxatdan o'tish uchun tasdiqlash codi (code) : %s";
//        message = String.format(message,code);
        sendSms(phoneNumber, message, code, SmsType.CHANGE_USERNAME_CONFIRM);
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message, String code, SmsType smsType) {
        // check
        Long count = smsHistoryService.getSmsCount(phoneNumber);
        if (count >= smsLimit) {
            System.out.println("---- Sms Limit Reached. Phone : " + phoneNumber);
            log.warn("Sms Limit Reached. Phone : " + phoneNumber);
            throw new AppBadException("Sms limit reached");
        }

        SmsSendResponseDTO result = sendSms(phoneNumber, message);
        smsHistoryService.create(phoneNumber, message, code, smsType);
        return result;
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message) {
        // get Token
        String token = getToken();
        // send sms
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);

        SmsRequestDTO body = new SmsRequestDTO();
        body.setMobile_phone(phoneNumber);
        body.setMessage(message);
        body.setFrom("4546");

        HttpEntity<SmsRequestDTO> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<SmsSendResponseDTO> response = restTemplate.exchange(
                    smsURL + "/message/sms/send",
                    HttpMethod.POST,
                    entity,
                    SmsSendResponseDTO.class
            );
            return response.getBody();
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Send sms. phone: {}, message: {}, errror: {}", phoneNumber, message, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getToken() {
        Optional<SmsProviderTokenHolderEntity> optional = smsProviderTokenHolderRepository.findTop1By();
        if (optional.isEmpty()) { // if token not exists
            String token = getTokenFromProvider();
            SmsProviderTokenHolderEntity entity = new SmsProviderTokenHolderEntity();
            entity.setToken(token);
            entity.setCreatedDate(LocalDateTime.now());
            entity.setExpiredDate(LocalDateTime.now().plusMonths(1));
            smsProviderTokenHolderRepository.save(entity);
            return token;
        }

        // if exists check it
        SmsProviderTokenHolderEntity entity = optional.get();
        if (LocalDateTime.now().isBefore(entity.getExpiredDate())) { // if not expired
            return entity.getToken();
        }
        // get new token and update
        String token = getTokenFromProvider();
        entity.setToken(token);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setExpiredDate(LocalDateTime.now().plusMonths(1));
        smsProviderTokenHolderRepository.save(entity);
        return token;
    }

    private String getTokenFromProvider() {

        SmsAuthDTO smsAuthDTO = new SmsAuthDTO();
        smsAuthDTO.setEmail(accountLogin);
        smsAuthDTO.setPassword(accountPassword);

        try {
            System.out.println("------ SmsSender new Token was taken ------");
            SmsAuthResponseDTO response = restTemplate.postForObject(smsURL + "/auth/login", smsAuthDTO, SmsAuthResponseDTO.class);
            return response.getData().getToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
