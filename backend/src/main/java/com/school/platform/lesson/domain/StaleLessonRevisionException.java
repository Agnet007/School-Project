package com.school.platform.lesson.domain;

public final class StaleLessonRevisionException extends RuntimeException {
    private final long currentRevision;

    public StaleLessonRevisionException(long currentRevision) {
        super("Draft revision is stale");
        this.currentRevision = currentRevision;
    }

    public long currentRevision() { return currentRevision; }
}