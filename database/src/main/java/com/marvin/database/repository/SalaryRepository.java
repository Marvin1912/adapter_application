package com.marvin.database.repository;

import com.marvin.entities.costs.SalaryEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryRepository extends JpaRepository<SalaryEntity, Integer> {

  Optional<SalaryEntity> findBySalaryDate(LocalDate salaryDate);

  List<SalaryEntity> findAllByOrderBySalaryDate();

}
