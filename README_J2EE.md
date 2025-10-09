# Boat Fuel Tracker - Legacy J2EE Edition

## Overview

This is a **legacy J2EE application** intentionally designed with anti-patterns and deprecated APIs for testing the [Konveyor](https://www.konveyor.io/) application modernization platform.

The application provides a web-based interface for tracking boat fuel consumption, including fuel-ups, costs, gallons, and statistics. Users can log in, add fuel-up records, view their fuel history, and see aggregated statistics.

⚠️ **WARNING**: This code contains intentional bad practices and should NOT be used as a reference for production applications!

## What is This?

This application is the Boat Fuel Tracker PWA converted into a legacy J2EE monolith with maximum anti-patterns to trigger Konveyor rule violations. It demonstrates common issues found in enterprise Java applications that need modernization.

## Technology Stack (Legacy)

### Backend
- **Java EE 7** (old specification)
- **EJB 3.0 with @Stateless** (using manual JNDI lookup anti-pattern)
- **Servlet 2.5** (old API with XML configuration)
- **JPA 2.1** with Hibernate 4.3.11.Final
- **Hibernate 4.3** (with proprietary annotations and vendor-specific features)
- **Log4j 1.2.17** (deprecated, security issues)
- **HTTP Basic Authentication** (legacy security)
- **Maven** for build

### Frontend
- **HTML/CSS/JavaScript** (vanilla, no frameworks)
- **Modern UI** with gradient design and responsive layout
- **AJAX** for data fetching and form submission
- **Servlet-based templating** for dynamic content

### Database
- **MySQL 8.0** (deployed via Podman)
- **JDBC Driver**: MySQL Connector/J 5.1.47
- **JPA/Hibernate** for ORM
- **Schema**: `boatfuel` with tables for users and fuel_ups

### Application Server
- **Apache TomEE 8.0.16** (Java EE 8, javax namespace)
- **JTA** managed transactions
- **JNDI** datasource configuration

## Anti-Patterns Included

This application contains **12 major categories** of violations:

1. ✅ EJB 2.x Home/Remote interfaces (with @Stateless using manual JNDI lookup)
2. ✅ Hibernate proprietary annotations (@Type, @Cache, vendor-specific settings)
3. ✅ Hardcoded JNDI lookups (manual `InitialContext().lookup()` in servlets)
4. ✅ WebSphere/JBoss vendor lock-in (JTA platform, IIOP references)
5. ✅ File system dependencies (FileSystemHelper with hardcoded paths)
6. ✅ Old Servlet API (2.5) with XML config (web.xml-based configuration)
7. ✅ Log4j 1.x usage (vulnerable logging framework)
8. ✅ Mixed JPA and JDBC (combination of EntityManager and direct JDBC)
9. ✅ Manual session management (servlet-based session handling)
10. ✅ Hibernate-specific persistence.xml (vendor-specific properties)
11. ✅ Hardcoded configuration in deployment descriptors (passwords, paths in XML)
12. ✅ Old security patterns (HTTP Basic Auth, tomcat-users.xml)

### Additional Anti-Patterns
- ✅ HTML generation in servlets (PrintWriter with HTML strings)
- ✅ No input validation framework (manual parameter extraction)
- ✅ Servlet-based templating (manual string replacement)
- ✅ BigDecimal for currency without proper precision handling
- ✅ No REST API (servlet-based HTML responses)
- ✅ Legacy authentication (HTTP Basic Auth cannot properly logout)

See [KONVEYOR_ANALYSIS.md](./KONVEYOR_ANALYSIS.md) for complete details on all violations.

## Project Structure

```
boat-fuel-tracker-j2ee/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/boatfuel/
│   │   │       ├── entity/          # JPA entities with Hibernate annotations
│   │   │       │   ├── User.java
│   │   │       │   └── FuelUp.java
│   │   │       ├── ejb/             # EJB 3.0 beans with anti-patterns
│   │   │       │   ├── FuelUpService.java         # @Local interface
│   │   │       │   ├── FuelUpServiceBean.java     # @Stateless bean
│   │   │       │   ├── FuelUpStatistics.java
│   │   │       │   ├── FuelUpServiceHome.java     # EJB 2.x Home (legacy)
│   │   │       │   └── FuelUpServiceRemote.java   # EJB 2.x Remote (legacy)
│   │   │       ├── servlet/         # Old-style servlets
│   │   │       │   ├── FuelUpServlet.java         # Main data servlet
│   │   │       │   ├── IndexServlet.java          # Template-based index
│   │   │       │   └── LogoutServlet.java         # Logout handler
│   │   │       └── util/            # Utilities with anti-patterns
│   │   │           ├── JNDILookupHelper.java
│   │   │           └── FileSystemHelper.java
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml  # Hibernate-specific config
│   │   │   └── log4j.properties     # Log4j 1.x config
│   │   └── webapp/
│   │       ├── index-template.html  # HTML template with {{USERNAME}} placeholder
│   │       └── WEB-INF/
│   │           ├── web.xml          # Servlet 2.5 descriptor
│   │           ├── ejb-jar.xml      # EJB 2.x deployment descriptor
│   │           └── resources.xml    # TomEE datasource config
├── docker-compose.yml               # MySQL database setup
├── init.sql                         # Database schema and sample data
├── pom.xml                          # Maven with old dependencies
├── KONVEYOR_ANALYSIS.md             # Detailed violation analysis
└── README_J2EE.md                   # This file
```

## Building the Application

### Prerequisites
- JDK 8
- Maven 3.x

### Build
```bash
mvn clean package
```

This will create `target/boat-fuel-tracker.war`

## Running the Application

### Prerequisites
- JDK 8 or 11
- Maven 3.x
- Podman or Docker
- Apache TomEE 8.0.16

### 1. Start MySQL Database

```bash
# Using Podman
podman-compose up -d

# Or using Docker
docker-compose up -d
```

This starts MySQL 8.0 on port 3306 with:
- Database: `boatfuel`
- User: `boatfuel` / `boatfuel123`
- Initializes schema and sample data from `init.sql`

### 2. Configure TomEE

**Configure Datasource** (`$TOMEE_HOME/conf/tomee.xml`):
```xml
<Resource id="BoatFuelTrackerDS" type="DataSource">
  JdbcDriver = com.mysql.jdbc.Driver
  JdbcUrl = jdbc:mysql://localhost:3306/boatfuel?useSSL=false
  UserName = boatfuel
  Password = boatfuel123
  JtaManaged = true
</Resource>
```

**Configure Users** (`$TOMEE_HOME/conf/tomcat-users.xml`):
```xml
<role rolename="user"/>
<role rolename="admin"/>
<user username="testuser" password="password" roles="user"/>
<user username="admin" password="admin123" roles="user,admin"/>
```

**Copy MySQL JDBC Driver**:
```bash
cp ~/.m2/repository/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar \
   $TOMEE_HOME/lib/
```

### 3. Build and Deploy

```bash
# Build the WAR
mvn clean package

# Deploy to TomEE
cp target/boat-fuel-tracker.war $TOMEE_HOME/webapps/

# Start TomEE (if not running)
$TOMEE_HOME/bin/startup.sh
```

### 4. Access the Application

Open your browser and navigate to:
```
http://localhost:8080/boat-fuel-tracker/
```

**Login Credentials**:
- Username: `testuser` / Password: `password`
- Username: `admin` / Password: `admin123`

### Features
- View fuel-up history
- Add new fuel-up records (date, gallons, price, engine hours, location, notes)
- View statistics (total fill-ups, total gallons, total spent, average price/gallon)
- User authentication with HTTP Basic Auth
- Logout functionality

## Deployment

### Supported Application Servers (Legacy)
- Apache TomEE 8.x (recommended for this version)
- IBM WebSphere Application Server 8.5+
- JBoss EAP 6.x/7.x
- Oracle WebLogic 12c

## Running Konveyor Analysis

### Install Konveyor
```bash
# Install Konveyor Analyzer LSP
# See: https://github.com/konveyor/analyzer-lsp
```

### Analyze for Quarkus Migration
```bash
kantra analyze \
  --input . \
  --output ./konveyor-output \
  --target quarkus \
  --source java-ee \
  --rules https://github.com/konveyor/rulesets
```

### Analyze for Spring Boot Migration
```bash
kantra analyze \
  --input . \
  --output ./konveyor-output \
  --target spring-boot \
  --source java-ee
```

### Expected Results
- **100+ total violations**
- **Critical issues**: EJB 2.x, vendor lock-in, security vulnerabilities
- **High priority**: File system dependencies, hardcoded config
- **Medium priority**: Logging framework, old servlet API

## Violations by Category

| Category | Count | Severity |
|----------|-------|----------|
| EJB 2.x patterns | 70+ | Critical |
| Hibernate proprietary | 20+ | High |
| Hardcoded JNDI | 15+ | High |
| File system deps | 10+ | High |
| Vendor-specific APIs | 5+ | Critical |
| Log4j 1.x | 30+ | Critical |
| Servlet 2.5 | 10+ | Medium |
| Configuration issues | 15+ | Medium |

## Modernization Targets

### Option 1: Jakarta EE 10
- Modern Jakarta EE with CDI
- Standard JPA 3.0
- Servlet 6.0
- Deployable to WildFly, Payara, OpenLiberty

### Option 2: Quarkus
- Cloud-native, Kubernetes-ready
- Fast startup, low memory
- GraalVM native compilation
- Reactive capabilities

### Option 3: Spring Boot
- Popular enterprise framework
- Spring Data JPA
- Spring Security
- Wide ecosystem support

## Why This Code is Bad

### Not Cloud-Native
- ❌ Hardcoded file paths
- ❌ Stateful architecture
- ❌ Vendor lock-in
- ❌ Configuration in WAR file

### Not Portable
- ❌ WebSphere/JBoss specific code
- ❌ Hibernate proprietary features
- ❌ IIOP/RMI dependencies

### Security Issues
- ❌ Log4j 1.x vulnerabilities
- ❌ Hardcoded passwords
- ❌ Old security API

### Maintenance Nightmare
- ❌ EJB 2.x boilerplate
- ❌ Manual resource management
- ❌ No dependency injection
- ❌ XML-heavy configuration

## Original Application

The modern PWA version is on the `main` branch:
```bash
git checkout main
```

That version uses:
- Firebase Authentication
- Firestore Database
- Progressive Web App features
- Modern JavaScript
- GitHub Pages deployment

## Contributing

This is a demonstration project. Feel free to add MORE anti-patterns if you find patterns that Konveyor should detect!

## License

MIT License - Use for testing and demonstration only!

## Resources

- [Konveyor Project](https://www.konveyor.io/)
- [Konveyor Rulesets](https://github.com/konveyor/rulesets)
- [Jakarta EE](https://jakarta.ee/)
- [Quarkus](https://quarkus.io/)
- [Spring Boot](https://spring.io/projects/spring-boot)
