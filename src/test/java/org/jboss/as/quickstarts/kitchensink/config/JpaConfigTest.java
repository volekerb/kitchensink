package org.jboss.as.quickstarts.kitchensink.config;

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.data.MemberRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test for JPA configuration
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@TestPropertySource(properties = {
    "mongodb.enabled=false"
})
public class JpaConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testJpaConfigLoaded() {
        // Verify that the JPA configuration is loaded
        assertNotNull(context.getBean(JpaConfig.class));
    }

    @Test
    public void testMemberRepositoryIsJpaRepository() {
        // Verify that the MemberRepository is a JPA repository when MongoDB is disabled
        MemberRepository repository = context.getBean(MemberRepository.class);
        assertTrue(repository instanceof JpaRepository);
        assertFalse(repository instanceof MemberRepositoryAdapter);
    }
}
