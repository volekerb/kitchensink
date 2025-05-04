# Kitchensink Components Documentation

This document provides detailed information about each component in the Kitchensink application, explaining their purpose, functionality, and relationships.

## Domain Model

### Member.java

**Package**: `org.jboss.as.quickstarts.kitchensink.model`

**Purpose**: Represents a member entity in the system with personal information.

**Key Features**:
- JPA entity mapped to the database
- Bean Validation constraints for data integrity
- Properties include:
  - `id`: Unique identifier (auto-generated)
  - `name`: Member's name (must not contain numbers)
  - `email`: Email address (must be valid format and unique)
  - `phoneNumber`: Contact number (must be valid format)

**Code Highlights**:
```java
@Entity
@XmlRootElement
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Member implements Serializable {

    @Id
    @GeneratedValue
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

## Data Access Layer

### MemberRepository.java

**Package**: `org.jboss.as.quickstarts.kitchensink.data`

**Purpose**: Provides data access methods for the Member entity.

**Key Features**:
- Uses JPA's EntityManager for database operations
- Provides methods to find members by ID or email
- Retrieves all members sorted by name

**Code Highlights**:
```java
@ApplicationScoped
public class MemberRepository {

    @Inject
    private EntityManager em;

    public Member findById(Long id) {
        return em.find(Member.class, id);
    }

    public Member findByEmail(String email) {
        // Uses JPA Criteria API to find a member by email
    }

    public List<Member> findAllOrderedByName() {
        // Uses JPA Criteria API to get all members ordered by name
    }
}
```

### MemberListProducer.java

**Package**: `org.jboss.as.quickstarts.kitchensink.data`

**Purpose**: Produces a list of members for use in the UI and keeps it updated.

**Key Features**:
- CDI producer for a list of all members
- Updates the list when members are added or changed
- Uses CDI events to detect changes

**Code Highlights**:
```java
@ApplicationScoped
public class MemberListProducer {

    @Inject
    private MemberRepository repository;

    private List<Member> members;

    // Method to retrieve all members from the database
    @Produces
    @Named
    public List<Member> getMembers() {
        return members;
    }

    // Method called after construction
    public void onMemberListChanged(@Observes final Member member) {
        retrieveAllMembersOrderedByName();
    }

    @PostConstruct
    public void retrieveAllMembersOrderedByName() {
        members = repository.findAllOrderedByName();
    }
}
```

## Service Layer

### MemberRegistration.java

**Package**: `org.jboss.as.quickstarts.kitchensink.service`

**Purpose**: Handles the business logic for registering new members.

**Key Features**:
- Stateless EJB with transaction management
- Persists new members to the database
- Fires events when a member is registered

**Code Highlights**:
```java
@Stateless
public class MemberRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Member> memberEventSrc;

    public void register(Member member) throws Exception {
        log.info("Registering " + member.getName());
        em.persist(member);
        memberEventSrc.fire(member);
    }
}
```

## Controller Layer

### MemberController.java

**Package**: `org.jboss.as.quickstarts.kitchensink.controller`

**Purpose**: Handles web requests for member registration.

**Key Features**:
- JSF backing bean for the registration form
- Validates input and displays messages
- Uses the service layer to register members

**Code Highlights**:
```java
@Model
public class MemberController {

    @Inject
    private FacesContext facesContext;

    @Inject
    private MemberRegistration memberRegistration;

    @Produces
    @Named
    private Member newMember;

    @PostConstruct
    public void initNewMember() {
        newMember = new Member();
    }

    public void register() throws Exception {
        try {
            memberRegistration.register(newMember);
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registered!", "Registration successful");
            facesContext.addMessage(null, m);
            initNewMember();
        } catch (Exception e) {
            String errorMessage = getRootErrorMessage(e);
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, "Registration unsuccessful");
            facesContext.addMessage(null, m);
        }
    }

    private String getRootErrorMessage(Exception e) {
        // Error handling logic
    }
}
```

## REST API

### JaxRsActivator.java

**Package**: `org.jboss.as.quickstarts.kitchensink.rest`

**Purpose**: Configures the JAX-RS application.

**Key Features**:
- Extends `Application` class
- Sets the path for REST endpoints to `/rest`

**Code Highlights**:
```java
@ApplicationPath("/rest")
public class JaxRsActivator extends Application {
    // Empty body
}
```

### MemberResourceRESTService.java

**Package**: `org.jboss.as.quickstarts.kitchensink.rest`

**Purpose**: Provides REST endpoints for member operations.

**Key Features**:
- Endpoints for listing, retrieving, and creating members
- Validation of input data
- Proper error handling and status codes

**Code Highlights**:
```java
@Path("/members")
@RequestScoped
public class MemberResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    private Validator validator;

    @Inject
    private MemberRepository repository;

    @Inject
    MemberRegistration registration;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Member> listAllMembers() {
        return repository.findAllOrderedByName();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Member lookupMemberById(@PathParam("id") long id) {
        Member member = repository.findById(id);
        if (member == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return member;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMember(Member member) {
        // Validation and member creation logic
    }

    // Helper methods for validation
}
```

## Utility Components

### Resources.java

**Package**: `org.jboss.as.quickstarts.kitchensink.util`

**Purpose**: Produces resources needed by the application.

**Key Features**:
- Produces a configured `Logger` instance
- Produces an `EntityManager` for database access
- Produces a `FacesContext` for JSF operations

**Code Highlights**:
```java
public class Resources {

    @Produces
    @PersistenceContext
    private EntityManager em;

    @Produces
    public Logger produceLog(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    @Produces
    @RequestScoped
    public FacesContext produceFacesContext() {
        return FacesContext.getCurrentInstance();
    }
}
```

## Web Interface

### index.xhtml

**Location**: `src/main/webapp/index.xhtml`

**Purpose**: Main page of the application with registration form and member list.

**Key Features**:
- Registration form with validation
- Table displaying all registered members
- Links to REST endpoints for each member

**Code Highlights**:
```xml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="jakarta.faces.facelets"
                xmlns:f="jakarta.faces.core"
                xmlns:h="jakarta.faces.html"
                template="/WEB-INF/templates/default.xhtml">
    <ui:define name="content">
        <h1>Welcome to JBoss!</h1>

        <!-- Registration Form -->
        <h:form id="reg">
            <h2>Member Registration</h2>
            <!-- Form fields with validation -->
        </h:form>

        <!-- Member List -->
        <h2>Members</h2>
        <h:dataTable var="_member" value="#{members}"
                     rendered="#{not empty members}"
                     styleClass="simpletablestyle">
            <!-- Table columns -->
        </h:dataTable>
    </ui:define>
</ui:composition>
```

## Configuration Files

### persistence.xml

**Location**: `src/main/resources/META-INF/persistence.xml`

**Purpose**: Configures JPA persistence.

**Key Features**:
- Defines the persistence unit
- Configures the datasource
- Sets Hibernate properties

**Code Highlights**:
```xml
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
   <persistence-unit name="primary">
      <jta-data-source>java:jboss/datasources/KitchensinkQuickstartDS</jta-data-source>
      <properties>
         <property name="hibernate.hbm2ddl.auto" value="create-drop" />
         <property name="hibernate.show_sql" value="false" />
      </properties>
   </persistence-unit>
</persistence>
```

### kitchensink-quickstart-ds.xml

**Location**: `src/main/webapp/WEB-INF/kitchensink-quickstart-ds.xml`

**Purpose**: Configures the datasource for the application.

**Key Features**:
- Defines an H2 in-memory database
- Sets connection parameters
- Configures the JNDI name

**Code Highlights**:
```xml
<datasources xmlns="http://www.jboss.org/ironjacamar/schema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.jboss.org/ironjacamar/schema http://docs.jboss.org/ironjacamar/schema/datasources_1_0.xsd">
    <datasource jndi-name="java:jboss/datasources/KitchensinkQuickstartDS"
        pool-name="kitchensink-quickstart" enabled="true"
        use-java-context="true">
        <connection-url>jdbc:h2:mem:kitchensink-quickstart;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1</connection-url>
        <driver>h2</driver>
        <security>
            <user-name>sa</user-name>
            <password>sa</password>
        </security>
    </datasource>
</datasources>
```

### beans.xml

**Location**: `src/main/webapp/WEB-INF/beans.xml`

**Purpose**: Configures CDI.

**Key Features**:
- Enables CDI for the application
- Configures bean discovery mode

**Code Highlights**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd"
       version="3.0"
       bean-discovery-mode="all">
</beans>
```

## Component Relationships

### Dependency Graph

```
Web Interface (JSF)
    ↓
MemberController
    ↓
MemberRegistration ← MemberResourceRESTService (REST API)
    ↓                   ↓
EntityManager       MemberRepository
    ↓                   ↓
Database            MemberListProducer
                        ↑
                    JSF Views
```

### Event Flow

1. User submits registration form → MemberController
2. MemberController calls MemberRegistration.register()
3. MemberRegistration persists the Member and fires event
4. MemberListProducer observes the event and updates the member list
5. Updated list is displayed in the UI
