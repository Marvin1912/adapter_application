package com.marvin.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "image", schema = "images")
public class Image {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    public Image(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }
}
