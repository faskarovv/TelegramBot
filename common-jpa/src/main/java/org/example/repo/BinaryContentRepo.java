package org.example.repo;

import org.example.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryContentRepo extends JpaRepository<BinaryContent , Long> {
}
