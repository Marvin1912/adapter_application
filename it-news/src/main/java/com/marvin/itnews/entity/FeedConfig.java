package com.marvin.itnews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feed_config", schema = "it_news")
@Audited
public class FeedConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feed_config_id_gen")
    @SequenceGenerator(
            name = "feed_config_id_gen",
            sequenceName = "it_news.feed_config_id_seq",
            allocationSize = 1
    )
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false, unique = true, length = 1000)
    private String url;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FeedConfig feedConfig = (FeedConfig) o;
        return Objects.equals(id, feedConfig.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
