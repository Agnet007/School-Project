package com.school.platform.lesson.application;

import java.util.UUID;

public interface LessonAuthorizationGateway {
    void require(String permission, UUID principalId, UUID lessonId, UUID ownerId);
}