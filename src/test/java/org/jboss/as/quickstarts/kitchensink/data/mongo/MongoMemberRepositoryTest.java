package org.jboss.as.quickstarts.kitchensink.data.mongo;

import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test for MongoMemberRepository using mocks
 * This test verifies that the custom methods in MongoMemberRepository work as expected
 */
@ExtendWith(MockitoExtension.class)
public class MongoMemberRepositoryTest {

    @Mock
    private MongoMemberRepository repository;

    private MongoMember testMember;
    private MongoMember testMember2;

    @BeforeEach
    void setUp() {
        // Create test members
        testMember = new MongoMember();
        testMember.setId("1");
        testMember.setName("John Doe");
        testMember.setEmail("john@example.com");
        testMember.setPhoneNumber("1234567890");

        testMember2 = new MongoMember();
        testMember2.setId("2");
        testMember2.setName("Jane Smith");
        testMember2.setEmail("jane@gmail.com");
        testMember2.setPhoneNumber("0987654321");
    }

    @Test
    void testCreateMember() {
        // given
        when(repository.save(any(MongoMember.class))).thenReturn(testMember);

        // when
        MongoMember savedMember = repository.save(testMember);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isEqualTo("1");
        assertThat(savedMember.getName()).isEqualTo("John Doe");
        assertThat(savedMember.getEmail()).isEqualTo("john@example.com");
        verify(repository).save(testMember);
    }

    @Test
    void testReadMember() {
        // given
        when(repository.findById("1")).thenReturn(Optional.of(testMember));

        // when
        Optional<MongoMember> foundMember = repository.findById("1");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("John Doe");
        assertThat(foundMember.get().getEmail()).isEqualTo("john@example.com");
        verify(repository).findById("1");
    }

    @Test
    void testUpdateMember() {
        // given
        MongoMember updatedMember = new MongoMember();
        updatedMember.setId("1");
        updatedMember.setName("Jane Doe");
        updatedMember.setEmail("jane@example.com");
        updatedMember.setPhoneNumber("1234567890");

        when(repository.save(any(MongoMember.class))).thenReturn(updatedMember);

        // when
        MongoMember result = repository.save(updatedMember);

        // then
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(repository).save(updatedMember);
    }

    @Test
    void testDeleteMember() {
        // given
        doNothing().when(repository).deleteById("1");
        when(repository.findById("1")).thenReturn(Optional.empty());

        // when
        repository.deleteById("1");
        Optional<MongoMember> result = repository.findById("1");

        // then
        assertThat(result).isEmpty();
        verify(repository).deleteById("1");
        verify(repository).findById("1");
    }

    @Test
    void testFindByEmail() {
        // given
        when(repository.findByEmail("john@example.com")).thenReturn(Optional.of(testMember));

        // when
        Optional<MongoMember> found = repository.findByEmail("john@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        verify(repository).findByEmail("john@example.com");
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        // given
        when(repository.findByNameContainingIgnoreCase("John"))
                .thenReturn(List.of(testMember));
        when(repository.findByNameContainingIgnoreCase("doe"))
                .thenReturn(List.of(testMember));

        // when
        List<MongoMember> johnsMembers = repository.findByNameContainingIgnoreCase("John");
        List<MongoMember> doesMembers = repository.findByNameContainingIgnoreCase("doe");

        // then
        assertThat(johnsMembers).hasSize(1);
        assertThat(johnsMembers.get(0).getName()).isEqualTo("John Doe");

        assertThat(doesMembers).hasSize(1);
        assertThat(doesMembers.get(0).getName()).isEqualTo("John Doe");

        verify(repository).findByNameContainingIgnoreCase("John");
        verify(repository).findByNameContainingIgnoreCase("doe");
    }

    @Test
    void testFindByEmailDomain() {
        // given
        when(repository.findByEmailDomain("@example.com"))
                .thenReturn(List.of(testMember));
        when(repository.findByEmailDomain("@gmail.com"))
                .thenReturn(List.of(testMember2));

        // when
        List<MongoMember> exampleDomainMembers = repository.findByEmailDomain("@example.com");
        List<MongoMember> gmailDomainMembers = repository.findByEmailDomain("@gmail.com");

        // then
        assertThat(exampleDomainMembers).hasSize(1);
        assertThat(exampleDomainMembers.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(gmailDomainMembers).hasSize(1);
        assertThat(gmailDomainMembers.get(0).getEmail()).isEqualTo("jane@gmail.com");

        verify(repository).findByEmailDomain("@example.com");
        verify(repository).findByEmailDomain("@gmail.com");
    }
}
