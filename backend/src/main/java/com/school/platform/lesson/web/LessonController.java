package com.school.platform.lesson.web;

import com.school.platform.lesson.application.LessonService;
import com.school.platform.lesson.domain.BlockConfiguration;
import com.school.platform.lesson.domain.BlockType;
import com.school.platform.lesson.domain.LessonBlock;
import com.school.platform.lesson.domain.LessonDraft;
import com.school.platform.lesson.domain.LessonVersion;
import com.school.platform.lesson.infrastructure.persistence.BlockJsonCodec;
import com.school.platform.lesson.web.LessonDtos.BlockMutationRequest;
import com.school.platform.lesson.web.LessonDtos.BlockOrderRequest;
import com.school.platform.lesson.web.LessonDtos.BlockResponse;
import com.school.platform.lesson.web.LessonDtos.CreateLessonRequest;
import com.school.platform.lesson.web.LessonDtos.DraftResponse;
import com.school.platform.lesson.web.LessonDtos.UpdateMetadataRequest;
import com.school.platform.lesson.web.LessonDtos.VersionResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {
    private final LessonService service;
    private final BlockJsonCodec codec;

    LessonController(LessonService service, BlockJsonCodec codec) {
        this.service = service;
        this.codec = codec;
    }

    @PostMapping
    ResponseEntity<DraftResponse> create(@Valid @RequestBody CreateLessonRequest request) {
        DraftResponse response = draft(service.createDraft(request.title(), request.description()));
        return ResponseEntity.created(URI.create("/api/v1/lessons/" + response.lessonId() + "/draft"))
                .eTag(etag(response.revision())).body(response);
    }

    @GetMapping
    List<DraftResponse> list() { return service.listDrafts().stream().map(this::draft).toList(); }

    @GetMapping("/{lessonId}/draft")
    ResponseEntity<DraftResponse> getDraft(@PathVariable UUID lessonId) {
        DraftResponse response = draft(service.getDraft(lessonId));
        return ResponseEntity.ok().eTag(etag(response.revision())).body(response);
    }

    @PatchMapping("/{lessonId}/draft")
    ResponseEntity<DraftResponse> updateMetadata(@PathVariable UUID lessonId,
            @RequestHeader("If-Match") String ifMatch, @Valid @RequestBody UpdateMetadataRequest request) {
        return draftResponse(service.updateMetadata(lessonId, request.title(), request.description(), revision(ifMatch)));
    }

    @PostMapping("/{lessonId}/draft/blocks")
    ResponseEntity<DraftResponse> addBlock(@PathVariable UUID lessonId,
            @RequestHeader("If-Match") String ifMatch, @Valid @RequestBody BlockMutationRequest request) {
        BlockType type = type(request.blockType());
        return draftResponse(service.addBlock(lessonId, type, configuration(type, request), revision(ifMatch)));
    }

    @PutMapping("/{lessonId}/draft/blocks/{blockId}")
    ResponseEntity<DraftResponse> updateBlock(@PathVariable UUID lessonId, @PathVariable UUID blockId,
            @RequestHeader("If-Match") String ifMatch, @Valid @RequestBody BlockMutationRequest request) {
        BlockType type = type(request.blockType());
        return draftResponse(service.updateBlock(lessonId, blockId, type, configuration(type, request), revision(ifMatch)));
    }

    @PostMapping("/{lessonId}/draft/blocks/{blockId}/duplicate")
    ResponseEntity<DraftResponse> duplicateBlock(@PathVariable UUID lessonId, @PathVariable UUID blockId,
            @RequestHeader("If-Match") String ifMatch) {
        return draftResponse(service.duplicateBlock(lessonId, blockId, revision(ifMatch)));
    }

    @DeleteMapping("/{lessonId}/draft/blocks/{blockId}")
    ResponseEntity<DraftResponse> removeBlock(@PathVariable UUID lessonId, @PathVariable UUID blockId,
            @RequestHeader("If-Match") String ifMatch) {
        return draftResponse(service.removeBlock(lessonId, blockId, revision(ifMatch)));
    }

    @PutMapping("/{lessonId}/draft/block-order")
    ResponseEntity<DraftResponse> reorder(@PathVariable UUID lessonId,
            @RequestHeader("If-Match") String ifMatch, @Valid @RequestBody BlockOrderRequest request) {
        return draftResponse(service.reorderBlocks(lessonId, request.blockIds(), revision(ifMatch)));
    }

    @PostMapping("/{lessonId}/publications")
    ResponseEntity<VersionResponse> publish(@PathVariable UUID lessonId,
            @RequestHeader("If-Match") String ifMatch) {
        VersionResponse response = version(service.publish(lessonId, revision(ifMatch)));
        return ResponseEntity.created(URI.create("/api/v1/lessons/" + lessonId + "/versions/" + response.version()))
                .body(response);
    }

    @GetMapping("/{lessonId}/versions")
    List<VersionResponse> versions(@PathVariable UUID lessonId) {
        return service.listVersions(lessonId).stream().map(this::version).toList();
    }

    @GetMapping("/{lessonId}/versions/{version}")
    VersionResponse version(@PathVariable UUID lessonId, @PathVariable int version) {
        return version(service.getVersion(lessonId, version));
    }

    private ResponseEntity<DraftResponse> draftResponse(LessonDraft value) {
        DraftResponse response = draft(value);
        return ResponseEntity.ok().eTag(etag(response.revision())).body(response);
    }

    private DraftResponse draft(LessonDraft value) {
        return new DraftResponse(value.lessonId(), value.draftId(), value.title(), value.description(),
                value.revision().value(), value.blocks().stream().map(this::block).toList(),
                value.createdAt(), value.updatedAt());
    }

    private VersionResponse version(LessonVersion value) {
        return new VersionResponse(value.lessonId(), value.version(), value.title(), value.description(),
                value.sourceRevision(), value.blocks().stream().map(this::block).toList(), value.publishedAt());
    }

    private BlockResponse block(LessonBlock value) {
        return new BlockResponse(value.blockId(), value.blockType().name(), value.blockSchemaVersion(),
                value.position(), codec.writeConfiguration(value.configuration()));
    }

    private BlockConfiguration configuration(BlockType type, BlockMutationRequest request) {
        return codec.readConfiguration(type, request.configuration());
    }

    private static BlockType type(String value) {
        try { return BlockType.valueOf(value); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unsupported block type"); }
    }

    private static long revision(String value) {
        try { return Long.parseLong(value.replace("\"", "")); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("If-Match must contain a numeric revision"); }
    }

    private static String etag(long revision) { return "\"" + revision + "\""; }
}