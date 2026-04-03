package com.marvin.backup.repository;

import com.marvin.entities.exports.BackupRunEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for backup run entities. */
public interface BackupRunRepository extends JpaRepository<BackupRunEntity, Long> {

    /**
     * Finds backup runs matching the given optional filters.
     *
     * @param from     the inclusive start date filter (nullable)
     * @param to       the inclusive end date filter (nullable)
     * @param status   the status filter (nullable)
     * @param pageable the pagination information
     * @return a page of matching backup run entities
     */
    @Query("SELECT b FROM BackupRunEntity b WHERE " +
            "(cast(:from as LocalDateTime) IS NULL OR b.startedAt >= :from) AND " +
            "(cast(:to as LocalDateTime) IS NULL OR b.startedAt <= :to) AND " +
            "(cast(:status as String) IS NULL OR b.status = :status)")
    Page<BackupRunEntity> findByFilters(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") String status,
            Pageable pageable);

}
