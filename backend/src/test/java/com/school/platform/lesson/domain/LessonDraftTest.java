package com.school.platform.lesson.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LessonDraftTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void blockOperationsPreserveStableIdentityAndRevision() {
        LessonDraft draft = LessonDraft.create(UUID.randomUUID(), "Algebra", "", NOW);
        LessonBlock first = draft.addBlock(BlockType.HEADING, new BlockConfiguration.Heading("Intro", 2), 0, NOW);
        LessonBlock duplicate = draft.duplicateBlock(first.blockId(), 1, NOW);
        draft.reorderBlocks(List.of(duplicate.blockId(), first.blockId()), 2, NOW);

        assertThat(draft.blocks()).extracting(LessonBlock::blockId)
                .containsExactly(duplicate.blockId(), first.blockId());
        assertThat(duplicate.blockId()).isNotEqualTo(first.blockId());
        assertThat(draft.revision().value()).isEqualTo(3);
    }

    @Test
    void staleMutationDoesNotOverwriteDraft() {
        LessonDraft draft = LessonDraft.create(UUID.randomUUID(), "Algebra", "", NOW);
        draft.updateMetadata("Geometry", "", 0, NOW);

        assertThatThrownBy(() -> draft.updateMetadata("Stale", "", 0, NOW))
                .isInstanceOf(StaleLessonRevisionException.class);
        assertThat(draft.title()).isEqualTo("Geometry");
    }

    @Test
    void publicationIsImmutableSnapshot() {
        LessonDraft draft = LessonDraft.create(UUID.randomUUID(), "Version one", "", NOW);
        LessonVersion version = draft.publish(1, draft.ownerId(), 0, NOW).version();
        draft.updateMetadata("Version two draft", "", 0, NOW);

        assertThat(version.title()).isEqualTo("Version one");
        assertThat(version.version()).isEqualTo(1);
    }

    @Test
    void blankTitleIsRejected() {
        assertThatThrownBy(() -> LessonDraft.create(UUID.randomUUID(), " ", "", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}