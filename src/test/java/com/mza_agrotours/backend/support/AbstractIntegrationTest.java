package com.mza_agrotours.backend.support;

import com.google.firebase.FirebaseApp;
import com.mza_agrotours.backend.services.ObjectStoragePolicies;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Clase base para los tests de integración
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * Tiene que coincidir con object-storage.bucket de application-test.properties.
     */
    protected static final String BUCKET = "mza-agrotours-test";

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.14");

    /**
     * MinIO ya no se publica en Docker Hub, de ahi el nombre completo de quay.io.
     */
    static final MinIOContainer MINIO = new MinIOContainer(
            DockerImageName.parse("quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z")
                    .asCompatibleSubstituteFor("minio/minio"));

    static {
        POSTGRES.start();
        MINIO.start();
        crearBucket();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("object-storage.endpoint", MINIO::getS3URL);
        registry.add("object-storage.access-key", MINIO::getUserName);
        registry.add("object-storage.secret-key", MINIO::getPassword);
        registry.add("object-storage.public-base-url", () -> MINIO.getS3URL() + "/" + BUCKET);
    }

    /**
     * El bucket de los tests queda como el de produccion: privado salvo las
     * carpetas publicas, que abre la misma policy que se aplica alla.
     */
    private static void crearBucket() {
        try (S3Client client = S3Client.builder()
                .endpointOverride(URI.create(MINIO.getS3URL()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(MINIO.getUserName(), MINIO.getPassword())))
                .forcePathStyle(true)
                .build()) {
            client.createBucket(request -> request.bucket(BUCKET));
            ObjectStoragePolicies.lecturaPublica(BUCKET).ifPresent(policy ->
                    client.putBucketPolicy(request -> request.bucket(BUCKET).policy(policy)));
        }
    }

    @MockitoBean
    protected FirebaseApp firebaseApp;

    @MockitoBean
    protected JavaMailSender mailSender;
}
