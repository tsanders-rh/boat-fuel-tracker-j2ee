# Konveyor Analysis - Remediation Guide

This document provides itemized code suggestions to fix each violation identified by the Konveyor kantra analysis tool.

## Table of Contents

1. [Critical Priority - Mandatory Violations](#critical-priority---mandatory-violations)
   - [1.1 Jakarta EE Namespace Migration (25 incidents)](#11-jakarta-ee-namespace-migration-25-incidents)
   - [1.2 Jakarta EE Maven Dependencies (4 incidents)](#12-jakarta-ee-maven-dependencies-4-incidents)
   - [1.3 Hibernate 6 Type Annotations (3 incidents)](#13-hibernate-6-type-annotations-3-incidents)
   - [1.4 File System Dependencies (11 incidents)](#14-file-system-dependencies-11-incidents)
   - [1.5 Localhost JDBC Connections (2 incidents)](#15-localhost-jdbc-connections-2-incidents)
2. [Important Priority - Potential Violations](#important-priority---potential-violations)
   - [2.1 Hibernate 6 MySQL Dialect](#21-hibernate-6-mysql-dialect)
   - [2.2 EJB to Quarkus CDI Migration](#22-ejb-to-quarkus-cdi-migration)
3. [Optional - Cloud Readiness](#optional---cloud-readiness)
   - [3.1 HTTP Session Management](#31-http-session-management)
   - [3.2 Servlet API Modernization](#32-servlet-api-modernization)

---

## Critical Priority - Mandatory Violations

### 1.1 Jakarta EE Namespace Migration (25 incidents)

**Rule ID:** `javax-to-jakarta-import-00001`
**Category:** Mandatory
**Description:** Replace all `javax.*` package imports with `jakarta.*` equivalents

#### Violation #1-4: EJB Package Imports

**File:** `src/main/java/com/boatfuel/ejb/FuelUpService.java:4`

**Current Code:**
```java
import javax.ejb.Local;
```

**Fixed Code:**
```java
import jakarta.ejb.Local;
```

---

**File:** `src/main/java/com/boatfuel/ejb/FuelUpServiceBean.java`

**Current Code:**
```java
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
```

**Fixed Code:**
```java
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
```

---

**File:** `src/main/java/com/boatfuel/ejb/FuelUpServiceHome.java`

**Current Code:**
```java
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
```

**Fixed Code:**
```java
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;
```

---

**File:** `src/main/java/com/boatfuel/ejb/FuelUpServiceRemote.java`

**Current Code:**
```java
import javax.ejb.EJBObject;
```

**Fixed Code:**
```java
import jakarta.ejb.EJBObject;
```

---

**File:** `src/main/java/com/boatfuel/ejb/UserSessionBean.java`

**Current Code:**
```java
import javax.ejb.Stateless;
```

**Fixed Code:**
```java
import jakarta.ejb.Stateless;
```

---

#### Violation #5-6: JPA/Persistence Package Imports

**File:** `src/main/java/com/boatfuel/entity/FuelUp.java:5`

**Current Code:**
```java
import javax.persistence.*;
```

**Fixed Code:**
```java
import jakarta.persistence.*;
```

**Detailed breakdown:**
```java
// Current imports
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

// Should be replaced with
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
```

---

**File:** `src/main/java/com/boatfuel/entity/User.java`

**Current Code:**
```java
import javax.persistence.*;
```

**Fixed Code:**
```java
import jakarta.persistence.*;
```

---

#### Violation #7-9: Servlet Package Imports

**File:** `src/main/java/com/boatfuel/servlet/FuelUpServlet.java`

**Current Code:**
```java
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
```

**Fixed Code:**
```java
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
```

---

**File:** `src/main/java/com/boatfuel/servlet/IndexServlet.java`

**Current Code:**
```java
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
```

**Fixed Code:**
```java
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
```

---

**File:** `src/main/java/com/boatfuel/servlet/LogoutServlet.java`

**Current Code:**
```java
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
```

**Fixed Code:**
```java
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
```

---

### 1.2 Jakarta EE Maven Dependencies (4 incidents)

**Rule IDs:**
- `javax-to-jakarta-dependencies-00001`
- `javax-to-jakarta-dependencies-00006`
- `javax-to-jakarta-dependencies-00007`

**Category:** Mandatory
**Description:** Update Maven dependencies from `javax` to `jakarta` namespace

#### Violation #1: Java EE API Dependency

**File:** `pom.xml:24-29`

**Current Code:**
```xml
<!-- Java EE 7 API (older version for more violations) -->
<dependency>
    <groupId>javax</groupId>
    <artifactId>javaee-api</artifactId>
    <version>7.0</version>
    <scope>provided</scope>
</dependency>
```

**Fixed Code:**
```xml
<!-- Jakarta EE 9.1 API -->
<dependency>
    <groupId>jakarta.platform</groupId>
    <artifactId>jakarta.jakartaee-api</artifactId>
    <version>9.1.0</version>
    <scope>provided</scope>
</dependency>
```

**Alternative for Jakarta EE 10:**
```xml
<dependency>
    <groupId>jakarta.platform</groupId>
    <artifactId>jakarta.jakartaee-api</artifactId>
    <version>10.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

#### Violation #2: Servlet API Dependency

**File:** `pom.xml:92-96`

**Current Code:**
```xml
<!-- Old servlet API - included in WAR for compatibility -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>servlet-api</artifactId>
    <version>2.5</version>
</dependency>
```

**Fixed Code:**
```xml
<!-- Jakarta Servlet API -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

#### Violation #3: EJB API Dependency

**File:** `pom.xml:120-125`

**Current Code:**
```xml
<!-- EJB 2.x support -->
<dependency>
    <groupId>javax.ejb</groupId>
    <artifactId>ejb-api</artifactId>
    <version>3.0</version>
    <scope>provided</scope>
</dependency>
```

**Fixed Code:**
```xml
<!-- Jakarta EJB API -->
<dependency>
    <groupId>jakarta.ejb</groupId>
    <artifactId>jakarta.ejb-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
```

**Note:** If migrating to Jakarta EE 9+, the main `jakarta.jakartaee-api` dependency includes EJB, so this separate dependency may not be needed.

---

#### Violation #4: Persistence Provider Configuration

**File:** `src/main/resources/META-INF/persistence.xml:9`

**Current Code:**
```xml
<provider>org.hibernate.ejb.HibernatePersistence</provider>
```

**Fixed Code:**
```xml
<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
```

**Note:** For Hibernate 6+:
```xml
<!-- Hibernate 6.x automatically discovers the provider, so this line can be removed entirely -->
<!-- Or use the explicit provider: -->
<provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
```

---

### 1.3 Hibernate 6 Type Annotations (3 incidents)

**Rule ID:** `hibernate6-00020`
**Category:** Mandatory
**Description:** String-based `@Type` annotations have been removed in Hibernate 6

#### Violation #1: FuelUp.location field

**File:** `src/main/java/com/boatfuel/entity/FuelUp.java:47-49`

**Current Code:**
```java
@Column(name = "LOCATION", length = 500)
@Type(type = "text") // Hibernate-specific type
private String location;
```

**Fixed Code (Option 1 - Remove annotation):**
```java
@Column(name = "LOCATION", length = 500, columnDefinition = "TEXT")
private String location;
```

**Fixed Code (Option 2 - Use JdbcTypeCode):**
```java
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Column(name = "LOCATION", length = 500)
@JdbcTypeCode(SqlTypes.LONGVARCHAR)
private String location;
```

**Fixed Code (Option 3 - Use @Lob for large text):**
```java
@Lob
@Column(name = "LOCATION")
private String location;
```

---

#### Violation #2: FuelUp.notes field

**File:** `src/main/java/com/boatfuel/entity/FuelUp.java:51-53`

**Current Code:**
```java
@Column(name = "NOTES", length = 2000)
@org.hibernate.annotations.Type(type = "text") // Hibernate-specific
private String notes;
```

**Fixed Code (Option 1 - Remove annotation):**
```java
@Column(name = "NOTES", length = 2000, columnDefinition = "TEXT")
private String notes;
```

**Fixed Code (Option 2 - Use JdbcTypeCode):**
```java
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Column(name = "NOTES", length = 2000)
@JdbcTypeCode(SqlTypes.LONGVARCHAR)
private String notes;
```

**Fixed Code (Option 3 - Use @Lob):**
```java
@Lob
@Column(name = "NOTES")
private String notes;
```

---

#### Violation #3: User entity (similar pattern)

**File:** `src/main/java/com/boatfuel/entity/User.java`

**Note:** Apply the same fix pattern as above for any `@Type(type = "text")` or other string-based type annotations in the User entity.

**General Migration Rule:**
- Remove `@Type(type = "...")` annotations
- Use standard JPA `columnDefinition` attribute OR
- Use Hibernate 6's `@JdbcTypeCode` with type-safe SqlTypes constants OR
- Use `@Lob` for large objects

---

### 1.4 File System Dependencies (11 incidents)

**Rule ID:** `local-storage-00001`
**Category:** Mandatory
**Description:** Applications running in containers should not use local file system storage

**File:** `src/main/java/com/boatfuel/util/FileSystemHelper.java`

All 11 incidents occur in this file. The entire class uses hardcoded file paths and file I/O operations.

#### Violation #1-4: Hardcoded File Paths

**Current Code (Lines 22-25):**
```java
private static final String CONFIG_DIR = "/opt/boatfuel/config";
private static final String LOG_DIR = "/var/log/boatfuel";
private static final String EXPORT_DIR = "C:\\BoatFuel\\exports"; // Windows path
private static final String TEMP_DIR = "/tmp/boatfuel";
```

**Recommended Fix:**

**Option 1: Use Environment Variables**
```java
private static final String CONFIG_DIR = System.getenv().getOrDefault("BOATFUEL_CONFIG_DIR", "/opt/boatfuel/config");
private static final String LOG_DIR = System.getenv().getOrDefault("BOATFUEL_LOG_DIR", "/var/log/boatfuel");
private static final String EXPORT_DIR = System.getenv().getOrDefault("BOATFUEL_EXPORT_DIR", "/tmp/exports");
private static final String TEMP_DIR = System.getenv().getOrDefault("BOATFUEL_TEMP_DIR", "/tmp/boatfuel");
```

**Option 2: Remove file-based configuration entirely**
```java
// Replace with:
// - Use MicroProfile Config or Spring Boot @ConfigurationProperties
// - Store configuration in environment variables
// - Use Kubernetes ConfigMaps
// - Use external configuration services (Spring Cloud Config, etc.)
```

---

#### Violation #5-6: File-based Configuration Loading

**Current Code (Lines 31-49):**
```java
public static Properties loadConfiguration() throws IOException {
    Properties props = new Properties();
    File configFile = new File(CONFIG_DIR, "application.properties");
    // ... file operations
}
```

**Recommended Fix:**

```java
// Use MicroProfile Config API
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public static String getConfigValue(String key) {
    Config config = ConfigProvider.getConfig();
    return config.getValue(key, String.class);
}

// Or use environment variables directly
public static String getConfigValue(String key, String defaultValue) {
    return System.getenv().getOrDefault(key, defaultValue);
}
```

**For Quarkus:**
```java
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConfigurationService {

    @ConfigProperty(name = "database.url")
    String databaseUrl;

    @ConfigProperty(name = "database.user")
    String databaseUser;

    // No file system access needed
}
```

---

#### Violation #7-8: File-based Data Export

**Current Code (Lines 71-88):**
```java
public static File exportToCSV(String userId, String csvData) throws IOException {
    File exportDir = new File(EXPORT_DIR);
    if (!exportDir.exists()) {
        exportDir.mkdirs();
    }
    String filename = "fuel-export-" + userId + "-" + System.currentTimeMillis() + ".csv";
    File exportFile = new File(exportDir, filename);
    // ... write to file
    return exportFile;
}
```

**Recommended Fix:**

**Option 1: Return data directly (REST API)**
```java
public static String exportToCSV(String userId, List<FuelUp> fuelUps) {
    StringBuilder csv = new StringBuilder();
    csv.append("Date,Gallons,Price,Total\n");
    for (FuelUp f : fuelUps) {
        csv.append(f.getDate()).append(",")
           .append(f.getGallons()).append(",")
           .append(f.getPricePerGallon()).append(",")
           .append(f.getTotalCost()).append("\n");
    }
    return csv.toString();
}

// In REST endpoint:
@GET
@Path("/export")
@Produces("text/csv")
public Response exportData(@QueryParam("userId") String userId) {
    String csv = exportToCSV(userId, getFuelUps(userId));
    return Response.ok(csv)
        .header("Content-Disposition", "attachment; filename=fuel-export.csv")
        .build();
}
```

**Option 2: Use object storage (S3, MinIO)**
```java
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

public static String exportToS3(String userId, String csvData) {
    S3Client s3 = S3Client.create();
    String key = "exports/fuel-export-" + userId + "-" + System.currentTimeMillis() + ".csv";

    s3.putObject(PutObjectRequest.builder()
        .bucket("boatfuel-exports")
        .key(key)
        .build(),
        RequestBody.fromString(csvData));

    return key; // Return S3 object key or pre-signed URL
}
```

---

#### Violation #9-10: File-based Audit Logging

**Current Code (Lines 120-134):**
```java
public static void writeAuditLog(String message) {
    File logDir = new File(LOG_DIR);
    if (!logDir.exists()) {
        logDir.mkdirs();
    }
    File logFile = new File(logDir, "audit.log");
    try (FileWriter writer = new FileWriter(logFile, true)) {
        String timestamp = new java.util.Date().toString();
        writer.write(timestamp + " - " + message + "\n");
    } catch (IOException e) {
        logger.error("Failed to write audit log", e);
    }
}
```

**Recommended Fix:**

**Option 1: Use standard logging to stdout (best for containers)**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogger {
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public static void writeAuditLog(String message) {
        // Logs will be captured by container runtime and forwarded to
        // centralized logging (Elasticsearch, Splunk, CloudWatch, etc.)
        auditLog.info("AUDIT: {}", message);
    }
}
```

**Option 2: Use structured logging with JSON**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.logstash.logback.argument.StructuredArguments;

public class AuditLogger {
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public static void writeAuditLog(String userId, String action, String resource) {
        auditLog.info("Audit event",
            StructuredArguments.kv("userId", userId),
            StructuredArguments.kv("action", action),
            StructuredArguments.kv("resource", resource),
            StructuredArguments.kv("timestamp", System.currentTimeMillis())
        );
    }
}
```

---

#### Violation #11: JBoss VFS API Usage

**Current Code (Lines 94-114):**
```java
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public static String readFileUsingJBossVFS(String path) throws IOException {
    logger.info("Reading file using JBoss VFS: " + path);
    VirtualFile virtualFile = VFS.getChild(path);
    // ... JBoss-specific file operations
}
```

**Recommended Fix:**

**Use standard Java NIO:**
```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public static String readFile(String path) throws IOException {
    logger.info("Reading file: " + path);
    Path filePath = Paths.get(path);
    return Files.readString(filePath);
}
```

**Or better - don't read from file system at all:**
```java
// Use classpath resources instead
public static String readResource(String resourcePath) throws IOException {
    try (InputStream is = FileSystemHelper.class.getResourceAsStream(resourcePath);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

        return reader.lines().collect(Collectors.joining("\n"));
    }
}
```

---

### 1.5 Localhost JDBC Connections (2 incidents)

**Rule ID:** `localhost-jdbc-00002`
**Category:** Mandatory
**Description:** Hardcoded localhost database URLs should be externalized

#### Violation #1: persistence.xml JNDI datasource

**File:** `src/main/resources/META-INF/persistence.xml:12`

**Current Code:**
```xml
<jta-data-source>jdbc/BoatFuelTrackerDS</jta-data-source>
```

**Recommended Fix:**

This is actually using JNDI which is good, but the datasource itself (configured elsewhere) likely has hardcoded localhost URLs.

**In application server (e.g., TomEE resources.xml):**

**Current:**
```xml
<Resource id="BoatFuelTrackerDS" type="DataSource">
    JdbcDriver com.mysql.jdbc.Driver
    JdbcUrl jdbc:mysql://localhost:3306/boatfuel
    UserName boatfuel_user
    Password boatfuel123
</Resource>
```

**Fixed - Use environment variables:**
```xml
<Resource id="BoatFuelTrackerDS" type="DataSource">
    JdbcDriver ${DB_DRIVER}
    JdbcUrl ${DB_URL}
    UserName ${DB_USER}
    Password ${DB_PASSWORD}
</Resource>
```

**For Quarkus (application.properties):**
```properties
# Don't hardcode values
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=${DB_USER}
quarkus.datasource.password=${DB_PASSWORD}
quarkus.datasource.jdbc.url=${DB_URL}

# Set via environment variables:
# DB_USER=boatfuel_user
# DB_PASSWORD=secret
# DB_URL=jdbc:mysql://mysql-service:3306/boatfuel
```

---

#### Violation #2: Hardcoded database configuration

**File:** `src/main/java/com/boatfuel/util/FileSystemHelper.java:58-60`

**Current Code:**
```java
defaults.setProperty("database.url", "jdbc:mysql://localhost:3306/boatfuel");
defaults.setProperty("database.user", "boatfuel_user");
defaults.setProperty("database.password", "changeme"); // Hardcoded password!
```

**Recommended Fix:**

**Remove this entire method and use externalized configuration:**

```java
// Delete the createDefaultConfiguration method entirely

// Instead, configure via environment variables or ConfigMaps in Kubernetes:
apiVersion: v1
kind: ConfigMap
metadata:
  name: boatfuel-config
data:
  database.url: "jdbc:mysql://mysql-service:3306/boatfuel"
  database.user: "boatfuel_user"

---
apiVersion: v1
kind: Secret
metadata:
  name: boatfuel-secrets
type: Opaque
stringData:
  database.password: "your-secure-password"
```

---

## Important Priority - Potential Violations

### 2.1 Hibernate 6 MySQL Dialect

**Rule ID:** `hibernate6-00270`
**Category:** Potential
**Description:** Community dialects like MySQL have moved to a separate module in Hibernate 6

**File:** `src/main/resources/META-INF/persistence.xml:23`

**Current Code:**
```xml
<property name="hibernate.dialect" value="org.hibernate.dialect.MySQL5InnoDBDialect"/>
```

**Recommended Fix:**

**Option 1: Update to modern MySQL dialect**
```xml
<!-- Hibernate 6.x -->
<property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
```

**Option 2: Add community dialects dependency (if using older MySQL version)**

In `pom.xml`:
```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
    <version>6.2.7.Final</version>
</dependency>
```

**Option 3: Let Hibernate auto-detect (recommended)**
```xml
<!-- Remove the dialect property entirely - Hibernate 6 auto-detects -->
<!-- Just remove line 23 from persistence.xml -->
```

**Complete Hibernate 6 pom.xml update:**
```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.2.7.Final</version>
</dependency>

<!-- Remove hibernate-entitymanager - it's included in hibernate-core in Hibernate 6 -->
```

---

### 2.2 EJB to Quarkus CDI Migration

**Rule ID:** `ee-to-quarkus-00000`
**Category:** Potential
**Description:** Replace `@Stateless` EJB annotation with CDI `@ApplicationScoped` for Quarkus

**File:** `src/main/java/com/boatfuel/ejb/FuelUpServiceBean.java`

**Current Code:**
```java
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class FuelUpServiceBean implements FuelUpService {

    @PersistenceContext(unitName = "BoatFuelTrackerPU")
    private EntityManager entityManager;

    // ... business methods
}
```

**Fixed Code for Quarkus:**
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FuelUpServiceBean implements FuelUpService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public FuelUp createFuelUp(FuelUp fuelUp) {
        entityManager.persist(fuelUp);
        return fuelUp;
    }

    @Transactional
    public void deleteFuelUp(Long fuelUpId) {
        FuelUp fuelUp = entityManager.find(FuelUp.class, fuelUpId);
        if (fuelUp != null) {
            entityManager.remove(fuelUp);
        }
    }

    // Read-only methods don't need @Transactional but can have it
    public List<FuelUp> getFuelUpsByUser(String userId) {
        // ... query logic
    }
}
```

**Key Changes:**
1. `@Stateless` → `@ApplicationScoped`
2. `@PersistenceContext` → `@Inject`
3. Add `@Transactional` to methods that modify data
4. Update imports from `javax.*` to `jakarta.*`

---

## Optional - Cloud Readiness

### 3.1 HTTP Session Management

**Rule ID:** `session-00001`
**Category:** Optional
**Description:** HTTP session usage may not work well in cloud/clustered environments

**File:** `src/main/java/com/boatfuel/servlet/FuelUpServlet.java`

**Current Code:**
```java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    HttpSession session = request.getSession();
    String userId = (String) session.getAttribute("userId");

    if (userId == null) {
        userId = "default-user";
        session.setAttribute("userId", userId);
    }
    // ...
}
```

**Recommended Fix:**

**Option 1: Use stateless JWT authentication**
```java
// Use JAX-RS with JWT
import jakarta.ws.rs.POST;
import jakarta.ws.rs.HeaderParam;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Path("/fuelups")
public class FuelUpResource {

    @POST
    public Response createFuelUp(
            @HeaderParam("Authorization") String authHeader,
            FuelUpDTO fuelUpDTO) {

        // Extract JWT token
        String token = authHeader.substring("Bearer ".length());

        // Validate and extract user ID from token
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        String userId = claims.getSubject();

        // No session needed - stateless
        FuelUp fuelUp = createFuelUp(userId, fuelUpDTO);
        return Response.ok(fuelUp).build();
    }
}
```

**Option 2: Use Jakarta Security API**
```java
import jakarta.security.enterprise.SecurityContext;
import jakarta.inject.Inject;

@WebServlet("/fuelup")
public class FuelUpServlet extends HttpServlet {

    @Inject
    SecurityContext securityContext;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get authenticated user from security context (no session needed)
        String userId = securityContext.getCallerPrincipal().getName();

        // Process request
        // ...
    }
}
```

**Option 3: Configure session replication (if sessions are required)**
```xml
<!-- In web.xml -->
<distributable/>

<!-- Configure external session store (Redis, Hazelcast, etc.) -->
```

---

### 3.2 Servlet API Modernization

**Rule ID:** `javaee-technology-usage-00120`
**Category:** Optional
**Description:** Consider modernizing from Servlets to JAX-RS REST API

**Current Servlet Approach:**
```java
@WebServlet("/fuelup")
public class FuelUpServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        // ... generate HTML
        out.println("</body></html>");
    }
}
```

**Recommended Modern Approach (JAX-RS REST API):**
```java
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;

@Path("/api/fuelups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FuelUpResource {

    @Inject
    FuelUpService fuelUpService;

    @GET
    public Response getFuelUps(@QueryParam("userId") String userId) {
        List<FuelUp> fuelUps = fuelUpService.getFuelUpsByUser(userId);
        return Response.ok(fuelUps).build();
    }

    @POST
    public Response createFuelUp(FuelUpDTO fuelUpDTO) {
        FuelUp created = fuelUpService.createFuelUp(fuelUpDTO.toEntity());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteFuelUp(@PathParam("id") Long id) {
        fuelUpService.deleteFuelUp(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/statistics")
    public Response getStatistics(@QueryParam("userId") String userId) {
        FuelUpStatistics stats = fuelUpService.getStatistics(userId);
        return Response.ok(stats).build();
    }
}
```

**Benefits:**
- RESTful API design
- JSON instead of HTML
- Better separation of concerns
- Can be consumed by modern front-ends (React, Vue, Angular)
- Easier to test
- Cloud-native friendly

---

## Migration Checklist

### Phase 1: Jakarta EE Namespace Migration (Mandatory)
- [ ] Update all `javax.*` imports to `jakarta.*` in Java files (25 files)
- [ ] Update Maven dependencies to Jakarta EE 9+ versions
- [ ] Update Hibernate persistence provider configuration
- [ ] Update application server to Jakarta EE 9+ compatible version (TomEE 9+, WildFly 27+, etc.)
- [ ] Test all functionality after namespace migration

### Phase 2: Hibernate 6 Migration (Mandatory)
- [ ] Remove all `@Type(type = "...")` string-based type annotations
- [ ] Replace with `@JdbcTypeCode` or `columnDefinition`
- [ ] Update Hibernate dependencies to 6.x
- [ ] Update or remove MySQL dialect configuration
- [ ] Test entity persistence and queries

### Phase 3: Externalize Configuration (Mandatory)
- [ ] Remove all hardcoded file paths
- [ ] Replace file-based configuration with environment variables or ConfigMaps
- [ ] Update database connection configuration to use environment variables
- [ ] Remove hardcoded passwords and credentials
- [ ] Configure secrets management (Kubernetes Secrets, Vault, etc.)

### Phase 4: Remove File System Dependencies (Mandatory)
- [ ] Replace file-based logging with stdout logging
- [ ] Replace file exports with REST API responses or object storage
- [ ] Remove JBoss VFS usage, use standard Java NIO
- [ ] Remove or externalize audit logging

### Phase 5: Cloud Readiness (Optional)
- [ ] Replace HTTP session management with stateless authentication (JWT)
- [ ] Implement health check endpoints
- [ ] Implement metrics endpoints
- [ ] Add distributed tracing
- [ ] Migrate from Servlets to JAX-RS REST API

### Phase 6: Testing
- [ ] Unit tests for all modified code
- [ ] Integration tests with Jakarta EE 9+ runtime
- [ ] Performance testing
- [ ] Security testing
- [ ] Container deployment testing

---

## Additional Resources

- [Jakarta EE 9 Migration Guide](https://jakarta.ee/resources/#documentation)
- [Hibernate 6 Migration Guide](https://hibernate.org/orm/documentation/6.0/)
- [Konveyor Documentation](https://www.konveyor.io/docs/)
- [Quarkus Migration Guide](https://quarkus.io/guides/migration-guide)
- [12-Factor App Methodology](https://12factor.net/)

---

## Summary

**Total Violations:** 45+
**Mandatory Fixes:** 41
**Potential Fixes:** 2
**Optional Improvements:** 2+

**Estimated Effort:**
- Phase 1 (Jakarta namespace): 2-4 hours
- Phase 2 (Hibernate 6): 1-2 hours
- Phase 3 (Externalize config): 2-3 hours
- Phase 4 (Remove file system): 3-5 hours
- Phase 5 (Cloud readiness): 5-10 hours
- **Total:** 13-24 hours

This remediation guide provides specific, actionable fixes for every violation detected by Konveyor. Follow the phases sequentially for the smoothest migration path.
