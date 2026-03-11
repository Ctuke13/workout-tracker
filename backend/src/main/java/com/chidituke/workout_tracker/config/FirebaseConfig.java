package com.chidituke.workout_tracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private Resource serviceAccountResource;

    @Value("${FIREBASE_SERVICE_ACCOUNT_BASE64:}")
    private String serviceAccountBase64;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("🔥 Firebase already initialized — skipping");
            return;
        }

        try {
            GoogleCredentials credentials;

            if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
                // Production: decode from base64 env var
                log.info("🔥 Loading Firebase credentials from environment variable");
                byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
                credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
            } else {
                // Local dev: read from file
                log.info("🔥 Loading Firebase credentials from file");
                credentials = GoogleCredentials.fromStream(serviceAccountResource.getInputStream());
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("🔥 Firebase Admin SDK initialized successfully");

        } catch (IOException e) {
            log.error("❌ Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}