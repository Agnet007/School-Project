package com.school.platform.lesson.application;

import com.school.platform.lesson.domain.LessonDraft;
import com.school.platform.lesson.domain.LessonVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository {
    void create(LessonDraft draft);
    void save(LessonDraft draft);
    Optional<LessonDraft> findDraft(UUID lessonId);
    List<LessonDraft> listDrafts(UUID ownerId);
    void saveVersion(LessonVersion version);
    Optional<LessonVersion> findVersion(UUID lessonId, int version);
    List<LessonVersion> listVersions(UUID lessonId);
}