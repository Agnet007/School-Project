package com.school.platform.lesson.infrastructure.authorization;

import com.school.platform.lesson.application.LessonAuthorizationGateway;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !test")
class OwnerLessonAuthorizationGateway implements LessonAuthorizationGateway {
    private static final Set<String> PERMISSIONS = Set.of("lesson.create", "lesson.read", "lesson.edit", "lesson.publish");

    @Override
    public void require(String permission, UUID principalId, UUID lessonId, UUID ownerId) {
        if (!PERMISSIONS.contains(permission) || principalId == null || ownerId == null || !principalId.equals(ownerId)) {
            throw new LessonAccessDeniedException();
        }
    }
}