package com.marvin.vocabulary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcard", schema = "vocabulary")
@Audited
public class FlashcardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flashcard_id_gen")
    @SequenceGenerator(name = "flashcard_id_gen", sequenceName = "vocabulary.flashcard_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverse_flashcard_id", unique = true)
    private FlashcardEntity reverseFlashcard;

    @Column(name = "anki_id")
    private String ankiId;

    @Column(name = "front", nullable = false)
    private String front;

    @Column(name = "back", nullable = false)
    private String back;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "updated", nullable = false)
    private boolean updated;

}
