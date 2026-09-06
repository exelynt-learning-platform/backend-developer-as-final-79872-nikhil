package com.example.booking;

import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {

        // Create test USER
        if (userRepository.findByUsername("rbacuser").isEmpty()) {

            User user = User.builder()
                    .username("rbacuser")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }


        // Create test ADMIN
        if (userRepository.findByUsername("rbacadmin").isEmpty()) {

            User admin = User.builder()
                    .username("rbacadmin")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
        }
    }


    // =========================================================
    // USER LOGIN
    // =========================================================

    @Test
    void userLoginShouldReturnToken() throws Exception {

        String requestBody = """
                {
                    "username": "rbacuser",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    // =========================================================
    // ADMIN LOGIN
    // =========================================================

    @Test
    void adminLoginShouldReturnToken() throws Exception {

        String requestBody = """
                {
                    "username": "rbacadmin",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    // =========================================================
    // USER CANNOT CREATE RESOURCE
    // =========================================================

    @Test
    void userShouldNotBeAllowedToCreateResource()
            throws Exception {

        String loginBody = """
                {
                    "username": "rbacuser",
                    "password": "password123"
                }
                """;

        String token =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        // Extract JWT from response
        String jwt = token
                .replace("{\"token\":\"", "")
                .replace("\"}", "");

        String resourceBody = """
                {
                    "name": "Test Resource",
                    "description": "RBAC test resource",
                    "price": 100.00
                }
                """;

        mockMvc.perform(
                        post("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwt
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resourceBody)
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // USER CAN VIEW RESOURCES
    // =========================================================

    @Test
    void userShouldBeAllowedToViewResources()
            throws Exception {

        String loginBody = """
                {
                    "username": "rbacuser",
                    "password": "password123"
                }
                """;

        String token =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String jwt = token
                .replace("{\"token\":\"", "")
                .replace("\"}", "");

        mockMvc.perform(
                        get("/api/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwt
                                )
                )
                .andExpect(status().isOk());
    }
}