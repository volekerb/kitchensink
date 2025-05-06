package org.jboss.as.quickstarts.kitchensink.data.mongo;

import org.jboss.as.quickstarts.kitchensink.config.TestcontainersMongoConfig;
import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for MongoMemberRepository using Testcontainers
 * This test uses a real MongoDB instance running in a Docker container
 * Tests are skipped if Docker is not available
 */
@SpringBootTest
@Import(TestcontainersMongoConfig.class)
@TestPropertySource(properties = {
    "mongodb.enabled=true"}
)
public class TestcontainersMongoMemberRepositoryTest {

    @Autowired
    private MongoMemberRepository repository;

    private MongoMember testMember;
    private MongoMember testMember2;

    @BeforeEach
    void setUp() {
        // Create test members
        testMember = new MongoMember();
        testMember.setName("John Doe");
        testMember.setEmail("john@example.com");
        testMember.setPhoneNumber("1234567890");

        testMember2 = new MongoMember();
        testMember2.setName("Jane Smith");
        testMember2.setEmail("jane@gmail.com");
        testMember2.setPhoneNumber("0987654321");

        // Save test members to the database
        repository.save(testMember);
        repository.save(testMember2);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        repository.deleteAll();
    }

    @Test
    void testCreateMember() {
        // given
        MongoMember newMember = new MongoMember();
        newMember.setName("Bob Johnson");
        newMember.setEmail("bob@example.com");
        newMember.setPhoneNumber("5555555555");

        // when
        MongoMember savedMember = repository.save(newMember);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getName()).isEqualTo("Bob Johnson");
        assertThat(savedMember.getEmail()).isEqualTo("bob@example.com");

        // Verify it was actually saved to the database
        Optional<MongoMember> foundMember = repository.findById(savedMember.getId());
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("Bob Johnson");
    }

    @Test
    void testReadMember() {
        // when
        Optional<MongoMember> foundMember = repository.findById(testMember.getId());

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("John Doe");
        assertThat(foundMember.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testUpdateMember() {
        // given
        testMember.setName("John Updated");
        testMember.setEmail("john.updated@example.com");

        // when
        MongoMember updatedMember = repository.save(testMember);

        // then
        assertThat(updatedMember.getId()).isEqualTo(testMember.getId());
        assertThat(updatedMember.getName()).isEqualTo("John Updated");
        assertThat(updatedMember.getEmail()).isEqualTo("john.updated@example.com");

        // Verify it was actually updated in the database
        Optional<MongoMember> foundMember = repository.findById(testMember.getId());
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("John Updated");
    }

    @Test
    void testDeleteMember() {
        // when
        repository.deleteById(testMember.getId());
        Optional<MongoMember> result = repository.findById(testMember.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByEmail() {
        // when
        Optional<MongoMember> found = repository.findByEmail("john@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        // when
        List<MongoMember> johnsMembers = repository.findByNameContainingIgnoreCase("John");
        List<MongoMember> doesMembers = repository.findByNameContainingIgnoreCase("doe");

        // then
        assertThat(johnsMembers).hasSize(1);
        assertThat(johnsMembers.get(0).getName()).isEqualTo("John Doe");

        assertThat(doesMembers).hasSize(1);
        assertThat(doesMembers.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByEmailDomain() {
        // when
        List<MongoMember> exampleDomainMembers = repository.findByEmailDomain("@example.com");
        List<MongoMember> gmailDomainMembers = repository.findByEmailDomain("@gmail.com");

        // then
        assertThat(exampleDomainMembers).hasSize(1);
        assertThat(exampleDomainMembers.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(gmailDomainMembers).hasSize(1);
        assertThat(gmailDomainMembers.get(0).getEmail()).isEqualTo("jane@gmail.com");
    }
}
