package com.marvin.image.repository;

import com.marvin.image.entity.Image;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface ImageRepository extends JpaRepository<Image, UUID> {

}
