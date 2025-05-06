package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.data.mongo.MongoMemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberRepositoryAdapterTest {

    @Mock
    private MongoMemberRepository mongoRepository;

    private MemberRepositoryAdapter adapter;

    private MongoMember mongoMember;
    private Member member;

    @BeforeEach
    void setUp() {
        adapter = new MemberRepositoryAdapter(mongoRepository);

        // Create test data
        mongoMember = new MongoMember();
        mongoMember.setId("mongo123");
        mongoMember.setName("John Doe");
        mongoMember.setEmail("john@example.com");
        mongoMember.setPhoneNumber("1234567890");

        member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
    }

    @Test
    void testFindByEmail() {
        // given
        when(mongoRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mongoMember));

        // when
        Optional<Member> result = adapter.findByEmail("john@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        verify(mongoRepository).findByEmail("john@example.com");
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        // given
        when(mongoRepository.findByNameContainingIgnoreCase("John"))
                .thenReturn(List.of(mongoMember));

        // when
        List<Member> result = adapter.findByNameContainingIgnoreCase("John");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(mongoRepository).findByNameContainingIgnoreCase("John");
    }

    @Test
    void testFindByEmailDomain() {
        // given
        when(mongoRepository.findByEmailDomain("@example.com"))
                .thenReturn(List.of(mongoMember));

        // when
        List<Member> result = adapter.findByEmailDomain("@example.com");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        verify(mongoRepository).findByEmailDomain("@example.com");
    }

    @Test
    void testSave() {
        // given
        when(mongoRepository.save(any(MongoMember.class))).thenReturn(mongoMember);

        // when
        Member savedMember = adapter.save(member);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getName()).isEqualTo("John Doe");
        assertThat(savedMember.getEmail()).isEqualTo("john@example.com");
        verify(mongoRepository).save(any(MongoMember.class));
    }

    @Test
    void testFindAll() {
        // given
        MongoMember mongoMember2 = new MongoMember();
        mongoMember2.setName("Jane Smith");
        mongoMember2.setEmail("jane@example.com");
        mongoMember2.setPhoneNumber("0987654321");

        when(mongoRepository.findAll()).thenReturn(Arrays.asList(mongoMember, mongoMember2));

        // when
        List<Member> result = adapter.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");
        verify(mongoRepository).findAll();
    }

    @Test
    void testDelete() {
        // given
        when(mongoRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mongoMember));
        doNothing().when(mongoRepository).delete(mongoMember);

        // when
        adapter.delete(member);

        // then
        verify(mongoRepository).findByEmail("john@example.com");
        verify(mongoRepository).delete(mongoMember);
    }

    @Test
    void testCount() {
        // given
        when(mongoRepository.count()).thenReturn(5L);

        // when
        long count = adapter.count();

        // then
        assertThat(count).isEqualTo(5L);
        verify(mongoRepository).count();
    }

    @Test
    void testFindAllPaged() {
        // given
        Pageable pageable = Pageable.ofSize(10);
        Page<MongoMember> page = new PageImpl<>(List.of(mongoMember), pageable, 1);
        when(mongoRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<Member> result = adapter.findAll(pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
        verify(mongoRepository).findAll(pageable);
    }
}