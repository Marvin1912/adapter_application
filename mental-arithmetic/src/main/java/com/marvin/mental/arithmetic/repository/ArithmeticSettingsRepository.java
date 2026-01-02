package com.marvin.mental.arithmetic.repository;

import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArithmeticSettingsRepository extends JpaRepository<ArithmeticSettingsEntity, Integer> {

    @Query("SELECT s FROM ArithmeticSettingsEntity s LEFT JOIN FETCH s.operations")
    List<ArithmeticSettingsEntity> findAllWithOperations();
}
