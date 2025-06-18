package org.example.repo;

import org.example.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser , Long> {
    AppUser findAppUserByTelegramBotId(Long id);
}
