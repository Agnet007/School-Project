package com.school.platform.lesson.domain;

import java.util.Objects;
import java.util.UUID;

public record LessonBlock(UUID blockId, BlockType blockType, int blockSchemaVersion, int position,
        BlockConfiguration configuration) {
    public LessonBlock {
        Objects.requireNonNull(blockId);
        Objects.requireNonNull(blockType);
        Objects.requireNonNull(configuration);
        if (blockSchemaVersion != 1 || position < 0) {
            throw new IllegalArgumentException("Invalid block version or position");
        }
        BlockConfigurationValidator.validate(blockType, configuration);
    }

    LessonBlock atPosition(int newPosition) {
        return new LessonBlock(blockId, blockType, blockSchemaVersion, newPosition, configuration);
    }

    LessonBlock withConfiguration(BlockConfiguration newConfiguration) {
        return new LessonBlock(blockId, blockType, blockSchemaVersion, position, newConfiguration);
    }
}