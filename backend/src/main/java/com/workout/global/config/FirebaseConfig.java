package com.workout.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class FirebaseConfig {

  private final ResourceLoader resourceLoader;

  @Value("${fcm.service-account-key-path}")
  private String keyPath;

  private FirebaseApp firebaseApp;

  public FirebaseConfig(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void initializeFCM() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      Resource resource = resourceLoader.getResource(keyPath);

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
          .setProjectId("workout-7e8f6")
          .build();

      this.firebaseApp = FirebaseApp.initializeApp(options);
    } else {
      this.firebaseApp = FirebaseApp.getInstance();
    }
  }

  @Bean
  public FirebaseMessaging firebaseMessaging() {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}