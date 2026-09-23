---
name: run-project
description: >-
  Guides and automates running, starting, testing, and verifying the full-stack Schooly ecosystem
  (schooly-backend and schooly-web). Covers port management (3000, 8080), Java 17 / Maven lifecycle,
  Neon PostgreSQL connectivity, Flyway migration conflict resolution, Vite dev server, proxy verification,
  and end-to-end health check validation. Use whenever the user asks to "run the project", "start backend
  and frontend", "run frontend and backend", "boot schooly", "launch fullstack", "restart services", or
  verify end-to-end connectivity.
---

# Schooly Full-Stack Run & Lifecycle Protocol

This skill provides standard operating procedures for starting, stopping, verifying, and troubleshooting both `schooly-backend` and `schooly-web` in the local development environment.

## 1. System Architecture & Port Map

| Component | Path | Technology | Port | Key Config |
| :--- | :--- | :--- | :--- | :--- |
| **Backend** | `/Users/admin/Desktop/Development/private/schooly-backend` | Spring Boot 4, Java 17, Neon PostgreSQL, Flyway | `8080` | `application.properties` |
| **Frontend** | `/Users/admin/Desktop/Development/private/schooly-web` | React 19, Vite, Tailwind v4, TanStack Query | `3000` | `vite.config.ts`, `.env` |
| **Database** | Remote AWS Neon Serverless PostgreSQL | PostgreSQL 16+ Pooler | `5432` | `spring.datasource.url` |

> [!NOTE]
> Vite runs on port `3000` and proxies all `/api`, `/auth`, `/onboarding`, `/import`, and `/health` requests to `http://localhost:8080`.
> In `schooly-web/.env`, set `VITE_USE_MOCK_API=false` to use the live backend, or `true` for MSW mocking.

---

## 2. Preflight Checklist

Before starting services, run the following preflight checks:

1. **Verify Ports**:
   ```bash
   lsof -i :8080 -i :3000
   ```
   - If port `8080` or `3000` has an orphaned or unwanted process:
     ```bash
     kill -9 $(lsof -ti :8080)
     kill -9 $(lsof -ti :3000)
     ```

2. **Verify Java 17 Environment**:
   ```bash
   java -version
   ```
   - Must be Java 17 (e.g., Eclipse Temurin 17).

3. **Verify Database Migrations Integrity**:
   Check that there are no duplicate Flyway migration versions between `src` and `target`:
   ```bash
   diff <(ls src/main/resources/db/migration) <(ls target/classes/db/migration 2>/dev/null)
   ```
   - If differences or duplicate version numbers exist, clean the target directory:
     ```bash
     rm -rf target/classes/db/migration && ./mvnw compile
     ```

---

## 3. Starting the Backend (`schooly-backend`)

1. **Working Directory**: `/Users/admin/Desktop/Development/private/schooly-backend`
2. **Launch as Background Daemon**:
   Use the `run_command` tool with `IsDaemon: true`:
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Monitor Startup**:
   Inspect task output or `backend.log`. Look for:
   ```text
   Tomcat started on port 8080 (http) with context path '/'
   Started SchoolErpBackendApplication in ... seconds
   ```
4. **Verify Backend Health**:
   ```bash
   curl -s -i http://localhost:8080/health
   ```
   - Expected response: `HTTP/1.1 200` with body `{"status":"UP"}`.

---

## 4. Starting the Frontend (`schooly-web`)

1. **Working Directory**: `/Users/admin/Desktop/Development/private/schooly-web`
2. **Check if Vite is Already Running**:
   ```bash
   lsof -i :3000
   ```
3. **Launch if Not Running**:
   Use the `run_command` tool with `IsDaemon: true`:
   ```bash
   npm run dev
   ```
4. **Verify Frontend Dev Server**:
   ```bash
   curl -s -i http://localhost:3000
   ```
   - Expected response: `HTTP/1.1 200 OK` serving `index.html`.

---

## 5. End-to-End Verification

Always perform end-to-end verification across the proxy boundary:

1. **Vite Proxy Health Check**:
   ```bash
   curl -s -i http://localhost:3000/health
   ```
   - Must return `HTTP/1.1 200 OK` and `{"status":"UP"}` via the Vite reverse proxy.

2. **API Endpoint Test**:
   ```bash
   curl -s -i -X POST http://localhost:3000/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"superadmin@schooly.com","password":"password"}'
   ```
   - A valid JSON response (e.g. validation error or auth token) confirms that requests route from Vite to Spring Boot, Spring Security processes them, and the response traverses back cleanly.

---

## 6. Troubleshooting & Common Pitfalls

### Issue 1: Flyway Duplicate Version (`FlywayException: Found more than one migration with version X`)
- **Cause**: A migration file was added with an existing version number, or stale compiled migrations remain in `target/classes/db/migration/`.
- **Solution**:
  1. Inspect `ls src/main/resources/db/migration/ | sort -V | tail -n 15`.
  2. Ensure each `V<N>__<name>.sql` has a strictly unique version number `<N>`.
  3. Run `rm -rf target/classes/db/migration && ./mvnw compile`.

### Issue 2: Neon PostgreSQL Connection Timeout (`HikariPool - Connection is not available`)
- **Cause**: Neon Serverless compute may be in a scale-to-zero sleep state and takes 1-3 seconds to wake up, or pooler connections reached capacity.
- **Solution**:
  - `application.properties` includes `spring.datasource.hikari.connection-timeout=30000` and `tcpKeepAlive=true`.
  - Allow 10 seconds for the initial wake-up connection. If necessary, test connectivity using `nc -zv ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech 5432`.

### Issue 3: Port 8080 or 3000 Already in Use
- **Cause**: A previous daemon process was left running.
- **Solution**:
  ```bash
  kill -9 $(lsof -ti :8080)
  kill -9 $(lsof -ti :3000)
  ```

### Issue 4: Vite Proxy Returns 502 Bad Gateway
- **Cause**: Spring Boot backend on port 8080 is either not running, still initializing, or crashed during startup.
- **Solution**:
  1. Check `lsof -i :8080`.
  2. Check the backend background task log to diagnose the exception.
