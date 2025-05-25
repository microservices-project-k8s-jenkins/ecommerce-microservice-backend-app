package com.selimhorri.app.unit;

import com.selimhorri.app.service.impl.UserServiceImpl;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceUnitTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

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
    void testSaveUser() {
        UserDto userDto = buildValidUser("test@mail.com");
        when(userRepository.save(any())).thenReturn(UserMappingHelper.map(userDto));
        UserDto result = userService.save(userDto);
        assertNotNull(result);
        assertEquals("test@mail.com", result.getEmail());
    }
    @Test
    void testFindUserById() {
        UserDto userDto = buildValidUser("find@mail.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(UserMappingHelper.map(userDto)));
        UserDto result = userService.findById(1);
        assertNotNull(result);
        assertEquals("find@mail.com", result.getEmail());
    }
    @Test
    void testUpdateUser() {
        UserDto userDto = buildValidUser("update@mail.com");
        when(userRepository.save(any())).thenReturn(UserMappingHelper.map(userDto));
        UserDto updated = userService.update(userDto);
        assertEquals("update@mail.com", updated.getEmail());
    }
    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1);
        assertDoesNotThrow(() -> userService.deleteById(1));
    }
    @Test
    void testListUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(UserMappingHelper.map(buildValidUser("list@mail.com"))));
        assertFalse(userService.findAll().isEmpty());
    }
} 