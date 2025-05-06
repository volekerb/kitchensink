package org.jboss.as.quickstarts.kitchensink.config;

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.data.MemberRepositoryAdapter;
import org.jboss.as.quickstarts.kitchensink.data.mongo.MongoMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for MongoDB configuration
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@TestPropertySource(properties = {
    "mongodb.enabled=true",
    "spring.data.mongodb.uri=mongodb://localhost:27017/test"
})
public class MongoConfigTest {

    @Autowired
    private ApplicationContext context;

    // Mock the MongoDB repository to avoid needing a real MongoDB instance
    @MockBean
    private MongoMemberRepository mongoMemberRepository;

    @Test
    public void testMongoConfigLoaded() {
        // Verify that the MongoDB configuration is loaded
        assertNotNull(context.getBean(MongoConfig.class));
    }

    @Test
    public void testMongoTemplateExists() {
        // Verify that the mongoTemplate bean exists
        assertNotNull(context.getBean("mongoTemplate"));
    }

    @Test
    public void testMemberRepositoryIsAdapter() {
        // Verify that the MemberRepository is the adapter when MongoDB is enabled
        MemberRepository repository = context.getBean(MemberRepository.class);
        assertTrue(repository instanceof MemberRepositoryAdapter);
    }
}
