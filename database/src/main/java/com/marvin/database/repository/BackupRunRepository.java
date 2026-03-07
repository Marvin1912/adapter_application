package com.marvin.database.repository;

import com.marvin.entities.exports.BackupRunEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BackupRunRepository extends JpaRepository<BackupRunEntity, Long> {

    @Query("SELECT b FROM BackupRunEntity b WHERE " +
            "(:from IS NULL OR b.startedAt >= :from) AND " +
            "(:to IS NULL OR b.startedAt <= :to) AND " +
            "(:status IS NULL OR b.status = :status)")
    Page<BackupRunEntity> findByFilters(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") String status,
            Pageable pageable);

}
