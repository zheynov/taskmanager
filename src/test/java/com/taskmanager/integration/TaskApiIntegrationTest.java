package com.taskmanager.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TaskApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("task_manager")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        // PostgreSQL + Flyway + Security
    }

    @Test
    void createTask_updateDelete_andOtherUserCannotModify() throws Exception {

        register("svetlana_nova", "svetlana@test.local", "password1");
        register("boris_morozov", "boris@test.local", "password1");

        long svetlanaId = userRepository.findByUsername("svetlana_nova").orElseThrow().getId();
        long borisId = userRepository.findByUsername("boris_morozov").orElseThrow().getId();

        String tokenSvetlana = login("svetlana_nova", "password1");
        String tokenBoris = login("boris_morozov", "password1");

        String createJson = """
                {"title":"Draft","description":"Work","status":"TODO","priority":"HIGH","assigneeId":%d}
                """
                .formatted(borisId);

        // 201
        String body = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + tokenSvetlana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Draft"))
                .andExpect(jsonPath("$.authorId").value(svetlanaId))
                .andExpect(jsonPath("$.assigneeId").value(borisId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long taskId = objectMapper.readTree(body).get("id").asLong();

        // 200
        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", "Bearer " + tokenBoris))
                .andExpect(status().isOk());

        // 403
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenBoris)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Hacked","description":"x","status":"DONE","priority":"LOW","assigneeId":null}
                                """))
                .andExpect(status().isForbidden());

        // 403
        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", "Bearer " + tokenBoris))
                .andExpect(status().isForbidden());

        // 200
        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + tokenSvetlana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Done work","description":"ok","status":"DONE","priority":"MEDIUM","assigneeId":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Done work"))
                .andExpect(jsonPath("$.status").value("DONE"));

        // 204
        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", "Bearer " + tokenSvetlana))
                .andExpect(status().isNoContent());
    }

    @Test
    void listTasks_withFilters() throws Exception {

        register("marina_orlova", "marina@test.local", "password1");
        register("dmitriy_kuznetsov", "dmitriy@test.local", "password1");

        long marinaId = userRepository.findByUsername("marina_orlova").orElseThrow().getId();
        long dmitriyId = userRepository.findByUsername("dmitriy_kuznetsov").orElseThrow().getId();

        String marinaToken = login("marina_orlova", "password1");

        String createJson = """
                {"title":"Filtered","description":"","status":"IN_PROGRESS","priority":"LOW","assigneeId":%d}
                """
                .formatted(dmitriyId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + marinaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks")
                        .param("authorId", String.valueOf(marinaId))
                        .header("Authorization", "Bearer " + marinaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Filtered"));

        mockMvc.perform(get("/api/tasks")
                        .param("status", "IN_PROGRESS")
                        .param("authorId", String.valueOf(marinaId))
                        .header("Authorization", "Bearer " + marinaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/tasks")
                        .param("authorId", String.valueOf(marinaId))
                        .header("Authorization", "Bearer " + marinaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorId").value(marinaId));

        mockMvc.perform(get("/api/tasks")
                        .param("assigneeId", String.valueOf(dmitriyId))
                        .param("authorId", String.valueOf(marinaId))
                        .header("Authorization", "Bearer " + marinaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assigneeId").value(dmitriyId));
    }

    private void register(String username, String email, String password) throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "email", email, "password", password))))
                .andExpect(status().isCreated());

        userRepository.flush();
    }

    private String login(String username, String password) throws Exception {

        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("token").asText();
    }
}
