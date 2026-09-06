package com.example.booking;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

        if (userRepository.findByUsername("testuser").isEmpty()) {

            User user = User.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }
    }

    @Test
    void loginWithValidCredentialsShouldReturnToken()
            throws Exception {

        String requestBody = """
                {
                    "username": "testuser",
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
    @Test
    void loginWithInvalidPasswordShouldReturnBadRequest()
            throws Exception {

        String requestBody = """
            {
                "username": "testuser",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}