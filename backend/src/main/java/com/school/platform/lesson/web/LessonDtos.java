package com.school.platform.lesson.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

final class LessonDtos {
    private LessonDtos() { }

    record CreateLessonRequest(@NotBlank @Size(max = 200) String title, @Size(max = 4000) String description) { }
    record UpdateMetadataRequest(@NotBlank @Size(max = 200) String title, @Size(max = 4000) String description) { }
    record BlockMutationRequest(@NotNull String blockType, @NotNull JsonNode configuration) { }
    record BlockOrderRequest(@NotNull @Size(max = 500) List<UUID> blockIds) { }
    record BlockResponse(UUID blockId, String blockType, int blockSchemaVersion, int position,
            JsonNode configuration) { }
    record DraftResponse(UUID lessonId, UUID draftId, String title, String description, long revision,
            List<BlockResponse> blocks, Instant createdAt, Instant updatedAt) { }
    record VersionResponse(UUID lessonId, int version, String title, String description, long sourceRevision,
            List<BlockResponse> blocks, Instant publishedAt) { }
}