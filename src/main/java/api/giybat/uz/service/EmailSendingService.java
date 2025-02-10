package api.giybat.uz.service;

import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exps.AppBadException;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EmailSendingService {

    private final JavaMailSender javaMailSender;
    private final EmailHistoryService emailHistoryService;

    @Value("${spring.mail.username}")
    private String fromAccount;

    @Value("${server.domain}")
    private String serverDomain;

    private Integer emailLimit = 3;

    public void sendRegistrationEmail(String email, Integer profileId, AppLanguage lang) {
        String subject = "Complete Registration";
        String body = "<!Doctype html>\n" +
                "<html lang=\"eng\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8x\">\n" +
                "    <title>Title</title>\n" +
                "    <style>\n" +
                "        a {\n" +
                "            padding: 10px 30px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "\n" +
                "        .button-link:hover{\n" +
                "            background-color: #c5c5ac;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                "<h1>Complete Registration</h1>\n" +
                "<p>Salom Yaxshimisiz</p>\n" +
                "<p>\n" +
                "    Please click button for completing registration: <a class=\"button-link\"\n" +
                "                                                        href=\"%s/auth/registration/email-verification/%s?lang=%s\"\n" +
                "                                                        target=\"_blank\">Click there</a>\n" +
                "</p>\n" +
                "</body>\n" +
                "</html>";
        body = String.format(body, serverDomain, JwtUtil.encode(profileId), lang.name());
        System.out.println(JwtUtil.encode(profileId));
        sendMimeEmail(email, subject, body);
    }

    public void sendResetPasswordEmail(String email, AppLanguage lang) {
        String subject = "Reset Password Confirmation";
        String code = RandomUtil.getRandomSmsCode();
        String body = "This is your confirm code for reset password. Your Code: " + code;
        checkAndSendMimeEmail(email, subject, body, code);
    }

    public void sendChangeUsernameEmail(String email, AppLanguage lang) {
        String subject = "Username change confirmation";
        String code = RandomUtil.getRandomSmsCode();
        String body = "This is your confirm code for changing username. Your Code: " + code;
        checkAndSendMimeEmail(email, subject, body, code);
    }

    private void checkAndSendMimeEmail(String email, String subject, String body, String code) {
        // check
        Long count = emailHistoryService.getEmailCount(email);
        if (count >= emailLimit) {
            System.out.println(" ----- Email Limit Reached. Email: " + email);
            throw new AppBadException("Sms limit reached");
        }
        // send
        sendMimeEmail(email, subject, body);
        // create
        emailHistoryService.create(email, code, SmsType.RESET_PASSWORD);
    }

    private void sendMimeEmail(String email, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAccount);
            helper.setTo(email);
            helper.setSubject(subject);

            // HTML formatda kontentni belgilash
            helper.setText(body, true);

            CompletableFuture.runAsync(() -> {
                javaMailSender.send(message);
            });
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSimpleEmail(String email, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAccount);
        msg.setTo(email);
        msg.setSubject(subject);
        msg.setText(body);
        javaMailSender.send(msg);
    }

}
