# Presentation Layer Migration

---

### REST API Migration

**JBoss REST Resource:**
```java
@Path("/members")
@RequestScoped
public class MemberResourceRESTService {
    @Inject
    private MemberRepository repository;
    
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
        Response.ResponseBuilder builder = null;
        try {
            // Validate the member
            validateMember(member);
            
            // Register the member
            registration.register(member);
            
            // Create a response with 201 Created
            builder = Response.status(Response.Status.CREATED)
                              .entity(member);
        } catch (ConstraintViolationException ce) {
            // Handle validation errors
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (Exception e) {
            // Handle generic exceptions
            builder = Response.status(Response.Status.BAD_REQUEST)
                              .entity(e.getMessage());
        }
        return builder.build();
    }
    
    // Other methods...
}
```

**Spring Boot REST Controller:**
```java
@RestController
@RequestMapping("/api/members")
public class MemberResourceRESTController {
    private final MemberService memberService;
    
    public MemberResourceRESTController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }
    
    @GetMapping("/{id}")
    public Member getMemberById(@PathVariable Long id) {
        return memberService.findById(id);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member createMember(@Valid @RequestBody Member member) {
        return memberService.register(member);
    }
    
    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Long id, 
                              @Valid @RequestBody Member member) {
        return memberService.updateMember(id, member);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }
}

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleMemberNotFound(MemberNotFoundException ex) {
        return new ApiError(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return new ApiError(HttpStatus.CONFLICT, ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                               .getFieldErrors()
                               .stream()
                               .map(error -> error.getField() + ": " + error.getDefaultMessage())
                               .collect(Collectors.toList());
        return new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }
}
```

---

### UI Migration

**JBoss JSF View:**
```xml
<h:form id="reg">
    <h2>Member Registration</h2>
    <p>Enforces annotation-based constraints defined on the model class.</p>
    <h:panelGrid columns="3" columnClasses="titleCell">
        <h:outputLabel for="name" value="Name:"/>
        <h:inputText id="name" value="#{newMember.name}"/>
        <h:message for="name" errorClass="invalid"/>

        <h:outputLabel for="email" value="Email:"/>
        <h:inputText id="email" value="#{newMember.email}"/>
        <h:message for="email" errorClass="invalid"/>

        <h:outputLabel for="phoneNumber" value="Phone #:"/>
        <h:inputText id="phoneNumber" value="#{newMember.phoneNumber}"/>
        <h:message for="phoneNumber" errorClass="invalid"/>
    </h:panelGrid>

    <p>
        <h:commandButton id="register" action="#{memberController.register}" value="Register" styleClass="register"/>
        <h:messages styleClass="messages" errorClass="invalid" infoClass="valid" warnClass="warning"
                    globalOnly="true"/>
    </p>
</h:form>
```

**Spring Boot Thymeleaf View:**
```html
<form th:action="@{/members}" th:object="${member}" method="post" class="needs-validation" novalidate>
    <h2>Member Registration</h2>
    <p>Enter your details below to register.</p>
    
    <div class="alert alert-danger" th:if="${#fields.hasErrors('*')}">
        <p>Please correct the errors below</p>
    </div>
    
    <div class="mb-3">
        <label for="name" class="form-label">Name:</label>
        <input type="text" class="form-control" id="name" th:field="*{name}" 
               th:classappend="${#fields.hasErrors('name')} ? 'is-invalid' : ''">
        <div class="invalid-feedback" th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></div>
    </div>
    
    <div class="mb-3">
        <label for="email" class="form-label">Email:</label>
        <input type="email" class="form-control" id="email" th:field="*{email}"
               th:classappend="${#fields.hasErrors('email')} ? 'is-invalid' : ''">
        <div class="invalid-feedback" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></div>
    </div>
    
    <div class="mb-3">
        <label for="phoneNumber" class="form-label">Phone #:</label>
        <input type="text" class="form-control" id="phoneNumber" th:field="*{phoneNumber}"
               th:classappend="${#fields.hasErrors('phoneNumber')} ? 'is-invalid' : ''">
        <div class="invalid-feedback" th:if="${#fields.hasErrors('phoneNumber')}" th:errors="*{phoneNumber}"></div>
    </div>
    
    <button type="submit" class="btn btn-primary">Register</button>
    
    <div class="alert alert-success mt-3" th:if="${successMessage}" th:text="${successMessage}"></div>
</form>
```

---

### Web Controller Migration

**JBoss Controller:**
```java
@Model
public class MemberController {
    @Inject
    private FacesContext facesContext;
    
    @Inject
    private MemberRegistration memberRegistration;
    
    private Member newMember;
    
    @Produces
    @Named
    public Member getNewMember() {
        return newMember;
    }
    
    public void register() throws Exception {
        try {
            memberRegistration.register(newMember);
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                             "Registered!", "Registration successful");
            facesContext.addMessage(null, m);
            initNewMember();
        } catch (Exception e) {
            String errorMessage = getRootErrorMessage(e);
            FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                             errorMessage, "Registration unsuccessful");
            facesContext.addMessage(null, m);
        }
    }
    
    @PostConstruct
    public void initNewMember() {
        newMember = new Member();
    }
    
    // Helper methods...
}
```

**Spring Boot Controller:**
```java
@Controller
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "members/list";
    }
    
    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        model.addAttribute("member", new Member());
        return "members/register";
    }
    
    @PostMapping
    public String registerMember(@Valid @ModelAttribute("member") Member member, 
                                BindingResult result, 
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "members/register";
        }
        
        try {
            memberService.register(member);
            redirectAttributes.addFlashAttribute("successMessage", 
                                               "Member registered successfully!");
            return "redirect:/members";
        } catch (EmailAlreadyExistsException e) {
            result.rejectValue("email", "error.email", "Email already in use");
            return "members/register";
        }
    }
    
    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "members/view";
    }
    
    // Other handler methods...
}
```

---

### Key Differences in Presentation Layer

- **REST API:**
  - JBoss: JAX-RS with `@Path`, `@GET`, `@POST`, etc.
  - Spring Boot: Spring MVC with `@RestController`, `@GetMapping`, `@PostMapping`, etc.

- **Web UI:**
  - JBoss: JSF with XML-based templates
  - Spring Boot: Thymeleaf with HTML templates

- **Form Handling:**
  - JBoss: JSF managed beans with CDI
  - Spring Boot: Controller methods with `@ModelAttribute` and `BindingResult`

- **Exception Handling:**
  - JBoss: Manual exception handling in controllers
  - Spring Boot: Global exception handling with `@RestControllerAdvice`
