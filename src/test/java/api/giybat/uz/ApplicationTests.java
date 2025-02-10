package api.giybat.uz;

import api.giybat.uz.enums.SmsType;
import api.giybat.uz.service.SmsSendService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void contextLoads() {
//        System.out.println(UUID.randomUUID());
        System.out.println(bCryptPasswordEncoder.encode("123456"));
    }

}
