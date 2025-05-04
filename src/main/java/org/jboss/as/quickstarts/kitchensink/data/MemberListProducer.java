package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bean that maintains a cached list of all members
 * This replaces the CDI producer from the original JBoss application
 */
@Component
public class MemberListProducer {

    private final MemberRepository memberRepository;
    private List<Member> members;

    public MemberListProducer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        retrieveAllMembersOrderedByName();
    }

    public List<Member> getMembers() {
        return members;
    }

    public void retrieveAllMembersOrderedByName() {
        this.members = memberRepository.findAll();
    }
}
