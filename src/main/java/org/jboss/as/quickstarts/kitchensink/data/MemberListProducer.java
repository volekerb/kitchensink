package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberRegisteredEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/**
 * Bean that maintains a cached list of all members
 * This replaces the CDI producer from the original JBoss application
 */
@Component
public class MemberListProducer {
    
    private static final Logger log = Logger.getLogger(MemberListProducer.class.getName());

    private final MemberRepository memberRepository;
    private List<Member> members;

    public MemberListProducer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Initialize the member list when the application starts
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        retrieveAllMembersOrderedByName();
    }

    /**
     * Update the member list when a new member is registered
     */
    @EventListener
    public void onMemberRegistered(MemberRegisteredEvent event) {
        log.info("Received member registered event for: " + event.getMember().getName());
        retrieveAllMembersOrderedByName();
    }

    /**
     * Get the current list of members
     */
    public List<Member> getMembers() {
        return members;
    }

    /**
     * Refresh the list of all members
     */
    public void retrieveAllMembersOrderedByName() {
        this.members = memberRepository.findAll();
    }
}
