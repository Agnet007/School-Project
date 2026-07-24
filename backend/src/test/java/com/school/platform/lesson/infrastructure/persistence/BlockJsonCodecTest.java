package com.school.platform.lesson.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.platform.lesson.domain.BlockConfiguration;
import com.school.platform.lesson.domain.BlockType;
import com.school.platform.lesson.domain.LessonBlock;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BlockJsonCodecTest {
    private final BlockJsonCodec codec = new BlockJsonCodec(new ObjectMapper());

    @Test
    void typedConfigurationRoundTrips() {
        LessonBlock block = new LessonBlock(UUID.randomUUID(), BlockType.HEADING, 1, 0,
                new BlockConfiguration.Heading("Hello", 2));
        List<LessonBlock> decoded = codec.readBlocks(codec.writeBlocks(List.of(block)));
        assertThat(decoded).containsExactly(block);
    }
}