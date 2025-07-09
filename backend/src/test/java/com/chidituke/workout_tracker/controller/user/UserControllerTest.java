package com.chidituke.workout_tracker.controller.user;

import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.security.JwtTokenProvider;
import com.chidituke.workout_tracker.security.UserPrincipal;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for UserController
 * Tests user management, discovery, search, and profile operations
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("👤 UserController Integration Tests")
class UserControllerTest {

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

    // Test users
    private User regularUser;
    private User professionalUser;
    private User adminUser;
    private User privateUser;

    // Test tokens
    private String regularUserToken;
    private String professionalUserToken;
    private String adminUserToken;
    private String privateUserToken;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        // Create test users
        createTestUsers();

        // Generate JWT tokens
        generateTokens();
    }

    private void createTestUsers() {
        // Regular user in NYC
        regularUser = User.builder()
                .username("regularuser")
                .email("regular@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PUBLIC)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.MALE)
                .zipcode("10001")
                .city("New York")
                .state("NY")
                .country("US")
                .fitnessLevel(User.FitnessLevel.INTERMEDIATE)
                .activityLevel(User.ActivityLevel.MODERATELY_ACTIVE)
                .heightCm(180)
                .weightKg(75.0)
                .bio("Regular user who loves fitness")
                .build();
        regularUser = userRepository.save(regularUser);

        // Professional user in LA
        professionalUser = User.builder()
                .username("prouser")
                .email("pro@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Jane")
                .lastName("Smith")
                .userType(UserType.PROFESSIONAL)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PUBLIC)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .gender(User.Gender.FEMALE)
                .zipcode("90210")
                .city("Los Angeles")
                .state("CA")
                .country("US")
                .fitnessLevel(User.FitnessLevel.EXPERT)
                .activityLevel(User.ActivityLevel.VERY_ACTIVE)
                .heightCm(165)
                .weightKg(60.0)
                .bio("Certified personal trainer")
                .build();
        professionalUser = userRepository.save(professionalUser);

        // Admin user in Chicago
        adminUser = User.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Admin")
                .lastName("User")
                .userType(UserType.ADMIN)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PUBLIC)
                .dateOfBirth(LocalDate.of(1980, 12, 25))
                .gender(User.Gender.OTHER)
                .zipcode("60601")
                .city("Chicago")
                .state("IL")
                .country("US")
                .fitnessLevel(User.FitnessLevel.ADVANCED)
                .activityLevel(User.ActivityLevel.LIGHTLY_ACTIVE)
                .bio("Platform administrator")
                .build();
        adminUser = userRepository.save(adminUser);

        // Private user in Miami
        privateUser = User.builder()
                .username("privateuser")
                .email("private@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .firstName("Private")
                .lastName("Person")
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PRIVATE)
                .dateOfBirth(LocalDate.of(1995, 3, 10))
                .gender(User.Gender.FEMALE)
                .zipcode("33101")
                .city("Miami")
                .state("FL")
                .country("US")
                .fitnessLevel(User.FitnessLevel.BEGINNER)
                .activityLevel(User.ActivityLevel.SEDENTARY)
                .bio("Private user profile")
                .build();
        privateUser = userRepository.save(privateUser);
    }

    private void generateTokens() {
        // Generate JWT tokens for each user
        regularUserToken = generateTokenForUser(regularUser);
        professionalUserToken = generateTokenForUser(professionalUser);
        adminUserToken = generateTokenForUser(adminUser);
        privateUserToken = generateTokenForUser(privateUser);
    }

    private String generateTokenForUser(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        return jwtTokenProvider.generateJwtToken(authentication);
    }

    @Nested
    @DisplayName("👤 Profile Management")
    class ProfileManagementTests {

        @Test
        @DisplayName("Should get current user profile")
        void shouldGetCurrentUserProfile() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(regularUser.getId()))
                    .andExpect(jsonPath("$.username").value("regularuser"))
                    .andExpect(jsonPath("$.email").value("regular@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.zipcode").value("10001"))
                    .andExpect(jsonPath("$.city").value("New York"))
                    .andExpect(jsonPath("$.state").value("NY"))
                    .andExpect(jsonPath("$.userType").value("REGULAR"))
                    .andExpect(jsonPath("$.fitnessLevel").value("INTERMEDIATE"))
                    .andExpect(jsonPath("$.bio").value("Regular user who loves fitness"));
        }

        @Test
        @DisplayName("Should get public user profile by ID")
        void shouldGetPublicUserProfileById() throws Exception {
            mockMvc.perform(get("/api/users/" + regularUser.getId())
                            .header("Authorization", "Bearer " + professionalUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(regularUser.getId()))
                    .andExpect(jsonPath("$.username").value("regularuser"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.privacySettings").value("PUBLIC"));
        }

        @Test
        @DisplayName("Should not get private user profile when not authorized")
        void shouldNotGetPrivateUserProfileWhenNotAuthorized() throws Exception {
            mockMvc.perform(get("/api/users/" + privateUser.getId())
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get own private profile")
        void shouldGetOwnPrivateProfile() throws Exception {
            mockMvc.perform(get("/api/users/" + privateUser.getId())
                            .header("Authorization", "Bearer " + privateUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(privateUser.getId()))
                    .andExpect(jsonPath("$.username").value("privateuser"))
                    .andExpect(jsonPath("$.privacySettings").value("PRIVATE"));
        }

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfileSuccessfully() throws Exception {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setFirstName("Johnny");
            updateRequest.setLastName("Updated");
            updateRequest.setBio("Updated bio information");
            updateRequest.setCity("Brooklyn");
            updateRequest.setState("NY");
            updateRequest.setZipcode("11201");
            updateRequest.setHeightCm(185);
            updateRequest.setWeightKg(80.0);
            updateRequest.setFitnessGoals(Arrays.asList("Lose weight", "Build muscle", "Improve endurance"));

            mockMvc.perform(put("/api/users/profile")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Johnny"))
                    .andExpect(jsonPath("$.lastName").value("Updated"))
                    .andExpect(jsonPath("$.bio").value("Updated bio information"))
                    .andExpect(jsonPath("$.city").value("Brooklyn"))
                    .andExpect(jsonPath("$.zipcode").value("11201"))
                    .andExpect(jsonPath("$.heightCm").value(185))
                    .andExpect(jsonPath("$.weightKg").value(80.0));

            // Verify changes in database
            User updatedUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getFirstName()).isEqualTo("Johnny");
            assertThat(updatedUser.getLastName()).isEqualTo("Updated");
            assertThat(updatedUser.getCity()).isEqualTo("Brooklyn");
            assertThat(updatedUser.getZipcode()).isEqualTo("11201");
        }

        @Test
        @DisplayName("Should validate profile update fields")
        void shouldValidateProfileUpdateFields() throws Exception {
            UserUpdateRequest invalidRequest = new UserUpdateRequest();
            invalidRequest.setFirstName("A"); // Too short
            invalidRequest.setZipcode("123"); // Invalid zipcode format
            invalidRequest.setHeightCm(10); // Too short
            invalidRequest.setWeightKg(5.0); // Too light

            mockMvc.perform(put("/api/users/profile")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").isArray())
                    .andExpect(jsonPath("$.validationErrors[*].field", hasItems("firstName", "zipcode", "heightCm", "weightKg")));
        }
    }

    @Nested
    @DisplayName("🔍 User Search and Discovery")
    class UserSearchTests {

        @Test
        @DisplayName("Should search users with text query")
        void shouldSearchUsersWithTextQuery() throws Exception {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.setQuery("John");

            mockMvc.perform(post("/api/users/search")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(searchRequest))
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }

        @Test
        @DisplayName("Should search users by location")
        void shouldSearchUsersByLocation() throws Exception {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.setZipcode("10001");

            mockMvc.perform(post("/api/users/search")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(searchRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].zipcode").value("10001"));
        }

        @Test
        @DisplayName("Should find users near location")
        void shouldFindUsersNearLocation() throws Exception {
            mockMvc.perform(get("/api/users/near")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("location", "New York")
                            .param("radiusMiles", "50")
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find users by zipcode")
        void shouldFindUsersByZipcode() throws Exception {
            mockMvc.perform(get("/api/users/zipcode/10001")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].zipcode").value("10001"));
        }

        @Test
        @DisplayName("Should find users by city")
        void shouldFindUsersByCity() throws Exception {
            mockMvc.perform(get("/api/users/city/New York")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find users by state")
        void shouldFindUsersByState() throws Exception {
            mockMvc.perform(get("/api/users/state/NY")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find users by fitness level")
        void shouldFindUsersByFitnessLevel() throws Exception {
            mockMvc.perform(get("/api/users/fitness-level/INTERMEDIATE")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find recently active users")
        void shouldFindRecentlyActiveUsers() throws Exception {
            mockMvc.perform(get("/api/users/active")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("💼 Professional User Features")
    class ProfessionalUserTests {

        @Test
        @DisplayName("Should find verified professionals without authentication")
        void shouldFindVerifiedProfessionalsWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/api/users/professionals")
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find verified professionals with location filter")
        void shouldFindVerifiedProfessionalsWithLocationFilter() throws Exception {
            mockMvc.perform(get("/api/users/professionals")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("location", "90210")
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should find available professionals")
        void shouldFindAvailableProfessionals() throws Exception {
            mockMvc.perform(get("/api/users/professionals/available")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("📈 Activity and Status")
    class ActivityStatusTests {

        @Test
        @DisplayName("Should get user activity status")
        void shouldGetUserActivityStatus() throws Exception {
            mockMvc.perform(get("/api/users/" + regularUser.getId() + "/activity-status")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Inactive")); // No recent activity
        }

        @Test
        @DisplayName("Should update user activity")
        void shouldUpdateUserActivity() throws Exception {
            mockMvc.perform(post("/api/users/activity/ping")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify last active was updated
            User updatedUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getLastActive()).isNotNull();
        }
    }

    @Nested
    @DisplayName("🔐 Admin Operations")
    class AdminOperationTests {

        @Test
        @DisplayName("Should get total active users as admin")
        void shouldGetTotalActiveUsersAsAdmin() throws Exception {
            mockMvc.perform(get("/api/users/admin/stats/total-active")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNumber());
        }

        @Test
        @DisplayName("Should get total professionals as admin")
        void shouldGetTotalProfessionalsAsAdmin() throws Exception {
            mockMvc.perform(get("/api/users/admin/stats/total-professionals")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isNumber());
        }

        @Test
        @DisplayName("Should deactivate user as admin")
        void shouldDeactivateUserAsAdmin() throws Exception {
            mockMvc.perform(post("/api/users/" + regularUser.getId() + "/deactivate")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify user was deactivated
            User deactivatedUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(deactivatedUser).isNotNull();
            assertThat(deactivatedUser.getAccountStatus()).isEqualTo(User.AccountStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should reactivate user as admin")
        void shouldReactivateUserAsAdmin() throws Exception {
            // First deactivate the user
            mockMvc.perform(post("/api/users/" + regularUser.getId() + "/deactivate")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andExpect(status().isOk());

            // Then reactivate
            mockMvc.perform(post("/api/users/" + regularUser.getId() + "/reactivate")
                            .header("Authorization", "Bearer " + adminUserToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify user was reactivated
            User reactivatedUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(reactivatedUser).isNotNull();
            assertThat(reactivatedUser.getAccountStatus()).isEqualTo(User.AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should reject admin operations from non-admin users")
        void shouldRejectAdminOperationsFromNonAdminUsers() throws Exception {
            // Regular user trying admin operation
            mockMvc.perform(get("/api/users/admin/stats/total-active")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/users/" + regularUser.getId() + "/deactivate")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("🗑️ Account Management")
    class AccountManagementTests {

        @Test
        @DisplayName("Should delete user account")
        void shouldDeleteUserAccount() throws Exception {
            mockMvc.perform(delete("/api/users/profile")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify user account was marked as suspended (soft delete)
            User deletedUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(deletedUser).isNotNull();
            assertThat(deletedUser.getAccountStatus()).isEqualTo(User.AccountStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("🔒 Authorization and Security")
    class AuthorizationSecurityTests {

        @Test
        @DisplayName("Should require authentication for protected endpoints")
        void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
            // Profile endpoints
            mockMvc.perform(get("/api/users/profile"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            // Search endpoints
            mockMvc.perform(post("/api/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            // Activity endpoints
            mockMvc.perform(post("/api/users/activity/ping"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow access to public endpoints without authentication")
        void shouldAllowAccessToPublicEndpointsWithoutAuthentication() throws Exception {
            // Professionals endpoint is public
            mockMvc.perform(get("/api/users/professionals"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should require authentication for test endpoint")
        void shouldRequireAuthenticationForTestEndpoint() throws Exception {
            // Test endpoint requires authentication per security config
            mockMvc.perform(get("/api/users/test"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow authenticated access to test endpoint")
        void shouldAllowAuthenticatedAccessToTestEndpoint() throws Exception {
            // Test endpoint works with authentication
            mockMvc.perform(get("/api/users/test")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("User controller is working!"));
        }

        @Test
        @DisplayName("Should handle user not found errors")
        void shouldHandleUserNotFoundErrors() throws Exception {
            Long nonExistentUserId = 99999L;

            mockMvc.perform(get("/api/users/" + nonExistentUserId)
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/users/" + nonExistentUserId + "/activity-status")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("✅ Input Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should validate search request parameters")
        void shouldValidateSearchRequestParameters() throws Exception {
            UserSearchRequest invalidRequest = new UserSearchRequest();
            invalidRequest.setQuery("a".repeat(101)); // Too long
            invalidRequest.setZipcode("123"); // Invalid format
            invalidRequest.setRadiusMiles(-5); // Negative value

            mockMvc.perform(post("/api/users/search")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").isArray());
        }

        @Test
        @DisplayName("Should handle empty search results")
        void shouldHandleEmptySearchResults() throws Exception {
            UserSearchRequest searchRequest = new UserSearchRequest();
            searchRequest.setQuery("nonexistentuser12345");

            mockMvc.perform(post("/api/users/search")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(searchRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should handle location searches with no results")
        void shouldHandleLocationSearchesWithNoResults() throws Exception {
            mockMvc.perform(get("/api/users/zipcode/00000")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("🎯 Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle invalid fitness level parameter")
        void shouldHandleInvalidFitnessLevelParameter() throws Exception {
            mockMvc.perform(get("/api/users/fitness-level/INVALID_LEVEL")
                            .header("Authorization", "Bearer " + regularUserToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle concurrent profile updates")
        void shouldHandleConcurrentProfileUpdates() throws Exception {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setFirstName("Updated");

            // Simulate concurrent updates
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(put("/api/users/profile")
                                .header("Authorization", "Bearer " + regularUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                        .andExpect(status().isOk());
            }

            // Verify final state
            User finalUser = userRepository.findById(regularUser.getId()).orElse(null);
            assertThat(finalUser).isNotNull();
            assertThat(finalUser.getFirstName()).isEqualTo("Updated");
        }
    }
}