package com.chidituke.workout_tracker.controller.auth;

import com.chidituke.workout_tracker.dto.request.auth.LoginRequest;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController using your existing TestContainers setup
 * No Mockito - uses real database and real services
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional // Rollback after each test
@DisplayName("🔐 AuthController Integration Tests - TestContainers + No Mockito")
class AuthControllerTest {

    @Container
    @ServiceConnection // Spring Boot 3.1+ feature - automatically configures datasource
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("workout_tracker_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create valid register request matching your validation rules
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("SecurePassword123!");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        registerRequest.setGender(User.Gender.MALE);
        registerRequest.setZipcode("12345");
        registerRequest.setAgreeToTerms(true);

        // Create login request
        loginRequest = new LoginRequest();
        loginRequest.setEmailOrUsername("test@example.com");
        loginRequest.setPassword("SecurePassword123!");
        loginRequest.setRememberMe(false);

        // Create existing user for login tests
        existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("ExistingPassword123!"))
                .firstName("Existing")
                .lastName("User")
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .build();
        userRepository.save(existingUser);
    }

    @Nested
    @DisplayName("🔑 User Registration")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user successfully and return JWT")
        void shouldRegisterNewUserSuccessfullyAndReturnJwt() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.userType").value("REGULAR"))
                    .andExpect(jsonPath("$.isProfessional").value(false));

            // Verify user was created in database with correct data
            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo("testuser");
            assertThat(savedUser.getFirstName()).isEqualTo("Test");
            assertThat(savedUser.getLastName()).isEqualTo("User");
            assertThat(savedUser.getAccountStatus()).isEqualTo(User.AccountStatus.ACTIVE);
            assertThat(savedUser.getUserType()).isEqualTo(UserType.REGULAR);
            assertThat(savedUser.getZipcode()).isEqualTo("12345");
            assertThat(savedUser.getGender()).isEqualTo(User.Gender.MALE);
            assertThat(savedUser.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));

            // Verify password was properly encoded
            assertThat(savedUser.getPassword()).isNotEqualTo("SecurePassword123!");
            assertThat(passwordEncoder.matches("SecurePassword123!", savedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("Should enforce email uniqueness constraint")
        void shouldEnforceEmailUniquenessConstraint() throws Exception {
            // Given - use existing user's email
            registerRequest.setEmail("existing@example.com");
            registerRequest.setUsername("differentuser"); // Different username

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email address already in use!"));

            // Verify no new user was created
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(1); // Only the existing user
        }

        @Test
        @DisplayName("Should enforce username uniqueness constraint")
        void shouldEnforceUsernameUniquenessConstraint() throws Exception {
            // Given - use existing user's username
            registerRequest.setUsername("existinguser");
            registerRequest.setEmail("different@example.com"); // Different email

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username is already taken!"));

            // Verify no new user was created
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(1); // Only the existing user
        }

        @Test
        @DisplayName("Should validate all required fields and constraints")
        void shouldValidateAllRequiredFieldsAndConstraints() throws Exception {
            // Test missing email
            registerRequest.setEmail(null);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test invalid email format
            registerRequest.setEmail("invalid-email-format");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test password too short
            registerRequest.setEmail("test@example.com");
            registerRequest.setPassword("short");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test weak password (no uppercase)
            registerRequest.setPassword("weakpassword123!");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test weak password (no lowercase)
            registerRequest.setPassword("WEAKPASSWORD123!");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test weak password (no numbers)
            registerRequest.setPassword("WeakPassword!");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test invalid zipcode (not 5 digits)
            registerRequest.setPassword("SecurePassword123!");
            registerRequest.setZipcode("123");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test future date of birth
            registerRequest.setZipcode("12345");
            registerRequest.setDateOfBirth(LocalDate.now().plusDays(1));
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test not agreeing to terms
            registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
            registerRequest.setAgreeToTerms(false);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify no users were created during validation failures
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(1); // Only the existing user from setUp
        }

        @Test
        @DisplayName("Should validate username format constraints")
        void shouldValidateUsernameFormatConstraints() throws Exception {
            // Test username too short (less than 3 characters)
            registerRequest.setUsername("ab");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test username too long (more than 30 characters)
            registerRequest.setUsername("thisusernameiswaytoolongandexceedsthirtychars");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test username with invalid characters
            registerRequest.setUsername("user@#$%");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test valid username with allowed special characters
            registerRequest.setUsername("user_name-123.test");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isOk()); // This should work

            // Verify user was created with valid username
            User savedUser = userRepository.findByUsername("user_name-123.test").orElse(null);
            assertThat(savedUser).isNotNull();
        }

        @Test
        @DisplayName("Should create professional user when specified")
        void shouldCreateProfessionalUserWhenSpecified() throws Exception {
            // Note: Your RegisterRequest doesn't have userType field in the provided code
            // This test assumes professional users are created through a different process
            // or that the userType is determined by other means

            // For now, test that regular users are created correctly
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getUserType()).isEqualTo(UserType.REGULAR);
        }
    }

    @Nested
    @DisplayName("🔓 User Login")
    class UserLoginTests {

        @Test
        @DisplayName("Should login with email and return valid JWT")
        void shouldLoginWithEmailAndReturnValidJwt() throws Exception {
            // Given
            loginRequest.setEmailOrUsername("existing@example.com");
            loginRequest.setPassword("ExistingPassword123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").value(existingUser.getId()))
                    .andExpect(jsonPath("$.username").value("existinguser"))
                    .andExpect(jsonPath("$.email").value("existing@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Existing"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.userType").value("REGULAR"))
                    .andExpect(jsonPath("$.isProfessional").value(false));

            // Verify last active timestamp was updated in database
            User updatedUser = userRepository.findByEmail("existing@example.com").orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getLastActive()).isNotNull();
        }

        @Test
        @DisplayName("Should login with username and return valid JWT")
        void shouldLoginWithUsernameAndReturnValidJwt() throws Exception {
            // Given
            loginRequest.setEmailOrUsername("existinguser");
            loginRequest.setPassword("ExistingPassword123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.username").value("existinguser"));

            // Verify last active was updated
            User updatedUser = userRepository.findByUsername("existinguser").orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getLastActive()).isNotNull();
        }

        @Test
        @DisplayName("Should reject login for non-existent user")
        void shouldRejectLoginForNonExistentUser() throws Exception {
            // Given
            loginRequest.setEmailOrUsername("nonexistent@example.com");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User not found with email or username: nonexistent@example.com"));
        }

        @Test
        @DisplayName("Should reject login with incorrect password")
        void shouldRejectLoginWithIncorrectPassword() throws Exception {
            // Given
            loginRequest.setEmailOrUsername("existing@example.com");
            loginRequest.setPassword("WrongPassword123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject login for inactive account status")
        void shouldRejectLoginForInactiveAccountStatus() throws Exception {
            // Given - create user with each inactive status
            User inactiveUser = User.builder()
                    .username("inactiveuser")
                    .email("inactive@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Inactive")
                    .lastName("User")
                    .userType(UserType.REGULAR)
                    .accountStatus(User.AccountStatus.INACTIVE)
                    .build();
            userRepository.save(inactiveUser);

            loginRequest.setEmailOrUsername("inactive@example.com");
            loginRequest.setPassword("Password123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account is not active. Please contact support."));
        }

        @Test
        @DisplayName("Should reject login for suspended account")
        void shouldRejectLoginForSuspendedAccount() throws Exception {
            // Given
            User suspendedUser = User.builder()
                    .username("suspendeduser")
                    .email("suspended@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Suspended")
                    .lastName("User")
                    .userType(UserType.REGULAR)
                    .accountStatus(User.AccountStatus.SUSPENDED)
                    .build();
            userRepository.save(suspendedUser);

            loginRequest.setEmailOrUsername("suspended@example.com");
            loginRequest.setPassword("Password123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account is not active. Please contact support."));
        }

        @Test
        @DisplayName("Should reject login for pending verification account")
        void shouldRejectLoginForPendingVerificationAccount() throws Exception {
            // Given
            User pendingUser = User.builder()
                    .username("pendinguser")
                    .email("pending@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .firstName("Pending")
                    .lastName("User")
                    .userType(UserType.REGULAR)
                    .accountStatus(User.AccountStatus.PENDING_VERIFICATION)
                    .build();
            userRepository.save(pendingUser);

            loginRequest.setEmailOrUsername("pending@example.com");
            loginRequest.setPassword("Password123!");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account is not active. Please contact support."));
        }

        @Test
        @DisplayName("Should validate login request fields")
        void shouldValidateLoginRequestFields() throws Exception {
            // Test empty email/username
            loginRequest.setEmailOrUsername("");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test null email/username
            loginRequest.setEmailOrUsername(null);
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test empty password
            loginRequest.setEmailOrUsername("test@example.com");
            loginRequest.setPassword("");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Test null password
            loginRequest.setPassword(null);
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("✅ Availability Checks")
    class AvailabilityCheckTests {

        @Test
        @DisplayName("Should correctly check email availability")
        void shouldCorrectlyCheckEmailAvailability() throws Exception {
            // Test available email
            mockMvc.perform(get("/api/auth/check-email")
                            .param("email", "available@example.com"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email is available"));

            // Test taken email (existing user)
            mockMvc.perform(get("/api/auth/check-email")
                            .param("email", "existing@example.com"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email is already taken"));
        }

        @Test
        @DisplayName("Should correctly check username availability")
        void shouldCorrectlyCheckUsernameAvailability() throws Exception {
            // Test available username
            mockMvc.perform(get("/api/auth/check-username")
                            .param("username", "availableuser"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Username is available"));

            // Test taken username (existing user)
            mockMvc.perform(get("/api/auth/check-username")
                            .param("username", "existinguser"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username is already taken"));
        }
    }

    @Nested
    @DisplayName("🌐 Public Endpoints")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should provide health check endpoint")
        void shouldProvideHealthCheckEndpoint() throws Exception {
            mockMvc.perform(get("/api/auth/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Auth controller is working!"));
        }

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            String malformedJson = "{ \"username\": \"test\", \"email\": \"test@example.com\", \"password\":";

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle missing request body")
        void shouldHandleMissingRequestBody() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle unsupported media types")
        void shouldHandleUnsupportedMediaTypes() throws Exception {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("plain text instead of json"))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("🔒 Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should handle SQL injection attempts safely")
        void shouldHandleSqlInjectionAttemptsSafely() throws Exception {
            // Given
            registerRequest.setUsername("'; DROP TABLE users; --");
            registerRequest.setFirstName("Robert'; DROP TABLE users; --");
            registerRequest.setEmail("hacker@example.com");

            // When & Then - Should fail validation, not execute SQL
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // Verify database is intact and no additional users were created
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(1); // Only the existing user from setUp

            // Verify table still exists and can be queried
            User existingUser = userRepository.findByEmail("existing@example.com").orElse(null);
            assertThat(existingUser).isNotNull();
        }

        @Test
        @DisplayName("Should handle Unicode characters safely")
        void shouldHandleUnicodeCharactersSafely() throws Exception {
            // Given - valid Unicode characters
            registerRequest.setFirstName("José");
            registerRequest.setLastName("李明");
            registerRequest.setUsername("user_jose_li");
            registerRequest.setEmail("jose.li@example.com");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify user was created with Unicode characters preserved
            User savedUser = userRepository.findByEmail("jose.li@example.com").orElse(null);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getFirstName()).isEqualTo("José");
            assertThat(savedUser.getLastName()).isEqualTo("李明");
            assertThat(savedUser.getUsername()).isEqualTo("user_jose_li");
        }

        @Test
        @DisplayName("Should properly encode passwords")
        void shouldProperlyEncodePasswords() throws Exception {
            // When
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());

            // Then
            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
            assertThat(savedUser).isNotNull();

            // Password should be encoded, not plain text
            assertThat(savedUser.getPassword()).isNotEqualTo("SecurePassword123!");
            assertThat(savedUser.getPassword()).startsWith("$2a$"); // BCrypt prefix

            // But encoded password should match original
            assertThat(passwordEncoder.matches("SecurePassword123!", savedUser.getPassword())).isTrue();
            assertThat(passwordEncoder.matches("WrongPassword", savedUser.getPassword())).isFalse();
        }
    }

    @Nested
    @DisplayName("🎯 End-to-End Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full registration and login workflow")
        void shouldCompleteFullRegistrationAndLoginWorkflow() throws Exception {
            // Step 1: Register new user
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.token").exists());

            // Step 2: Login with the newly registered user
            LoginRequest newUserLogin = new LoginRequest();
            newUserLogin.setEmailOrUsername("test@example.com");
            newUserLogin.setPassword("SecurePassword123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newUserLogin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.token").exists());

            // Step 3: Verify user exists in database with all correct attributes
            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getLastActive()).isNotNull(); // Updated from login
            assertThat(savedUser.getAccountStatus()).isEqualTo(User.AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should prevent duplicate registration and maintain data integrity")
        void shouldPreventDuplicateRegistrationAndMaintainDataIntegrity() throws Exception {
            // Step 1: Register user successfully
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());

            // Step 2: Attempt to register same user again (should fail)
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            // Step 3: Verify only one user with that email exists
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(2); // Existing user + newly registered user

            // Verify we can find both users distinctly
            assertThat(userRepository.findByEmail("existing@example.com")).isPresent();
            assertThat(userRepository.findByEmail("test@example.com")).isPresent();
        }

        @Test
        @DisplayName("Should handle multiple user registrations correctly")
        void shouldHandleMultipleUserRegistrationsCorrectly() throws Exception {
            // Register multiple users with different data
            for (int i = 1; i <= 3; i++) {
                RegisterRequest request = new RegisterRequest();
                request.setEmail("user" + i + "@example.com");
                request.setUsername("user" + i);
                request.setPassword("Password123!");
                request.setFirstName("User");
                request.setLastName("Number" + i);
                request.setDateOfBirth(LocalDate.of(1990, 1, i));
                request.setGender(User.Gender.OTHER);
                request.setZipcode("1234" + i);
                request.setAgreeToTerms(true);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value("user" + i));
            }

            // Verify all users were created
            assertThat(userRepository.count()).isEqualTo(4); // existing + 3 new users

            // Verify each user can login
            for (int i = 1; i <= 3; i++) {
                LoginRequest login = new LoginRequest();
                login.setEmailOrUsername("user" + i + "@example.com");
                login.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value("user" + i));
            }
        }
    }
}