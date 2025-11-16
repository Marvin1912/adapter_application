package com.marvin.database.repository;

import com.marvin.entities.costs.MonthlyCostEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyCostRepository extends JpaRepository<MonthlyCostEntity, Integer> {

    Optional<MonthlyCostEntity> findByCostDate(LocalDate costDate);

    List<MonthlyCostEntity> findAllByOrderByCostDate();

}
