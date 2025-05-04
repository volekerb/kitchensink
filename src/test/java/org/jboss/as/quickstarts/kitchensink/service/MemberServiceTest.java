package org.jboss.as.quickstarts.kitchensink.service;

import jakarta.validation.ConstraintViolationException;
import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    private LocalValidatorFactoryBean validator;
    
    private MemberService memberService;
    
    @BeforeEach
    public void setup() {
        // Set up validator with hibernate implementation
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        // Initialize the service with mocks
        memberService = new MemberService(memberRepository, validator, eventPublisher);
    }
    
    @Test
    public void shouldRegisterValidMember() throws Exception {
        // given
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            savedMember.setId(1L);
            return savedMember;
        });
        
        // when
        Member registered = memberService.register(member);
        
        // then
        assertThat(registered.getId()).isEqualTo(1L);
        verify(memberRepository).save(member);
        
        // verify event was published
        ArgumentCaptor<MemberRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MemberRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getMember()).isEqualTo(registered);
    }
    
    @Test
    public void shouldRejectInvalidMember() {
        // given
        Member member = new Member();
        member.setName(""); // invalid: name cannot be empty
        member.setEmail("invalid-email"); // invalid: not a proper email
        member.setPhoneNumber("123"); // invalid: too short
        
        // when, then
        Exception exception = assertThrows(ConstraintViolationException.class, () -> {
            memberService.register(member);
        });
        
        // Verify repository was not called
        verify(memberRepository, never()).save(any(Member.class));
    }
    
    @Test
    public void shouldRejectDuplicateEmail() {
        // given
        Member existingMember = new Member();
        existingMember.setId(1L);
        existingMember.setName("Existing User");
        existingMember.setEmail("john@example.com");
        existingMember.setPhoneNumber("1234567890");
        
        Member newMember = new Member();
        newMember.setName("John Doe");
        newMember.setEmail("john@example.com"); // Same email as existing user
        newMember.setPhoneNumber("9876543210");
        
        when(memberRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingMember));
        
        // when, then
        Exception exception = assertThrows(Exception.class, () -> {
            memberService.register(newMember);
        });
        
        assertThat(exception.getMessage()).contains("already exists");
        
        // Verify repository save was not called
        verify(memberRepository, never()).save(any(Member.class));
    }
}
