package com.appointymed.notification.infrastructure.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Profile("!test")
public class AwsSqsConfig {

    @Bean
    public SqsClient sqsClient(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.aws.endpoint-url:}") String endpointUrl,
            @Value("${app.aws.credentials.access-key:test}") String accessKey,
            @Value("${app.aws.credentials.secret-key:test}") String secretKey) {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        valueOrDefault(accessKey, "test"), valueOrDefault(secretKey, "test"));

        var builder =
                SqsClient.builder()
                        .region(Region.of(valueOrDefault(region, "us-east-1")))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }

        return builder.build();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
