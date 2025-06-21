package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AppUser;
import org.example.entity.UserToken;
import org.example.repo.AppUserRepo;
import org.example.repo.UserTokenRepo;
import org.example.service.VerificationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final AppUserRepo appUserRepo;
    private final UserTokenRepo userTokenRepo;


    @Override
    public void verifyToken(String token) {
        UserToken userToken = userTokenRepo.findUserTokenByToken(token);

        if (userToken.isUsed()) {
            throw new RuntimeException("link is used");
        }
        if (userToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("link is expired");
        }

        userToken.setUsed(true);
        userTokenRepo.save(userToken);

        AppUser verifiedUser = userToken.getAppUser();
        verifiedUser.setIsActive(true);
        appUserRepo.save(verifiedUser);
    }

    @Override
    public String generateToken(String email) {
        AppUser appUser = appUserRepo.findAppUserByEmail(email).orElseThrow(
                () -> new RuntimeException("no appuser exists with that email")
        );
        String token = UUID.randomUUID().toString();

        return UserToken.builder()
                .token(token)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .used(false)
                .appUser(appUser)
                .build().toString();
    }

}
