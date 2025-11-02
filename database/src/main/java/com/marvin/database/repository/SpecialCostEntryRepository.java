package com.marvin.database.repository;

import com.marvin.entities.costs.SpecialCostEntryEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialCostEntryRepository extends JpaRepository<SpecialCostEntryEntity, Integer> {

    List<SpecialCostEntryEntity> findBySpecialCostCostDate(LocalDate costDate);

    List<SpecialCostEntryEntity> findAllByOrderBySpecialCostCostDate();

}
