# Custom Konveyor Rules for Quarkus Migration

This directory contains custom Konveyor rules that fill critical gaps in the default Konveyor rulesets.

## What These Rules Detect

These custom rules catch **CRITICAL** issues that the default Konveyor rulesets miss:

### ⛔ CRITICAL (Application Won't Run Without Fixes)

1. **JNDI InitialContext Usage** (`custom-quarkus-jndi-00001`, `00002`, `00003`)
   - Detects `new InitialContext()` and `Context.lookup()` calls
   - Detects `PortableRemoteObject` usage (RMI-IIOP)
   - **Why:** JNDI not supported in Quarkus - runtime failure

2. **Missing @Transactional** (`custom-quarkus-transaction-00001`)
   - Detects `EntityManager.persist/merge/remove` calls
   - Warns that CDI beans don't have automatic transactions like EJB
   - **Why:** Database writes won't commit without transactions

3. **Servlet JNDI Lookups** (`custom-quarkus-servlet-00001`)
   - Detects servlets with JNDI lookups in `init()` method
   - **Why:** Common anti-pattern that fails in Quarkus

4. **Log4j 1.x Usage** (`custom-quarkus-logging-00001`)
   - Detects `org.apache.log4j.Logger` imports
   - **Why:** Not included in Quarkus, security vulnerabilities

### 🎯 RECOMMENDED (Improves Code Quality)

5. **@PersistenceContext Usage** (`custom-quarkus-persistence-00001`, `00002`)
   - Detects `@PersistenceContext` annotations
   - Recommends `@Inject` instead

6. **Mixed JDBC/JPA** (`custom-quarkus-jdbc-00001`)
   - Detects `PreparedStatement` usage
   - Recommends using JPA/JPQL consistently

7. **EJB Legacy Interfaces** (`custom-quarkus-ejb-00001`, `00002`)
   - Detects EJB 2.x Home/Remote interfaces
   - Recommends deleting them (obsolete)

8. **Panache Recommendation** (`custom-quarkus-panache-00001`)
   - Suggests using Hibernate Panache for simpler JPA

---

## How to Use These Rules

### Option 1: Run Analysis with Custom Rules Only

```bash
kantra analyze \
  --input ~/Workspace/boat-fuel-tracker-j2ee \
  --output ./konveyor-custom-report \
  --rules ./custom-rules/quarkus-custom-rules.yaml \
  --target quarkus
```

This runs **only** the custom rules (useful for focused analysis).

### Option 2: Run with Both Default and Custom Rules (Recommended)

```bash
kantra analyze \
  --input ~/Workspace/boat-fuel-tracker-j2ee \
  --output ./konveyor-full-report \
  --rules ./custom-rules/quarkus-custom-rules.yaml \
  --source java-ee \
  --target quarkus \
  --target cloud-readiness \
  --target jakarta-ee9
```

This runs **both** default rulesets and custom rules for comprehensive coverage.

### Option 3: Custom Rules Directory

If you have multiple custom rule files:

```bash
kantra analyze \
  --input ~/Workspace/boat-fuel-tracker-j2ee \
  --output ./konveyor-report \
  --rules ./custom-rules/ \
  --target quarkus
```

---

## Expected Results

Running with these custom rules on this project should detect:

| Rule ID | Expected Matches | Files |
|---------|------------------|-------|
| custom-quarkus-jndi-00001 | 3-5 | JNDILookupHelper.java, FuelUpServlet.java |
| custom-quarkus-jndi-00002 | 5-10 | JNDILookupHelper.java, FuelUpServiceBean.java |
| custom-quarkus-transaction-00001 | 3-4 | FuelUpServiceBean.java |
| custom-quarkus-persistence-00001 | 1 | FuelUpServiceBean.java |
| custom-quarkus-logging-00001 | 8-10 | All Java files |
| custom-quarkus-servlet-00001 | 1 | FuelUpServlet.java |
| custom-quarkus-jdbc-00001 | 1 | FuelUpServiceBean.java |
| custom-quarkus-ejb-00001 | 1 | FuelUpServiceHome.java |
| custom-quarkus-ejb-00002 | 1 | FuelUpServiceRemote.java |

---

## Verifying Rules Work

### Test a Single Rule

```bash
# Test JNDI detection
grep -rn "new InitialContext()" src/

# Expected files:
# - src/main/java/com/boatfuel/servlet/FuelUpServlet.java:47
# - src/main/java/com/boatfuel/util/JNDILookupHelper.java:74
```

### Compare with Default Rules

```bash
# Run default rules only
kantra analyze --input . --output ./report-default --target quarkus

# Run custom rules only
kantra analyze --input . --output ./report-custom --rules ./custom-rules/quarkus-custom-rules.yaml

# Compare violation counts
diff report-default/output.yaml report-custom/output.yaml
```

---

## Customizing Rules

### Modify Rule Severity

Change the `category` field:
- `mandatory` - Blocking issue (red flag)
- `potential` - Likely issue (yellow flag)
- `optional` - Nice to have (blue flag)

```yaml
- ruleID: custom-quarkus-jndi-00001
  category: mandatory  # Change to 'potential' if desired
```

### Modify Effort Estimate

Change the `effort` field (1-5 scale, where 5 is most effort):

```yaml
- ruleID: custom-quarkus-jndi-00001
  effort: 5  # High effort - lots of code changes
```

### Add More Pattern Matching

Example - detect additional JNDI patterns:

```yaml
- ruleID: custom-quarkus-jndi-00004
  description: "JNDI env-entry lookups"
  when:
    java.referenced:
      location: METHOD_CALL
      pattern: "*.lookup"
      from: "javax.naming.InitialContext"
  message: "JNDI env-entry lookups not supported..."
```

---

## Rule Development Tips

### 1. Test Patterns with Grep First

Before creating a rule, verify the pattern exists:

```bash
# Find all InitialContext usage
grep -rn "InitialContext" src/

# Find EntityManager write operations
grep -rn "entityManager.persist\|entityManager.merge" src/
```

### 2. Start Simple, Then Refine

Start with a basic pattern:
```yaml
when:
  java.referenced:
    pattern: javax.naming.InitialContext
```

Then add specificity:
```yaml
when:
  java.referenced:
    location: CONSTRUCTOR_CALL
    pattern: javax.naming.InitialContext
```

### 3. Use Compound Conditions for Complex Rules

```yaml
when:
  and:
    - java.referenced:
        location: INHERITANCE
        pattern: javax.servlet.http.HttpServlet
    - java.referenced:
        location: METHOD_CALL
        pattern: javax.naming.Context.lookup*
```

### 4. Test Against Multiple Codebases

Test your rules against:
- This sample app
- Other legacy J2EE apps
- Spring Boot apps (should NOT match)

---

## Contributing Custom Rules

If you create additional useful rules, consider:

1. **Document the rule** - Add comments explaining what it detects
2. **Test thoroughly** - Verify it doesn't have false positives
3. **Share with community** - Submit to https://github.com/konveyor/rulesets

### Rule Submission Checklist

- [ ] Clear `ruleID` and `description`
- [ ] Appropriate `category` (mandatory/optional/potential)
- [ ] Realistic `effort` estimate (1-5)
- [ ] Helpful `message` with before/after examples
- [ ] Tested on real codebases
- [ ] No false positives

---

## Troubleshooting

### Rules Not Being Applied

**Problem:** Custom rules don't show up in report

**Solutions:**
1. Check YAML syntax: `yamllint quarkus-custom-rules.yaml`
2. Verify `--rules` path is correct
3. Check kantra logs: `kantra analyze ... --verbose`
4. Ensure `labels` include `konveyor.io/target=quarkus`

### Too Many False Positives

**Problem:** Rule matches code that shouldn't be flagged

**Solutions:**
1. Add more specific `location` constraints
2. Use compound `and` conditions
3. Narrow the pattern (less wildcards)
4. Consider changing `category` to `optional`

### Rule Patterns Not Matching

**Problem:** Rule should match but doesn't

**Solutions:**
1. Check if pattern is too specific
2. Try broader pattern with wildcards: `*InitialContext*`
3. Verify the code actually compiles (analyzer needs compiled code)
4. Check if you need `location: METHOD_CALL` vs `TYPE`

---

## Resources

- [Konveyor Analyzer Rules Documentation](https://github.com/konveyor/analyzer-lsp/blob/main/docs/rules.md)
- [Default Quarkus Rulesets](https://github.com/konveyor/rulesets/tree/main/default/generated/quarkus)
- [Kantra CLI Documentation](https://github.com/konveyor/kantra)
- [This Project's Analysis](../KONVEYOR_RULESET_ANALYSIS.md)

---

## Quick Reference

### Common Pattern Locations

- `CONSTRUCTOR_CALL` - `new Foo()`
- `METHOD_CALL` - `object.method()`
- `IMPORT` - `import com.example.*`
- `TYPE` - References to a class/interface
- `ANNOTATION` - `@MyAnnotation`
- `INHERITANCE` - `extends Foo` or `implements Bar`
- `FIELD_DECLARATION` - Field/variable declarations

### Common Patterns

```yaml
# Match any class in package
pattern: "com.example.*"

# Match specific method
pattern: "com.example.MyClass.myMethod*"

# Match any method with name
pattern: "*.lookup"

# Match wildcard
pattern: "*InitialContext*"
```

### Effort Guidelines

- **1** - Simple find/replace (5-15 min)
- **2** - Straightforward refactor (30-60 min)
- **3** - Moderate refactor (1-3 hours)
- **5** - Complex refactor (1-2 days)
- **7** - Major redesign (1 week+)

### Category Guidelines

- **mandatory** - Application won't run/compile
- **potential** - Likely needs fixing, but might work
- **optional** - Best practice, quality improvement
