package com.marvin.costs.repository;

import com.marvin.entities.costs.SpecialCostEntryEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for special cost entry entities. */
public interface SpecialCostEntryRepository extends JpaRepository<SpecialCostEntryEntity, Integer> {

    /**
     * Finds special cost entries by the cost date of the parent special cost.
     *
     * @param costDate the cost date to filter by
     * @return the list of matching entities
     */
    List<SpecialCostEntryEntity> findBySpecialCostCostDate(LocalDate costDate);

    /**
     * Finds all special cost entries ordered by the cost date of the parent special cost.
     *
     * @return the list of all entities ordered by cost date
     */
    List<SpecialCostEntryEntity> findAllByOrderBySpecialCostCostDate();

}
