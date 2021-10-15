package io.kgu.chatservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@Setter @Validated
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseConfig {

    @NotEmpty private String path;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new PathResource(path).getInputStream()
        );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(options, "RandHand-Chat");
        return FirebaseMessaging.getInstance(app);
    }

}
