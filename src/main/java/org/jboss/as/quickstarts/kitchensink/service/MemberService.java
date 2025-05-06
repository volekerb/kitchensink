package org.jboss.as.quickstarts.kitchensink.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service handling member registration and management
 * Replaces the original JBoss MemberRegistration class
 */
@Service
public class MemberService {

    private static final Logger log = Logger.getLogger(MemberService.class.getName());

    private MemberRepository memberRepository;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MemberService(MemberRepository memberRepository, 
                         Validator validator, 
                         ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Register a new member
     * @param member the member to register
     * @throws Exception if registration fails
     */
    @Transactional
    public Member register(Member member) throws Exception {
        log.info("Registering " + member.getName());

        // Validate the member
        validateMember(member);

        // Check if email already exists
        Optional<Member> existingMember = memberRepository.findByEmail(member.getEmail());
        if (existingMember.isPresent()) {
            throw new Exception("Email " + member.getEmail() + " already exists");
        }

        // Save the member
        Member savedMember = memberRepository.save(member);

        // Notify listeners of the new registration
        eventPublisher.publishEvent(new MemberRegisteredEvent(this, savedMember));

        return savedMember;
    }

    /**
     * Find a member by ID
     * @param id the member ID
     * @return the member, if found
     */
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * Get all members
     * @return list of all members
     */
    public Iterable<Member> findAll() {
        return memberRepository.findAll();
    }

    /**
     * Delete a member
     * @param id the member ID to delete
     */
    @Transactional
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }

    /**
     * Validate member against bean validation constraints
     * @param member the member to validate
     * @throws ConstraintViolationException if validation fails
     */
    private void validateMember(Member member) {
        // Create a bean validator and check for issues
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
