package org.jboss.as.quickstarts.kitchensink.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Test configuration for MongoDB using Testcontainers
 * This configuration is only active when Docker is available
 */
@TestConfiguration
@EnableMongoRepositories(basePackages = "org.jboss.as.quickstarts.kitchensink.data.mongo")
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class
})
public class TestcontainersMongoConfig {

    private static final Logger LOG = Logger.getLogger(TestcontainersMongoConfig.class.getName());
    private static final MongoDBContainer mongoDBContainer;
    public static boolean dockerAvailable = false;

    static {
        MongoDBContainer container = null;

        try {
            // Check if Docker is available
            dockerAvailable = isDockerAvailable();

            if (dockerAvailable) {
                LOG.info("Docker is available, starting MongoDB container");
                container = new MongoDBContainer(DockerImageName.parse("mongo:6.0.8"));
                container.start();

                // Set system properties for MongoDB connection
                System.setProperty("spring.data.mongodb.uri", container.getReplicaSetUrl());
                System.setProperty("mongodb.enabled", "true");
                System.setProperty("spring.data.mongodb.auto-index-creation", "true");
            } else {
                LOG.warning("Docker is not available, MongoDB tests will be skipped");
            }
        } catch (Exception e) {
            LOG.severe("Failed to start MongoDB container: " + e.getMessage());
            dockerAvailable = false;
        }

        mongoDBContainer = container;
    }

    /**
     * Check if Docker is available
     * @return true if Docker is available, false otherwise
     */
    public static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Bean
    @Primary
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoDBContainer.getReplicaSetUrl());

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToSocketSettings(builder -> 
                builder.connectTimeout(30, TimeUnit.SECONDS)
                       .readTimeout(30, TimeUnit.SECONDS))
            .applyToClusterSettings(builder -> 
                builder.serverSelectionTimeout(30, TimeUnit.SECONDS))
            .applyToConnectionPoolSettings(builder -> 
                builder.maxWaitTime(30, TimeUnit.SECONDS))
            .build();

        return MongoClients.create(settings);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "test");
    }
}
