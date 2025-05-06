# Business Logic Migration

---

### Service Layer Migration

**JBoss Service:**
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

**Spring Boot Service:**
```java
@Service
@Transactional
public class MemberService {
    private final Logger log = LoggerFactory.getLogger(MemberService.class);
    
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public MemberService(MemberRepository memberRepository, 
                         ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public Member register(Member member) {
        log.info("Registering {}", member.getName());
        
        // Validation logic
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new EmailAlreadyExistsException(member.getEmail());
        }
        
        Member savedMember = memberRepository.save(member);
        eventPublisher.publishEvent(new MemberRegisteredEvent(savedMember));
        return savedMember;
    }
    
    public List<Member> getAllMembers() {
        return memberRepository.findAllByOrderByNameAsc();
    }
    
    public Member findById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFoundException(id));
    }
    
    // Additional service methods
}
```

---

### Event Handling Migration

**JBoss Event Handling:**
```java
// Event producer
@Stateless
public class MemberRegistration {
    @Inject
    private Event<Member> memberEventSrc;
    
    public void register(Member member) throws Exception {
        // ... other code
        memberEventSrc.fire(member);
    }
}

// Event observer
@ApplicationScoped
public class MemberListProducer {
    @Inject
    private MemberRepository repository;
    
    private List<Member> members;
    
    @Produces
    @Named
    public List<Member> getMembers() {
        return members;
    }
    
    public void onMemberListChanged(@Observes(notifyObserver = Reception.IF_EXISTS) 
                                   final Member member) {
        retrieveAllMembersOrderedByName();
    }
    
    @PostConstruct
    public void retrieveAllMembersOrderedByName() {
        members = repository.findAllOrderedByName();
    }
}
```

**Spring Boot Event Handling:**
```java
// Event class
public class MemberRegisteredEvent {
    private final Member member;
    
    public MemberRegisteredEvent(Member member) {
        this.member = member;
    }
    
    public Member getMember() {
        return member;
    }
}

// Event publisher
@Service
public class MemberService {
    private final ApplicationEventPublisher eventPublisher;
    
    // ... other code
    
    public Member register(Member member) {
        // ... other code
        Member savedMember = memberRepository.save(member);
        eventPublisher.publishEvent(new MemberRegisteredEvent(savedMember));
        return savedMember;
    }
}

// Event listener
@Component
public class MemberEventListener {
    private final MemberRepository memberRepository;
    private List<Member> members;
    
    public MemberEventListener(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.members = memberRepository.findAllByOrderByNameAsc();
    }
    
    @EventListener
    public void handleMemberRegisteredEvent(MemberRegisteredEvent event) {
        // Refresh the member list
        this.members = memberRepository.findAllByOrderByNameAsc();
    }
    
    public List<Member> getMembers() {
        return members;
    }
}
```

---

### Key Differences in Business Logic

- **Dependency Injection:**
  - JBoss: Field-based injection with `@Inject`
  - Spring Boot: Constructor-based injection (preferred)

- **Transaction Management:**
  - JBoss: `@Stateless` EJBs with container-managed transactions
  - Spring Boot: `@Transactional` annotation with declarative transactions

- **Event System:**
  - JBoss: CDI events with `@Observes`
  - Spring Boot: Spring's `ApplicationEventPublisher` with `@EventListener`

- **Exception Handling:**
  - JBoss: Often uses checked exceptions
  - Spring Boot: Typically uses runtime exceptions with global exception handlers
