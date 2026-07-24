package com.school.platform.lesson.domain;

import java.time.Instant;
import java.util.UUID;

public record Lesson(UUID lessonId, UUID ownerId, Instant createdAt) { }