package com.marvin.costs.repository;

import com.marvin.costs.entity.MonthlyCostEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for monthly cost entities. */
@Repository
public interface MonthlyCostRepository extends JpaRepository<MonthlyCostEntity, Integer> {

    /**
     * Finds a monthly cost entity by cost date.
     *
     * @param costDate the cost date
     * @return an optional containing the entity if found
     */
    Optional<MonthlyCostEntity> findByCostDate(LocalDate costDate);

    /**
     * Finds all monthly cost entities ordered by cost date.
     *
     * @return the list of all entities ordered by cost date
     */
    List<MonthlyCostEntity> findAllByOrderByCostDate();

}
