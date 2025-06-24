package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.EmailSendService;
import org.example.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService service;
    private final EmailSendService emailSendService;

    @PostMapping("/verification/sendEmail/{email}")
    public ResponseEntity<Void> sendEmail(@PathVariable String email) {
        emailSendService.sendEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        try {
            service.verifyToken(token);
            return ResponseEntity.ok("Your account has been verified!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
