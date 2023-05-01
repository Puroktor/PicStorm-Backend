package ru.vsu.cs.picstorm.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YandexCloudConfig {

    public static final String YANDEX_CLOUD_STORAGE_ENDPOINT_NAME = "storage.yandexcloud.net";
    public static final String YANDEX_CLOUD_STORAGE_REGION = "ru-central1";

    @Value("${yandex.cloud.storage.access.key.id}")
    private String accessKeyId;
    @Value("${yandex.cloud.storage.secret.key}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                YANDEX_CLOUD_STORAGE_ENDPOINT_NAME, YANDEX_CLOUD_STORAGE_REGION
                        )
                )
                .build();
    }

}
