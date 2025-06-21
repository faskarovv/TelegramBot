package org.example.service;

public interface EmailSendService {
    boolean sendVerificationEmail(String email);
    void sendEmail(String email);
}
