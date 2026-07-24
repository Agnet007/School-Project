package com.school.platform.lesson.infrastructure.authorization;

public final class LessonAccessDeniedException extends RuntimeException {
    public LessonAccessDeniedException() { super("Lesson access denied"); }
}