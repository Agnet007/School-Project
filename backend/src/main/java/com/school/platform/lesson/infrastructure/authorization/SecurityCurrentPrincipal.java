package com.school.platform.lesson.infrastructure.authorization;

import com.school.platform.lesson.application.CurrentPrincipal;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !test")
class SecurityCurrentPrincipal implements CurrentPrincipal {
    @Override
    public UUID principalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) throw new LessonAccessDeniedException();
        return UUID.fromString(authentication.getName());
    }
}