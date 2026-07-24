package com.school.platform.lesson.infrastructure.authorization;

import com.school.platform.lesson.application.CurrentPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Profile({"dev", "test"})
class DevelopmentCurrentPrincipal implements CurrentPrincipal {
    static final UUID DEFAULT_DEVELOPMENT_PRINCIPAL = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final HttpServletRequest request;

    DevelopmentCurrentPrincipal(HttpServletRequest request) { this.request = request; }

    @Override
    public UUID principalId() {
        String value = request.getHeader("X-Principal-Id");
        return value == null ? DEFAULT_DEVELOPMENT_PRINCIPAL : UUID.fromString(value);
    }
}