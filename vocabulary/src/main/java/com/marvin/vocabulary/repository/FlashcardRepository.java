package com.marvin.vocabulary.repository;

import com.marvin.vocabulary.model.FlashcardEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<FlashcardEntity, Integer> {

    @Query("SELECT f.ankiId FROM FlashcardEntity f")
    Set<String> getAllAnkiIds();

    Optional<FlashcardEntity> findByFrontAndBack(String front, String back);

    List<FlashcardEntity> findByAnkiIdIsNull();

    List<FlashcardEntity> findByUpdated(boolean updated);

}
