package com.marvin.plants.repository;

import com.marvin.plants.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {

    Collection<Plant> findByNextWateredDate(LocalDate date);

}
