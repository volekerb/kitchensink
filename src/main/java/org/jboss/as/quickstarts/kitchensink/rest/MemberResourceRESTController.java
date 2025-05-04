package org.jboss.as.quickstarts.kitchensink.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Member", description = "Member management API")
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
    @Operation(summary = "Get all members", description = "Returns a list of all registered members")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of members")
    public List<Member> listAllMembers() {
        return (List<Member>) memberService.findAll();
    }
    
    /**
     * Look up a member by id
     * @param id the id of the member
     * @return the member if found, or 404 response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a member by ID", description = "Returns a member as per the ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the member"),
            @ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
    })
    public ResponseEntity<Member> lookupMemberById(
            @Parameter(description = "ID of the member to retrieve") 
            @PathVariable("id") Long id) {
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
    @Operation(summary = "Create a new member", description = "Creates a new member and returns the created entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid member data supplied"),
            @ApiResponse(responseCode = "409", description = "Member with the same email already exists")
    })
    public ResponseEntity<?> createMember(
            @Parameter(description = "Member to create", required = true, schema = @Schema(implementation = Member.class))
            @RequestBody Member member) {
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
    @Operation(summary = "Delete a member", description = "Deletes a member by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "ID of the member to delete")
            @PathVariable("id") Long id) {
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
