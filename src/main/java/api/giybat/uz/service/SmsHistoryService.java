package api.giybat.uz.service;

import api.giybat.uz.entity.SmsHistoryEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsHistoryService {

    private final SmsHistoryRepository smsHistoryRepository;
    private final ResourceBundleService bundleService;

    public void create(String phoneNumber, String message, String code, SmsType smsType) {
        SmsHistoryEntity entity = new SmsHistoryEntity();
        entity.setPhone(phoneNumber);
        entity.setMessage(message);
        entity.setCode(code);
        entity.setType(smsType);
        entity.setAttemptCount(0);
        entity.setCreatedDate(LocalDateTime.now());
        smsHistoryRepository.save(entity);
    }

    public Long getSmsCount(String phone){
        LocalDateTime now = LocalDateTime.now();
        return smsHistoryRepository.countByPhoneAndCreatedDateBetween(phone, now.minusMinutes(1), now);
    }

    public void check(String phoneNumber, String code, AppLanguage lang){
        // find last sms by phoneNumber
        Optional<SmsHistoryEntity> optional = smsHistoryRepository.findTop1ByPhoneOrderByCreatedDateDesc(phoneNumber);
        if(optional.isEmpty()){
            log.warn("Attempt count limit reached phone: {}", phoneNumber);
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        SmsHistoryEntity entity = optional.get();
        //Attempt count
        if(entity.getAttemptCount() >= 3){
            throw new AppBadException(bundleService.getMessage("attempts.number.expired", lang));
        }
        // check code
        if (!entity.getCode().equals(code)){
            smsHistoryRepository.updateAttemptCount(entity.getId()); // update attempt count ++
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        // check time
        LocalDateTime expDate = entity.getCreatedDate().plusMinutes(2);
        if(LocalDateTime.now().isAfter(expDate)){ // not valid
            throw new AppBadException(bundleService.getMessage("code.timed.out", lang));
        }
    }
}
