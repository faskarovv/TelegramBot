package org.example.repo;

import org.example.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataRepo extends JpaRepository<RawData , Long> {

}
