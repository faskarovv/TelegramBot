package org.example.service;

import org.example.entity.AppUser;

public interface VerificationService {
    void verifyToken(String token);
    String generateToken(String email);
}
