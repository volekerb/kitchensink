# MongoDB Integration (Stretch Goal)

---

### MongoDB Entity Adaptation

**JPA Entity:**
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Member {
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
    
    // getters and setters
}
```

**MongoDB Document:**
```java
@Document(collection = "members")
public class MongoMember {
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
    
    private Date createdDate;
    
    @PrePersist
    public void prePersist() {
        createdDate = new Date();
    }
    
    // getters and setters
}
```

---

### MongoDB Repository Adaptation

**JPA Repository:**
```java
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    
    List<Member> findAllByOrderByNameAsc();
    
    @Query("SELECT m FROM Member m WHERE m.email LIKE %:domain%")
    List<Member> findByEmailDomain(@Param("domain") String domain);
}
```

**MongoDB Repository:**
```java
@Repository
public interface MongoMemberRepository extends MongoRepository<MongoMember, String> {
    MongoMember findByEmail(String email);
    
    List<MongoMember> findAllByOrderByNameAsc();
    
    List<MongoMember> findByEmailContaining(String domain);
    
    @Query("{'email': {$regex: ?0, $options: 'i'}}")
    List<MongoMember> findByEmailDomainCustomQuery(String domain);
}
```

---

### Service Adaptation for MongoDB

**Unified Service Interface:**
```java
public interface MemberServiceInterface {
    Object register(Object member);
    List<?> getAllMembers();
    Object findById(String id);
    void deleteMember(String id);
}
```

**MongoDB Service Implementation:**
```java
@Service
@Profile("mongodb")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public class MongoMemberService implements MemberServiceInterface {
    private final MongoMemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public MongoMemberService(MongoMemberRepository memberRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public MongoMember register(Object memberObj) {
        MongoMember member = (MongoMember) memberObj;
        
        // Check if email already exists
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new EmailAlreadyExistsException(member.getEmail());
        }
        
        MongoMember savedMember = memberRepository.save(member);
        eventPublisher.publishEvent(new MemberRegisteredEvent(savedMember));
        return savedMember;
    }
    
    @Override
    public List<MongoMember> getAllMembers() {
        return memberRepository.findAllByOrderByNameAsc();
    }
    
    @Override
    public MongoMember findById(String id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFoundException(id));
    }
    
    @Override
    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new MemberNotFoundException(id);
        }
        memberRepository.deleteById(id);
    }
}
```

---

### Controller Adaptation for MongoDB

**REST Controller Factory:**
```java
@Configuration
public class ControllerConfig {
    @Bean
    @ConditionalOnProperty(name = "mongodb.enabled", havingValue = "false", matchIfMissing = true)
    public MemberResourceRESTController jpaController(MemberService memberService) {
        return new MemberResourceRESTController(memberService);
    }
    
    @Bean
    @ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
    public MongoMemberResourceRESTController mongoController(MongoMemberService memberService) {
        return new MongoMemberResourceRESTController(memberService);
    }
}
```

**MongoDB REST Controller:**
```java
@RestController
@RequestMapping("/api/members")
@Profile("mongodb")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public class MongoMemberResourceRESTController {
    private final MongoMemberService memberService;
    
    public MongoMemberResourceRESTController(MongoMemberService memberService) {
        this.memberService = memberService;
    }
    
    @GetMapping
    public List<MongoMember> getAllMembers() {
        return memberService.getAllMembers();
    }
    
    @GetMapping("/{id}")
    public MongoMember getMemberById(@PathVariable String id) {
        return memberService.findById(id);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MongoMember createMember(@Valid @RequestBody MongoMember member) {
        return memberService.register(member);
    }
    
    // Other endpoints...
}
```

---

### MongoDB Configuration

**MongoDB Properties:**
```properties
# MongoDB configuration
mongodb.enabled=true
spring.data.mongodb.uri=mongodb://localhost:27017/kitchensink
```

**MongoDB Configuration Class:**
```java
@Configuration
@Profile("mongodb")
@EnableMongoRepositories(basePackages = "com.example.kitchensink.repository.mongo")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public class MongoConfig {
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(
            Arrays.asList(
                new DateToZonedDateTimeConverter(),
                new ZonedDateTimeToDateConverter()
            )
        );
    }
    
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(
            LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }
}
```
