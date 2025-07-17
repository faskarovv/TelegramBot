package org.example.service;

import org.example.dto.AppUserDto;
import org.example.dto.TokenRequest;
import org.example.entity.AppUser;
import org.example.entity.UserToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@FeignClient(name = "feignClientService", url = "http://localhost:8082")
public interface FeignClientInterface {

    @PostMapping("/saveUserToken")
    void  saveUserToken(UserToken userToken);

    @PostMapping("/saveVerifiedUser")
    void saveVerifiedAppUser(AppUser verifiedAppUser);

    @PutMapping("/updateUserToken")
    void updateUserToken(@RequestBody UserToken userToken);

    @GetMapping("/getByAppUserId")
    Optional<UserToken> findUserTokenByAppUserId(@RequestParam Long appUserId);

    @PostMapping("/getByToken")
    UserToken findUserTokenByTokenRequest(@RequestBody TokenRequest token);

    @GetMapping("/getByEmail")
    AppUserDto findAppUserByEmail(@RequestParam String email);

    @GetMapping("/getByAppId")
    AppUser findAppUserById(@RequestParam Long appUserId);

}
