# Authentication & Authorization Guide

## Option 1: Keycloak + OIDC (Recommended)

### Setup Keycloak

```bash
# Start Keycloak
docker run -d \
  --name keycloak \
  -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev

# Access admin console: http://localhost:8180
# Login: admin / admin
```

### Configure Keycloak

1. Create realm: `boat-fuel-tracker`
2. Create client: `boat-fuel-app`
   - Client ID: `boat-fuel-app`
   - Valid redirect URIs: `http://localhost:8080/*`
   - Web origins: `http://localhost:8080`
3. Create roles: `user`, `admin`
4. Create test user with role

### Add Quarkus OIDC Extension

```bash
mvn quarkus:add-extension -Dextensions="oidc,oidc-token-propagation"
```

### Configure application.properties

```properties
# OIDC Configuration
quarkus.oidc.auth-server-url=http://localhost:8180/realms/boat-fuel-tracker
quarkus.oidc.client-id=boat-fuel-app
quarkus.oidc.credentials.secret=<your-client-secret>
quarkus.oidc.application-type=web-app

# Authentication
quarkus.http.auth.permission.authenticated.paths=/*
quarkus.http.auth.permission.authenticated.policy=authenticated
quarkus.http.auth.permission.public.paths=/,/index.html,/q/*
quarkus.http.auth.permission.public.policy=permit
```

### Update REST Resources

```java
@GET
@Path("/user/{userId}")
@RolesAllowed("user")
public List<FuelUp> getFuelUpsByUser(
    @PathParam("userId") String userId,
    @Context SecurityContext ctx) {

    // Get authenticated user
    String authenticatedUser = ctx.getUserPrincipal().getName();

    // Users can only see their own data (or admins can see all)
    if (!ctx.isUserInRole("admin") && !authenticatedUser.equals(userId)) {
        throw new ForbiddenException("Access denied");
    }

    return fuelUpService.getFuelUpsByUser(userId);
}
```

---

## Option 2: Simple JWT Authentication

### Add JWT Extension

```bash
mvn quarkus:add-extension -Dextensions="smallrye-jwt"
```

### Generate Keys

```bash
# Generate private key
openssl genrsa -out privateKey.pem 2048

# Generate public key
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

### Configure

```properties
mp.jwt.verify.publickey.location=META-INF/resources/publicKey.pem
mp.jwt.verify.issuer=https://boat-fuel-tracker.com
```

### Secure Endpoints

```java
@Inject
JsonWebToken jwt;

@GET
@RolesAllowed({"user", "admin"})
public List<FuelUp> getFuelUps() {
    String userId = jwt.getClaim("sub");
    return FuelUp.findByUser(userId);
}
```

---

## Option 3: Database Authentication

### Add Security JDBC Extension

```bash
mvn quarkus:add-extension -Dextensions="security-jdbc,elytron-security-jdbc"
```

### Update User Entity

```java
@Entity
@Table(name = "USERS")
public class User extends PanacheEntityBase {

    @Id
    public String userId;

    public String email;

    @Column(name = "PASSWORD_HASH")
    public String passwordHash; // BCrypt hash

    @Column(name = "ROLE")
    public String role; // "user" or "admin"
}
```

### Configure

```properties
quarkus.security.jdbc.enabled=true
quarkus.security.jdbc.principal-query.sql=SELECT password_hash FROM users WHERE user_id=?
quarkus.security.jdbc.principal-query.bcrypt-password-mapper.enabled=true
quarkus.security.jdbc.principal-query.bcrypt-password-mapper.password-index=1
```

### Create User Registration

```java
@POST
@Path("/register")
@Transactional
public Response register(UserRegistration reg) {
    User user = new User();
    user.userId = reg.username;
    user.email = reg.email;
    user.passwordHash = BcryptUtil.bcryptHash(reg.password);
    user.role = "user";
    user.persist();
    return Response.ok().build();
}
```

---

## Recommended Approach

**For this demo app**: Use **Keycloak + OIDC**

### Why?
- ✅ Production-grade security
- ✅ Easy to demonstrate
- ✅ Complete user management UI
- ✅ Supports social login
- ✅ MFA/2FA support
- ✅ Industry standard

### Quick Start

```bash
# 1. Add to docker-compose.yml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    command: start-dev
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8180:8080"

# 2. Add extension
mvn quarkus:add-extension -Dextensions="oidc"

# 3. Configure application.properties
quarkus.oidc.auth-server-url=http://localhost:8180/realms/boat-fuel-tracker
quarkus.oidc.client-id=boat-fuel-app

# 4. Add @RolesAllowed to endpoints
@RolesAllowed("user")
```

---

## Security Best Practices

1. **Always use HTTPS in production**
2. **Never store passwords in plain text** (use BCrypt)
3. **Implement rate limiting** for auth endpoints
4. **Use strong session management**
5. **Validate JWT expiration**
6. **Implement CORS properly**
7. **Use security headers** (CSP, X-Frame-Options, etc.)
8. **Log authentication events**
9. **Implement account lockout** after failed attempts
10. **Use refresh tokens** for long-lived sessions

---

## Testing Authentication

```bash
# Get token from Keycloak
TOKEN=$(curl -X POST 'http://localhost:8180/realms/boat-fuel-tracker/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=boat-fuel-app' \
  -d 'client_secret=<secret>' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=password' | jq -r '.access_token')

# Use token in API request
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fuelups/user/testuser
```

---

## Resources

- [Quarkus Security Guide](https://quarkus.io/guides/security-overview)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OIDC Specification](https://openid.net/connect/)
