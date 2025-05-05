# Kitchensink Spring Boot Migration

This project is a migration of the JBoss Kitchensink quickstart application to Spring Boot with Java 21.

## Project Overview

The original Kitchensink application is a JBoss-based Java EE application that demonstrates Jakarta EE 8 and CDI capabilities. This project migrates that application to use:

- Spring Boot 3.x
- Java 21
- Spring Data JPA
- H2 Database (with optional MongoDB support)
- Spring MVC for REST endpoints
- Thymeleaf for server-side templating

## Features

- Member registration and listing
- Form validation with error messages
- RESTful API for CRUD operations
- Responsive Bootstrap UI
- API documentation with OpenAPI/Swagger
- Docker support for containerization
- Kubernetes deployment with Helm charts

## Architecture

The application follows a multi-layered architecture:

- **Presentation Layer**: Spring MVC controllers and Thymeleaf templates
- **Business Layer**: Services with transaction management
- **Data Access Layer**: Spring Data JPA repositories
- **Domain Model**: JPA entities with validation

## Getting Started

### Prerequisites

- Java 21 or later
- Maven 3.8+
- Git
- Docker (optional, for containerization)

### Building the Application

Clone the repository and build the application:

```bash
git clone <repository-url>
cd kitchensink-spring
./mvnw clean package
```

### Running the Application

#### Running Locally

You can run the application locally using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or you can run the JAR file directly after building:

```bash
java -jar target/kitchensink-spring-0.0.1-SNAPSHOT.jar
```

#### Running with Docker

Build and run the application using Docker:

```bash
# Build the Docker image
docker build -t kitchensink-spring .

# Run the container
docker run -p 8080:8080 kitchensink-spring
```

#### Running with Docker Compose

Run the application using Docker Compose:

```bash
docker-compose up
```

#### Running on Kubernetes with Helm

Deploy the application to Kubernetes using Helm:

```bash
# Build the Docker image
docker build -t kitchensink-spring:latest .

# Install the Helm chart
helm install kitchensink-spring ./charts

# To customize the deployment
helm install kitchensink-spring ./charts \
  --set replicaCount=2 \
  --set image.repository=your-registry/kitchensink-spring \
  --set image.tag=1.0.0
```

For more detailed instructions on Helm deployment, see the [deployment documentation](kitchensink-documentation/build-deploy.md#spring-boot-version).

### Accessing the Application

Once the application is running, you can access it at:

- Web Interface: http://localhost:8080
- REST API: http://localhost:8080/api/members
- API Documentation: http://localhost:8080/swagger-ui.html
- H2 Database Console (in development mode): http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:kitchensinkdb
  - Username: sa
  - Password: password

### Configuration Options

The application can be configured using the following methods:

#### Application Properties

Key application properties include:

- `server.port`: The port the application runs on (default: 8080)
- `spring.profiles.active`: Active profile (default: development, options: prod)
- `spring.datasource.url`: Database URL
- `spring.datasource.username`: Database username
- `spring.datasource.password`: Database password

#### Environment Variables

In production mode, the application uses environment variables:

- `PORT`: Server port
- `SPRING_PROFILES_ACTIVE`: Active Spring profile
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

#### Production Mode

To run the application in production mode:

```bash
# Locally
java -jar -Dspring.profiles.active=prod target/kitchensink-spring-0.0.1-SNAPSHOT.jar

# With Docker
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod kitchensink-spring
```
