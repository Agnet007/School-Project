package com.school.platform.lesson.application;

import com.school.platform.lesson.domain.BlockConfiguration;
import com.school.platform.lesson.domain.BlockType;
import com.school.platform.lesson.domain.LessonDraft;
import com.school.platform.lesson.domain.LessonVersion;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {
    private final LessonRepository repository;
    private final CurrentPrincipal currentPrincipal;
    private final LessonAuthorizationGateway authorization;
    private final Clock clock;

    @Autowired
    public LessonService(LessonRepository repository, CurrentPrincipal currentPrincipal,
            LessonAuthorizationGateway authorization) {
        this(repository, currentPrincipal, authorization, Clock.systemUTC());
    }

    LessonService(LessonRepository repository, CurrentPrincipal currentPrincipal,
            LessonAuthorizationGateway authorization, Clock clock) {
        this.repository = repository;
        this.currentPrincipal = currentPrincipal;
        this.authorization = authorization;
        this.clock = clock;
    }

    @Transactional
    public LessonDraft createDraft(String title, String description) {
        UUID principal = currentPrincipal.principalId();
        authorization.require("lesson.create", principal, null, principal);
        LessonDraft draft = LessonDraft.create(principal, title, description, Instant.now(clock));
        repository.create(draft);
        return draft;
    }

    @Transactional(readOnly = true)
    public LessonDraft getDraft(UUID lessonId) {
        LessonDraft draft = requireDraft(lessonId);
        authorization.require("lesson.read", currentPrincipal.principalId(), lessonId, draft.ownerId());
        return draft;
    }

    @Transactional(readOnly = true)
    public List<LessonDraft> listDrafts() {
        UUID principal = currentPrincipal.principalId();
        return repository.listDrafts(principal).stream().peek(draft ->
                authorization.require("lesson.read", principal, draft.lessonId(), draft.ownerId())).toList();
    }

    @Transactional
    public LessonDraft updateMetadata(UUID lessonId, String title, String description, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.updateMetadata(title, description, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonDraft addBlock(UUID lessonId, BlockType type, BlockConfiguration configuration, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.addBlock(type, configuration, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonDraft updateBlock(UUID lessonId, UUID blockId, BlockType type,
            BlockConfiguration configuration, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.updateBlock(blockId, type, configuration, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonDraft duplicateBlock(UUID lessonId, UUID blockId, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.duplicateBlock(blockId, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonDraft removeBlock(UUID lessonId, UUID blockId, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.removeBlock(blockId, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonDraft reorderBlocks(UUID lessonId, List<UUID> blockIds, long expectedRevision) {
        LessonDraft draft = editableDraft(lessonId);
        draft.reorderBlocks(blockIds, expectedRevision, Instant.now(clock));
        repository.save(draft);
        return draft;
    }

    @Transactional
    public LessonVersion publish(UUID lessonId, long expectedRevision) {
        LessonDraft draft = requireDraft(lessonId);
        UUID principal = currentPrincipal.principalId();
        authorization.require("lesson.publish", principal, lessonId, draft.ownerId());
        int nextVersion = repository.listVersions(lessonId).stream().mapToInt(LessonVersion::version).max().orElse(0) + 1;
        LessonVersion version = draft.publish(nextVersion, principal, expectedRevision, Instant.now(clock)).version();
        repository.saveVersion(version);
        return version;
    }

    @Transactional(readOnly = true)
    public LessonVersion getVersion(UUID lessonId, int version) {
        LessonDraft draft = requireDraft(lessonId);
        authorization.require("lesson.read", currentPrincipal.principalId(), lessonId, draft.ownerId());
        return repository.findVersion(lessonId, version).orElseThrow(() -> new IllegalArgumentException("Version not found"));
    }

    @Transactional(readOnly = true)
    public List<LessonVersion> listVersions(UUID lessonId) {
        LessonDraft draft = requireDraft(lessonId);
        authorization.require("lesson.read", currentPrincipal.principalId(), lessonId, draft.ownerId());
        return repository.listVersions(lessonId);
    }

    private LessonDraft editableDraft(UUID lessonId) {
        LessonDraft draft = requireDraft(lessonId);
        authorization.require("lesson.edit", currentPrincipal.principalId(), lessonId, draft.ownerId());
        return draft;
    }

    private LessonDraft requireDraft(UUID lessonId) {
        return repository.findDraft(lessonId).orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
    }
}