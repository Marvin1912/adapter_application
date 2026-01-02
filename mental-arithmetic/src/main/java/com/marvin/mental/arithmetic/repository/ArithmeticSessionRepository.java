package com.marvin.mental.arithmetic.repository;

import com.marvin.mental.arithmetic.entity.ArithmeticSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArithmeticSessionRepository extends JpaRepository<ArithmeticSessionEntity, String> {

    @Query("SELECT s FROM ArithmeticSessionEntity s LEFT JOIN FETCH s.problems LEFT JOIN FETCH s.settings WHERE s.id = :id")
    Optional<ArithmeticSessionEntity> findByIdWithProblemsAndSettings(String id);

    @Query("SELECT s FROM ArithmeticSessionEntity s LEFT JOIN FETCH s.problems LEFT JOIN FETCH s.settings")
    java.util.List<ArithmeticSessionEntity> findAllWithProblemsAndSettings();
}
