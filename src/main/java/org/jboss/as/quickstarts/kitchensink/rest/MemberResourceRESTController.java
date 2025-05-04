package org.jboss.as.quickstarts.kitchensink.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * REST controller for managing Member resources
 * Replaces the original JAX-RS MemberResourceRESTService
 */
@RestController
@RequestMapping("/api/members")
public class MemberResourceRESTController {
    
    private static final Logger log = Logger.getLogger(MemberResourceRESTController.class.getName());
    
    private final MemberService memberService;
    
    public MemberResourceRESTController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    /**
     * List all members
     * @return list of members
     */
    @GetMapping
    public List<Member> listAllMembers() {
        return (List<Member>) memberService.findAll();
    }
    
    /**
     * Look up a member by id
     * @param id the id of the member
     * @return the member if found, or 404 response
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> lookupMemberById(@PathVariable("id") Long id) {
        log.info("Fetching member with id: " + id);
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new member
     * @param member the member to create
     * @return 201 CREATED response with the created member
     */
    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        log.info("Creating member: " + member.getName());
        
        try {
            Member registered = memberService.register(member);
            return ResponseEntity.status(HttpStatus.CREATED).body(registered);
        } catch (ConstraintViolationException e) {
            return createViolationResponse(e.getConstraintViolations());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Delete a member by id
     * @param id the id of the member to delete
     * @return 204 NO CONTENT response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("id") Long id) {
        log.info("Deleting member with id: " + id);
        
        try {
            memberService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a response for bean validation errors
     * @param violations the constraint violations
     * @return 400 BAD REQUEST response with validation errors
     */
    private ResponseEntity<Map<String, Object>> createViolationResponse(Set<? extends ConstraintViolation<?>> violations) {
        log.info("Validation errors occurred");
        
        Map<String, String> validationErrors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : violations) {
            validationErrors.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            );
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("violations", validationErrors);
        
        return ResponseEntity.badRequest().body(body);
    }
}
