package com.school.platform.lesson.web;

import com.school.platform.lesson.domain.StaleLessonRevisionException;
import com.school.platform.lesson.infrastructure.authorization.LessonAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class LessonExceptionHandler {
    @ExceptionHandler(StaleLessonRevisionException.class)
    ResponseEntity<ProblemDetail> stale(StaleLessonRevisionException exception, HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.CONFLICT, "LESSON_REVISION_CONFLICT", exception.getMessage(), request);
        detail.setProperty("currentRevision", exception.currentRevision());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
    }

    @ExceptionHandler(LessonAccessDeniedException.class)
    ResponseEntity<ProblemDetail> denied(LessonAccessDeniedException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problem(HttpStatus.FORBIDDEN, "LESSON_ACCESS_DENIED", exception.getMessage(), request));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ProblemDetail> invalid(Exception exception, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "LESSON_VALIDATION_FAILED",
                "Lesson request is invalid", request));
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://school.example/problems/" + code.toLowerCase().replace('_', '-')));
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        String correlationId = request.getHeader("X-Correlation-Id");
        problem.setProperty("correlationId", correlationId == null ? UUID.randomUUID().toString() : correlationId);
        return problem;
    }
}