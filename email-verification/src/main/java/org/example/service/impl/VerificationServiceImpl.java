package org.example.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AppUserDto;
import org.example.dto.TokenRequest;
import org.example.dto.UserTokenDto;
import org.example.entity.AppUser;
import org.example.entity.UserToken;
import org.example.entity.enums.UserState;
import org.example.service.FeignClientInterface;
import org.example.service.VerificationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final FeignClientInterface feignClient;

    @Override
    public void verifyToken(String token) {
        try {
            log.info("trying to verify for the token {}", token);
            UserToken userToken = feignClient.findUserTokenByTokenRequest(new TokenRequest(token));
            log.info("sent a request with token {}", token);
            if (userToken == null) {
                throw new RuntimeException("Token not found or invalid");
            }

            if (userToken.isUsed()) {
                throw new RuntimeException("link is used");
            }
            if (userToken.getExpiresAt().isBefore(Instant.now())) {
                throw new RuntimeException("link is expired");
            }

            userToken.setUsed(true);

            log.info("starting to call feign client findAppUserById");
            AppUser verifiedUser = feignClient.findAppUserById(userToken.getAppUserId());
            verifiedUser.setIsActive(true);
            verifiedUser.setUserState(UserState.APPROVED_STATE);
            log.info("trying to save the verified user");
            feignClient.saveVerifiedAppUser(verifiedUser);
            log.info("trying to save updated user token");
            feignClient.updateUserToken(userToken);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Token not found");
        }
    }

    @Override
    public String generateToken(String email) {
        log.info("starting to generate token for email {}", email);
        String token = UUID.randomUUID().toString().trim();

        log.info("trying to find app user with email {}", email);
        AppUserDto appUser = feignClient.findAppUserByEmail(email);
        Long appUserId = appUser.getId();
        try {
            log.info("trying to find the user token");
            Optional<UserToken> existingUserToken = feignClient.findUserTokenByAppUserId(appUserId);

            if (existingUserToken.isPresent()) {
                existingUserToken.get().setToken(token);
                existingUserToken.get().setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
                existingUserToken.get().setUsed(false);
                log.info("updating the existing user token");
                feignClient.updateUserToken(existingUserToken.get());
            } else {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            UserToken newUserToken = UserToken.builder()
                    .token(token)
                    .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                    .used(false)
                    .appUserId(appUserId).build();

            log.info("saved a new user token");
            feignClient.saveUserToken(newUserToken);
        }
        return token;
    }

}
