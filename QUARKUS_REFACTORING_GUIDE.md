# Additional Refactoring Needs for Quarkus Migration

This document covers critical refactoring areas **not explicitly flagged by Konveyor** that are essential for a successful Quarkus migration.

---

## ⚠️ CRITICAL: Required vs Nice-to-Have

**IMPORTANT:** Not all issues in this guide are blocking. This section clarifies what's **REQUIRED to run on Quarkus** vs **NICE TO HAVE for modernization**.

### ✅ REQUIRED - Application Will Not Run Without These

These changes are **blocking** - the application will fail at runtime if not addressed:

#### 1. **Delete/Replace JNDILookupHelper.java** ⛔ CRITICAL
**Why:** Quarkus doesn't support JNDI lookups. This code fails at runtime:
```java
DataSource ds = JNDILookupHelper.lookupDataSource(); // ❌ FAILS in Quarkus
```
**Action:** Delete `JNDILookupHelper.java` entirely, replace all calls with `@Inject`

#### 2. **Fix Servlet EJB Lookups** ⛔ CRITICAL
**Problem in `FuelUpServlet.java:47-48`:**
```java
Context ctx = new InitialContext();
fuelUpService = (FuelUpService) ctx.lookup("java:global/boat-fuel-tracker/FuelUpService");
// ❌ FAILS - Quarkus doesn't support JNDI EJB lookups
```
**Action:** Replace with CDI injection:
```java
@Inject
FuelUpService fuelUpService;
```

#### 3. **Add @Transactional to Write Operations** ⛔ CRITICAL
**Why:** EJB `@Stateless` provided automatic transactions. CDI `@ApplicationScoped` doesn't.

**Without this, database writes won't commit:**
```java
@ApplicationScoped
public class FuelUpServiceBean {
    @Transactional  // ✅ REQUIRED for createFuelUp, deleteFuelUp
    public FuelUp createFuelUp(FuelUp fuelUp) {
        entityManager.persist(fuelUp);
        return fuelUp;
    }
}
```

#### 4. **Replace @PersistenceContext with @Inject** ⛔ REQUIRED
```java
// ❌ Old (Quarkus doesn't use persistence unit names):
@PersistenceContext(unitName = "BoatFuelTrackerPU")
private EntityManager entityManager;

// ✅ New (required):
@Inject
EntityManager entityManager;
```

#### 5. **Replace Log4j 1.x** ⛔ REQUIRED
```java
// ❌ Old (Log4j 1.x not supported):
import org.apache.log4j.Logger;
private static final Logger logger = Logger.getLogger(FuelUpServiceBean.class);

// ✅ New (required):
import org.jboss.logging.Logger;
private static final Logger LOG = Logger.getLogger(FuelUpServiceBean.class);
```

#### 6. **Fix getStatistics() JDBC Code** ⛔ CRITICAL
**Current code in `FuelUpServiceBean.java:88`:**
```java
DataSource ds = JNDILookupHelper.lookupDataSource(); // ❌ FAILS
conn = ds.getConnection();
```
**Action:** Replace with JPA/JPQL (see Issue #5 below)

#### 7. **Delete or Migrate web.xml** ⛔ SEMI-REQUIRED
Quarkus doesn't use `web.xml`. You must either:
- Add `@WebServlet` annotations to keep servlets, OR
- Convert to JAX-RS REST API (recommended)

**Security and config in web.xml must move to `application.properties`**

---

### 🎯 NICE TO HAVE - Improves Quality but Not Blocking

These improvements are **recommended but not required** to run on Quarkus:

- ✨ **DTOs** (Issue #4) - App works without them, but exposing entities directly is risky
- ✨ **REST API** (Issue #1) - Can keep servlets, but REST is more modern
- ✨ **Bean Validation** (Issue #10) - Works without, but validation is best practice
- ✨ **Exception Mappers** (Issue #11) - Works without, returns generic 500 errors
- ✨ **Pagination** (Issue #7) - Works without, but performance issue with large datasets
- ✨ **Health Checks** (Issue #17) - Not required, but needed for Kubernetes
- ✨ **Metrics** (Issue #18) - Not required for functionality
- ✨ **OpenAPI** (Issue #19) - Not required for functionality
- ✨ **Tests** (Issue #20) - Not required to run
- ✨ **Caching** (Issue #21) - Not required for functionality
- ✨ **Proper Exception Handling** (Issue #11) - App runs without it
- ✨ **Delete Legacy EJB Interfaces** (Issue #13) - Dead code, but not blocking

---

### 📊 Migration Effort Comparison

**Minimal Migration (Just Get It Running):**
- **Effort:** 1-2 weeks
- **What:** Only the 7 REQUIRED items above + Konveyor fixes
- **Result:** App runs on Quarkus but keeps old patterns (servlets, HTML, etc.)

**Full Modernization (Production-Ready):**
- **Effort:** 4-5 weeks (as outlined in this guide)
- **What:** All REQUIRED + NICE TO HAVE items
- **Result:** Modern cloud-native REST API with best practices

---

### ⚡ Quick Start: Minimum Required Changes

If you just want to **get the app running on Quarkus** with minimal changes:

**Week 1: Critical Fixes**
1. ✅ Complete all Konveyor fixes (javax→jakarta, Maven deps, Hibernate 6)
2. ✅ Delete `JNDILookupHelper.java` and `FileSystemHelper.java`
3. ✅ Replace all JNDI lookups with `@Inject` in servlets and services
4. ✅ Add `@Transactional` to `createFuelUp()` and `deleteFuelUp()`
5. ✅ Replace `@PersistenceContext` with `@Inject EntityManager`
6. ✅ Replace Log4j imports with JBoss Logging
7. ✅ Rewrite `getStatistics()` to use JPQL instead of JDBC
8. ✅ Add `@WebServlet` annotations to servlets OR convert to JAX-RS

**Result:** Working Quarkus application (1-2 weeks)

**Then optionally proceed with modernization items for production readiness.**

---

## Table of Contents

1. [Architecture & Design Patterns](#1-architecture--design-patterns)
2. [API Layer Redesign](#2-api-layer-redesign)
3. [Data Access Layer Issues](#3-data-access-layer-issues)
4. [Entity and DTO Patterns](#4-entity-and-dto-patterns)
5. [Validation and Error Handling](#5-validation-and-error-handling)
6. [Transaction Management](#6-transaction-management)
7. [Legacy Code Removal](#7-legacy-code-removal)
8. [Logging Strategy](#8-logging-strategy)
9. [Security Modernization](#9-security-modernization)
10. [Cloud-Native Patterns](#10-cloud-native-patterns)
11. [Testing Strategy](#11-testing-strategy)
12. [Performance and Scalability](#12-performance-and-scalability)

---

## 1. Architecture & Design Patterns

### Issue #1: Complete Absence of REST API

**Current State:**
- Application uses Servlets that generate HTML
- No JSON endpoints
- No API-first design
- Cannot be consumed by modern frontends

**Files Affected:**
- `src/main/java/com/boatfuel/servlet/FuelUpServlet.java`
- `src/main/java/com/boatfuel/servlet/IndexServlet.java`
- `src/main/java/com/boatfuel/servlet/LogoutServlet.java`

**Current Anti-Pattern:**
```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    out.println("<h1>Your Fuel-Ups</h1>");
    // ... more HTML generation
}
```

**Required Refactoring:**

**Create RESTful Resource Classes:**
```java
package com.boatfuel.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.inject.Inject;
import org.eclipse.microprofile.openapi.annotations.*;
import org.eclipse.microprofile.openapi.annotations.responses.*;

@Path("/api/v1/fuelups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Fuel-Ups", description = "Fuel tracking operations")
public class FuelUpResource {

    @Inject
    FuelUpService fuelUpService;

    @GET
    @Operation(summary = "Get all fuel-ups for a user")
    @APIResponse(responseCode = "200", description = "Success")
    public Response getUserFuelUps(
            @QueryParam("userId") String userId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<FuelUpDTO> fuelUps = fuelUpService.getFuelUpsByUser(userId, page, size);
        return Response.ok(fuelUps).build();
    }

    @POST
    @Operation(summary = "Create a new fuel-up")
    @APIResponse(responseCode = "201", description = "Created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    public Response createFuelUp(@Valid CreateFuelUpRequest request) {
        FuelUpDTO created = fuelUpService.createFuelUp(request);
        return Response.status(Response.Status.CREATED)
            .entity(created)
            .build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a fuel-up")
    public Response deleteFuelUp(@PathParam("id") Long id) {
        fuelUpService.deleteFuelUp(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/statistics")
    @Operation(summary = "Get fuel statistics")
    public Response getStatistics(@QueryParam("userId") String userId) {
        FuelUpStatisticsDTO stats = fuelUpService.getStatistics(userId);
        return Response.ok(stats).build();
    }
}
```

**Benefits:**
- Modern RESTful API
- Can be consumed by any frontend (React, Vue, mobile apps)
- OpenAPI/Swagger documentation auto-generated
- Proper HTTP status codes
- Content negotiation support

---

### Issue #2: No Separation of Concerns

**Current State:**
- Business logic mixed in Servlets (lines 134-169 in FuelUpServlet)
- Data access mixed with business logic
- No service layer abstraction

**Required Refactoring:**

**Create proper service layer:**
```java
package com.boatfuel.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FuelUpService {

    @Inject
    FuelUpRepository repository;

    @Inject
    UserService userService;

    @Inject
    FuelUpMapper mapper;

    @Transactional
    public FuelUpDTO createFuelUp(CreateFuelUpRequest request) {
        // Validation logic
        User user = userService.findOrCreateUser(request.getUserId());

        // Business logic
        FuelUp fuelUp = mapper.toEntity(request);
        fuelUp.setUser(user);
        fuelUp.calculateTotalCost();

        // Persistence
        repository.persist(fuelUp);

        return mapper.toDTO(fuelUp);
    }

    public List<FuelUpDTO> getFuelUpsByUser(String userId, int page, int size) {
        return repository.findByUser(userId, page, size)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

---

### Issue #3: Entire Helper Classes Should Be Deleted

**Current State:**
- `JNDILookupHelper.java` - 109 lines of vendor-specific JNDI code
- `FileSystemHelper.java` - 135 lines of file system operations
- These are architectural anti-patterns

**Files to DELETE:**
- `src/main/java/com/boatfuel/util/JNDILookupHelper.java` - **Delete entirely**
- `src/main/java/com/boatfuel/util/FileSystemHelper.java` - **Delete entirely**

**Reason:**
- JNDI lookups replaced by CDI `@Inject`
- File system operations replaced by environment variables and logging framework
- These classes represent the old J2EE mindset that doesn't exist in Quarkus

**Migration:**
All JNDI lookup code like this:
```java
DataSource ds = JNDILookupHelper.lookupDataSource();
conn = ds.getConnection();
```

Becomes:
```java
@Inject
EntityManager em;

// Use JPA/Panache - no JNDI needed
```

---

## 2. API Layer Redesign

### Issue #4: No DTOs - Exposing Entities Directly

**Current State:**
- Entities are being passed directly to/from clients
- No separation between internal and external models
- Risk of over-fetching, N+1 queries, and Jackson serialization issues

**Example Problem in `FuelUpServlet.java:169`:**
```java
fuelUpService.createFuelUp(fuelUp);  // Passing entity directly
```

**Required Refactoring:**

**Create DTO classes:**
```java
package com.boatfuel.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Fuel-up response")
public class FuelUpDTO {

    @Schema(description = "Fuel-up ID")
    private Long id;

    @Schema(description = "Date of fuel-up", example = "2024-01-15")
    private LocalDate date;

    @Schema(description = "Gallons purchased", example = "45.5")
    private BigDecimal gallons;

    @Schema(description = "Price per gallon", example = "3.89")
    private BigDecimal pricePerGallon;

    @Schema(description = "Total cost", example = "177.05")
    private BigDecimal totalCost;

    @Schema(description = "Engine hours at fill-up", example = "234.5")
    private BigDecimal engineHours;

    private String location;
    private String notes;

    // User info - flattened, not full User object
    private String userId;
    private String userDisplayName;

    // Getters/Setters
}

@Schema(description = "Create fuel-up request")
public class CreateFuelUpRequest {

    @NotNull
    @Schema(required = true)
    private String userId;

    @NotNull
    @PastOrPresent
    private LocalDate date;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("10000")
    private BigDecimal gallons;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal pricePerGallon;

    @DecimalMin("0")
    private BigDecimal engineHours;

    @Size(max = 500)
    private String location;

    @Size(max = 2000)
    private String notes;

    // Getters/Setters
}
```

**Create Mapper (use MapStruct):**
```java
package com.boatfuel.mapper;

import org.mapstruct.*;

@Mapper(componentModel = "cdi")
public interface FuelUpMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.displayName", target = "userDisplayName")
    FuelUpDTO toDTO(FuelUp entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    FuelUp toEntity(CreateFuelUpRequest request);

    List<FuelUpDTO> toDTOList(List<FuelUp> entities);
}
```

**Add MapStruct dependency:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

---

## 3. Data Access Layer Issues

### Issue #5: Mixed JPA and Raw JDBC

**Current State:**
- `FuelUpServiceBean.getStatistics()` uses raw JDBC (lines 81-123)
- Manual connection management
- SQL injection risk
- Not using JPA capabilities

**Current Anti-Pattern:**
```java
public FuelUpStatistics getStatistics(String userId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
        DataSource ds = JNDILookupHelper.lookupDataSource();
        conn = ds.getConnection();

        String sql = "SELECT COUNT(*), SUM(GALLONS), SUM(TOTAL_COST), AVG(PRICE_PER_GALLON) " +
                    "FROM FUEL_UPS WHERE USER_ID = ?";
        // ... more JDBC code
    } finally {
        // Manual cleanup
    }
}
```

**Required Refactoring - Option 1 (JPA):**
```java
@ApplicationScoped
public class FuelUpService {

    @Inject
    EntityManager em;

    public FuelUpStatisticsDTO getStatistics(String userId) {
        // Use JPQL with aggregate functions
        String jpql = """
            SELECT new com.boatfuel.dto.FuelUpStatisticsDTO(
                COUNT(f),
                COALESCE(SUM(f.gallons), 0),
                COALESCE(SUM(f.totalCost), 0),
                COALESCE(AVG(f.pricePerGallon), 0)
            )
            FROM FuelUp f
            WHERE f.user.userId = :userId
        """;

        return em.createQuery(jpql, FuelUpStatisticsDTO.class)
            .setParameter("userId", userId)
            .getSingleResult();
    }
}
```

**Required Refactoring - Option 2 (Hibernate Panache - Recommended for Quarkus):**
```java
package com.boatfuel.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FuelUpRepository implements PanacheRepository<FuelUp> {

    public List<FuelUp> findByUser(String userId, int page, int size) {
        return find("user.userId = ?1 ORDER BY date DESC", userId)
            .page(page, size)
            .list();
    }

    public FuelUpStatistics calculateStatistics(String userId) {
        // Use Panache projections
        return find("""
            SELECT
                COUNT(f) as count,
                COALESCE(SUM(f.gallons), 0) as totalGallons,
                COALESCE(SUM(f.totalCost), 0) as totalSpent,
                COALESCE(AVG(f.pricePerGallon), 0) as avgPrice
            FROM FuelUp f
            WHERE f.user.userId = :userId
            """)
            .project(FuelUpStatistics.class)
            .setParameter("userId", userId)
            .singleResult();
    }

    public long countByUser(String userId) {
        return count("user.userId", userId);
    }
}
```

---

### Issue #6: Using Old Query API Instead of TypedQuery

**Current State:**
```java
Query query = entityManager.createQuery(
    "SELECT f FROM FuelUp f WHERE f.user.userId = :userId ORDER BY f.date DESC");
query.setParameter("userId", userId);
return query.getResultList();  // Unchecked warning
```

**Required Refactoring:**
```java
TypedQuery<FuelUp> query = entityManager.createQuery(
    "SELECT f FROM FuelUp f WHERE f.user.userId = :userId ORDER BY f.date DESC",
    FuelUp.class);
query.setParameter("userId", userId);
return query.getResultList();  // Type-safe
```

---

### Issue #7: No Pagination - Potential Performance Issue

**Current State:**
- `getFuelUpsByUser()` returns ALL results
- Could be thousands of records
- Memory issues and poor performance

**Required Refactoring:**
```java
public List<FuelUpDTO> getFuelUpsByUser(String userId, int page, int size) {
    return repository.findByUser(userId, page, size)
        .stream()
        .map(mapper::toDTO)
        .collect(Collectors.toList());
}

// In repository (Panache):
public List<FuelUp> findByUser(String userId, int page, int size) {
    return find("user.userId = ?1 ORDER BY date DESC", userId)
        .page(page, size)
        .list();
}

// Or return paginated response:
public PaginatedResponse<FuelUpDTO> getFuelUpsPaginated(
        String userId, int page, int size) {

    long total = repository.countByUser(userId);
    List<FuelUpDTO> items = repository.findByUser(userId, page, size)
        .stream()
        .map(mapper::toDTO)
        .collect(Collectors.toList());

    return new PaginatedResponse<>(items, page, size, total);
}
```

---

## 4. Entity and DTO Patterns

### Issue #8: Lazy Loading Will Break with REST API

**Current State:**
```java
@Entity
public class User {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FuelUp> fuelUps;
}
```

**Problem:**
- When serializing User to JSON, if session is closed, `LazyInitializationException`
- N+1 query problem if you do fetch the collection

**Required Refactoring:**

**Option 1: Remove bidirectional relationship (recommended):**
```java
@Entity
public class User {
    // Remove fuelUps collection entirely
    // Query fuelUps separately when needed
}

// To get user's fuelups:
List<FuelUp> fuelUps = fuelUpRepository.findByUser(userId);
```

**Option 2: Use DTOs that don't include the relationship:**
```java
@Mapper
public interface UserMapper {
    @Mapping(target = "fuelUps", ignore = true)  // Don't map lazy collection
    UserDTO toDTO(User user);
}
```

**Option 3: Use @JsonIgnore:**
```java
@OneToMany(mappedBy = "user")
@JsonIgnore  // Don't serialize this field
private List<FuelUp> fuelUps;
```

---

### Issue #9: Creating Detached Entities in Servlet

**Current Problem in `FuelUpServlet.java:164-166`:**
```java
// Anti-pattern: Creating detached User entity
com.boatfuel.entity.User user = new com.boatfuel.entity.User();
user.setUserId(userId);  // Only setting ID, not a managed entity
fuelUp.setUser(user);
```

**Problem:**
- User is detached (not managed by EntityManager)
- Will fail constraint checks or create orphan records
- Not proper JPA usage

**Required Refactoring:**
```java
@ApplicationScoped
public class FuelUpService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public FuelUpDTO createFuelUp(CreateFuelUpRequest request) {
        // Properly fetch or create user
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        FuelUp fuelUp = mapper.toEntity(request);
        fuelUp.setUser(user);  // Managed entity

        repository.persist(fuelUp);
        return mapper.toDTO(fuelUp);
    }
}
```

---

## 5. Validation and Error Handling

### Issue #10: No Input Validation

**Current State in `FuelUpServlet.java:135-150`:**
```java
// Manual parsing with NO validation
String dateStr = request.getParameter("date");
String gallonsStr = request.getParameter("gallons");

// Direct parsing - will throw exception if invalid
java.sql.Date date = java.sql.Date.valueOf(dateStr);
BigDecimal gallons = new BigDecimal(gallonsStr);
```

**Problems:**
- No null checks
- No range validation
- No business rule validation
- Poor error messages

**Required Refactoring:**

**Add Bean Validation:**
```java
package com.boatfuel.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateFuelUpRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "Gallons is required")
    @DecimalMin(value = "0.01", message = "Gallons must be greater than 0")
    @DecimalMax(value = "10000", message = "Gallons must be less than 10000")
    @Digits(integer = 10, fraction = 2, message = "Invalid gallon value")
    private BigDecimal gallons;

    @NotNull(message = "Price per gallon is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "1000", message = "Price seems unrealistic")
    private BigDecimal pricePerGallon;

    @DecimalMin(value = "0", message = "Engine hours cannot be negative")
    @DecimalMax(value = "99999", message = "Engine hours seems unrealistic")
    private BigDecimal engineHours;

    @Size(max = 500, message = "Location must be less than 500 characters")
    private String location;

    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;

    // Getters/Setters
}
```

**Use in REST endpoint:**
```java
@POST
public Response createFuelUp(@Valid CreateFuelUpRequest request) {
    // Validation happens automatically
    // If invalid, returns 400 with validation errors
    FuelUpDTO created = fuelUpService.createFuelUp(request);
    return Response.status(Response.Status.CREATED).entity(created).build();
}
```

---

### Issue #11: Poor Exception Handling

**Current State:**
```java
try {
    // ... business logic
} catch (Exception e) {
    logger.error("Error creating fuel-up", e);
    throw new RuntimeException("Failed to create fuel-up", e);
}
```

**Problems:**
- Generic Exception catch
- Generic RuntimeException thrown
- No proper HTTP status codes
- No client-friendly error messages

**Required Refactoring:**

**Create custom exceptions:**
```java
package com.boatfuel.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    private Map<String, String> errors;

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

**Create exception mappers:**
```java
package com.boatfuel.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            exception.getMessage(),
            Response.Status.NOT_FOUND.getStatusCode()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }
}

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException exception) {
        ValidationErrorResponse error = new ValidationErrorResponse(
            "VALIDATION_ERROR",
            exception.getMessage(),
            exception.getErrors()
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}
```

**Error response DTOs:**
```java
public class ErrorResponse {
    private String code;
    private String message;
    private int status;
    private LocalDateTime timestamp;

    // Constructor, getters
}

public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> fieldErrors;

    // Constructor, getters
}
```

---

## 6. Transaction Management

### Issue #12: Unclear Transaction Boundaries

**Current State:**
- EJB `@Stateless` provides automatic transactions
- Moving to CDI `@ApplicationScoped` requires explicit `@Transactional`

**Required Refactoring:**

```java
@ApplicationScoped
public class FuelUpService {

    @Inject
    FuelUpRepository repository;

    // Read-only - no transaction needed (or can use @Transactional for consistency)
    public List<FuelUpDTO> getFuelUpsByUser(String userId, int page, int size) {
        return repository.findByUser(userId, page, size)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    // Write operation - needs transaction
    @Transactional
    public FuelUpDTO createFuelUp(CreateFuelUpRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        FuelUp fuelUp = mapper.toEntity(request);
        fuelUp.setUser(user);
        repository.persist(fuelUp);

        return mapper.toDTO(fuelUp);
    }

    // Write operation - needs transaction
    @Transactional
    public void deleteFuelUp(Long id) {
        FuelUp fuelUp = repository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Fuel-up not found"));
        repository.delete(fuelUp);
    }

    // Complex operation - might need custom transaction handling
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void processMonthlyReport(String userId) {
        // Runs in new transaction, separate from calling code
    }
}
```

---

## 7. Legacy Code Removal

### Issue #13: EJB 2.x Home/Remote Interfaces - DELETE

**Files to DELETE entirely:**
- `src/main/java/com/boatfuel/ejb/FuelUpServiceHome.java`
- `src/main/java/com/boatfuel/ejb/FuelUpServiceRemote.java`
- `src/main/webapp/WEB-INF/ejb-jar.xml` (if exists)

**Reason:**
- EJB 2.x Home/Remote pattern is completely obsolete
- Not used anywhere in current code
- No equivalent in Quarkus
- Just legacy cruft

---

### Issue #14: Delete Entire web.xml Configuration

**File to DELETE:**
- `src/main/webapp/WEB-INF/web.xml`

**Reason:**
- Servlet configuration replaced by JAX-RS `@Path` annotations
- Security configuration replaced by Quarkus security
- All 129 lines become irrelevant

**Replace with application.properties:**
```properties
# Quarkus configuration
quarkus.http.port=8080
quarkus.http.cors=true

# Security
quarkus.security.auth.enabled-in-dev-mode=true
quarkus.oidc.enabled=true

# Session (if still needed for some reason)
quarkus.http.session.timeout=30M
```

---

## 8. Logging Strategy

### Issue #15: Using Log4j 1.x Throughout

**Current State:**
```java
import org.apache.log4j.Logger;

private static final Logger logger = Logger.getLogger(FuelUpServiceBean.class);
```

**Required Refactoring for Quarkus:**

**Option 1: Use JBoss Logging (recommended for Quarkus):**
```java
import org.jboss.logging.Logger;

@ApplicationScoped
public class FuelUpService {

    private static final Logger LOG = Logger.getLogger(FuelUpService.class);

    public void someMethod() {
        LOG.info("Processing fuel-up");
        LOG.debugf("User %s has %d fuel-ups", userId, count);
        LOG.error("Failed to process", exception);
    }
}
```

**Option 2: Use SLF4J:**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(FuelUpService.class);
```

**Configure in application.properties:**
```properties
# Logging configuration
quarkus.log.level=INFO
quarkus.log.category."com.boatfuel".level=DEBUG
quarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n

# JSON logging for production
quarkus.log.console.json=false
%prod.quarkus.log.console.json=true
```

---

## 9. Security Modernization

### Issue #16: HTTP Basic Auth with Manual Session Management

**Current State:**
- HTTP Basic Auth in web.xml
- Manual session management in servlets
- No JWT, OAuth2, or modern auth

**Required Refactoring - Use OIDC/JWT:**

**Add Quarkus OIDC:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>
```

**Configure:**
```properties
quarkus.oidc.auth-server-url=https://your-keycloak.com/realms/boat-fuel
quarkus.oidc.client-id=boat-fuel-api
quarkus.oidc.credentials.secret=your-secret
quarkus.oidc.application-type=service
```

**Secure endpoints:**
```java
@Path("/api/v1/fuelups")
@Authenticated  // Requires valid JWT
public class FuelUpResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getUserFuelUps() {
        // Get authenticated user from JWT
        String userId = securityIdentity.getPrincipal().getName();
        // or
        String userId = jwt.getSubject();

        List<FuelUpDTO> fuelUps = service.getFuelUpsByUser(userId);
        return Response.ok(fuelUps).build();
    }

    @POST
    @RolesAllowed("user")  // Require specific role
    public Response createFuelUp(@Valid CreateFuelUpRequest request) {
        // ...
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Response deleteFuelUp(@PathParam("id") Long id) {
        // Ensure user can only delete their own data
        String userId = securityIdentity.getPrincipal().getName();
        service.deleteFuelUp(id, userId);
        return Response.noContent().build();
    }
}
```

---

## 10. Cloud-Native Patterns

### Issue #17: No Health Checks

**Required Addition:**
```java
package com.boatfuel.health;

import org.eclipse.microprofile.health.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("boat-fuel-api");
    }
}

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Inject
    EntityManager em;

    @Override
    public HealthCheckResponse call() {
        try {
            // Check database connectivity
            em.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.up("database");
        } catch (Exception e) {
            return HealthCheckResponse.down("database");
        }
    }
}
```

**Endpoints created:**
- `/q/health/live` - Liveness probe
- `/q/health/ready` - Readiness probe
- `/q/health` - All health checks

---

### Issue #18: No Metrics/Observability

**Required Addition:**

**Add dependencies:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

**Add custom metrics:**
```java
@ApplicationScoped
public class FuelUpService {

    @Inject
    MeterRegistry registry;

    @Counted(name = "fuelup.created", description = "Number of fuel-ups created")
    @Timed(name = "fuelup.create.time", description = "Time to create fuel-up")
    public FuelUpDTO createFuelUp(CreateFuelUpRequest request) {
        // Business logic
        registry.counter("fuelup.gallons.total").increment(request.getGallons().doubleValue());
        return created;
    }
}
```

**Metrics endpoint:**
- `/q/metrics` - Prometheus format metrics

---

### Issue #19: No OpenAPI Documentation

**Required Addition:**

**Add dependency:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

**Annotate resources:**
```java
@Path("/api/v1/fuelups")
@Tag(name = "Fuel-Ups", description = "Fuel tracking operations")
public class FuelUpResource {

    @POST
    @Operation(summary = "Create a new fuel-up", description = "Records a new fuel purchase")
    @APIResponse(responseCode = "201", description = "Fuel-up created successfully",
                 content = @Content(schema = @Schema(implementation = FuelUpDTO.class)))
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public Response createFuelUp(@Valid CreateFuelUpRequest request) {
        // ...
    }
}
```

**Swagger UI available at:**
- `/q/swagger-ui` - Interactive API documentation

---

## 11. Testing Strategy

### Issue #20: No Tests Exist

**Required Creation:**

**Add test dependencies:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

**Create integration tests:**
```java
@QuarkusTest
public class FuelUpResourceTest {

    @Test
    public void testCreateFuelUp() {
        CreateFuelUpRequest request = new CreateFuelUpRequest();
        request.setUserId("testuser");
        request.setDate(LocalDate.now());
        request.setGallons(new BigDecimal("45.5"));
        request.setPricePerGallon(new BigDecimal("3.89"));

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/fuelups")
        .then()
            .statusCode(201)
            .body("gallons", equalTo(45.5f))
            .body("totalCost", equalTo(177.05f));
    }

    @Test
    public void testGetUserFuelUps() {
        given()
            .queryParam("userId", "testuser")
        .when()
            .get("/api/v1/fuelups")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}
```

---

## 12. Performance and Scalability

### Issue #21: No Caching Strategy

**Required Addition:**

**Add Quarkus Cache:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-cache</artifactId>
</dependency>
```

**Use caching:**
```java
@ApplicationScoped
public class UserService {

    @CacheResult(cacheName = "user-cache")
    public User findById(String userId) {
        return repository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @CacheInvalidate(cacheName = "user-cache")
    public void updateUser(User user) {
        repository.persist(user);
    }
}
```

---

### Issue #22: Consider Reactive Patterns (Optional but Recommended)

**Current:** All blocking/synchronous operations

**Quarkus supports reactive:**
```java
@Path("/api/v1/fuelups")
public class FuelUpResource {

    @Inject
    FuelUpService service;

    @GET
    public Uni<List<FuelUpDTO>> getUserFuelUps(@QueryParam("userId") String userId) {
        return service.getFuelUpsReactive(userId);
    }

    @POST
    public Uni<Response> createFuelUp(@Valid CreateFuelUpRequest request) {
        return service.createFuelUpReactive(request)
            .map(dto -> Response.status(Response.Status.CREATED).entity(dto).build());
    }
}
```

---

## Migration Priority

### Phase 1: Foundation (Week 1)
1. Delete legacy files (JNDILookupHelper, FileSystemHelper, Home/Remote interfaces)
2. Create DTO classes and mappers
3. Set up proper logging (JBoss Logging)
4. Add validation annotations

### Phase 2: API Layer (Week 2)
1. Create JAX-RS resource classes
2. Implement exception mappers
3. Add OpenAPI annotations
4. Delete servlets and web.xml

### Phase 3: Data Access (Week 3)
1. Convert to Panache repositories
2. Fix transaction management
3. Add pagination
4. Remove direct JDBC code

### Phase 4: Cloud-Native (Week 4)
1. Add health checks
2. Add metrics
3. Implement proper security (OIDC/JWT)
4. Add caching

### Phase 5: Testing & Polish (Week 5)
1. Write integration tests
2. Add performance tests
3. Documentation
4. Code review and cleanup

---

## Summary

**Critical Issues Not Flagged by Konveyor:**
1. No REST API - must completely redesign from Servlets to JAX-RS
2. No DTOs - exposing entities directly
3. No input validation - Bean Validation missing
4. Mixed JPA/JDBC - inconsistent data access
5. No pagination - scalability issue
6. Lazy loading problems - will break with REST
7. Creating detached entities - JPA misuse
8. Poor exception handling - generic errors
9. Legacy code to delete - 3 entire files
10. No health checks, metrics, or OpenAPI
11. No tests whatsoever
12. HTTP Basic Auth - needs modern security

**Estimated Additional Effort:** 4-5 weeks (160-200 hours) beyond Konveyor fixes

This represents the architectural modernization needed for a production-ready Quarkus application.
