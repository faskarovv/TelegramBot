package org.example.repo;

import org.example.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDocumentRepo extends JpaRepository<AppDocument , Long> {
}
