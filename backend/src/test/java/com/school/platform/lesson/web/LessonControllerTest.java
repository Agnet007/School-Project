package com.school.platform.lesson.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LessonControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void completePublicationFlowPreservesPriorVersion() throws Exception {
        JsonNode created = createLesson("Version one");
        String lessonId = created.required("lessonId").asText();

        JsonNode withBlock = body(mockMvc.perform(post("/api/v1/lessons/{id}/draft/blocks", lessonId)
                        .header("If-Match", "0").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"blockType":"HEADING","configuration":{"text":"First heading","level":2}}
                                """))
                .andExpect(status().isOk()).andExpect(header().string("ETag", "\"1\""))
                .andReturn());
        String blockId = withBlock.at("/blocks/0/blockId").asText();

        mockMvc.perform(post("/api/v1/lessons/{id}/publications", lessonId).header("If-Match", "1"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(patch("/api/v1/lessons/{id}/draft", lessonId).header("If-Match", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Version two draft\",\"description\":\"changed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.revision").value(2));

        mockMvc.perform(post("/api/v1/lessons/{id}/publications", lessonId).header("If-Match", "2"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.version").value(2));
        mockMvc.perform(get("/api/v1/lessons/{id}/versions/1", lessonId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Version one"))
                .andExpect(jsonPath("$.blocks[0].blockId").value(blockId));
        mockMvc.perform(get("/api/v1/lessons/{id}/versions", lessonId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void staleAndUnauthorizedMutationsAreRejected() throws Exception {
        String lessonId = createLesson("Concurrency").required("lessonId").asText();
        mockMvc.perform(patch("/api/v1/lessons/{id}/draft", lessonId).header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Current\",\"description\":\"\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/lessons/{id}/draft", lessonId).header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Stale\",\"description\":\"\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LESSON_REVISION_CONFLICT"));
        mockMvc.perform(patch("/api/v1/lessons/{id}/draft", lessonId)
                        .header("X-Principal-Id", UUID.randomUUID()).header("If-Match", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Unauthorized\",\"description\":\"\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingPermissionsRejectCreateEditAndPublish() throws Exception {
        mockMvc.perform(post("/api/v1/lessons").header("X-Lesson-Permissions", "lesson.read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Denied\",\"description\":\"\"}"))
                .andExpect(status().isForbidden());
        String lessonId = createLesson("Protected").required("lessonId").asText();
        mockMvc.perform(patch("/api/v1/lessons/{id}/draft", lessonId)
                        .header("X-Lesson-Permissions", "lesson.read").header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Denied\",\"description\":\"\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/lessons/{id}/publications", lessonId)
                        .header("X-Lesson-Permissions", "lesson.read").header("If-Match", "0"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unsafeRichTextAndUnknownFieldsAreRejected() throws Exception {
        String lessonId = createLesson("Validation").required("lessonId").asText();
        mockMvc.perform(post("/api/v1/lessons/{id}/draft/blocks", lessonId).header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"blockType":"RICH_TEXT","configuration":{"content":[{"kind":"paragraph","spans":[{"text":"<script>alert(1)</script>","bold":false,"italic":false,"inlineCode":false}],"items":[]}]}}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/lessons/{id}/draft/blocks", lessonId).header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blockType\":\"HEADING\",\"configuration\":{\"text\":\"Hi\",\"level\":2,\"rawHtml\":\"<b>x</b>\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishedVersionHasNoMutationEndpoint() throws Exception {
        String lessonId = createLesson("Immutable").required("lessonId").asText();
        mockMvc.perform(post("/api/v1/lessons/{id}/publications", lessonId).header("If-Match", "0"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/lessons/{id}/versions/1", lessonId)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    private JsonNode createLesson(String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/lessons").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"description\":\"\"}"))
                .andExpect(status().isCreated()).andReturn();
        return body(result);
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}