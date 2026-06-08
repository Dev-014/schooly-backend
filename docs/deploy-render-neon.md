# Schooly Backend Deployment Guide

This guide explains how this backend is deployed, how the database works, what values to enter in Render, how to test every important API, and how to fix common problems.

You should be able to deploy this project again from scratch after reading this.

## What We Are Building

The project has two separate parts:

| Part | Tool | Purpose |
| --- | --- | --- |
| API server | Render | Runs the Spring Boot backend and exposes HTTPS APIs. |
| Database | Neon | Stores PostgreSQL data used by the backend. |

Important decision:

**Neon does not host the API server.** Neon only hosts PostgreSQL. The Java/Spring Boot backend must run somewhere else. For this project, use **Render** for the backend because the repo already has a `Dockerfile` and `render.yaml`.

Current live API:

```text
https://schooly-backend-2pky.onrender.com
```

Current health check:

```text
https://schooly-backend-2pky.onrender.com/health
```

Expected response:

```json
{"status":"UP"}
```

## Project Files You Need To Know

| File | Why it matters |
| --- | --- |
| `src/main/resources/application.properties` | Spring Boot runtime config. Reads database and JWT values from environment variables. |
| `render.yaml` | Render Blueprint config for creating the backend service. |
| `Dockerfile` | Builds and runs the Spring Boot app in a container. |
| `src/main/resources/db/migration` | Flyway migrations that create and seed database tables. |
| `pom.xml` | Maven dependencies and build config. |

Do not put production secrets directly in committed files. Use Render environment variables.

## Current Neon Database Values

Use these values in Render:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_rowJC37Isvuj
```

The password is the part between the username and host in the Neon connection string:

```text
postgresql://neondb_owner:npg_rowJC37Isvuj@ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb
```

In this example:

```text
username = neondb_owner
password = npg_rowJC37Isvuj
host = ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech
database = neondb
```

Security note: because this password has been shared in setup notes, rotate it in Neon before serious production use.

## What To Put In APP_JWT_SECRET

`APP_JWT_SECRET` is the private key used to sign login tokens. Anyone with this value can forge API tokens, so treat it like a password.

For Render, use this generated value:

```text
APP_JWT_SECRET=nDZgWnJ64fm9c5wrN7+8HS6kjNhSVZ7YHD87FM58flARN776udK+BjH+1yT2G06b
```

If you want to generate a new one on macOS/Linux:

```bash
openssl rand -base64 48
```

Rules for `APP_JWT_SECRET`:

- Use a long random value.
- Do not use a normal word or school name.
- Do not commit it to GitHub.
- Store it only in Render environment variables.
- If it leaks, replace it and redeploy.

## Required Render Environment Variables

In Render, open:

```text
Service > Environment
```

Add these:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_rowJC37Isvuj
APP_JWT_SECRET=nDZgWnJ64fm9c5wrN7+8HS6kjNhSVZ7YHD87FM58flARN776udK+BjH+1yT2G06b
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

Optional values:

```text
APP_JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000
APP_JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000
```

Meaning:

| Variable | Meaning |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL used by Spring Boot to connect to Neon. |
| `SPRING_DATASOURCE_USERNAME` | Neon database user. |
| `SPRING_DATASOURCE_PASSWORD` | Neon database password. |
| `APP_JWT_SECRET` | Secret key for signing JWT auth tokens. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Keeps Hibernate in validation mode. Flyway manages schema changes. |

## Deploy From GitHub To Render

Use this flow when deploying from scratch.

1. Push this repository to GitHub.
2. Open Render Dashboard.
3. Click **New**.
4. Choose **Blueprint**.
5. Connect this GitHub repository.
6. Render reads `render.yaml`.
7. Render creates a web service named `schooly-backend`.
8. Add secret environment variables when Render asks.
9. Deploy.

Render uses:

```text
render.yaml
Dockerfile
```

You normally do not need to manually enter a build command because `runtime: docker` tells Render to use the `Dockerfile`.

## Deploy Manually On Render

Use this if you do not use Render Blueprints.

1. Open Render Dashboard.
2. Click **New > Web Service**.
3. Connect the GitHub repo.
4. Runtime: **Docker**.
5. Dockerfile path:

```text
./Dockerfile
```

6. Health check path:

```text
/health
```

7. Add the environment variables from the earlier section.
8. Click **Deploy Web Service**.

## How Startup Works

When Render starts the backend:

1. Render builds the Docker image.
2. Docker runs the Spring Boot jar.
3. Spring Boot reads environment variables.
4. HikariCP opens a connection to Neon.
5. Flyway checks migration files in `src/main/resources/db/migration`.
6. Flyway applies missing migrations.
7. Hibernate validates entity mappings against the database.
8. Tomcat starts the API server.
9. `/health` returns `{"status":"UP"}`.

If any step fails, Render deployment will show an error in logs.

## How To Test The Live API

Set this in your terminal:

```bash
BASE=https://schooly-backend-2pky.onrender.com
```

Test health:

```bash
curl "$BASE/health"
```

Expected:

```json
{"status":"UP"}
```

## Login And Get A Token

Seed data creates an admin user with phone:

```text
9999999999
```

Login:

```bash
curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"9999999999"}'
```

Expected response includes:

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "phone": "9999999999"
  }
}
```

Get schools for the user:

```bash
curl -s "$BASE/auth/schools?userId=1"
```

Expected response includes a school id, usually:

```text
schoolId = 1
```

Select school and get token:

```bash
curl -s -X POST "$BASE/auth/select-school" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"schoolId":1}'
```

Copy `data.accessToken`. You need it for protected APIs.

## Test Protected APIs

Set token:

```bash
TOKEN=<paste-access-token-here>
```

Test schools:

```bash
curl -s "$BASE/api/schools" \
  -H "Authorization: Bearer $TOKEN"
```

Test classes:

```bash
curl -s "$BASE/api/classes" \
  -H "Authorization: Bearer $TOKEN"
```

Test students:

```bash
curl -s "$BASE/api/students" \
  -H "Authorization: Bearer $TOKEN"
```

Test dashboard:

```bash
curl -s "$BASE/api/dashboard/kpis" \
  -H "Authorization: Bearer $TOKEN"
```

If the token is missing or wrong, the API returns:

```json
{"status":"error","data":null,"message":"Invalid or expired token","pagination":null}
```

## Full Smoke Test Script

Use this script after every deployment:

```bash
BASE="https://schooly-backend-2pky.onrender.com"

curl -s "$BASE/health"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"9999999999"}')

USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.data.id')

SCHOOLS_RESPONSE=$(curl -s "$BASE/auth/schools?userId=$USER_ID")
SCHOOL_ID=$(echo "$SCHOOLS_RESPONSE" | jq -r '.data[0].schoolId // .data[0].id')

TOKEN_RESPONSE=$(curl -s -X POST "$BASE/auth/select-school" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"schoolId\":$SCHOOL_ID}")

TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.data.accessToken')

curl -s "$BASE/api/schools" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/classes" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/students" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/staff" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/fee-invoices" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/payments" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/attendance" -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/api/dashboard/kpis" -H "Authorization: Bearer $TOKEN"
```

Requirement:

```bash
brew install jq
```

`jq` is used to extract ids and tokens from JSON responses.

## Common API Endpoints

Public endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Check if app is running. |
| `POST` | `/auth/login` | Login/signup by phone. |
| `GET` | `/auth/schools?userId=1` | Get schools for user. |
| `POST` | `/auth/select-school` | Select school and receive JWT tokens. |

Protected endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/schools` | List schools. |
| `GET` | `/api/classes` | List classes. |
| `GET` | `/api/students` | List students. |
| `GET` | `/api/staff` | List staff. |
| `GET` | `/api/fee-invoices` | List fee invoices. |
| `GET` | `/api/payments` | List payments. |
| `GET` | `/api/attendance` | List attendance records. |
| `GET` | `/api/dashboard/kpis` | Dashboard KPIs. |
| `GET` | `/parent/children` | Parent child list. |

Protected endpoints require:

```text
Authorization: Bearer <access-token>
```

## Troubleshooting

### Render Deploy Fails During Build

Check Render logs.

Common causes:

- Java build failed.
- Maven dependency download failed.
- Dockerfile path is wrong.
- GitHub repo did not include latest files.

Local verification:

```bash
./mvnw clean package
```

Expected:

```text
BUILD SUCCESS
```

### App Starts But Health Fails

Check:

```text
https://<render-url>/health
```

If it does not return `{"status":"UP"}`, inspect Render logs.

Common causes:

- App crashed on startup.
- Wrong port config.
- Missing environment variable.

This app uses:

```properties
server.port=${PORT:8080}
server.address=0.0.0.0
```

Render automatically sets `PORT`.

### Database Connection Fails

Symptoms in logs:

```text
password authentication failed
connection timeout
SSL connection has been closed unexpectedly
```

Check Render environment variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Make sure URL uses JDBC format:

```text
jdbc:postgresql://host/database?sslmode=require&channelBinding=require
```

Do not use this format in Spring Boot:

```text
postgresql://user:password@host/database
```

That format is for tools like `psql`, not `spring.datasource.url`.

### Token Fails

If protected APIs return:

```text
Invalid or expired token
```

Check:

- You added `Authorization: Bearer <token>`.
- You copied `accessToken`, not `refreshToken`.
- `APP_JWT_SECRET` did not change after token generation.
- Token has not expired.

If you changed `APP_JWT_SECRET`, old tokens stop working. Login again and select school again.

### Flyway Migration Fails

Flyway runs automatically on startup.

Migration files live here:

```text
src/main/resources/db/migration
```

Rules:

- Never edit an already-applied migration on a shared database.
- Add a new migration file instead.
- Use names like `V4__add_new_table.sql`.

### API Works Locally But Not On Render

Compare local environment values with Render environment values.

Local app may use defaults from `application.properties`, but Render should use environment variables.

Check Render:

```text
Service > Environment
```

Then redeploy after changes.

## How To Rotate Neon Password

Rotate the password if it was leaked or shared.

1. Open Neon Dashboard.
2. Open project `schooly-backend`.
3. Go to **Roles**.
4. Select role `neondb_owner`.
5. Reset or reveal password depending on Neon UI.
6. Copy the new password.
7. Update Render variable:

```text
SPRING_DATASOURCE_PASSWORD=<new-password>
```

8. Redeploy Render service.
9. Test `/health`.
10. Run the smoke test.

## How To Change JWT Secret

Generate a new secret:

```bash
openssl rand -base64 48
```

Update Render:

```text
APP_JWT_SECRET=<new-generated-secret>
```

Redeploy after changing it.

Important effect:

All old access tokens and refresh tokens become invalid after changing the JWT secret.

## Local Development

Set environment variables before running locally:

```bash
export SPRING_DATASOURCE_PASSWORD='npg_rowJC37Isvuj'
export APP_JWT_SECRET='local-dev-secret-change-me-change-me-change-me'
```

Run:

```bash
./mvnw spring-boot:run
```

Test:

```bash
curl http://localhost:8080/health
```

Expected:

```json
{"status":"UP"}
```

## Local Build And Tests

Run tests:

```bash
SPRING_DATASOURCE_PASSWORD='npg_rowJC37Isvuj' ./mvnw test
```

Build jar:

```bash
SPRING_DATASOURCE_PASSWORD='npg_rowJC37Isvuj' ./mvnw clean package
```

The jar appears here:

```text
target/erp-0.0.1-SNAPSHOT.jar
```

## Final Deployment Checklist

Before saying the API is ready:

- `SPRING_DATASOURCE_URL` is set in Render.
- `SPRING_DATASOURCE_USERNAME` is set in Render.
- `SPRING_DATASOURCE_PASSWORD` is set in Render.
- `APP_JWT_SECRET` is set in Render.
- Render latest deploy status is successful.
- `/health` returns `{"status":"UP"}`.
- `/auth/login` works with phone `9999999999`.
- `/auth/select-school` returns an access token.
- `/api/schools` works with bearer token.
- Render logs do not show startup errors.

If all checks pass, the deployed backend is ready to be consumed by frontend or mobile clients.
