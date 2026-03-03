package tr.org.lider.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.org.lider.entities.ForgotPasswordImpl;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.ForgotPasswordService;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ConfigurationService configService;
    private final ForgotPasswordService forgotPasswordService;

    @Autowired
    public EmailService(ConfigurationService configService, ForgotPasswordService forgotPasswordService) {
        this.configService = configService;
        this.forgotPasswordService = forgotPasswordService;
    }

    public boolean sendPasswordResetEmail(String username, String recipientEmail, String liderURL) {
        String resetUID = generateResetUID();
        String subject = "Liderahenk Parola Yenileme Linki";
        String content = buildPasswordResetContent(liderURL, resetUID);

        boolean emailSent = sendEmail(recipientEmail, subject, content);
        if (emailSent) {
            savePasswordResetRecord(username, resetUID);
            return true;
        }
        return false;
    }


    public boolean sendOTPEmail(String recipientEmail, String otp) {
        long otpExpiryDurationMs = configService.getOtpExpiryDuration();
        long otpExpiryDurationMinutes = otpExpiryDurationMs / 1000 / 60;

        String subject = "Liderahenk Tek Kullanımlık Şifreniz";
        String content = "Kimliğinizi doğrulamak için, lütfen aşağıdaki kodu kullanın.<br>" +
                "<b>" + otp + "</b><br>" +
                "Bu tek seferlik şifreyi kimseyle paylaşmayın. Bu kod " + otpExpiryDurationMinutes + " dakika süreyle geçerlidir.";

        return sendEmail(recipientEmail, subject, content);
    }

    // Private Helper Methods

    private boolean sendEmail(String recipientEmail, String subject, String content) {
        try {
            Session session = createEmailSession();
            Message message = createEmailMessage(session, recipientEmail, subject, content);
            Transport.send(message);
            logger.info("Email successfully sent to {}", recipientEmail);
            return true;
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}. Error: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    private Session createEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", configService.getMailSmtpAuth().toString());
        props.put("mail.smtp.starttls.enable", configService.getMailSmtpStartTlsEnable());
        props.put("mail.smtp.host", configService.getMailHost());
        props.put("mail.smtp.port", configService.getMailSmtpPort());

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configService.getMailAddress(), configService.getMailPassword());
            }
        });
    }

    private Message createEmailMessage(Session session, String recipientEmail, String subject, String content) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configService.getMailAddress(), false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setContent(content, "text/html; charset=UTF-8");
        message.setSentDate(new Date());
        return message;
    }

    private String buildPasswordResetContent(String liderURL, String resetUID) {
        return "Merhaba,<br><br>Parolanızı aşağıdaki linkten 1 saat içerisinde yenileyebilirsiniz.<br><br>" +
                "<a href=\"" + liderURL + "#/forgot-password/id/" + resetUID + "\">Parola Yenile</a>";
    }

    private String generateResetUID() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private void savePasswordResetRecord(String username, String resetUID) {
        ForgotPasswordImpl forgotPassword = new ForgotPasswordImpl();
        forgotPassword.setUsername(username);
        forgotPassword.setResetUID(resetUID);
        forgotPasswordService.save(forgotPassword);
        logger.info("Password reset record saved for username: {}", username);
    }
}
