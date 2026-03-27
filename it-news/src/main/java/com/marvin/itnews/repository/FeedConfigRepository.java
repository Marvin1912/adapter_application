package com.marvin.itnews.repository;

import com.marvin.itnews.entity.FeedConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedConfigRepository extends JpaRepository<FeedConfig, Integer> {

    List<FeedConfig> findByActiveTrue();

}
