package com.school.platform.lesson.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@IdClass(LessonVersionId.class)
@Table(name = "lesson_version")
class LessonVersionEntity {
    @Id
    @Column(name = "lesson_id", nullable = false)
    UUID lessonId;
    @Id
    @Column(nullable = false)
    int version;
    @Column(name = "source_revision", nullable = false)
    long sourceRevision;
    @Column(nullable = false, length = 200)
    String title;
    @Column(nullable = false, length = 4000)
    String description;
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    String snapshotJson;
    @Column(name = "published_at", nullable = false)
    Instant publishedAt;
    @Column(name = "published_by", nullable = false)
    UUID publishedBy;

    protected LessonVersionEntity() { }
}