package com.marvin.mental.arithmetic.repository;

import com.marvin.mental.arithmetic.entity.ArithmeticSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArithmeticSettingsRepository extends JpaRepository<ArithmeticSettingsEntity, Integer> {
}
