package com.example.userservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Setup
        RegisterRequest request = new RegisterRequest();
        request.setName("testuser");
        request.setEmail("test@test.com");
        request.setPassword("password123");

        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("testuser");
        savedUser.setRole("USER");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");

        // Execute
        AuthResponse response = userService.register(request);

        // Verify
        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("testuser", response.getName());
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setName("existing");
        request.setEmail("test@test.com");
        request.setPassword("pass");

        when(userRepository.existsByName(anyString())).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.register(request);
        });

        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("newuser");
        request.setEmail("existing@test.com");
        request.setPassword("pass");

        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.register(request);
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setName("testuser");
        request.setPassword("password");

        User user = new User();
        user.setName("testuser");
        user.setPassword("hashedPassword");
        user.setRole("USER");

        when(userRepository.findByName(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("login-token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("login-token", response.getToken());
    }

    @Test
    void testLoginUser_NotFound() {
        AuthRequest request = new AuthRequest();
        request.setName("nonexistent");
        request.setPassword("pass");

        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            userService.login(request);
        });

        assertEquals("User not found", exception.getMessage());
    }
}