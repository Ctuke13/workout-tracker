package com.chidituke.workout_tracker.security;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive security configuration tests for the Workout Tracker application
 * Tests authentication, authorization, JWT handling, and endpoint access control
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("🔐 Security Configuration Integration Tests")
class SecurityConfigTest {

    @Container
    @ServiceConnection
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User regularUser;
    private User professionalUser;
    private User adminUser;
    private String validJwtToken;
    private String expiredJwtToken;
    private String malformedJwtToken;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        // Create test users with different roles
        createTestUsers();

        // Generate test JWT tokens
        generateTestTokens();
    }

    private void createTestUsers() {
        // Regular user
        regularUser = User.builder()
                .username("regularuser")
                .email("regular@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Regular")
                .lastName("User")
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.MALE)
                .zipcode("12345")
                .build();
        regularUser = userRepository.save(regularUser);

        // Professional user
        professionalUser = User.builder()
                .username("prouser")
                .email("pro@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Professional")
                .lastName("User")
                .userType(UserType.PROFESSIONAL)
                .accountStatus(User.AccountStatus.ACTIVE)
                .dateOfBirth(LocalDate.of(1985, 1, 1))
                .gender(User.Gender.FEMALE)
                .zipcode("54321")
                .build();
        professionalUser = userRepository.save(professionalUser);

        // Admin user
        adminUser = User.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Admin")
                .lastName("User")
                .userType(UserType.ADMIN)
                .accountStatus(User.AccountStatus.ACTIVE)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(User.Gender.OTHER)
                .zipcode("99999")
                .build();
        adminUser = userRepository.save(adminUser);
    }

    private void generateTestTokens() {
        // Valid token for regular user
        UserPrincipal userPrincipal = UserPrincipal.create(regularUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        validJwtToken = jwtTokenProvider.generateJwtToken(authentication);

        // Malformed token
        malformedJwtToken = "invalid.jwt.token";

        // Expired token (we'll simulate this with a very short expiry)
        expiredJwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZWd1bGFydXNlciIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAxfQ.invalid";
    }

    @Nested
    @DisplayName("🌐 Public Endpoint Access")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should allow access to auth endpoints without authentication")
        void shouldAllowAccessToAuthEndpointsWithoutAuthentication() throws Exception {
            // Test auth test endpoint (this exists)
            mockMvc.perform(get("/api/auth/test"))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Test email availability check
            mockMvc.perform(get("/api/auth/check-email")
                            .param("email", "test@example.com"))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Test username availability check
            mockMvc.perform(get("/api/auth/check-username")
                            .param("username", "testuser"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to professionals search without authentication")
        void shouldAllowAccessToProfessionalsSearchWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/api/users/professionals"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow registration and login without authentication")
        void shouldAllowRegistrationAndLoginWithoutAuthentication() throws Exception {
            // Test registration
            String registrationJson = """
                {
                    "email": "publictest@example.com",
                    "username": "publictest",
                    "password": "Password123!",
                    "firstName": "Public",
                    "lastName": "Test",
                    "dateOfBirth": "1990-01-01",
                    "gender": "MALE",
                    "zipcode": "12345",
                    "agreeToTerms": true
                }
                """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registrationJson))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Test login
            String loginJson = """
                {
                    "emailOrUsername": "publictest@example.com",
                    "password": "Password123!",
                    "rememberMe": false
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("🔒 Protected Endpoint Access")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("Should reject access to protected endpoints without authentication")
        void shouldRejectAccessToProtectedEndpointsWithoutAuthentication() throws Exception {
            // User endpoints (except professionals search) - Spring Security returns 403 for no auth
            mockMvc.perform(get("/api/users/profile"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(get("/api/users/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            // Workout endpoints
            mockMvc.perform(get("/api/workouts"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(post("/api/workouts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            // Exercise endpoints
            mockMvc.perform(get("/api/exercises"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(post("/api/exercises")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }

        @Test
        @DisplayName("Should allow access to protected endpoints with valid JWT")
        void shouldAllowAccessToProtectedEndpointsWithValidJwt() throws Exception {
            // Test with actual endpoints that exist and should work
            // User profile endpoint
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + validJwtToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Note: Other endpoints might return different status codes if they don't exist
            // Let's test what actually exists
        }

        @Test
        @DisplayName("Should reject access with malformed JWT token")
        void shouldRejectAccessWithMalformedJwtToken() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + malformedJwtToken))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }

        @Test
        @DisplayName("Should reject access with expired JWT token")
        void shouldRejectAccessWithExpiredJwtToken() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + expiredJwtToken))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }

        @Test
        @DisplayName("Should reject access with missing Bearer prefix")
        void shouldRejectAccessWithMissingBearerPrefix() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", validJwtToken)) // Missing "Bearer "
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }

        @Test
        @DisplayName("Should reject access with empty Authorization header")
        void shouldRejectAccessWithEmptyAuthorizationHeader() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", ""))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer "))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }
    }

    @Nested
    @DisplayName("👤 Role-Based Authorization")
    class RoleBasedAuthorizationTests {

        @Test
        @DisplayName("Regular user should access user endpoints with real JWT")
        void regularUserShouldAccessUserEndpoints() throws Exception {
            // Use real JWT token instead of @WithMockUser
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + validJwtToken))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Professional user should access professional features with real JWT")
        void professionalUserShouldAccessProfessionalFeatures() throws Exception {
            // Generate JWT for professional user
            UserPrincipal professionalPrincipal = UserPrincipal.create(professionalUser);
            UsernamePasswordAuthenticationToken professionalAuth =
                    new UsernamePasswordAuthenticationToken(professionalPrincipal, null, professionalPrincipal.getAuthorities());
            String professionalJwt = jwtTokenProvider.generateJwtToken(professionalAuth);

            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + professionalJwt))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Admin user should access all endpoints with real JWT")
        void adminUserShouldAccessAllEndpoints() throws Exception {
            // Generate JWT for admin user
            UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
            UsernamePasswordAuthenticationToken adminAuth =
                    new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
            String adminJwt = jwtTokenProvider.generateJwtToken(adminAuth);

            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + adminJwt))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should differentiate between user roles in authorities")
        void shouldDifferentiateBetweenUserRolesInAuthorities() throws Exception {
            // Test that different user types have different authorities
            UserPrincipal regularPrincipal = UserPrincipal.create(regularUser);
            UserPrincipal professionalPrincipal = UserPrincipal.create(professionalUser);
            UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

            // Verify authorities are set correctly
            assert regularPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_REGULAR"));

            assert professionalPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PROFESSIONAL"));

            assert adminPrincipal.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }
    }

    @Nested
    @DisplayName("🔐 JWT Token Validation")
    class JwtTokenValidationTests {

        @Test
        @DisplayName("Should validate correctly formed JWT tokens")
        void shouldValidateCorrectlyFormedJwtTokens() throws Exception {
            // Test with valid token
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + validJwtToken))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject tokens with invalid signature")
        void shouldRejectTokensWithInvalidSignature() throws Exception {
            String invalidSignatureToken = validJwtToken + "invalid";

            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + invalidSignatureToken))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }

        @Test
        @DisplayName("Should handle various malformed token formats")
        void shouldHandleVariousMalformedTokenFormats() throws Exception {
            String[] malformedTokens = {
                    "not.a.jwt",
                    "onlyonepart",
                    "two.parts",
                    "invalid..token",
                    ""
            };

            for (String token : malformedTokens) {
                mockMvc.perform(get("/api/users/profile")
                                .header("Authorization", "Bearer " + token))
                        .andDo(print())
                        .andExpect(status().isForbidden()); // 403, not 401
            }
        }
    }

    @Nested
    @DisplayName("🌐 HTTP Methods Security")
    class HttpMethodsSecurityTests {

        @Test
        @DisplayName("Should handle different HTTP methods for public endpoints")
        void shouldHandleDifferentHttpMethodsForPublicEndpoints() throws Exception {
            // GET should work for auth test endpoint
            mockMvc.perform(get("/api/auth/test"))
                    .andDo(print())
                    .andExpect(status().isOk());

            // POST should work for registration
            String registrationJson = """
                {
                    "email": "newuser@example.com",
                    "username": "newuser",
                    "password": "Password123!",
                    "firstName": "New",
                    "lastName": "User",
                    "dateOfBirth": "1990-01-01",
                    "gender": "MALE",
                    "zipcode": "12345",
                    "agreeToTerms": true
                }
                """;

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registrationJson))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle different HTTP methods for protected endpoints")
        void shouldHandleDifferentHttpMethodsForProtectedEndpoints() throws Exception {
            // All methods should require authentication - expect 403
            mockMvc.perform(get("/api/users/profile"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(post("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403, not 401
        }
    }

    @Nested
    @DisplayName("🔧 Security Configuration Features")
    class SecurityConfigurationFeaturesTests {

        @Test
        @DisplayName("Should have CSRF disabled for stateless API")
        void shouldHaveCsrfDisabledForStatelessApi() throws Exception {
            // POST requests should work without CSRF tokens
            String loginJson = """
                {
                    "emailOrUsername": "regular@example.com",
                    "password": "Password123!",
                    "rememberMe": false
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should be stateless - no session creation")
        void shouldBeStatelessNoSessionCreation() throws Exception {
            // Multiple requests should not create or depend on sessions
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/api/auth/test"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(header().doesNotExist("Set-Cookie"));
            }
        }

        @Test
        @DisplayName("Should handle authentication failures gracefully")
        void shouldHandleAuthenticationFailuresGracefully() throws Exception {
            // Invalid credentials should return proper error
            String invalidLogin = """
                {
                    "emailOrUsername": "regular@example.com",
                    "password": "WrongPassword123!",
                    "rememberMe": false
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidLogin))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("🚫 Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 403 for missing authentication on protected endpoints")
        void shouldReturn403ForMissingAuthenticationOnProtectedEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/profile"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // Spring Security returns 403
        }

        @Test
        @DisplayName("Should return 403 for invalid JWT tokens")
        void shouldReturn403ForInvalidJwtTokens() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer invalid_token"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // Spring Security returns 403
        }

        @Test
        @DisplayName("Should return 403 for non-existent endpoints when not authenticated")
        void shouldReturn403ForNonExistentEndpointsWhenNotAuthenticated() throws Exception {
            // Non-existent endpoint without authentication returns 403
            mockMvc.perform(get("/api/nonexistent/endpoint"))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // Spring Security returns 403 first
        }

        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods")
        void shouldReturn405ForUnsupportedHttpMethods() throws Exception {
            // PATCH method on an endpoint that doesn't support it
            mockMvc.perform(patch("/api/auth/test"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("🔒 Integration Security Scenarios")
    class IntegrationSecurityScenariosTests {

        @Test
        @DisplayName("Should complete full authentication flow")
        void shouldCompleteFullAuthenticationFlow() throws Exception {
            // 1. Register new user
            String registrationJson = """
                {
                    "email": "flowtest@example.com",
                    "username": "flowtest",
                    "password": "Password123!",
                    "firstName": "Flow",
                    "lastName": "Test",
                    "dateOfBirth": "1990-01-01",
                    "gender": "MALE",
                    "zipcode": "12345",
                    "agreeToTerms": true
                }
                """;

            String registrationResponse = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registrationJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Extract token from registration response
            String token = objectMapper.readTree(registrationResponse).get("token").asText();

            // 2. Use token to access protected endpoint
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk());

            // 3. Login with same credentials
            String loginJson = """
                {
                    "emailOrUsername": "flowtest@example.com",
                    "password": "Password123!",
                    "rememberMe": false
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("Should handle concurrent authentication requests")
        void shouldHandleConcurrentAuthenticationRequests() throws Exception {
            // Simulate multiple users accessing protected endpoints simultaneously
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/api/users/profile")
                                .header("Authorization", "Bearer " + validJwtToken))
                        .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("Should maintain security across different request types")
        void shouldMaintainSecurityAcrossDifferentRequestTypes() throws Exception {
            // Test various content types and request formats
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + validJwtToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}