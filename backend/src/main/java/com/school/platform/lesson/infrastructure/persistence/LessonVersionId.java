package com.school.platform.lesson.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

record LessonVersionId(UUID lessonId, int version) implements Serializable { }