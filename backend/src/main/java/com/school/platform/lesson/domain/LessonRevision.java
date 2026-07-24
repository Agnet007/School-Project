package com.school.platform.lesson.domain;

public record LessonRevision(long value) {
    public LessonRevision {
        if (value < 0) throw new IllegalArgumentException("Revision cannot be negative");
    }

    public LessonRevision next() {
        return new LessonRevision(value + 1);
    }
}