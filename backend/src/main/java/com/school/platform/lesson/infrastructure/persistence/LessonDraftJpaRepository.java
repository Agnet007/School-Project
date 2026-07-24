package com.school.platform.lesson.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LessonDraftJpaRepository extends JpaRepository<LessonDraftEntity, UUID> {
    Optional<LessonDraftEntity> findByLessonId(UUID lessonId);
}