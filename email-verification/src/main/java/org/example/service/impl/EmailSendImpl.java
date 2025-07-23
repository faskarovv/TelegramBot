package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.EmailRequestDto;
import org.example.dto.Recipient;
import org.example.dto.Sender;
import org.example.service.VerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendImpl  {

    private final VerificationService verificationService;

    @Value("${app.verification.base-url}")
    private String baseUrl;

    @Value("${bravo.api.key}")
    private String bravoApiKey;

    private final WebClient webClient;


    public void sendVerificationEmail(String email) {
        try {
            String token = verificationService.generateToken(email);
            String verificationUrl = baseUrl + "/verify?token=" + token;

            log.info("Attempting to send email to: {}", email);
            EmailRequestDto emailRequestDto = new EmailRequestDto(
                    new Sender("FaridCo", "noreply@faridtgbot.com"),
                    List.of(new Recipient(email)),
                    "Verify your email",
                    "<p>Click the link to verify your email: <a href=\"" + verificationUrl + "\">Verify</a></p>"
            );

            webClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", bravoApiKey)
                    .bodyValue(emailRequestDto)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(e -> log.info("verification email was sent to the user with email: {}" , email))
                    .doOnError(e -> log.info("unable to send email to {}" , email))
                    .subscribe();

            log.info("reached the end of the func");
        } catch (Exception e) {
            log.error("email service error", e);
        }
    }

}
