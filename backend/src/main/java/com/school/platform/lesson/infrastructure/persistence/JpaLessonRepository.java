package com.school.platform.lesson.infrastructure.persistence;

import com.school.platform.lesson.application.LessonRepository;
import com.school.platform.lesson.domain.LessonDraft;
import com.school.platform.lesson.domain.LessonRevision;
import com.school.platform.lesson.domain.LessonVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaLessonRepository implements LessonRepository {
    private final LessonJpaRepository lessons;
    private final LessonDraftJpaRepository drafts;
    private final LessonVersionJpaRepository versions;
    private final BlockJsonCodec codec;

    JpaLessonRepository(LessonJpaRepository lessons, LessonDraftJpaRepository drafts,
            LessonVersionJpaRepository versions, BlockJsonCodec codec) {
        this.lessons = lessons;
        this.drafts = drafts;
        this.versions = versions;
        this.codec = codec;
    }

    @Override
    public void create(LessonDraft draft) {
        lessons.save(new LessonEntity(draft.lessonId(), draft.ownerId(), draft.createdAt()));
        drafts.save(toEntity(draft));
    }

    @Override
    public void save(LessonDraft draft) { drafts.save(toEntity(draft)); }

    @Override
    public Optional<LessonDraft> findDraft(UUID lessonId) {
        return drafts.findByLessonId(lessonId).map(this::toDomain);
    }

    @Override
    public List<LessonDraft> listDrafts(UUID ownerId) {
        return lessons.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(lesson -> findDraft(lesson.lessonId).orElseThrow()).toList();
    }

    @Override
    public void saveVersion(LessonVersion version) {
        LessonVersionEntity entity = new LessonVersionEntity();
        entity.lessonId = version.lessonId();
        entity.version = version.version();
        entity.sourceRevision = version.sourceRevision();
        entity.title = version.title();
        entity.description = version.description();
        entity.snapshotJson = codec.writeBlocks(version.blocks());
        entity.publishedAt = version.publishedAt();
        entity.publishedBy = version.publishedBy();
        versions.save(entity);
    }

    @Override
    public Optional<LessonVersion> findVersion(UUID lessonId, int version) {
        return versions.findById(new LessonVersionId(lessonId, version)).map(this::toDomain);
    }

    @Override
    public List<LessonVersion> listVersions(UUID lessonId) {
        return versions.findByLessonIdOrderByVersionAsc(lessonId).stream().map(this::toDomain).toList();
    }

    private LessonDraftEntity toEntity(LessonDraft draft) {
        LessonDraftEntity entity = drafts.findByLessonId(draft.lessonId()).orElseGet(LessonDraftEntity::new);
        entity.draftId = draft.draftId();
        entity.lessonId = draft.lessonId();
        entity.title = draft.title();
        entity.description = draft.description();
        entity.revision = draft.revision().value();
        entity.blocksJson = codec.writeBlocks(draft.blocks());
        entity.createdAt = draft.createdAt();
        entity.updatedAt = draft.updatedAt();
        return entity;
    }

    private LessonDraft toDomain(LessonDraftEntity entity) {
        UUID ownerId = lessons.findById(entity.lessonId).orElseThrow().ownerId;
        return new LessonDraft(entity.lessonId, entity.draftId, ownerId, entity.title, entity.description,
                new LessonRevision(entity.revision), entity.createdAt, entity.updatedAt, codec.readBlocks(entity.blocksJson));
    }

    private LessonVersion toDomain(LessonVersionEntity entity) {
        return new LessonVersion(entity.lessonId, entity.version, entity.title, entity.description,
                codec.readBlocks(entity.snapshotJson), entity.sourceRevision, entity.publishedAt, entity.publishedBy);
    }
}