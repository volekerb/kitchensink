package org.jboss.as.quickstarts.kitchensink.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestExceptionHandlerTest {

    private RestExceptionHandler exceptionHandler;
    private Validator validator;

    @BeforeEach
    public void setup() {
        exceptionHandler = new RestExceptionHandler();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void testHandleValidationExceptions() {
        // Create a mock MethodArgumentNotValidException
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        // Create field errors
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("member", "name", "Name cannot be empty"));
        fieldErrors.add(new FieldError("member", "email", "Invalid email format"));
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        
        // Call the handler
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);
        
        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("violations"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> violations = (Map<String, String>) response.getBody().get("violations");
        assertEquals(2, violations.size());
        assertEquals("Name cannot be empty", violations.get("name"));
        assertEquals("Invalid email format", violations.get("email"));
    }

    @Test
    public void testHandleConstraintViolation() {
        // Create an invalid member to generate constraint violations
        Member invalidMember = new Member();
        invalidMember.setName("");  // Invalid: name is required
        invalidMember.setEmail("invalid-email");  // Invalid: not a proper email
        invalidMember.setPhoneNumber("123");  // Invalid: too short
        
        Set<ConstraintViolation<Member>> violations = validator.validate(invalidMember);
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);
        
        // Call the handler
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolation(exception);
        
        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("violations"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> violationMap = (Map<String, String>) response.getBody().get("violations");
        assertTrue(violationMap.size() >= 3);  // At least 3 violations (name, email, phoneNumber)
    }

    @Test
    public void testHandleGeneralExceptions() {
        // Create a general exception
        Exception exception = new RuntimeException("Something went wrong");
        
        // Call the handler
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGeneralExceptions(exception);
        
        // Verify response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Something went wrong", response.getBody().get("error"));
    }
}