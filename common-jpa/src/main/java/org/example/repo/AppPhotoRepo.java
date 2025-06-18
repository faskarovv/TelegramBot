package org.example.repo;

import org.example.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppPhotoRepo extends JpaRepository<AppPhoto , Long> {
}
