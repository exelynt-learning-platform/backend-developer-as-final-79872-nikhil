package com.example.booking;

import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    // =========================================================
    // TEST SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        createUserIfNotExists(
                "resourceuser",
                "password123",
                Role.USER
        );

        createUserIfNotExists(
                "resourceadmin",
                "password123",
                Role.ADMIN
        );
    }


    // =========================================================
    // CREATE TEST USER
    // =========================================================

    private void createUserIfNotExists(
            String username,
            String password,
            Role role) {

        if (userRepository.findByUsername(username).isEmpty()) {

            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .build();

            userRepository.save(user);
        }
    }


    // =========================================================
    // LOGIN HELPER
    // =========================================================

    private String login(
            String username,
            String password)
            throws Exception {

        String requestBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode jsonNode =
                objectMapper.readTree(responseBody);

        return jsonNode
                .get("token")
                .asText();
    }


    // =========================================================
    // 1. USER CAN VIEW ALL RESOURCES
    // =========================================================

    @Test
    void userShouldBeAbleToViewResources()
            throws Exception {

        String token =
                login(
                        "resourceuser",
                        "password123"
                );

        mockMvc.perform(
                        get("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // 2. ADMIN CAN VIEW ALL RESOURCES
    // =========================================================

    @Test
    void adminShouldBeAbleToViewResources()
            throws Exception {

        String token =
                login(
                        "resourceadmin",
                        "password123"
                );

        mockMvc.perform(
                        get("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // 3. USER CANNOT CREATE RESOURCE
    // =========================================================

    @Test
    void userShouldNotBeAbleToCreateResource()
            throws Exception {

        String token =
                login(
                        "resourceuser",
                        "password123"
                );

        String requestBody = """
                {
                    "name": "Test Room",
                    "description": "Test resource",
                    "price": 100
                }
                """;

        mockMvc.perform(
                        post("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // 4. ADMIN CAN CREATE RESOURCE
    // =========================================================

    @Test
    void adminShouldBeAbleToCreateResource()
            throws Exception {

        String token =
                login(
                        "resourceadmin",
                        "password123"
                );

        String requestBody = """
                {
                    "name": "Test Room",
                    "description": "Test resource",
                    "price": 100
                }
                """;

        mockMvc.perform(
                        post("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // 5. USER CANNOT UPDATE RESOURCE
    // =========================================================

    @Test
    void userShouldNotBeAbleToUpdateResource()
            throws Exception {

        String token =
                login(
                        "resourceuser",
                        "password123"
                );

        String requestBody = """
                {
                    "name": "Updated Room",
                    "description": "Updated resource",
                    "price": 200
                }
                """;

        mockMvc.perform(
                        put("/api/resources/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // 6. ADMIN CAN UPDATE RESOURCE
    // =========================================================

    @Test
    void adminShouldBeAbleToUpdateResource()
            throws Exception {

        String token =
                login(
                        "resourceadmin",
                        "password123"
                );

        String requestBody = """
                {
                    "name": "Updated Room",
                    "description": "Updated resource",
                    "price": 200
                }
                """;

        // First create a resource
        MvcResult createResult =
                mockMvc.perform(
                                post("/api/resources")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                    "name": "Original Room",
                                                    "description": "Original resource",
                                                    "price": 100
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode createdResource =
                objectMapper.readTree(
                        createResult
                                .getResponse()
                                .getContentAsString()
                );

        Long resourceId =
                createdResource
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        put("/api/resources/" + resourceId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // 7. USER CANNOT DELETE RESOURCE
    // =========================================================

    @Test
    void userShouldNotBeAbleToDeleteResource()
            throws Exception {

        String token =
                login(
                        "resourceuser",
                        "password123"
                );

        mockMvc.perform(
                        delete("/api/resources/1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // 8. ADMIN CAN DELETE RESOURCE
    // =========================================================

    @Test
    void adminShouldBeAbleToDeleteResource()
            throws Exception {

        String token =
                login(
                        "resourceadmin",
                        "password123"
                );

        // Create resource first
        MvcResult createResult =
                mockMvc.perform(
                                post("/api/resources")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                    "name": "Delete Room",
                                                    "description": "Resource for delete test",
                                                    "price": 150
                                                }
                                                """)
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode createdResource =
                objectMapper.readTree(
                        createResult
                                .getResponse()
                                .getContentAsString()
                );

        Long resourceId =
                createdResource
                        .get("id")
                        .asLong();

        mockMvc.perform(
                        delete("/api/resources/" + resourceId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // 9. INVALID RESOURCE REQUEST
    // =========================================================

    @Test
    void invalidResourceRequestShouldReturnBadRequest()
            throws Exception {

        String token =
                login(
                        "resourceadmin",
                        "password123"
                );

        String requestBody = """
                {
                    "name": "",
                    "description": "",
                    "price": 0
                }
                """;

        mockMvc.perform(
                        post("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}