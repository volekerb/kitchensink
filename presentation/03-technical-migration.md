# Technical Migration Demonstration

---

### Core Infrastructure Migration

#### Project Structure Comparison

**JBoss Structure:**
```
kitchensink/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/jboss/as/quickstarts/kitchensink/
│   │   │       ├── controller/
│   │   │       ├── data/
│   │   │       ├── model/
│   │   │       ├── rest/
│   │   │       ├── service/
│   │   │       └── util/
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   └── import.sql
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── beans.xml
│   │       │   ├── faces-config.xml
│   │       │   └── templates/
│   │       └── index.xhtml
│   └── test/
└── pom.xml
```

**Spring Boot Structure:**
```
kitchensink-spring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/kitchensink/
│   │   │       ├── controller/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       ├── rest/
│   │   │       ├── service/
│   │   │       ├── config/
│   │   │       └── exception/
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── static/
│   │   │   ├── templates/
│   │   │   └── data.sql
│   └── test/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── docker-compose-mongo.yml
```

---

### Entity Migration

**JBoss Entity:**
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
    
    // getters and setters
}
```

**Spring Boot Entity:**
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

---

### Repository Layer Migration

**JBoss Repository:**
```java
@ApplicationScoped
public class MemberRepository {
    @Inject
    private EntityManager em;
    
    public Member findById(Long id) {
        return em.find(Member.class, id);
    }
    
    public Member findByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Member> criteria = cb.createQuery(Member.class);
        Root<Member> member = criteria.from(Member.class);
        criteria.select(member).where(cb.equal(member.get("email"), email));
        return em.createQuery(criteria).getSingleResult();
    }
    
    public List<Member> findAllOrderedByName() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Member> criteria = cb.createQuery(Member.class);
        Root<Member> member = criteria.from(Member.class);
        criteria.select(member).orderBy(cb.asc(member.get("name")));
        return em.createQuery(criteria).getResultList();
    }
    
    public void persist(Member member) {
        em.persist(member);
    }
}
```

**Spring Boot Repository:**
```java
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    
    List<Member> findAllByOrderByNameAsc();
    
    @Query("SELECT m FROM Member m WHERE m.email LIKE %:domain%")
    List<Member> findByEmailDomain(@Param("domain") String domain);
}
```

---

### Key Differences in Repository Layer

- **JBoss approach:**
  - Manual EntityManager injection
  - Custom criteria queries
  - Manual transaction management

- **Spring Boot approach:**
  - Interface-based repositories
  - Method name conventions for queries
  - Declarative transaction management
  - Less boilerplate code
