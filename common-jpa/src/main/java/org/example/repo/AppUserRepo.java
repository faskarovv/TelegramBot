package org.example.repo;

import org.example.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser , Long> {
    AppUser findAppUserByTelegramBotId(Long id);

    Optional<AppUser> findAppUserByEmail(String email);
}
