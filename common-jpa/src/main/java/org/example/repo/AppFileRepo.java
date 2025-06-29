package org.example.repo;

import org.example.entity.AppFile;
import org.example.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppFileRepo extends JpaRepository<AppFile , Long> {
    List<AppFile> findAllByAppUser(AppUser appUser);

}
