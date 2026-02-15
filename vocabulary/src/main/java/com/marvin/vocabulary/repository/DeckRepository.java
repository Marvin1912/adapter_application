package com.marvin.vocabulary.repository;

import com.marvin.vocabulary.model.DeckEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends JpaRepository<DeckEntity, Integer> {

    Optional<DeckEntity> findByName(String name);
}
