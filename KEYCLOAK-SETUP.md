# Keycloak Setup Guide for Boat Fuel Tracker

## Quick Start

### 1. Start Keycloak with Docker Compose

```bash
docker-compose up -d keycloak
```

Wait for Keycloak to be healthy (~30 seconds):
```bash
docker-compose logs -f keycloak
# Wait for: "Keycloak 23.0 on JVM (powered by Quarkus) started"
```

### 2. Access Keycloak Admin Console

Open: **http://localhost:8180**

**Login credentials:**
- Username: `admin`
- Password: `admin`

---

## Keycloak Configuration

### Step 1: Create Realm

1. Click the dropdown at the top left (says "master")
2. Click **"Create Realm"**
3. **Realm name**: `boat-fuel-tracker`
4. Click **"Create"**

### Step 2: Create Client

1. In the left menu, click **"Clients"**
2. Click **"Create client"**
3. Fill in:
   - **Client ID**: `boat-fuel-app`
   - **Client type**: OpenID Connect
   - Click **"Next"**
4. Configure capabilities:
   - ✅ Client authentication: **ON**
   - ✅ Authorization: **OFF**
   - ✅ Authentication flow: **Standard flow** ✓, **Direct access grants** ✓
   - Click **"Next"**
5. Configure settings:
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Valid post logout redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `http://localhost:8080`
   - Click **"Save"**

### Step 3: Get Client Secret

1. Click on the **"Credentials"** tab
2. Copy the **Client secret** value
3. Update `src/main/resources/application.properties`:
   ```properties
   quarkus.oidc.credentials.secret=YOUR_CLIENT_SECRET_HERE
   ```

### Step 4: Create Roles

1. In the left menu, click **"Realm roles"**
2. Click **"Create role"**
3. Create two roles:
   - **Role name**: `user`
   - **Role name**: `admin`

### Step 5: Create Test User

1. In the left menu, click **"Users"**
2. Click **"Add user"**
3. Fill in:
   - **Username**: `testuser`
   - **Email**: `testuser@example.com`
   - **First name**: `Test`
   - **Last name**: `User`
   - **Email verified**: ✓ ON
   - Click **"Create"**

4. Set password:
   - Go to **"Credentials"** tab
   - Click **"Set password"**
   - **Password**: `password`
   - **Password confirmation**: `password`
   - **Temporary**: OFF (uncheck)
   - Click **"Save"**
   - Confirm by clicking **"Save password"**

5. Assign role:
   - Go to **"Role mapping"** tab
   - Click **"Assign role"**
   - Select `user` role
   - Click **"Assign"**

### Step 6: Create Admin User (Optional)

Repeat Step 5 with:
- **Username**: `admin`
- **Email**: `admin@example.com`
- **Password**: `admin`
- **Roles**: `user` AND `admin`

---

## Testing Authentication

### Option 1: Using Docker Compose (Full Stack)

```bash
# Start all services (Keycloak + MySQL + App)
docker-compose up -d

# Check logs
docker-compose logs -f app
```

Access: http://localhost:8080

### Option 2: Dev Mode with Auth Enabled

Edit `application.properties` and remove the dev override:
```properties
# Comment out these lines:
# %dev.quarkus.oidc.enabled=false
# %dev.quarkus.http.auth.permission.authenticated.policy=permit
```

Then start:
```bash
mvn quarkus:dev
```

### Option 3: Dev Mode WITHOUT Auth (Current Setup)

By default, authentication is disabled in dev mode for easier testing:
```bash
mvn quarkus:dev
# No login required!
```

---

## Get Access Token (for API testing)

```bash
# Get token
TOKEN=$(curl -X POST 'http://localhost:8180/realms/boat-fuel-tracker/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=boat-fuel-app' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=password' \
  | jq -r '.access_token')

# Test API with token
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/fuelups/user/testuser
```

---

## Verify Setup

### Check Keycloak is Running

```bash
curl http://localhost:8180/health/ready
# Should return: {"status":"UP"}
```

### Check Realm Configuration

```bash
curl http://localhost:8180/realms/boat-fuel-tracker/.well-known/openid-configuration | jq
```

### Test Login Flow

1. Access protected endpoint: http://localhost:8080/fuelups/user/testuser
2. You should be redirected to Keycloak login
3. Login with `testuser` / `password`
4. You should be redirected back to the application

---

## Troubleshooting

### Keycloak Won't Start

```bash
# Check logs
docker-compose logs keycloak

# Restart
docker-compose restart keycloak
```

### "Invalid redirect URI" Error

Make sure the redirect URIs in Keycloak client settings include:
- `http://localhost:8080/*`

### Token Validation Failed

1. Check the client secret matches in both Keycloak and `application.properties`
2. Verify the realm name is `boat-fuel-tracker`
3. Check the auth server URL: `http://localhost:8180/realms/boat-fuel-tracker`

### Port 8180 Already in Use

```bash
# Find process using port 8180
lsof -i :8180

# Kill it or change Keycloak port in docker-compose.yml
```

---

## Production Considerations

### 1. Use External Database

Instead of the built-in H2, configure PostgreSQL or MySQL:

```yaml
services:
  keycloak:
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: password
```

### 2. Enable HTTPS

```properties
quarkus.oidc.auth-server-url=https://keycloak.example.com/realms/boat-fuel-tracker
```

### 3. Use Secrets Management

Don't hardcode the client secret:
```properties
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
```

### 4. Configure SMTP for Email Verification

In Keycloak:
- Realm Settings → Email
- Configure SMTP server for email verification and password reset

### 5. Enable Rate Limiting

Protect against brute force attacks in Keycloak:
- Realm Settings → Security Defenses → Brute Force Detection

---

## Useful Keycloak Commands

```bash
# Export realm configuration
docker exec -it boat-fuel-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /tmp/export --realm boat-fuel-tracker

# Import realm configuration
docker exec -it boat-fuel-keycloak /opt/keycloak/bin/kc.sh import \
  --file /tmp/export/boat-fuel-tracker-realm.json

# Create admin user via CLI
docker exec -it boat-fuel-keycloak /opt/keycloak/bin/kcadm.sh \
  create users -r boat-fuel-tracker \
  -s username=newuser -s enabled=true
```

---

## Integration with Front-End

The front-end will automatically redirect to Keycloak for login when accessing protected resources. After successful authentication, users will be redirected back to the application with a valid session.

For manual integration, see: [AUTHENTICATION.md](AUTHENTICATION.md)

---

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Quarkus OIDC Guide](https://quarkus.io/guides/security-oidc-code-flow-authentication)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html)

---

## Support

If you encounter issues:
1. Check Keycloak logs: `docker-compose logs keycloak`
2. Check app logs: `mvn quarkus:dev` or `docker-compose logs app`
3. Verify Keycloak health: `curl http://localhost:8180/health/ready`
