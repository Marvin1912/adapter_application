package com.marvin.costs.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Base entity providing audit timestamps for all cost-related entities. */
@MappedSuperclass
public class BasicEntity {

    @Basic
    @Column(name = "creation_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;

    @Basic
    @Column(name = "last_modified", nullable = false)
    @UpdateTimestamp
    private LocalDateTime lastModified;

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date.
     *
     * @param creationDate the creation date to set
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the last modified date.
     *
     * @return the last modified date
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified date.
     *
     * @param lastModified the last modified date to set
     */
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BasicEntity that = (BasicEntity) o;
        return Objects.equals(creationDate, that.creationDate) && Objects.equals(lastModified,
                that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creationDate, lastModified);
    }

    @Override
    public String toString() {
        return "BasicEntity{"
                + "creationDate=" + creationDate
                + ", lastModified=" + lastModified
                + '}';
    }
}
