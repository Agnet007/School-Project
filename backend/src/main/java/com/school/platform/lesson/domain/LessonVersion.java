package com.school.platform.lesson.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LessonVersion(UUID lessonId, int version, String title, String description,
        List<LessonBlock> blocks, long sourceRevision, Instant publishedAt, UUID publishedBy) {
    public LessonVersion {
        blocks = List.copyOf(blocks);
        if (version < 1) throw new IllegalArgumentException("Version must be positive");
    }
}