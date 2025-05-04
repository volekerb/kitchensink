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
