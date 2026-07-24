package com.school.platform.lesson.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson")
class LessonEntity {
    @Id
    @Column(name = "lesson_id", nullable = false)
    UUID lessonId;
    @Column(name = "owner_id", nullable = false)
    UUID ownerId;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected LessonEntity() { }
    LessonEntity(UUID lessonId, UUID ownerId, Instant createdAt) {
        this.lessonId = lessonId;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }
}