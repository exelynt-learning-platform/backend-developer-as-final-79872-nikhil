# Resource Booking System

A RESTful Resource Booking System built using Spring Boot, Java 17,
Spring Security, JWT, PostgreSQL, JPA/Hibernate, and Maven.

The application allows users to view available resources, create and manage
their own reservations, while administrators have full access to manage
resources and reservations.

---

## Assignment

**Backend Developer Assignment**

**Deadline:** 30th September 2026

---

# 1. Project Overview

This project implements a secure Resource Booking API.

A **Resource** represents a bookable item such as:

- Room
- Vehicle
- Equipment

A **Reservation** represents a booking made for a resource for a specific
start and end time.

The application provides:

- JWT authentication
- Role-based authorization
- ADMIN and USER roles
- Resource CRUD
- Reservation management
- Reservation ownership
- Reservation validation
- Reservation filtering
- Pagination
- Sorting
- PostgreSQL persistence
- Global error handling
- Swagger/OpenAPI documentation
- Seed users
- Automated tests

---

# 2. Technologies Used

- Java 17
- Spring Boot
- Spring Security
- JWT
- BCrypt
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Lombok
- Jakarta Validation
- Swagger/OpenAPI
- JUnit
- MockMvc

---

# 3. Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.example.booking
│   │       │
│   │       ├── config
│   │       │   ├── DataSeeder.java
│   │       │   ├── JacksonConfig.java
│   │       │   └── OpenApiConfig.java
│   │       │
│   │       ├── controller
│   │       │   ├── AuthController.java
│   │       │   ├── ReservationController.java
│   │       │   └── ResourceController.java
│   │       │
│   │       ├── dto
│   │       │   ├── auth
│   │       │   │   ├── LoginRequest.java
│   │       │   │   └── LoginResponse.java
│   │       │   │
│   │       │   ├── reservation
│   │       │   │   ├── ReservationRequest.java
│   │       │   │   └── ReservationResponse.java
│   │       │   │
│   │       │   └── resource
│   │       │       ├── ResourceRequest.java
│   │       │       └── ResourceResponse.java
│   │       │
│   │       ├── entity
│   │       │   ├── Reservation.java
│   │       │   ├── ReservationStatus.java
│   │       │   ├── Resource.java
│   │       │   ├── Role.java
│   │       │   └── User.java
│   │       │
│   │       ├── exception
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ResourceAlreadyBookedException.java
│   │       │   └── ResourceNotFoundException.java
│   │       │
│   │       ├── repository
│   │       │   ├── ReservationRepository.java
│   │       │   ├── ResourceRepository.java
│   │       │   └── UserRepository.java
│   │       │
│   │       ├── security
│   │       │   ├── CustomUserDetailsService.java
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   ├── JwtService.java
│   │       │   └── SecurityConfig.java
│   │       │
│   │       ├── service
│   │       │   ├── ReservationService.java
│   │       │   └── ResourceService.java
│   │       │
│   │       └── BookingApplication.java
│   │
│   └── resources
│       └── application.properties
│
└── test
    └── java
        └── com.example.booking
            ├── AuthControllerTest.java
            ├── BookingApplicationTests.java
            ├── ReservationAuthorizationTest.java
            ├── ReservationValidationTest.java
            ├── ResourceControllerTest.java
            └── SecurityAuthorizationTest.java
4. Authentication

The application implements JWT-based login authentication.

Login
POST /auth/login

Request:

{
  "username": "user",
  "password": "user123"
}

Successful response:

{
  "token": "JWT_TOKEN"
}

The JWT must be provided when accessing protected endpoints:

Authorization: Bearer JWT_TOKEN

Passwords are stored using BCrypt hashing.

The application uses stateless JWT authentication.

5. Roles and Authorization

The application contains two roles:

USER
ADMIN
USER permissions

A USER can:

Login
View resources
Create reservations
View their own reservations
View their own reservation by ID
Cancel their own reservation

A USER cannot:

Create resources
Update resources
Delete resources
View all reservations
View another user's reservation
Update reservations
Delete reservations
Use administrator-only reservation filtering
ADMIN permissions

An ADMIN can:

Login
Create resources
View resources
Update resources
Delete resources
Create reservations
View all reservations
View reservations by ID
Update reservations
Cancel reservations
Delete reservations
Filter reservations
Use pagination and sorting
6. Resource API

Base endpoint:

/api/resources
Get all resources
GET /api/resources

Accessible by:

USER
ADMIN
Get resource by ID
GET /api/resources/{id}

Accessible by:

USER
ADMIN
Create resource
POST /api/resources

ADMIN only.

Example request:

{
  "name": "Conference Room",
  "description": "Large meeting room",
  "price": 500.00
}
Update resource
PUT /api/resources/{id}

ADMIN only.

Delete resource
DELETE /api/resources/{id}

ADMIN only.

7. Reservation API

Base endpoint:

/api/reservations
Create reservation
POST /api/reservations

USER and ADMIN can create reservations.

Example:

{
  "resourceId": 1,
  "startTime": "2026-09-10T10:00:00",
  "endTime": "2026-09-10T12:00:00"
}

The user identity is taken from the authenticated JWT.

The client does not provide the user ID in the reservation request.

Get my reservations
GET /api/reservations/my

Returns reservations belonging to the currently authenticated user.

Example:

/api/reservations/my?page=0&size=10
Get all reservations
GET /api/reservations

ADMIN only.

Returns reservations for all users.

Get reservation by ID
GET /api/reservations/{id}

USER and ADMIN can access this endpoint.

A USER can only access their own reservation.

An ADMIN can access any reservation.

Update reservation
PUT /api/reservations/{id}

ADMIN only.

Cancel reservation
PUT /api/reservations/{id}/cancel

A USER can cancel only their own reservation.

An ADMIN can cancel any reservation.

Delete reservation
DELETE /api/reservations/{id}

ADMIN only.

8. Reservation Status

Reservations support the following statuses:

PENDING
CONFIRMED
CANCELLED

The default reservation status is:

PENDING

Cancelled reservations are excluded from reservation overlap checks.

9. Reservation Price

Reservation prices are stored using:

BigDecimal

This provides decimal precision for monetary values.

The reservation entity stores the price with decimal precision.

10. Reservation Validation

The application validates reservation requests and prevents invalid
reservations.

Validation includes:

Resource ID is required
Start time is required
End time is required
Start time must be before end time
Reservation price must be valid
Reservation time conflicts are prevented
Cancelled reservations do not cause booking conflicts

The application also validates filtering parameters.

Examples:

Page cannot be negative
Size must be greater than zero
Size cannot exceed 100
Minimum price cannot be negative
Maximum price cannot be negative
Minimum price cannot be greater than maximum price
Sorting direction must be asc or desc
11. Reservation Overlap Prevention

The application checks whether a resource already has an active reservation
during the requested time period.

If another non-cancelled reservation overlaps with the requested period,
the reservation is rejected.

When updating a reservation, the current reservation is excluded from the
overlap check.

12. Filtering

Administrators can filter reservations using:

Status
Minimum price
Maximum price

Endpoint:

GET /api/reservations/filter

Example:

/api/reservations/filter?status=CONFIRMED&minPrice=100&maxPrice=1000

Filters can also be combined with pagination and sorting.

13. Pagination

Reservation filtering supports pagination using:

page
size

Example:

/api/reservations/filter?page=0&size=10

Default values:

page = 0
size = 10

Maximum page size:

100
14. Sorting

Reservation results support optional sorting.

Example:

/api/reservations/filter?page=0&size=10&sortBy=price&direction=desc

Supported directions:

asc
desc

If sorting is not provided, the results are returned without an explicit
sort order.

15. Database

The application uses PostgreSQL with JPA/Hibernate.

Database:

booking_db

Create the database:

CREATE DATABASE booking_db;

The application persists:

Users
Resources
Reservations

The main reservation relationships are:

User
  │
  └── Reservation

Resource
  │
  └── Reservation

A reservation belongs to one user and one resource.

16. Environment Variables

The application uses environment variables for database and JWT
configuration.

Required variables:

DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
PowerShell example
$env:DB_URL="jdbc:postgresql://localhost:5432/booking_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_database_password"
$env:JWT_SECRET="your_jwt_secret"

Do not commit real database passwords or JWT secrets to the repository.

17. Seed Users

The application provides seed users for testing.

ADMIN
Username: admin
Password: admin123
Role: ADMIN
USER
Username: user
Password: user123
Role: USER

The seed process creates these users only if they do not already exist.

Passwords are encoded using BCrypt before being stored.

18. Error Handling

The application uses a global exception handler to provide consistent
error responses.

Handled cases include:

Validation errors
Resource not found
Reservation conflicts
Forbidden access
Invalid request parameters
Unexpected server errors

Example validation response:

{
  "timestamp": "2026-09-07T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "resourceId": "Resource ID is required"
  }
}
19. HTTP Status Codes

The API uses appropriate HTTP status codes.

Common responses:

200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
20. Swagger / OpenAPI

The application provides API documentation using Swagger/OpenAPI.

After starting the application, Swagger UI is available at:

http://localhost:8080/swagger-ui/index.html

OpenAPI specification:

http://localhost:8080/v3/api-docs

The API documentation includes JWT Bearer authentication.

Using JWT in Swagger
Login using:
POST /auth/login
Copy the returned JWT token.
Click the Authorize button in Swagger.
Enter:
Bearer YOUR_JWT_TOKEN
Authorize the requests.
21. Running the Application

Make sure PostgreSQL is running and the required environment variables
are configured.

Run tests

Windows PowerShell:

.\mvnw.cmd clean test
Start the application
.\mvnw.cmd spring-boot:run

The application runs on:

http://localhost:8080
22. Testing

The project contains automated tests covering:

Authentication
Authorization
USER and ADMIN roles
Resource access
Reservation authorization
Reservation ownership
Reservation validation
Application context
Controller behavior

Run all tests using:

.\mvnw.cmd clean test

The current test suite contains 27 tests, all passing.

Expected result:

Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
23. Security

The application implements:

JWT authentication
Stateless authentication
Spring Security
BCrypt password hashing
Role-based access control
Protected API endpoints
JWT-based user identity
Reservation ownership enforcement

USER identity is obtained from the authenticated JWT instead of being
accepted from the reservation request.

This prevents a USER from creating a reservation on behalf of another user
by simply changing a user ID in the request.

24. Architecture

The application follows a layered architecture:

Client
  │
  ▼
Controller
  │
  ▼
Service
  │
  ▼
Repository
  │
  ▼
PostgreSQL

Security flow:

Login
  │
  ▼
Authentication
  │
  ▼
JWT Generation
  │
  ▼
JWT sent with request
  │
  ▼
JwtAuthenticationFilter
  │
  ▼
Spring Security
  │
  ▼
Role / Ownership Authorization
  │
  ▼
Controller

DTOs are used for API requests and responses, while entities are used for
database persistence.

25. Assignment Requirements

The implementation covers the requested assignment requirements:

Requirement	Status
JWT login authentication	✅
ADMIN and USER roles	✅
RBAC	✅
ADMIN resource CRUD	✅
ADMIN reservation management	✅
USER resource read access	✅
USER reservation creation	✅
USER own-reservation access	✅
JWT-based USER identity	✅
Reservation statuses	✅
Decimal reservation price	✅
Reservation filtering	✅
Pagination	✅
Optional sorting	✅
Reservation ownership	✅
Validation	✅
PostgreSQL/JPA	✅
REST API/status codes	✅
Error handling	✅
Clean project structure	✅
Swagger/OpenAPI	✅
Seed users	✅
Automated testing	✅
26. Build and Test

Clean and run the complete test suite:

.\mvnw.cmd clean test

Build the application:

.\mvnw.cmd clean package

Run the application:

.\mvnw.cmd spring-boot:run
27. Conclusion

This project provides a secure and functional RESTful Resource Booking
System with:

Authentication
Authorization
Resource management
Reservation management
Reservation ownership
Validation
Filtering
Pagination
Sorting
PostgreSQL persistence
Error handling
Swagger/OpenAPI documentation
Automated security and authorization testing

The implementation is structured using Controller, Service, Repository,
DTO, Entity, Exception, Configuration, and Security layers.