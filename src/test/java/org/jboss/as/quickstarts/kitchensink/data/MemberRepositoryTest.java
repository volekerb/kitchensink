package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@org.springframework.test.context.TestPropertySource(properties = {
    "mongodb.enabled=false"
})
public class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void shouldFindMemberByEmail() {
        // given
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        entityManager.persist(member);
        entityManager.flush();

        // when
        Optional<Member> found = memberRepository.findByEmail("john@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    public void shouldFindMembersByNameContaining() {
        // given
        Member member1 = new Member();
        member1.setName("John Smith");
        member1.setEmail("john@example.com");
        member1.setPhoneNumber("1234567890");

        Member member2 = new Member();
        member2.setName("Jane Smith");
        member2.setEmail("jane@example.com");
        member2.setPhoneNumber("0987654321");

        Member member3 = new Member();
        member3.setName("Robert Johnson");
        member3.setEmail("robert@example.com");
        member3.setPhoneNumber("5555555555");

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.flush();

        // when
        List<Member> smithMembers = memberRepository.findByNameContainingIgnoreCase("Smith");
        List<Member> johnMembers = memberRepository.findByNameContainingIgnoreCase("John");

        // then
        assertThat(smithMembers).hasSize(2);
        assertThat(johnMembers).hasSize(2); // Matches both "John Smith" and "Robert Johnson"
    }

    @Test
    public void shouldFindMembersByEmailDomain() {
        // given
        Member member1 = new Member();
        member1.setName("John Doe");
        member1.setEmail("john@example.com");
        member1.setPhoneNumber("1234567890");

        Member member2 = new Member();
        member2.setName("Jane Smith");
        member2.setEmail("jane@example.com");
        member2.setPhoneNumber("0987654321");

        Member member3 = new Member();
        member3.setName("Alice Johnson");
        member3.setEmail("alice@gmail.com");
        member3.setPhoneNumber("5555555555");

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.flush();

        // when
        List<Member> exampleDomainMembers = memberRepository.findByEmailDomain("@example.com");
        List<Member> gmailDomainMembers = memberRepository.findByEmailDomain("@gmail.com");

        // then
        assertThat(exampleDomainMembers).hasSize(2);
        assertThat(gmailDomainMembers).hasSize(1);
    }
}
