# Kitchensink Migration Guide

This guide provides detailed instructions for migrating the JBoss Kitchensink application to a modern Spring Boot platform using Java 21.

## Table of Contents

1. [Migration Strategy](#migration-strategy)
2. [Technology Mapping](#technology-mapping)
3. [Step-by-Step Migration Process](#step-by-step-migration-process)
4. [Testing and Validation](#testing-and-validation)
5. [MongoDB Migration (Optional)](#mongodb-migration-optional)

## Migration Strategy

### Approach

The recommended migration approach is incremental and component-based:

1. **Setup & Analysis**: Create the new project structure and analyze the existing application
2. **Core Components**: Migrate the domain model and data access layer first
3. **Business Logic**: Migrate the service layer next
4. **Presentation Layer**: Migrate the REST API and web interface last
5. **Testing**: Ensure comprehensive testing at each step

### Principles

- **Maintain Functionality**: Ensure all features work the same way after migration
- **Leverage Modern Features**: Use Java 21 and Spring Boot features where appropriate
- **Clean Architecture**: Maintain separation of concerns and SOLID principles
- **Continuous Testing**: Test each component as it's migrated

## Technology Mapping

| JBoss/Jakarta EE Component | Spring Boot Equivalent |
|----------------------------|------------------------|
| JPA/Hibernate | Spring Data JPA |
| CDI | Spring Dependency Injection |
| EJB | Spring Services |
| JAX-RS | Spring REST Controllers |
| Bean Validation | Spring Validation |
| JSF | Thymeleaf or Spring MVC |
| JTA | Spring Transaction Management |

## Step-by-Step Migration Process

### 1. Project Setup

1. **Create a new Spring Boot project**:
   - Use Spring Initializr (https://start.spring.io/)
   - Select Java 21
   - Add dependencies: Spring Web, Spring Data JPA, Validation, H2 Database

2. **Configure application properties**:
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:h2:mem:kitchensink
   spring.datasource.driverClassName=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=sa
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto=create-drop
   spring.h2.console.enabled=true
   ```

### 2. Domain Model Migration

1. **Migrate the Member entity**:
   - Create the package structure: `org.jboss.as.quickstarts.kitchensink.model`
   - Copy the Member class, updating annotations if needed
   - JPA annotations remain largely the same
   - Bean Validation annotations remain largely the same

2. **Example migrated Member entity**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.model;

   import jakarta.persistence.*;
   import jakarta.validation.constraints.*;
   import java.io.Serializable;

   @Entity
   @Table(name = "member", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
   public class Member implements Serializable {
       
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @NotNull
       @Size(min = 1, max = 25)
       @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
       private String name;
       
       @NotNull
       @NotEmpty
       @Email
       private String email;
       
       @NotNull
       @Size(min = 10, max = 12)
       @Digits(fraction = 0, integer = 12)
       @Column(name = "phone_number")
       private String phoneNumber;
       
       // Getters and setters
   }
   ```

### 3. Repository Layer Migration

1. **Create Spring Data JPA repository**:
   - Create the package: `org.jboss.as.quickstarts.kitchensink.repository`
   - Create a repository interface extending JpaRepository

2. **Example repository interface**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.repository;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.springframework.data.jpa.repository.JpaRepository;
   import org.springframework.stereotype.Repository;
   
   import java.util.List;
   import java.util.Optional;

   @Repository
   public interface MemberRepository extends JpaRepository<Member, Long> {
       
       Optional<Member> findByEmail(String email);
       
       List<Member> findAllByOrderByNameAsc();
   }
   ```

### 4. Service Layer Migration

1. **Create service interface and implementation**:
   - Create the package: `org.jboss.as.quickstarts.kitchensink.service`
   - Create a service interface and implementation

2. **Example service**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.service;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import java.util.List;
   import java.util.Optional;

   public interface MemberService {
       
       Member register(Member member) throws Exception;
       
       List<Member> findAllOrderedByName();
       
       Optional<Member> findById(Long id);
       
       boolean emailExists(String email);
   }
   ```

3. **Service implementation with event publishing**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.service;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.jboss.as.quickstarts.kitchensink.repository.MemberRepository;
   import org.springframework.context.ApplicationEventPublisher;
   import org.springframework.stereotype.Service;
   import org.springframework.transaction.annotation.Transactional;
   
   import java.util.List;
   import java.util.Optional;
   import java.util.logging.Logger;

   @Service
   public class MemberServiceImpl implements MemberService {
       
       private final Logger log = Logger.getLogger(MemberServiceImpl.class.getName());
       private final MemberRepository memberRepository;
       private final ApplicationEventPublisher eventPublisher;
       
       public MemberServiceImpl(MemberRepository memberRepository, ApplicationEventPublisher eventPublisher) {
           this.memberRepository = memberRepository;
           this.eventPublisher = eventPublisher;
       }
       
       @Override
       @Transactional
       public Member register(Member member) throws Exception {
           log.info("Registering " + member.getName());
           
           if (emailExists(member.getEmail())) {
               throw new Exception("Email already exists: " + member.getEmail());
           }
           
           Member savedMember = memberRepository.save(member);
           eventPublisher.publishEvent(new MemberRegistrationEvent(savedMember));
           return savedMember;
       }
       
       // Other methods implementation
   }
   ```

4. **Create event class**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.service;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.springframework.context.ApplicationEvent;

   public class MemberRegistrationEvent extends ApplicationEvent {
       
       public MemberRegistrationEvent(Member member) {
           super(member);
       }
       
       public Member getMember() {
           return (Member) getSource();
       }
   }
   ```

### 5. REST API Migration

1. **Create REST controller**:
   - Create the package: `org.jboss.as.quickstarts.kitchensink.rest`
   - Create a controller class with REST endpoints

2. **Example REST controller**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.rest;

   import jakarta.validation.Valid;
   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.jboss.as.quickstarts.kitchensink.service.MemberService;
   import org.springframework.http.HttpStatus;
   import org.springframework.http.ResponseEntity;
   import org.springframework.web.bind.annotation.*;
   import org.springframework.web.server.ResponseStatusException;
   
   import java.util.HashMap;
   import java.util.List;
   import java.util.Map;

   @RestController
   @RequestMapping("/api/members")
   public class MemberRestController {
       
       private final MemberService memberService;
       
       public MemberRestController(MemberService memberService) {
           this.memberService = memberService;
       }
       
       @GetMapping
       public List<Member> listAllMembers() {
           return memberService.findAllOrderedByName();
       }
       
       @GetMapping("/{id}")
       public Member lookupMemberById(@PathVariable("id") Long id) {
           return memberService.findById(id)
                   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
       }
       
       @PostMapping
       public ResponseEntity<?> createMember(@Valid @RequestBody Member member) {
           // Implementation
       }
   }
   ```

3. **Create exception handler**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.rest;

   import jakarta.validation.ConstraintViolation;
   import jakarta.validation.ConstraintViolationException;
   import org.springframework.http.HttpStatus;
   import org.springframework.http.ResponseEntity;
   import org.springframework.validation.FieldError;
   import org.springframework.web.bind.MethodArgumentNotValidException;
   import org.springframework.web.bind.annotation.ControllerAdvice;
   import org.springframework.web.bind.annotation.ExceptionHandler;
   
   import java.util.HashMap;
   import java.util.Map;
   import java.util.Set;

   @ControllerAdvice
   public class RestExceptionHandler {
       
       @ExceptionHandler(MethodArgumentNotValidException.class)
       public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
           // Implementation
       }
       
       // Other exception handlers
   }
   ```

### 6. Web Interface Migration

1. **Create Thymeleaf templates**:
   - Create directory: `src/main/resources/templates`
   - Create an index.html template

2. **Example Thymeleaf template**:
   ```html
   <!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org">
   <head>
       <title>Kitchensink</title>
       <meta charset="UTF-8">
       <meta name="viewport" content="width=device-width, initial-scale=1.0">
       <!-- CSS styles -->
   </head>
   <body>
       <h1>Member Registration</h1>
       
       <!-- Registration form -->
       <form th:action="@{/members}" method="post" th:object="${member}">
           <!-- Form fields -->
       </form>
       
       <!-- Member list -->
       <h2>Members</h2>
       <table>
           <!-- Table headers and rows -->
       </table>
   </body>
   </html>
   ```

3. **Create web controller**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.controller;

   import jakarta.validation.Valid;
   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.jboss.as.quickstarts.kitchensink.service.MemberService;
   import org.springframework.stereotype.Controller;
   import org.springframework.ui.Model;
   import org.springframework.validation.BindingResult;
   import org.springframework.web.bind.annotation.*;
   
   @Controller
   public class MemberController {
       
       private final MemberService memberService;
       
       public MemberController(MemberService memberService) {
           this.memberService = memberService;
       }
       
       @GetMapping("/")
       public String showRegistrationForm(Model model) {
           // Implementation
       }
       
       @PostMapping("/members")
       public String registerMember(@Valid @ModelAttribute("member") Member member, 
                                   BindingResult result, 
                                   Model model) {
           // Implementation
       }
   }
   ```

## Testing and Validation

### Unit Testing

1. **Entity tests**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.model;

   import jakarta.validation.Validation;
   import jakarta.validation.Validator;
   import jakarta.validation.ValidatorFactory;
   import org.junit.jupiter.api.BeforeEach;
   import org.junit.jupiter.api.Test;
   
   import static org.junit.jupiter.api.Assertions.*;

   public class MemberTest {
       
       private Validator validator;
       
       @BeforeEach
       public void setUp() {
           ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
           validator = factory.getValidator();
       }
       
       @Test
       public void testValidMember() {
           // Test implementation
       }
       
       // Other tests
   }
   ```

2. **Service tests**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.service;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.jboss.as.quickstarts.kitchensink.repository.MemberRepository;
   import org.junit.jupiter.api.Test;
   import org.junit.jupiter.api.extension.ExtendWith;
   import org.mockito.InjectMocks;
   import org.mockito.Mock;
   import org.mockito.junit.jupiter.MockitoExtension;
   import org.springframework.context.ApplicationEventPublisher;
   
   import static org.junit.jupiter.api.Assertions.*;
   import static org.mockito.Mockito.*;

   @ExtendWith(MockitoExtension.class)
   public class MemberServiceTest {
       
       @Mock
       private MemberRepository memberRepository;
       
       @Mock
       private ApplicationEventPublisher eventPublisher;
       
       @InjectMocks
       private MemberServiceImpl memberService;
       
       @Test
       public void testRegisterMember() throws Exception {
           // Test implementation
       }
       
       // Other tests
   }
   ```

### Integration Testing

1. **Repository tests**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.repository;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
   
   import static org.junit.jupiter.api.Assertions.*;

   @DataJpaTest
   public class MemberRepositoryTest {
       
       @Autowired
       private MemberRepository memberRepository;
       
       @Test
       public void testFindByEmail() {
           // Test implementation
       }
       
       // Other tests
   }
   ```

2. **Controller tests**:
   ```java
   package org.jboss.as.quickstarts.kitchensink.rest;

   import org.jboss.as.quickstarts.kitchensink.model.Member;
   import org.jboss.as.quickstarts.kitchensink.service.MemberService;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
   import org.springframework.boot.test.mock.mockito.MockBean;
   import org.springframework.test.web.servlet.MockMvc;
   
   import static org.mockito.Mockito.*;
   import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
   import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

   @WebMvcTest(MemberRestController.class)
   public class MemberRestControllerTest {
       
       @Autowired
       private MockMvc mockMvc;
       
       @MockBean
       private MemberService memberService;
       
       @Test
       public void testGetAllMembers() throws Exception {
           // Test implementation
       }
       
       // Other tests
   }
   ```

## MongoDB Migration (Optional)

### 1. Add MongoDB Dependencies

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### 2. Configure MongoDB

In `application.properties`:
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=kitchensink
```

### 3. Update Entity for MongoDB

```java
package org.jboss.as.quickstarts.kitchensink.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "members")
public class Member implements Serializable {
    
    @Id
    private String id;
    
    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String name;
    
    @NotNull
    @NotEmpty
    @Email
    @Indexed(unique = true)
    private String email;
    
    @NotNull
    @Size(min = 10, max = 12)
    @Digits(fraction = 0, integer = 12)
    private String phoneNumber;
    
    // Getters and setters
}
```

### 4. Create MongoDB Repository

```java
package org.jboss.as.quickstarts.kitchensink.repository;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    
    Optional<Member> findByEmail(String email);
    
    List<Member> findAllByOrderByNameAsc();
}
```

### 5. Update Service Implementation

Update the service implementation to work with the MongoDB repository. The interface can remain the same, but the implementation will need to handle the different ID type (String instead of Long).

### 6. Testing MongoDB Implementation

Create specific tests for the MongoDB implementation to ensure it works correctly.
