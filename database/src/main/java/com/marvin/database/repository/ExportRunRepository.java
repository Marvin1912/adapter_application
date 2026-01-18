package com.marvin.database.repository;

import com.marvin.entities.exports.ExportRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRunRepository extends JpaRepository<ExportRunEntity, Long> {
}
