package com.marvin.costs.repository;

import com.marvin.entities.costs.DailyCostEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for daily cost entities. */
@Repository
public interface DailyCostRepository extends JpaRepository<DailyCostEntity, Integer> {

    /**
     * Finds a daily cost entity by cost date and description.
     *
     * @param costDate the cost date
     * @param description the description
     * @return an optional containing the entity if found
     */
    Optional<DailyCostEntity> findByCostDateAndDescriptionOrderByCostDate(LocalDate costDate,
            String description);

    /**
     * Finds all daily cost entities with a cost date greater than or equal to the given date.
     *
     * @param localDate the date to compare against
     * @return the list of matching entities
     */
    List<DailyCostEntity> findByCostDateGreaterThanEqualOrderByCostDate(LocalDate localDate);

}
