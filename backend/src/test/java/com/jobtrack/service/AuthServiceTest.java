package com.jobtrack.service;

import com.jobtrack.dto.request.LoginRequest;
import com.jobtrack.dto.request.RegisterRequest;
import com.jobtrack.dto.response.AuthResponse;
import com.jobtrack.entity.Role;
import com.jobtrack.entity.User;
import com.jobtrack.exception.EmailAlreadyExistsException;
import com.jobtrack.exception.InvalidCredentialsException;
import com.jobtrack.repository.UserRepository;
import com.jobtrack.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests unitaires")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setFirstName("Jane");
        validRegisterRequest.setLastName("Doe");
        validRegisterRequest.setEmail("jane@example.com");
        validRegisterRequest.setPassword("Password123!");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("jane@example.com");
        validLoginRequest.setPassword("Password123!");

        savedUser = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .password("$2a$10$encodedPassword")
                .role(Role.USER)
                .build();
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() — succès → retourne AuthResponse avec token")
    void register_success_returnsAuthResponse() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = authService.register(validRegisterRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.USER);

        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    @DisplayName("register() — email déjà utilisé → lève EmailAlreadyExistsException")
    void register_emailAlreadyExists_throwsException() {
        // Given
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() — le mot de passe est hashé avec BCrypt")
    void register_passwordIsHashed() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            assertThat(u.getPassword()).isEqualTo("$2a$10$hashedPassword");
            return savedUser;
        });
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        authService.register(validRegisterRequest);

        // Then
        verify(passwordEncoder).encode("Password123!");
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login() — succès → retourne AuthResponse avec token")
    void login_success_returnsAuthResponse() {
        // Given
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(savedUser, null, savedUser.getAuthorities())
        );
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = authService.login(validLoginRequest);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("login() — mauvais mot de passe → lève InvalidCredentialsException")
    void login_badPassword_throwsInvalidCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When / Then
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login() — email inconnu → lève InvalidCredentialsException")
    void login_unknownEmail_throwsInvalidCredentialsException() {
        // Given — AuthManager ne lève pas d'exception mais user n'existe pas en DB
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("unknown@test.com", null)
        );
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
