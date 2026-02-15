package com.marvin.vocabulary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "deck", schema = "vocabulary")
@Audited
public class DeckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deck_id_gen")
    @SequenceGenerator(name = "deck_id_gen", sequenceName = "vocabulary.deck_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverse_deck_id", unique = true)
    private DeckEntity reverseDeck;
}
