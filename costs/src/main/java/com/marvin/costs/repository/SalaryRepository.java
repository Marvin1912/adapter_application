package com.marvin.costs.repository;

import com.marvin.entities.costs.SalaryEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for salary entities. */
public interface SalaryRepository extends JpaRepository<SalaryEntity, Integer> {

    /**
     * Finds a salary entity by salary date.
     *
     * @param salaryDate the salary date
     * @return an optional containing the entity if found
     */
    Optional<SalaryEntity> findBySalaryDate(LocalDate salaryDate);

    /**
     * Finds all salary entities ordered by salary date.
     *
     * @return the list of all entities ordered by salary date
     */
    List<SalaryEntity> findAllByOrderBySalaryDate();

}
