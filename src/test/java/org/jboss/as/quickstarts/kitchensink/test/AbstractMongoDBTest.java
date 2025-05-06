package org.jboss.as.quickstarts.kitchensink.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for MongoDB tests using Testcontainers
 */
@Testcontainers
public abstract class AbstractMongoDBTest {

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0.8"));

    @BeforeAll
    static void startContainers() {
        mongoDBContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        if (mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("mongodb.enabled", () -> "true");
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }
}