package com.selimhorri.app.integration;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.HttpStatus;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private UserDto buildValidUser(String email) {
        CredentialDto cred = CredentialDto.builder()
            .username("testuser")
            .password("1234")
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        return UserDto.builder()
            .firstName("Test")
            .lastName("User")
            .email(email)
            .credentialDto(cred)
            .build();
    }

    @Test
    void testCreateUser() throws Exception {
        UserDto userDto = buildValidUser("test@mail.com");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }
    @Test
    void testGetUserById() throws Exception {
        User user = userRepository.save(new User());
        mockMvc.perform(get("/api/users/" + user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getUserId()));
    }
    @Test
    void testGetNonExistentUserById() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void testListUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").exists());
    }
    @Test
    void testUpdateUser() throws Exception {
        User user = userRepository.save(new User());
        UserDto userDto = buildValidUser("updated@mail.com");
        userDto.setUserId(user.getUserId());
        mockMvc.perform(put("/api/users/" + user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }
    @Test
    void testDeleteUser() throws Exception {
        User user = userRepository.save(new User());
        mockMvc.perform(delete("/api/users/" + user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
} 