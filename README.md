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
- Flexible database support:
  - H2 in-memory database (default)
  - MongoDB document database (optional)
- Docker support for containerization
- Kubernetes deployment with Helm charts

## Architecture

The application follows a multi-layered architecture:

- **Presentation Layer**: Spring MVC controllers and Thymeleaf templates
- **Business Layer**: Services with transaction management
- **Data Access Layer**: Spring Data repositories (JPA or MongoDB)
- **Domain Model**: Entities with validation

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

#### Running Tests

You can run the application tests using the Maven wrapper:

```bash
./mvnw clean install
```

#### Running Locally

You can run the application locally using the Maven wrapper:

```bash
# Run with H2 database (default)
./mvnw spring-boot:run

# Run with MongoDB
# First start MongoDB
docker run --name mongodb -d -p 27017:27017 mongo
# Then run the application with MongoDB enabled
./mvnw spring-boot:run -Dspring-boot.run.arguments="--mongodb.enabled=true"
```

Or you can run the JAR file directly after building:

```bash
# Run with H2 database (default)
java -jar target/kitchensink-spring-0.0.1-SNAPSHOT.jar

# Run with MongoDB
# First start MongoDB
docker run --name mongodb -d -p 27017:27017 mongo
# Then run the application with MongoDB enabled
java -jar target/kitchensink-spring-0.0.1-SNAPSHOT.jar --mongodb.enabled=true
```

#### Running with Docker

Build and run the application using Docker:

```bash
# Build the Docker image
docker build -t kitchensink-spring .

# Run the container with H2 database (default)
docker run -p 8080:8080 kitchensink-spring

# Run the container with MongoDB
# First create a Docker network
docker network create kitchensink-network

# Run MongoDB container
docker run --name mongodb -d --network kitchensink-network -p 27017:27017 mongo

# Run the application container with MongoDB enabled
docker run -p 8080:8080 --network kitchensink-network -e MONGODB_ENABLED=true -e MONGODB_URI=mongodb://mongodb:27017/kitchensink kitchensink-spring
```

#### Running with Docker Compose

For H2 database (default, using existing docker-compose.yml):

```bash
docker-compose up
```

For MongoDB:

```bash
docker-compose -f docker-compose-mongo.yml up
```

#### Running on Kubernetes with Helm

Deploy the application to Kubernetes using Helm:

```bash
# Build the Docker image
docker build -t kitchensink-spring:latest .

# Install the Helm chart with H2 database (default)
helm install kitchensink-spring ./charts

# To customize the deployment with H2
helm install kitchensink-spring ./charts \
  --set replicaCount=2 \
  --set image.repository=your-registry/kitchensink-spring \
  --set image.tag=1.0.0
```

To deploy with MongoDB:

```bash
# Install MongoDB using Helm (if not already installed)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install mongodb bitnami/mongodb

# Install the application with MongoDB enabled
helm install kitchensink-spring ./charts \
  --set env.MONGODB_ENABLED=true \
  --set env.MONGODB_URI=mongodb://mongodb:27017/kitchensink
```

For more detailed instructions on Helm deployment, see the [deployment documentation](kitchensink-documentation/build-deploy.md#spring-boot-version).

### Accessing the Application

Once the application is running, you can access it at:

- Web Interface: http://localhost:8080
- REST API: http://localhost:8080/api/members
- API Documentation: http://localhost:8080/swagger-ui.html

#### H2 Database Console

When using H2 database (default), you can access the H2 console in development mode:

- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:kitchensinkdb
- Username: sa
- Password: password

Note: The H2 console is not available when using MongoDB.

### Configuration Options

The application can be configured using the following methods:

#### Application Properties

Key application properties include:

- `server.port`: The port the application runs on (default: 8080)
- `spring.profiles.active`: Active profile (default: development, options: prod)

##### H2 Database Configuration (Default)
- `spring.datasource.url`: H2 Database URL
- `spring.datasource.username`: H2 Database username
- `spring.datasource.password`: H2 Database password

##### MongoDB Configuration
- `mongodb.enabled`: Enable MongoDB instead of H2 (default: false)
- `spring.data.mongodb.uri`: MongoDB connection URI

#### Environment Variables

In production mode, the application uses environment variables:

- `PORT`: Server port
- `SPRING_PROFILES_ACTIVE`: Active Spring profile

##### H2 Database Environment Variables
- `SPRING_DATASOURCE_URL`: H2 Database URL
- `SPRING_DATASOURCE_USERNAME`: H2 Database username
- `SPRING_DATASOURCE_PASSWORD`: H2 Database password

##### MongoDB Environment Variables
- `MONGODB_ENABLED`: Enable MongoDB instead of H2 (default: false)
- `MONGODB_URI`: MongoDB connection URI

#### Production Mode

To run the application in production mode:

```bash
# Locally with H2 Database (default)
java -jar -Dspring.profiles.active=prod target/kitchensink-spring-0.0.1-SNAPSHOT.jar

# Locally with MongoDB
java -jar -Dspring.profiles.active=prod -Dmongodb.enabled=true -Dspring.data.mongodb.uri=mongodb://localhost:27017/kitchensink target/kitchensink-spring-0.0.1-SNAPSHOT.jar

# With Docker using H2 Database
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod kitchensink-spring

# With Docker using MongoDB
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e MONGODB_ENABLED=true -e MONGODB_URI=mongodb://mongo-host:27017/kitchensink kitchensink-spring
```

#### Running with MongoDB in Kubernetes

To deploy the application with MongoDB in Kubernetes using Helm:

```bash
# Install MongoDB using Helm (if not already installed)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install mongodb bitnami/mongodb

# Install the application with MongoDB enabled
helm install kitchensink-spring ./charts \
  --set env.MONGODB_ENABLED=true \
  --set env.MONGODB_URI=mongodb://mongodb:27017/kitchensink
```
