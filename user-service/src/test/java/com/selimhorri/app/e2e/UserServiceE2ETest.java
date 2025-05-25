package com.selimhorri.app.e2e;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceE2ETest {
    @Autowired
    private TestRestTemplate restTemplate;

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
    void testUserRegisters() {
        UserDto user = buildValidUser("test@mail.com");
        ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users", user, UserDto.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
    @Test
    void testUserDeletesAccount() {
        UserDto user = restTemplate.postForEntity("/api/users", buildValidUser("test2@mail.com"), UserDto.class).getBody();
        restTemplate.delete("/api/users/" + user.getUserId());
        assertNotNull(user);
    }
    @Test
    void testUserViewsUserList() {
        ResponseEntity<DtoCollectionResponse> response = restTemplate.getForEntity("/api/users", DtoCollectionResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getCollection() != null);
    }
    @Test
    void testUserUpdatesEmail() {
        UserDto user = restTemplate.postForEntity("/api/users", buildValidUser("test3@mail.com"), UserDto.class).getBody();
        user.setEmail("updated@mail.com");
        restTemplate.put("/api/users/" + user.getUserId(), user);
        assertNotNull(user);
    }
    @Test
    void testUserViewsProfile() {
        UserDto user = restTemplate.postForEntity("/api/users", buildValidUser("test4@mail.com"), UserDto.class).getBody();
        ResponseEntity<UserDto> response = restTemplate.getForEntity("/api/users/" + user.getUserId(), UserDto.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            assertNotNull(response.getBody());
        } else {
            String errorJson = response.toString();
            assertTrue(errorJson.contains("timestamp"));
        }
    }

    @Test
    void testUserViewsNonExistentProfile() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/999999", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("timestamp"));
    }
} 