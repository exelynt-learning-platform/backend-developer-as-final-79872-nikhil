package com.example.booking;

import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.entity.Resource;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {

        if (userRepository.findByUsername("validationuser").isEmpty()) {

            User user = User.builder()
                    .username("validationuser")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }

        if (resourceRepository.findAll().isEmpty()) {

            Resource resource = Resource.builder()
                    .name("Validation Room")
                    .description("Room for validation tests")
                    .price(new java.math.BigDecimal("100.00"))
                    .build();

            resourceRepository.save(resource);
        }
    }


    // =========================================================
    // LOGIN HELPER
    // =========================================================

    private String login() throws Exception {

        String loginBody = """
                {
                    "username": "validationuser",
                    "password": "password123"
                }
                """;

        String response =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return response
                .replace("{\"token\":\"", "")
                .replace("\"}", "");
    }


    // =========================================================
    // 1. RESOURCE ID IS REQUIRED
    // =========================================================

    @Test
    void reservationWithoutResourceIdShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "startTime": "2026-10-01T10:00:00",
                    "endTime": "2026-10-01T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 2. START TIME IS REQUIRED
    // =========================================================

    @Test
    void reservationWithoutStartTimeShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "resourceId": 1,
                    "endTime": "2026-10-01T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 3. END TIME IS REQUIRED
    // =========================================================

    @Test
    void reservationWithoutEndTimeShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "resourceId": 1,
                    "startTime": "2026-10-01T10:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 4. START TIME MUST BE BEFORE END TIME
    // =========================================================

    @Test
    void startTimeAfterEndTimeShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "resourceId": 1,
                    "startTime": "2026-10-01T14:00:00",
                    "endTime": "2026-10-01T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 5. START TIME AND END TIME CANNOT BE SAME
    // =========================================================

    @Test
    void sameStartAndEndTimeShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "resourceId": 1,
                    "startTime": "2026-10-01T12:00:00",
                    "endTime": "2026-10-01T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 6. RESOURCE ID MUST EXIST
    // =========================================================

    @Test
    void nonExistingResourceShouldReturnBadRequest()
            throws Exception {

        String token = login();

        String requestBody = """
                {
                    "resourceId": 999999,
                    "startTime": "2026-10-01T10:00:00",
                    "endTime": "2026-10-01T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
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