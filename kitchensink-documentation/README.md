# Kitchensink Application Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Architecture Overview](#architecture-overview)
4. [Technology Stack](#technology-stack)
5. [Core Components](#core-components)
   - [Domain Model](#domain-model)
   - [Data Access Layer](#data-access-layer)
   - [Service Layer](#service-layer)
   - [REST API](#rest-api)
   - [Web Interface](#web-interface)
6. [Database Configuration](#database-configuration)
7. [Building and Running](#building-and-running)
8. [Testing](#testing)
9. [Deployment Options](#deployment-options)
10. [Migration Considerations](#migration-considerations)

## Introduction

Kitchensink is a sample Jakarta EE application that demonstrates various Jakarta EE technologies working together in a single application. It serves as a reference implementation and starting point for developers looking to understand Jakarta EE applications on the JBoss EAP platform.

The application implements a simple member registration system with a web interface and REST API. It showcases:

- Jakarta EE technologies integration
- Bean Validation
- JPA for database persistence
- CDI for dependency injection
- JSF for the web interface
- JAX-RS for REST services

This documentation provides a comprehensive overview of the application structure, components, and functionality to help developers understand and potentially extend or migrate the application.

## Project Structure

The Kitchensink application follows a standard Maven project structure:

```
kitchensink/
├── pom.xml                                  # Maven project configuration
├── README.adoc                              # Project documentation
├── src/
│   ├── main/
│   │   ├── java/                            # Java source files
│   │   │   └── org/jboss/as/quickstarts/kitchensink/
│   │   │       ├── controller/              # Web controllers
│   │   │       ├── data/                    # Data access components
│   │   │       ├── model/                   # Domain model
│   │   │       ├── rest/                    # REST API endpoints
│   │   │       ├── service/                 # Business services
│   │   │       └── util/                    # Utility classes
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       ├── persistence.xml          # JPA configuration
│   │   │       └── ...                      # Other configuration files
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── beans.xml                # CDI configuration
│   │       │   ├── faces-config.xml         # JSF configuration
│   │       │   ├── kitchensink-quickstart-ds.xml # Datasource configuration
│   │       │   └── templates/               # JSF templates
│   │       ├── resources/                   # Static resources (CSS, JS, images)
│   │       └── index.xhtml                  # Main JSF page
│   └── test/                                # Test sources
└── ...
```

## Architecture Overview

Kitchensink follows a layered architecture pattern:

1. **Presentation Layer**: JSF views and JAX-RS endpoints
2. **Business Layer**: Services with business logic
3. **Persistence Layer**: JPA entities and repositories
4. **Data Layer**: H2 in-memory database (for development)

The application uses CDI (Contexts and Dependency Injection) to wire these components together, creating a loosely coupled architecture.

## Technology Stack

The Kitchensink application leverages the following technologies:

- **Jakarta EE**: The core platform for enterprise Java development
- **JPA (Jakarta Persistence API)**: For object-relational mapping
- **Hibernate**: As the JPA implementation
- **CDI (Contexts and Dependency Injection)**: For dependency injection
- **Bean Validation**: For model validation
- **JSF (Jakarta Server Faces)**: For the web interface
- **JAX-RS (Jakarta RESTful Web Services)**: For the REST API
- **H2 Database**: In-memory database for development
- **Maven**: For project build and dependency management
- **JBoss EAP**: As the application server

## Core Components

### Domain Model

The application has a single entity class:

#### Member Entity

The `Member` class represents a person with basic contact information:

- **ID**: Unique identifier (auto-generated)
- **Name**: Person's name (validated to not contain numbers)
- **Email**: Email address (validated for format and uniqueness)
- **Phone Number**: Contact number (validated for format)

The entity uses Jakarta Bean Validation annotations to enforce data integrity rules.

### Data Access Layer

The data access layer consists of:

#### MemberRepository

Responsible for CRUD operations on the Member entity:

- Finding members by ID
- Finding members by email
- Retrieving all members ordered by name

#### MemberListProducer

A CDI producer that maintains a cached list of all members and updates it when the database changes.

### Service Layer

The service layer contains:

#### MemberRegistration

Handles the business logic for registering new members:

- Validates member data
- Persists the member to the database
- Fires events when a new member is registered

### REST API

The REST API provides programmatic access to the application:

#### JaxRsActivator

Configures the JAX-RS application and sets the base path for REST endpoints.

#### MemberResourceRESTService

Exposes member-related operations through REST endpoints:

- `GET /rest/members`: List all members
- `GET /rest/members/{id}`: Get a specific member by ID
- `POST /rest/members`: Create a new member

The API handles validation errors and returns appropriate HTTP status codes and error messages.

### Web Interface

The web interface is built using JSF (Jakarta Server Faces):

#### MemberController

Manages the web interface for member registration:

- Handles form submissions
- Validates input data
- Displays success/error messages

#### JSF Views

- `index.xhtml`: The main page with the registration form and member list
- Templates for consistent layout and styling

## Database Configuration

Kitchensink uses an H2 in-memory database for development purposes:

- **Configuration**: Defined in `src/main/webapp/WEB-INF/kitchensink-quickstart-ds.xml`
- **JNDI Name**: `java:jboss/datasources/KitchensinkQuickstartDS`
- **Connection URL**: `jdbc:h2:mem:kitchensink-quickstart;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1`
- **Credentials**: Username: `sa`, Password: `sa`

The JPA configuration is in `src/main/resources/META-INF/persistence.xml`, which defines:

- The persistence unit name: `primary`
- Hibernate configuration for automatic schema generation
- SQL logging settings

## Building and Running

### Prerequisites

- JDK 11 or later
- Maven 3.6.0 or later
- JBoss EAP 8.0 or compatible application server

### Build Steps

1. Configure Maven to use JBoss repositories
2. Build the application:
   ```bash
   mvn clean package
   ```

### Deployment

1. Start JBoss EAP:
   ```bash
   $JBOSS_HOME/bin/standalone.sh
   ```

2. Deploy the application:
   ```bash
   mvn wildfly:deploy
   ```

3. Access the application at: http://localhost:8080/kitchensink

## Testing

Kitchensink includes Arquillian tests for integration testing:

- **Test Configuration**: Located in `src/test/resources/`
- **Test Cases**: Located in `src/test/java/`

To run the tests:

```bash
mvn test -Parq-remote
```

## Deployment Options

### Standard Deployment

Deploy to a local JBoss EAP instance as described in the Building and Running section.

### OpenShift Deployment

The application can be deployed to OpenShift using:

1. The S2I (Source-to-Image) process
2. Helm charts for containerized deployment

Configuration for OpenShift deployment is included in the project.

## Migration Considerations

When migrating this application to a different platform (e.g., Spring Boot), consider:

### Architecture Mapping

- **CDI** → Spring's dependency injection
- **JPA/Hibernate** → Spring Data JPA
- **JAX-RS** → Spring REST controllers
- **JSF** → Thymeleaf or another view technology
- **Bean Validation** → Spring's validation framework

### Database Considerations

- Configure equivalent datasource in the target platform
- Update entity annotations if needed
- Consider using database migration tools like Flyway or Liquibase

### Testing Strategy

- Replace Arquillian with the target platform's testing framework
- Maintain test coverage during migration
- Consider incremental migration with parallel testing

### Documentation

- Update deployment instructions for the target platform
- Document any API changes
- Provide migration guides for users of the application
