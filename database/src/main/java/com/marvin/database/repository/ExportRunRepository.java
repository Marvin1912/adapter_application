package com.marvin.database.repository;

import com.marvin.entities.exports.ExportRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExportRunRepository extends JpaRepository<ExportRunEntity, Long> {

    @Query("SELECT e FROM ExportRunEntity e WHERE " +
           "(:from IS NULL OR e.startedAt >= :from) AND " +
           "(:to IS NULL OR e.startedAt <= :to) AND " +
           "(:type IS NULL OR e.exporterType = :type) AND " +
           "(:status IS NULL OR e.status = :status) " +
           "ORDER BY e.startedAt DESC")
    Page<ExportRunEntity> findByFilters(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        @Param("type") String type,
                                        @Param("status") String status,
                                        Pageable pageable);

    Optional<ExportRunEntity> findById(Long id);
}
