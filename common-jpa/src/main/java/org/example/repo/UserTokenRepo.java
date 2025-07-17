package org.example.repo;

import org.example.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepo extends JpaRepository<UserToken , Long> {

    UserToken findUserTokenByToken(String token);

    Optional<UserToken> findByAppUserId(Long appUserId);
}
