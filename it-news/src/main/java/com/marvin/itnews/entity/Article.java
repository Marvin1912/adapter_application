package com.marvin.itnews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "article", schema = "it_news")
@Audited
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_id_gen")
    @SequenceGenerator(
            name = "article_id_gen",
            sequenceName = "it_news.article_id_seq",
            allocationSize = 1
    )
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "link", nullable = false, unique = true, length = 1000)
    private String link;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Article article = (Article) o;
        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
