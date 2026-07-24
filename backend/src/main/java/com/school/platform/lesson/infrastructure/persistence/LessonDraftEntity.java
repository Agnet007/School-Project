package com.school.platform.lesson.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lesson_draft")
class LessonDraftEntity {
    @Id
    @Column(name = "draft_id", nullable = false)
    UUID draftId;
    @Column(name = "lesson_id", nullable = false, unique = true)
    UUID lessonId;
    @Column(nullable = false, length = 200)
    String title;
    @Column(nullable = false, length = 4000)
    String description;
    @Column(nullable = false)
    @Version
    long revision;
    @Column(name = "blocks_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    String blocksJson;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    protected LessonDraftEntity() { }
}