# Konveyor Analysis Rules for J2EE to Quarkus Migration

## Overview

This repository contains a comprehensive set of Konveyor analyzer rules (`konveyor-rules.yaml`) designed to identify J2EE anti-patterns and migration issues when moving applications to Quarkus.

## Rule Categories

### 1. EJB Migration Rules
- **ejb-sessionbean-to-cdi**: Detects EJB 2.x SessionBean interfaces → Replace with CDI @ApplicationScoped
- **ejb-home-interface**: Identifies EJB Home interfaces → Remove, use CDI @Inject
- **ejb-remote-interface**: Finds EJB Remote interfaces → Replace with CDI beans
- **portable-remote-object-narrow**: Detects PortableRemoteObject.narrow() → Use CDI injection
- **ejb-ref-in-webxml**: Finds EJB references in web.xml → Remove, use CDI

### 2. Servlet to JAX-RS Rules
- **httpservlet-to-jaxrs**: Detects HttpServlet classes → Replace with JAX-RS @Path resources
- **servlet-request-response**: Finds servlet API usage → Use JAX-RS annotations
- **javax-servlet-package**: Identifies javax.servlet imports → Replace with JAX-RS

### 3. JNDI and Dependency Injection Rules
- **jndi-lookup-to-inject**: Detects manual JNDI lookups → Replace with @Inject
- **hardcoded-jndi-names**: Finds hardcoded JNDI strings → Move to configuration

### 4. Hibernate and JPA Rules
- **hibernate-cache-annotation**: Detects @Cache annotation → Configure via properties
- **hibernate-type-annotation**: Finds @Type annotation → Use standard JPA
- **hibernate-generic-generator**: Identifies @GenericGenerator → Use JPA or Panache
- **hibernate-creation-timestamp**: Detects @CreationTimestamp → Use @PrePersist
- **hibernate-index-annotation**: Finds old @Index → Use JPA @Table(indexes=...)
- **entity-to-panache**: Suggests converting entities to Panache (optional)

### 5. Jakarta EE Migration Rules
- **javax-to-jakarta-persistence**: Detects javax.persistence.* → Replace with jakarta.persistence.*
- **javax-ejb-package**: Finds javax.ejb.* → Replace with jakarta.enterprise.context.*
- **javax-servlet-package**: Identifies javax.servlet.* → Use jakarta.ws.rs.*

### 6. Logging Rules
- **log4j-to-quarkus-logging**: Detects Log4j 1.x → Replace with Quarkus Log

### 7. Vendor Lock-in Rules
- **websphere-vendor-lockin**: Identifies WebSphere-specific APIs → Remove/replace
- **jboss-vendor-lockin**: Finds JBoss-specific APIs → Use standard alternatives

### 8. Configuration Rules
- **webxml-to-quarkus-config**: Detects web.xml → Replace with application.properties
- **persistence-xml-to-quarkus**: Finds persistence.xml → Use application.properties

### 9. JDBC and Database Rules
- **jdbc-to-panache**: Detects direct JDBC code → Replace with Panache
- **commons-dbcp-to-agroal**: Finds Commons DBCP → Use Quarkus Agroal
- **mysql-connector-upgrade**: Detects old MySQL driver → Use quarkus-jdbc-mysql

### 10. Java Modernization Rules
- **date-to-java-time**: Suggests java.util.Date → java.time API
- **manual-resource-cleanup**: Finds manual try/finally → Use try-with-resources

## Rule Effort Levels

- **Effort 1**: Simple changes (remove annotation, update import)
- **Effort 2**: Moderate changes (refactor method, update configuration)
- **Effort 3**: Significant changes (refactor class, change architecture pattern)
- **Effort 5**: Major changes (complete rewrite of component)

## Rule Categories by Severity

- **mandatory**: Must be addressed for successful migration
- **optional**: Recommended improvements for better Quarkus integration
- **potential**: Code smells or configuration issues to review
- **information**: Informational messages about dependencies

## Using These Rules with Konveyor

### 1. Install Konveyor CLI

```bash
# Download from https://github.com/konveyor/analyzer-lsp/releases
# Or use container image
podman pull quay.io/konveyor/analyzer-lsp
```

### 2. Run Analysis

```bash
# Using CLI
konveyor-analyzer \
  --rules=/path/to/konveyor-rules.yaml \
  --input=/path/to/boat-fuel-tracker-j2ee \
  --output=/path/to/analysis-results

# Using container
podman run -v $(pwd):/app quay.io/konveyor/analyzer-lsp \
  analyze \
  --rules=/app/konveyor-rules.yaml \
  --input=/app \
  --output=/app/analysis-output
```

### 3. Review Results

Analysis results will include:
- Number of incidents per rule
- Effort estimation for migration
- File locations of issues
- Links to migration documentation
- Recommended actions

### Example Output

```
Rule: ejb-sessionbean-to-cdi
Incidents: 2
Effort: 10 (5 per incident)
Files:
  - src/main/java/com/boatfuel/ejb/FuelUpServiceBean.java:29
  - src/main/java/com/boatfuel/ejb/UserSessionBean.java:15

Rule: httpservlet-to-jaxrs
Incidents: 1
Effort: 5
Files:
  - src/main/java/com/boatfuel/servlet/FuelUpServlet.java:31

Total Effort: 85 story points
```

## Rule Patterns Explained

### Java Referenced Patterns

The rules use various `java.referenced` patterns:

- **IMPLEMENTS_TYPE**: Detects when a class implements an interface
- **INHERITANCE**: Detects when a class extends another class
- **METHOD_CALL**: Detects method invocations
- **IMPORT**: Detects import statements
- **ANNOTATION**: Detects annotation usage
- **VARIABLE_DECLARATION**: Detects variable declarations
- **LITERAL**: Detects string literals

### XML Patterns

For configuration files, rules use XPath:

```yaml
builtin.xml:
  xpath: /web-app/servlet
  filepaths:
    - "**/web.xml"
```

## Customizing Rules

You can customize these rules for your specific needs:

1. **Adjust effort levels** based on your team's experience
2. **Add custom messages** with specific instructions for your codebase
3. **Add links** to internal documentation or wiki pages
4. **Create new rules** for organization-specific patterns
5. **Change categories** (mandatory → optional) based on priorities

## Rule Labels

Rules are labeled for filtering and organization:

- `konveyor.io/target=quarkus`: Target framework
- `konveyor.io/source=java-ee`: Source framework
- Additional labels: `ejb`, `servlet`, `jpa`, `vendor-lockin`, etc.

## Integration with CI/CD

You can integrate Konveyor analysis into your CI/CD pipeline:

```yaml
# GitHub Actions example
- name: Run Konveyor Analysis
  run: |
    konveyor-analyzer \
      --rules=konveyor-rules.yaml \
      --input=. \
      --output=analysis-results

- name: Upload Results
  uses: actions/upload-artifact@v2
  with:
    name: konveyor-analysis
    path: analysis-results
```

## Related Resources

- [Konveyor Documentation](https://konveyor.io/docs/)
- [Analyzer LSP Rules Reference](https://github.com/konveyor/analyzer-lsp/blob/main/docs/rules.md)
- [Quarkus Migration Guide](https://quarkus.io/guides/migration-guide)
- [Quarkus from Spring Boot Guide](https://quarkus.io/guides/spring-boot-properties)

## Contributing

To add new rules:

1. Follow the rule schema in `konveyor-rules.yaml`
2. Test rules against sample code
3. Document the rule in this README
4. Submit a pull request with test cases

## License

These rules are provided as examples for educational purposes. Customize as needed for your organization.
