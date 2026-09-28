package com.mza_agrotours.backend.support;

import com.google.firebase.FirebaseApp;
import com.mza_agrotours.backend.services.ObjectStoragePolicies;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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

    private static final String RUSTFS_ACCESS_KEY = "rustfsadmin";
    private static final String RUSTFS_SECRET_KEY = "rustfsadmin";

    static final GenericContainer<?> RUSTFS = new GenericContainer<>("rustfs/rustfs:1.0.0")
            .withExposedPorts(9000)
            .withEnv("RUSTFS_ACCESS_KEY", RUSTFS_ACCESS_KEY)
            .withEnv("RUSTFS_SECRET_KEY", RUSTFS_SECRET_KEY)
            .withEnv("RUSTFS_VOLUMES", "/data")
            .waitingFor(Wait.forHttp("/health").forPort(9000));

    static {
        POSTGRES.start();
        RUSTFS.start();
        crearBucket();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("object-storage.endpoint", AbstractIntegrationTest::rustfsUrl);
        registry.add("object-storage.access-key", () -> RUSTFS_ACCESS_KEY);
        registry.add("object-storage.secret-key", () -> RUSTFS_SECRET_KEY);
        registry.add("object-storage.public-base-url", () -> rustfsUrl() + "/" + BUCKET);
    }

    private static String rustfsUrl() {
        return "http://" + RUSTFS.getHost() + ":" + RUSTFS.getMappedPort(9000);
    }

    /**
     * El bucket de los tests queda como el de produccion: privado salvo las
     * carpetas publicas, que abre la misma policy que se aplica alla.
     */
    private static void crearBucket() {
        try (S3Client client = S3Client.builder()
                .endpointOverride(URI.create(rustfsUrl()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(RUSTFS_ACCESS_KEY, RUSTFS_SECRET_KEY)))
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
