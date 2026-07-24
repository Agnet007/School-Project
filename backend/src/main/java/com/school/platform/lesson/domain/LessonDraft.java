package com.school.platform.lesson.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class LessonDraft {
    private final UUID lessonId;
    private final UUID draftId;
    private final UUID ownerId;
    private String title;
    private String description;
    private LessonRevision revision;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<LessonBlock> blocks;

    public LessonDraft(UUID lessonId, UUID draftId, UUID ownerId, String title, String description,
            LessonRevision revision, Instant createdAt, Instant updatedAt, List<LessonBlock> blocks) {
        this.lessonId = lessonId;
        this.draftId = draftId;
        this.ownerId = ownerId;
        this.title = requireTitle(title);
        this.description = description == null ? "" : description;
        this.revision = revision;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blocks = new ArrayList<>(blocks);
        validateOrder();
    }

    public static LessonDraft create(UUID ownerId, String title, String description, Instant now) {
        return new LessonDraft(UUID.randomUUID(), UUID.randomUUID(), ownerId, title, description,
                new LessonRevision(0), now, now, List.of());
    }

    public void updateMetadata(String newTitle, String newDescription, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        title = requireTitle(newTitle);
        description = newDescription == null ? "" : newDescription;
        changed(now);
    }

    public LessonBlock addBlock(BlockType type, BlockConfiguration configuration, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        LessonBlock block = new LessonBlock(UUID.randomUUID(), type, 1, blocks.size(), configuration);
        blocks.add(block);
        changed(now);
        return block;
    }

    public void updateBlock(UUID blockId, BlockType type, BlockConfiguration configuration,
            long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        int index = indexOf(blockId);
        LessonBlock existing = blocks.get(index);
        blocks.set(index, new LessonBlock(existing.blockId(), type, 1, existing.position(), configuration));
        changed(now);
    }

    public LessonBlock duplicateBlock(UUID blockId, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        int sourceIndex = indexOf(blockId);
        LessonBlock source = blocks.get(sourceIndex);
        LessonBlock duplicate = new LessonBlock(UUID.randomUUID(), source.blockType(), 1,
                sourceIndex + 1, source.configuration());
        blocks.add(sourceIndex + 1, duplicate);
        normalizePositions();
        changed(now);
        return duplicate;
    }

    public void removeBlock(UUID blockId, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        blocks.remove(indexOf(blockId));
        normalizePositions();
        changed(now);
    }

    public void reorderBlocks(List<UUID> orderedIds, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        if (orderedIds.size() != blocks.size() || new HashSet<>(orderedIds).size() != blocks.size()) {
            throw new IllegalArgumentException("Block order must contain every block exactly once");
        }
        List<LessonBlock> reordered = orderedIds.stream().map(id -> blocks.get(indexOf(id))).toList();
        blocks.clear();
        blocks.addAll(reordered);
        normalizePositions();
        changed(now);
    }

    public PublicationResult publish(int nextVersion, UUID principalId, long expectedRevision, Instant now) {
        requireRevision(expectedRevision);
        LessonVersion version = new LessonVersion(lessonId, nextVersion, title, description, blocks,
                revision.value(), now, principalId);
        return new PublicationResult(version, this);
    }

    private void requireRevision(long expectedRevision) {
        if (revision.value() != expectedRevision) throw new StaleLessonRevisionException(revision.value());
    }

    private int indexOf(UUID blockId) {
        for (int index = 0; index < blocks.size(); index++) {
            if (blocks.get(index).blockId().equals(blockId)) return index;
        }
        throw new IllegalArgumentException("Unknown block: " + blockId);
    }

    private void changed(Instant now) {
        revision = revision.next();
        updatedAt = now;
    }

    private void normalizePositions() {
        for (int index = 0; index < blocks.size(); index++) blocks.set(index, blocks.get(index).atPosition(index));
    }

    private void validateOrder() {
        for (int index = 0; index < blocks.size(); index++) {
            if (blocks.get(index).position() != index) throw new IllegalArgumentException("Block positions must be contiguous");
        }
    }

    private static String requireTitle(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        return value.strip();
    }

    public UUID lessonId() { return lessonId; }
    public UUID draftId() { return draftId; }
    public UUID ownerId() { return ownerId; }
    public String title() { return title; }
    public String description() { return description; }
    public LessonRevision revision() { return revision; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public List<LessonBlock> blocks() { return List.copyOf(blocks); }
}