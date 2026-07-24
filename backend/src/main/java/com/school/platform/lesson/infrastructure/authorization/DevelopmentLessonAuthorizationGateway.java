package com.school.platform.lesson.infrastructure.authorization;

import com.school.platform.lesson.application.LessonAuthorizationGateway;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Profile({"dev", "test"})
class DevelopmentLessonAuthorizationGateway implements LessonAuthorizationGateway {
    private static final Set<String> ALL_LESSON_PERMISSIONS = Set.of(
            "lesson.create", "lesson.read", "lesson.edit", "lesson.publish");
    private final HttpServletRequest request;

    DevelopmentLessonAuthorizationGateway(HttpServletRequest request) { this.request = request; }

    @Override
    public void require(String permission, UUID principalId, UUID lessonId, UUID ownerId) {
        String header = request.getHeader("X-Lesson-Permissions");
        Set<String> permissions = header == null ? ALL_LESSON_PERMISSIONS : Arrays.stream(header.split(","))
                .map(String::strip).filter(value -> !value.isEmpty()).collect(Collectors.toSet());
        if (!permissions.contains(permission) || principalId == null || ownerId == null || !principalId.equals(ownerId)) {
            throw new LessonAccessDeniedException();
        }
    }
}