package com.school.platform.lesson.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LessonJpaRepository extends JpaRepository<LessonEntity, UUID> {
    List<LessonEntity> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}