package com.vetsm.backend;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void authenticatedUserCanCreateResourcesButCannotAccessAnotherUsersResources() throws Exception {
        String ownerToken = registerAndGetToken("owner");
        MvcResult mascotaResult = mockMvc.perform(post("/api/mascotas")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luna","tipo":"Perro","raza":"Mestiza","edad":3,"microchip":"ABC123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andReturn();
        long mascotaId = objectMapper.readTree(mascotaResult.getResponse().getContentAsString())
                .get("id").asLong();

        MvcResult citaResult = mockMvc.perform(post("/api/citas")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fecha":"%s","hora":"10:30:00","motivo":"Control","mascotaId":%d}
                                """.formatted(LocalDate.now().plusDays(1), mascotaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mascota.id").value(mascotaId))
                .andReturn();
        long citaId = objectMapper.readTree(citaResult.getResponse().getContentAsString())
                .get("id").asLong();

        String otherToken = registerAndGetToken("other");
        mockMvc.perform(get("/api/mascotas/{id}", mascotaId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/citas/{id}", citaId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    private String registerAndGetToken(String prefix) throws Exception {
        String email = prefix + System.nanoTime() + "@example.com";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s","email":"%s","password":"password-seguro"}
                                """.formatted(prefix, email)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }
}
