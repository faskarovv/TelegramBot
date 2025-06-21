package org.example.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repo.AppUserRepo;
import org.example.service.EmailSendService;
import org.example.service.VerificationService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendImpl implements EmailSendService {

    private final JavaMailSender javaMailSender;
    private final VerificationService verificationService;
    private final AppUserRepo appUserRepo;

    @Override
    public boolean sendVerificationEmail(String email) {
        try {
            String token = verificationService.generateToken(email);
            String verificationUrl = "http://your-domain.com/verify?token=" + token;

            log.info("Attempting to send email to: {}", email);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom("fasgerov06@gmail.com");
            helper.setTo(email);
            helper.setSubject("Your Verification EMAIL");
            helper.setText(String.format(
                    "Your verification email is: <b>%s</b><br> ",
                    verificationUrl
            ), true);


            javaMailSender.send(message);

            return true;
        } catch (Exception e) {
            log.error("email service error", e);
            return false;
        }
    }

    public void sendEmail(String email) {
        try {
            boolean emailSent = sendVerificationEmail(email);

            if (!emailSent) {
                log.error("could not send the email");
            }
        } catch (RuntimeException e) {
            log.error("error sending email to {}:", email);
            throw new RuntimeException(e);
        }
    }
}
