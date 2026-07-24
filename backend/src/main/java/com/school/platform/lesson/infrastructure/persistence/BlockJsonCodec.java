package com.school.platform.lesson.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.school.platform.lesson.domain.BlockConfiguration;
import com.school.platform.lesson.domain.BlockType;
import com.school.platform.lesson.domain.LessonBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BlockJsonCodec {
    private final ObjectMapper objectMapper;

    public BlockJsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().findAndRegisterModules()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public String writeBlocks(List<LessonBlock> blocks) {
        try {
            return objectMapper.writeValueAsString(blocks.stream().map(this::toStoredBlock).toList());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot serialize lesson blocks", exception);
        }
    }

    public List<LessonBlock> readBlocks(String json) {
        try {
            List<LessonBlock> blocks = new ArrayList<>();
            for (JsonNode node : objectMapper.readTree(json)) {
                BlockType type = BlockType.valueOf(node.required("blockType").asText());
                blocks.add(new LessonBlock(UUID.fromString(node.required("blockId").asText()), type,
                        node.required("blockSchemaVersion").asInt(), node.required("position").asInt(),
                        readConfiguration(type, node.required("configuration"))));
            }
            return blocks;
        } catch (RuntimeException | JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid persisted lesson blocks", exception);
        }
    }

    public BlockConfiguration readConfiguration(BlockType type, JsonNode node) {
        try {
            Class<? extends BlockConfiguration> configurationType = switch (type) {
                case HEADING -> BlockConfiguration.Heading.class;
                case RICH_TEXT -> BlockConfiguration.RichText.class;
                case RULE -> BlockConfiguration.Rule.class;
                case IMAGE -> BlockConfiguration.Image.class;
                case FORMULA -> BlockConfiguration.Formula.class;
                case CHART -> BlockConfiguration.Chart.class;
                case MULTIPLE_CHOICE -> BlockConfiguration.MultipleChoice.class;
                case NUMERIC_ANSWER -> BlockConfiguration.NumericAnswer.class;
                case TEXT_ANSWER -> BlockConfiguration.TextAnswer.class;
                case SIMULATION -> BlockConfiguration.Simulation.class;
                case WORKSPACE_LAUNCHER -> BlockConfiguration.WorkspaceLauncher.class;
            };
            return objectMapper.treeToValue(node, configurationType);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid " + type + " configuration", exception);
        }
    }

    public JsonNode writeConfiguration(BlockConfiguration configuration) {
        return objectMapper.valueToTree(configuration);
    }

    private StoredBlock toStoredBlock(LessonBlock block) {
        return new StoredBlock(block.blockId(), block.blockType(), block.blockSchemaVersion(), block.position(),
                objectMapper.valueToTree(block.configuration()));
    }

    private record StoredBlock(UUID blockId, BlockType blockType, int blockSchemaVersion, int position,
            JsonNode configuration) { }
}