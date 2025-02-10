package api.giybat.uz.service;

import api.giybat.uz.entity.EmailHistoryEntity;
import api.giybat.uz.entity.SmsHistoryEntity;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.repository.EmailHistoryRepository;
import api.giybat.uz.repository.SmsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailHistoryService {

    private final EmailHistoryRepository emailHistoryRepository;
    private final ResourceBundleService bundleService;

    public void create(String email, String code, SmsType emailType) {
        EmailHistoryEntity entity = new EmailHistoryEntity();
        entity.setEmail(email);
        entity.setCode(code);
        entity.setEmailType(emailType);
        entity.setAttemptCount(0);
        entity.setCreatedDate(LocalDateTime.now());
        emailHistoryRepository.save(entity);
    }

    public Long getEmailCount(String email){
        LocalDateTime now = LocalDateTime.now();
        return emailHistoryRepository.countByEmailAndCreatedDateBetween(email, now.minusMinutes(1), now);
    }

    public void check(String email, String code, AppLanguage lang){
        // find last sms by phoneNumber
        Optional<EmailHistoryEntity> optional = emailHistoryRepository.findTop1ByEmailOrderByCreatedDateDesc(email);
        if(optional.isEmpty()){
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        EmailHistoryEntity entity = optional.get();
        //Attempt count
        if(entity.getAttemptCount() >= 3){
            throw new AppBadException(bundleService.getMessage("attempts.number.expired", lang));
        }
        // check code
        if (!entity.getCode().equals(code)){
            emailHistoryRepository.updateAttemptCount(entity.getId()); // update attempt count ++
            throw new AppBadException(bundleService.getMessage("verification.failed", lang));
        }

        // check time
        LocalDateTime expDate = entity.getCreatedDate().plusMinutes(2);
        if(LocalDateTime.now().isAfter(expDate)){ // not valid
            throw new AppBadException(bundleService.getMessage("code.timed.out", lang));
        }
    }
}
