package com.chidituke.workout_tracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;

import java.io.IOException;

/**
 * Initializes the Firebase Admin SDK on startup.
 * Reads credentials from the service account JSON file.
 * <p>
 * The service account file must NOT be committed to git.
 * It is loaded from src/main/resources/firebase-service-account.json
 * and excluded via .gitignore.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private Resource serviceAccountResource;

    @PostConstruct
    public void initializeFirebase() {
        // Avoid re-initializing if already done (e.g. hot reload)
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("🔥 Firebase already initialized — skipping");
            return;
        }

        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(serviceAccountResource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("🔥 Firebase Admin SDK initialized successfully");

        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.error("Make sure firebase-service-account.json exists in src/main/resources/");
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}