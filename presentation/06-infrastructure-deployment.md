# Infrastructure & Deployment

---

### Docker Containerization

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

**Key Docker Features:**
- Multi-stage build to reduce final image size
- Uses Eclipse Temurin (AdoptOpenJDK) for Java 21
- Production profile activated by default
- Exposes port 8080 for web access

---

### Docker Compose Configurations

**Standard H2 Configuration (docker-compose.yml):**
```yaml
version: '3.8'

services:
  app:
    build: .
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:kitchensink
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=password
    volumes:
      - ./data:/app/data
```

**MongoDB Configuration (docker-compose-mongo.yml):**
```yaml
version: '3.8'

services:
  app:
    build: .
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MONGODB_ENABLED=true
      - MONGODB_URI=mongodb://mongodb:27017/kitchensink
    depends_on:
      - mongodb
  
  mongodb:
    image: mongo
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

volumes:
  mongodb_data:
```

---

### Kubernetes Deployment with Helm

**Helm Chart Structure:**
```
charts/
├── .helmignore
├── Chart.yaml
├── values.yaml
├── templates/
│   ├── _helpers.tpl
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── NOTES.txt
└── charts/
```

**Deployment Template:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "kitchensink.fullname" . }}
  labels:
    {{- include "kitchensink.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "kitchensink.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "kitchensink.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: {{ .Values.env.profile }}
            {{- if .Values.env.MONGODB_ENABLED }}
            - name: MONGODB_ENABLED
              value: "{{ .Values.env.MONGODB_ENABLED }}"
            - name: MONGODB_URI
              value: {{ .Values.env.MONGODB_URI }}
            {{- end }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
```

---

### Database Flexibility

**Configuration for H2:**
```java
@Configuration
@Profile("!mongodb")
public class H2Config {
    @Bean
    public CommandLineRunner initDatabase(MemberRepository repository) {
        return args -> {
            // Initialize with sample data
            repository.save(new Member("John Doe", "john@example.com", "1234567890"));
            repository.save(new Member("Jane Smith", "jane@example.com", "0987654321"));
            // More sample data...
        };
    }
}
```

**Configuration for MongoDB:**
```java
@Configuration
@Profile("mongodb")
@EnableMongoRepositories(basePackages = "com.example.kitchensink.repository.mongo")
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public class MongoConfig {
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "kitchensink");
    }
    
    @Bean
    public CommandLineRunner initMongoDB(MongoMemberRepository repository) {
        return args -> {
            // Initialize with sample data
            repository.save(new MongoMember("John Doe", "john@example.com", "1234567890"));
            repository.save(new MongoMember("Jane Smith", "jane@example.com", "0987654321"));
            // More sample data...
        };
    }
}
```

---

### Environment Configuration

**application.properties (default):**
```properties
# Server configuration
server.port=8080

# H2 Database configuration
spring.datasource.url=jdbc:h2:mem:kitchensinkdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# MongoDB configuration (disabled by default)
mongodb.enabled=false
```

**application-prod.properties:**
```properties
# Disable development features
spring.h2.console.enabled=false
spring.jpa.show-sql=false

# Optimize for production
spring.jpa.hibernate.ddl-auto=update
server.tomcat.max-threads=200
```
