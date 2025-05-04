package org.jboss.as.quickstarts.kitchensink.service;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is fired when a new member is registered
 */
public class MemberRegisteredEvent extends ApplicationEvent {
    
    private final Member member;
    
    public MemberRegisteredEvent(Object source, Member member) {
        super(source);
        this.member = member;
    }
    
    public Member getMember() {
        return member;
    }
}
