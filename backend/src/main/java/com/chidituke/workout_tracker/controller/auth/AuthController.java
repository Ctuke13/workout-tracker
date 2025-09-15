package com.chidituke.workout_tracker.controller.auth;

import com.chidituke.workout_tracker.dto.request.auth.LoginRequest;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.response.auth.JwtResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.security.JwtTokenProvider;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // Better than @Autowired
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 🔐 USER REGISTRATION
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email address already in use!"));
        }

        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        User user = userService.registerUser(registerRequest);

        // Generate JWT token for immediate login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(), // Always use email for authentication
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.getProfessionalProfile() != null,
                user.getSubscriptionTier() != null ? user.getSubscriptionTier() : SubscriptionTier.FREE // ✅ Added subscription tier
        );

        return ResponseEntity.ok(jwtResponse);
    }

    // 🔑 USER LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt;
        if (loginRequest.getEmailOrUsername().contains("@")) {
            userOpt = userService.findByEmail(loginRequest.getEmailOrUsername());
        } else {
            userOpt = userService.findByUsername(loginRequest.getEmailOrUsername());
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "User not found with email or username: " +
                            loginRequest.getEmailOrUsername()));
        }

        User user = userOpt.get();

        // Check if account is active
        if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Account is not active. Please contact support."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), // Always use email for authentication
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Update last active timestamp
        userService.updateLastActive(user.getId());

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.getProfessionalProfile() != null,
                user.getSubscriptionTier() != null ? user.getSubscriptionTier() : SubscriptionTier.FREE // ✅ Added subscription tier
        );

        return ResponseEntity.ok(jwtResponse);
    }

    // 🔍 CHECK AVAILABILITY ENDPOINTS
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = !userService.existsByEmail(email);
        return ResponseEntity.ok(new ApiResponse(isAvailable,
                isAvailable ? "Email is available" : "Email is already taken"));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = !userService.existsByUsername(username);
        return ResponseEntity.ok(new ApiResponse(isAvailable,
                isAvailable ? "Username is available" : "Username is already taken"));
    }

    // 👤 CURRENT USER INFO
    @GetMapping("/me")
    public ResponseEntity<UserSummary> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());

        UserSummary userSummary = new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.getAccountStatus(),
                user.getActivityLevel(),
                user.getProfessionalProfile() != null,
                user.getProfessionalProfile() != null ? user.getProfessionalProfile().getIsVerified() : false,
                user.getSubscriptionTier() != null ? user.getSubscriptionTier() : SubscriptionTier.FREE // ✅ Added subscription tier
        );

        return ResponseEntity.ok(userSummary);
    }

    // 🔓 LOGOUT (Optional - JWT is stateless)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@CurrentUser UserPrincipal userPrincipal) {
        // Update last active timestamp
        userService.updateLastActive(userPrincipal.getId());

        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
    }

    // 🔄 TOKEN REFRESH
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CurrentUser UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId());

        // Check if account is still active
        if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Account is no longer active"));
        }

        // Generate new token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Update last active timestamp
        userService.updateLastActive(user.getId());

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.getProfessionalProfile() != null,
                user.getSubscriptionTier() != null ? user.getSubscriptionTier() : SubscriptionTier.FREE // ✅ Added subscription tier
        );

        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/debug/my-authorities")
    public ResponseEntity<?> getMyAuthorities(Authentication auth) {
        if (auth == null) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("authenticated", false);
            debug.put("message", "No authentication found - user not logged in");
            return ResponseEntity.ok(debug);
        }

        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", true);
        debug.put("username", auth.getName());
        debug.put("authorities", auth.getAuthorities());
        debug.put("principal", auth.getPrincipal().getClass().getSimpleName());
        return ResponseEntity.ok(debug);
    }

    // 🧪 TESTING ENDPOINT
    @GetMapping("/test")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("Auth controller is working!");
    }

    // 📊 HELPER CLASSES FOR RESPONSES
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private UserType userType;
        private User.AccountStatus accountStatus;
        private User.ActivityLevel activityLevel;
        private Boolean isProfessional;
        private Boolean isVerified;
        private SubscriptionTier subscriptionTier; // ✅ Added subscription tier

        // ✅ Updated constructor with subscription tier
        public UserSummary(Long id, String username, String email, String firstName, String lastName,
                           UserType userType, User.AccountStatus accountStatus, User.ActivityLevel activityLevel,
                           Boolean isProfessional, Boolean isVerified, SubscriptionTier subscriptionTier) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.userType = userType;
            this.accountStatus = accountStatus;
            this.activityLevel = activityLevel;
            this.isProfessional = isProfessional;
            this.isVerified = isVerified;
            this.subscriptionTier = subscriptionTier;
        }

        // ✅ Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
        }

        public User.AccountStatus getAccountStatus() {
            return accountStatus;
        }

        public void setAccountStatus(User.AccountStatus accountStatus) {
            this.accountStatus = accountStatus;
        }

        public User.ActivityLevel getActivityLevel() {
            return activityLevel;
        }

        public void setActivityLevel(User.ActivityLevel activityLevel) {
            this.activityLevel = activityLevel;
        }

        public Boolean getIsProfessional() {
            return isProfessional;
        }

        public void setIsProfessional(Boolean isProfessional) {
            this.isProfessional = isProfessional;
        }

        public Boolean getIsVerified() {
            return isVerified;
        }

        public void setIsVerified(Boolean isVerified) {
            this.isVerified = isVerified;
        }

        public SubscriptionTier getSubscriptionTier() {
            return subscriptionTier;
        }

        public void setSubscriptionTier(SubscriptionTier subscriptionTier) {
            this.subscriptionTier = subscriptionTier;
        }

        // ✅ Utility methods for subscription checks
        public boolean canAccessPaidPlans() {
            if (subscriptionTier == null) return false;
            return subscriptionTier == SubscriptionTier.PLUS ||
                    subscriptionTier == SubscriptionTier.PRO ||
                    subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
        }

        public boolean hasProFeatures() {
            if (subscriptionTier == null) return false;
            return subscriptionTier == SubscriptionTier.PRO ||
                    subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
        }

        public boolean isFreeTier() {
            return subscriptionTier == null || subscriptionTier == SubscriptionTier.FREE;
        }
    }
}