package com.marvin.image.repository;

import com.marvin.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public interface ImageRepository extends JpaRepository<Image, UUID> {
}
