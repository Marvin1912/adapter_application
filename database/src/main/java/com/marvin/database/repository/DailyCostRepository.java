package com.marvin.database.repository;

import com.marvin.entities.costs.DailyCostEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyCostRepository extends JpaRepository<DailyCostEntity, Integer> {

  Optional<DailyCostEntity> findByCostDateAndDescriptionOrderByCostDate(LocalDate costDate,
      String description);

  List<DailyCostEntity> findByCostDateGreaterThanEqualOrderByCostDate(LocalDate localDate);

}
