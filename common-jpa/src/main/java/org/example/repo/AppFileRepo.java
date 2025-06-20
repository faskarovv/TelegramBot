package org.example.repo;

import org.example.entity.AppFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppFileRepo extends JpaRepository<AppFile , Long> {
}
