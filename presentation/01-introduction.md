# Migrating JBoss Kitchensink to Spring Boot
## A Modern Application Migration Journey

---

### The Challenge

- Migrate the legacy JBoss 'kitchensink' application to Spring Boot
- Target the latest stable Spring Boot version with Java 21
- Create a new public GitHub repository with comprehensive documentation
- Apply enterprise-grade migration practices suitable for larger codebases
- Optional: Modify the application to work with MongoDB

---

### Original Application

- JBoss EAP Quickstart 'kitchensink' application
- Built with Java EE technologies:
  - CDI (Contexts and Dependency Injection)
  - JPA (Java Persistence API)
  - JAX-RS (Java API for RESTful Web Services)
  - Bean Validation
  - JSF (JavaServer Faces)
- Designed to demonstrate JBoss EAP capabilities

---

### Migration Target

- Spring Boot 3.x with Java 21
- Modern application architecture:
  - Spring MVC for controllers
  - Spring Data JPA for repositories
  - Thymeleaf for view templates
  - Spring Validation
  - OpenAPI/Swagger for API documentation
- Containerization with Docker
- Kubernetes deployment with Helm
- Dual database support: H2 and MongoDB
