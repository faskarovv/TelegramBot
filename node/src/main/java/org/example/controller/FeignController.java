package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AppUserDto;
import org.example.dto.TokenRequest;
import org.example.entity.AppUser;
import org.example.entity.UserToken;
import org.example.repo.AppUserRepo;
import org.example.repo.UserTokenRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeignController {
    private final UserTokenRepo userTokenRepo;
    private final AppUserRepo appUserRepo;

    @PutMapping("/updateUserToken")
    ResponseEntity<UserToken> updateUserToken(@RequestBody UserToken userToken) {
        Optional<UserToken> existingUserToken = userTokenRepo.findByAppUserId(userToken.getAppUserId());

        if (existingUserToken.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserToken existingToken = existingUserToken.get();
        existingToken.setUsed(userToken.isUsed());
        existingToken.setToken(userToken.getToken());
        existingToken.setExpiresAt(userToken.getExpiresAt());

        userTokenRepo.save(existingToken);
        return ResponseEntity.ok(existingToken);
    }

    @PostMapping("/saveUserToken")
    ResponseEntity<UserToken> saveUserToken(@RequestBody UserToken userToken) {
        UserToken savedUserToken = userTokenRepo.save(userToken);

        return ResponseEntity.ok(savedUserToken);
    }

    @PostMapping("/saveVerifiedUser")
    ResponseEntity<AppUser> saveVerifiedAppUser(@RequestBody AppUser appUser) {
        AppUser savedAppUser = appUserRepo.save(appUser);

        return ResponseEntity.ok(savedAppUser);
    }

    @GetMapping("/getByEmail")
    ResponseEntity<AppUserDto> findAppUserByEmail(@RequestParam String email) {
        log.info("looking for app use with the email {}", email);
        AppUser appUser = appUserRepo.findAppUserByEmail(email);
        if (appUser == null) {
            throw new RuntimeException("No app user exists with this email");
        }
        AppUserDto appUserDto = AppUserDto.builder()
                .id(appUser.getId())
                .email(appUser.getEmail())
                .build();

        log.info("returning the app user with the email {}", appUserDto.getEmail());
        return ResponseEntity.ok(appUserDto);
    }

    @PostMapping("/getByToken")
    UserToken findUserTokenByTokenRequest(@RequestBody TokenRequest tokenInput) {
        String token = tokenInput.getToken().trim();
        log.info("looking for user token with token {}", token);

        UserToken savedUserToken = userTokenRepo.findUserTokenByToken(token);
        log.info("found user token: {}", savedUserToken);

        if (savedUserToken == null) {
            log.info("no such token for this token {}", token);
            throw new RuntimeException("no such user token exists");
        }

        return UserToken.builder()
                .token(token)
                .appUserId(savedUserToken.getAppUserId())
                .expiresAt(savedUserToken.getExpiresAt())
                .used(savedUserToken.isUsed())
                .build();
    }

    @GetMapping("/getByAppUserId")
    public Optional<UserToken> findUserTokenByAppUserId(@RequestParam Long appUserId) {
        log.info("starting to look for the user token with app user id: {}", appUserId);
        Optional<UserToken> savedUserToken = userTokenRepo.findByAppUserId(appUserId);


        log.info("returning the saved user token");
        return savedUserToken;
    }

    @GetMapping("/getByAppId")
    public ResponseEntity<AppUser> findUserByUserId(@RequestParam Long appUserId) {
        Optional<AppUser> savedAppUser = appUserRepo.findById(appUserId);

        if (savedAppUser.isPresent()) {
            return ResponseEntity.ok(savedAppUser.get());
        }
        return ResponseEntity.notFound().build();

    }

}
