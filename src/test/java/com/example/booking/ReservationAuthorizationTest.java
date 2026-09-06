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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =========================================================
    // SETUP USERS
    // =========================================================

    @BeforeEach
    void setUp() {

        // USER 1
        if (userRepository.findByUsername("reservationuser1").isEmpty()) {

            User user = User.builder()
                    .username("reservationuser1")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }


        // USER 2
        if (userRepository.findByUsername("reservationuser2").isEmpty()) {

            User user = User.builder()
                    .username("reservationuser2")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }


        // ADMIN
        if (userRepository.findByUsername("reservationadmin").isEmpty()) {

            User admin = User.builder()
                    .username("reservationadmin")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
        }
    }


    // =========================================================
    // CREATE A NEW RESOURCE FOR TEST
    // =========================================================

    private Resource createTestResource() {

        Resource resource = Resource.builder()
                .name("Test Resource " + System.nanoTime())
                .description("Resource created for authorization testing")
                .price(new java.math.BigDecimal("100.00"))
                .build();

        return resourceRepository.save(resource);
    }


    // =========================================================
    // LOGIN AND GET JWT
    // =========================================================

    private String getToken(
            String username,
            String password) throws Exception {

        String loginBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

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
    // TEST 1
    // USER CAN CREATE RESERVATION
    // =========================================================

    @Test
    void userShouldBeAbleToCreateReservation()
            throws Exception {

        String token =
                getToken(
                        "reservationuser1",
                        "password123"
                );


        Resource resource = createTestResource();


        String reservationBody = """
                {
                    "resourceId": %d,
                    "startTime": "2027-01-10T10:00:00",
                    "endTime": "2027-01-10T12:00:00"
                }
                """.formatted(resource.getId());


        mockMvc.perform(
                        post("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(reservationBody)
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // TEST 2
    // USER CAN VIEW OWN RESERVATIONS
    // =========================================================

    @Test
    void userShouldBeAbleToViewOwnReservations()
            throws Exception {

        String token =
                getToken(
                        "reservationuser1",
                        "password123"
                );


        mockMvc.perform(
                        get("/api/reservations/my")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // TEST 3
    // USER 2 CANNOT VIEW USER 1 RESERVATION
    // =========================================================

    @Test
    void userShouldNotAccessAnotherUsersReservation()
            throws Exception {

        String user1Token =
                getToken(
                        "reservationuser1",
                        "password123"
                );


        Resource resource = createTestResource();


        String reservationBody = """
                {
                    "resourceId": %d,
                    "startTime": "2027-02-10T10:00:00",
                    "endTime": "2027-02-10T12:00:00"
                }
                """.formatted(resource.getId());


        String response =
                mockMvc.perform(
                                post("/api/reservations")
                                        .header(
                                                "Authorization",
                                                "Bearer " + user1Token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(reservationBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        Long reservationId =
                extractId(response);


        String user2Token =
                getToken(
                        "reservationuser2",
                        "password123"
                );


        mockMvc.perform(
                        get(
                                "/api/reservations/"
                                        + reservationId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + user2Token
                                )
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // TEST 4
    // USER 2 CANNOT CANCEL USER 1 RESERVATION
    // =========================================================

    @Test
    void userShouldNotCancelAnotherUsersReservation()
            throws Exception {

        String user1Token =
                getToken(
                        "reservationuser1",
                        "password123"
                );


        Resource resource = createTestResource();


        String reservationBody = """
                {
                    "resourceId": %d,
                    "startTime": "2027-03-10T10:00:00",
                    "endTime": "2027-03-10T12:00:00"
                }
                """.formatted(resource.getId());


        String response =
                mockMvc.perform(
                                post("/api/reservations")
                                        .header(
                                                "Authorization",
                                                "Bearer " + user1Token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(reservationBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        Long reservationId =
                extractId(response);


        String user2Token =
                getToken(
                        "reservationuser2",
                        "password123"
                );


        mockMvc.perform(
                        put(
                                "/api/reservations/"
                                        + reservationId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + user2Token
                                )
                )
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // TEST 5
    // ADMIN CAN ACCESS ALL RESERVATIONS
    // =========================================================

    @Test
    void adminShouldBeAbleToViewAllReservations()
            throws Exception {

        String adminToken =
                getToken(
                        "reservationadmin",
                        "password123"
                );


        mockMvc.perform(
                        get("/api/reservations")
                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken
                                )
                )
                .andExpect(status().isOk());
    }


    // =========================================================
    // EXTRACT RESERVATION ID
    // =========================================================

    private Long extractId(String response) {

        String idValue =
                response
                        .split("\"id\":")[1]
                        .split(",")[0];

        return Long.parseLong(
                idValue.trim()
        );
    }
}