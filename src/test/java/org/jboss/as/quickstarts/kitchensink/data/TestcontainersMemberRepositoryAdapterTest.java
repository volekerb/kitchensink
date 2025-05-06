package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.config.TestcontainersMongoConfig;
import org.jboss.as.quickstarts.kitchensink.data.mongo.MongoMemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for MemberRepositoryAdapter using Testcontainers
 * This test uses a real MongoDB instance running in a Docker container
 * Tests are skipped if Docker is not available
 */
@SpringBootTest
@Import(TestcontainersMongoConfig.class)
@TestPropertySource(properties = {
    "mongodb.enabled=true"
})
public class TestcontainersMemberRepositoryAdapterTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MongoMemberRepository mongoRepository;

    private Member testMember;
    private Member testMember2;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        clearDatabase();

        // Create test members
        testMember = new Member();
        testMember.setName("John Doe");
        testMember.setEmail("john@example.com");
        testMember.setPhoneNumber("1234567890");

        testMember2 = new Member();
        testMember2.setName("Jane Smith");
        testMember2.setEmail("jane@gmail.com");
        testMember2.setPhoneNumber("0987654321");

        // Save test members to the database
        memberRepository.save(testMember);
        memberRepository.save(testMember2);
    }

    /**
     * Clear all data from the database
     */
    private void clearDatabase() {
        // Delete all data from MongoDB
        mongoRepository.deleteAll();

        // Verify the database is empty
        List<MongoMember> remainingMembers = mongoRepository.findAll();
        if (!remainingMembers.isEmpty()) {
            System.out.println("[DEBUG_LOG] Failed to clear database, still have " + remainingMembers.size() + " members");
            // Try harder to delete everything
            for (MongoMember member : remainingMembers) {
                mongoRepository.delete(member);
            }
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        mongoRepository.deleteAll();
    }

    @Test
    void testFindByEmail() {
        // when
        Optional<Member> result = memberRepository.findByEmail("john@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        // when
        List<Member> johnsMembers = memberRepository.findByNameContainingIgnoreCase("John");
        List<Member> doesMembers = memberRepository.findByNameContainingIgnoreCase("doe");

        // then
        assertThat(johnsMembers).hasSize(1);
        assertThat(johnsMembers.get(0).getName()).isEqualTo("John Doe");

        assertThat(doesMembers).hasSize(1);
        assertThat(doesMembers.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByEmailDomain() {
        // when
        List<Member> exampleDomainMembers = memberRepository.findByEmailDomain("@example.com");
        List<Member> gmailDomainMembers = memberRepository.findByEmailDomain("@gmail.com");

        // then
        assertThat(exampleDomainMembers).hasSize(1);
        assertThat(exampleDomainMembers.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(gmailDomainMembers).hasSize(1);
        assertThat(gmailDomainMembers.get(0).getEmail()).isEqualTo("jane@gmail.com");
    }

    @Test
    void testSave() {
        // given
        Member newMember = new Member();
        newMember.setName("Bob Johnson");
        newMember.setEmail("bob@example.com");
        newMember.setPhoneNumber("5555555555");

        // when
        Member savedMember = memberRepository.save(newMember);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getName()).isEqualTo("Bob Johnson");
        assertThat(savedMember.getEmail()).isEqualTo("bob@example.com");

        // Verify it was actually saved to the database
        Optional<MongoMember> foundMongoMember = mongoRepository.findByEmail("bob@example.com");
        assertThat(foundMongoMember).isPresent();
        assertThat(foundMongoMember.get().getName()).isEqualTo("Bob Johnson");
    }

    @Test
    void testFindAll() {
        // when
        List<Member> result = memberRepository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(Member::getName))
            .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void testDelete() {
        // when
        memberRepository.delete(testMember);

        // then
        Optional<MongoMember> result = mongoRepository.findByEmail("john@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void testCount() {
        // when
        long count = memberRepository.count();

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindAllPaged() {
        // given
        Pageable pageable = Pageable.ofSize(10);

        // when
        Page<Member> result = memberRepository.findAll(pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getContent().stream().map(Member::getName))
            .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }
}
