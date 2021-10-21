package io.kgu.chatservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

@Setter @Validated
@Configuration
@ConfigurationProperties(prefix = "cloud.aws")
public class AmazonS3Config {

    @NotEmpty private String s3AccessKey;
    @NotEmpty private String s3SecretKey;
    @NotEmpty private String s3Region;
    @NotEmpty public static String s3Bucket;

    @Bean
    public AmazonS3Client amazonS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(s3Region)
                .build();
    }

    public void setS3Bucket(String bucket) {
        s3Bucket = bucket;
    }
}
