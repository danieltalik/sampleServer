package org.example.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PetStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededPets() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Alphonse"))
                .andExpect(jsonPath("$[0].type").value("CAT"));
    }

    @Test
    void getsPetById() throws Exception {
        mockMvc.perform(get("/pets/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Sloopy"));
    }

    @Test
    void returns404ForUnknownOrInvalidId() throws Exception {
        mockMvc.perform(get("/pets/999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/pets/abc")).andExpect(status().isNotFound());
    }

    @Test
    void createsPet() throws Exception {
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nemo\",\"age\":1,\"type\":\"FISH\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/pets/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.type").value("FISH"));
    }

    @Test
    void rejectsInvalidPet() throws Exception {
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"age\":1,\"type\":\"FISH\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesPet() throws Exception {
        mockMvc.perform(put("/pets/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Al\",\"age\":5,\"type\":\"CAT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Al"))
                .andExpect(jsonPath("$.age").value(5));
        mockMvc.perform(put("/pets/999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Al\",\"age\":5,\"type\":\"CAT\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesPet() throws Exception {
        mockMvc.perform(delete("/pets/2")).andExpect(status().isNoContent());
        mockMvc.perform(get("/pets/2")).andExpect(status().isNotFound());
        mockMvc.perform(delete("/pets/2")).andExpect(status().isNotFound());
    }
}
