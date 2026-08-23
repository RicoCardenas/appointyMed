package com.appointymed.appointment.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsSqsConfig {

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${app.aws.endpoint-url:}")
    private String endpointUrl;

    @Value("${app.aws.credentials.access-key:test}")
    private String accessKey;

    @Value("${app.aws.credentials.secret-key:test}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        var credentials = AwsBasicCredentials.create(
                accessKey != null && !accessKey.isBlank() ? accessKey : "test",
                secretKey != null && !secretKey.isBlank() ? secretKey : "test"
        );

        var builder = SqsClient.builder()
                .region(Region.of(region != null && !region.isBlank() ? region : "us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }

        return builder.build();
    }
}
