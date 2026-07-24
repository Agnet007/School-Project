package com.school.platform.lesson.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LessonVersionJpaRepository extends JpaRepository<LessonVersionEntity, LessonVersionId> {
    List<LessonVersionEntity> findByLessonIdOrderByVersionAsc(UUID lessonId);
}