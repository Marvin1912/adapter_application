package com.marvin.mental.arithmetic.repository;

import com.marvin.mental.arithmetic.entity.ArithmeticProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArithmeticProblemRepository extends JpaRepository<ArithmeticProblemEntity, String> {
}
