package org.example.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack tests through MockMvc. Each test runs in a transaction that is rolled back,
 * so the seed data (3 pets) is the same for every test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetStoreControllerTest {

    private static final String VALID_PET = "{\"name\":\"Al\",\"age\":5,\"type\":\"CAT\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededPets() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name").value(contains("Alphonse", "Edward", "Sloopy")));
    }

    @ParameterizedTest(name = "GET /pets/{0} -> {1}")
    @CsvSource({
            "1, Alphonse, 4, CAT",
            "2, Edward,   4, CAT",
            "3, Sloopy,   1, DOG"
    })
    void getsEachSeededPet(int id, String name, int age, String type) throws Exception {
        mockMvc.perform(get("/pets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age))
                .andExpect(jsonPath("$.type").value(type));
    }

    @ParameterizedTest(name = "{0} /pets/{1} -> 404")
    @MethodSource("unknownIdRequests")
    void unknownOrInvalidIdsReturn404(HttpMethod method, String id) throws Exception {
        var request = request(method, "/pets/{id}", id);
        if (method == HttpMethod.PUT) {
            request.contentType(MediaType.APPLICATION_JSON).content(VALID_PET);
        }
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Pet not found"));
    }

    static Stream<Arguments> unknownIdRequests() {
        String[] ids = {"999", "0", "-1", "abc", "1.5", "2147483648"};
        return Stream.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
                .flatMap(method -> Stream.of(ids).map(id -> Arguments.of(method, id)));
    }

    @ParameterizedTest
    @EnumSource(Pet.TypeEnum.class)
    void createsPetOfEveryType(Pet.TypeEnum type) throws Exception {
        // The identity counter isn't rolled back between tests, so read the new id from the response.
        String location = mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"age\":1,\"type\":\"" + type.getValue() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("http://localhost/pets/\\d+")))
                .andExpect(jsonPath("$.id").value(greaterThan(3)))
                .andExpect(jsonPath("$.type").value(type.getValue()))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.type").value(type.getValue()));
    }

    @Test
    void createIgnoresClientSuppliedId() throws Exception {
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Nemo\",\"age\":1,\"type\":\"FISH\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(1)));

        mockMvc.perform(get("/pets/1")).andExpect(jsonPath("$.name").value("Alphonse"));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidPets")
    void rejectsInvalidPetOnCreate(String description, String body) throws Exception {
        mockMvc.perform(post("/pets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/pets")).andExpect(jsonPath("$", hasSize(3)));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidPets")
    void rejectsInvalidPetOnUpdate(String description, String body) throws Exception {
        mockMvc.perform(put("/pets/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/pets/1")).andExpect(jsonPath("$.name").value("Alphonse"));
    }

    static Stream<Arguments> invalidPets() {
        return Stream.of(
                Arguments.of("missing name", "{\"age\":1,\"type\":\"CAT\"}"),
                Arguments.of("missing age", "{\"name\":\"A\",\"type\":\"CAT\"}"),
                Arguments.of("missing type", "{\"name\":\"A\",\"age\":1}"),
                Arguments.of("unknown type", "{\"name\":\"A\",\"age\":1,\"type\":\"BIRD\"}"),
                Arguments.of("lowercase type", "{\"name\":\"A\",\"age\":1,\"type\":\"cat\"}"),
                Arguments.of("age is not a number", "{\"name\":\"A\",\"age\":\"old\",\"type\":\"CAT\"}"),
                Arguments.of("empty object", "{}"),
                Arguments.of("malformed JSON", "{\"name\":"),
                Arguments.of("array instead of object", "[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_XML_VALUE})
    void rejectsNonJsonBodies(String contentType) throws Exception {
        mockMvc.perform(post("/pets").contentType(contentType).content(VALID_PET))
                .andExpect(status().isUnsupportedMediaType());
    }

    @ParameterizedTest(name = "PUT /pets/{0} keeps id {0}")
    @ValueSource(ints = {1, 2, 3})
    void updatesPet(int id) throws Exception {
        mockMvc.perform(put("/pets/{id}", id).contentType(MediaType.APPLICATION_JSON).content(VALID_PET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Al"))
                .andExpect(jsonPath("$.age").value(5));

        mockMvc.perform(get("/pets/{id}", id)).andExpect(jsonPath("$.name").value("Al"));
    }

    @ParameterizedTest(name = "DELETE /pets/{0}")
    @ValueSource(ints = {1, 2, 3})
    void deletesPetOnlyOnce(int id) throws Exception {
        mockMvc.perform(delete("/pets/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/pets/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/pets/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/pets")).andExpect(jsonPath("$", hasSize(2)));
    }
}
