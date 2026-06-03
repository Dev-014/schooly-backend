# Deploy to Render with Neon Postgres

Neon hosts the PostgreSQL database. Render hosts the Spring Boot API so clients can consume the endpoints over HTTPS.

## Render Blueprint

This repo includes `render.yaml`, which defines a Docker web service named `schooly-backend`.

1. Push this repository to GitHub.
2. Open Render Dashboard.
3. Create a new Blueprint from this repository.
4. When Render asks for secret values, set:

```text
SPRING_DATASOURCE_PASSWORD=<Neon database password>
APP_JWT_SECRET=<strong random JWT secret>
```

The non-secret Neon values are already defined in `render.yaml`:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-snowy-king-aopqgrht-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

After deployment, test:

```bash
curl https://<render-service-url>/health
```

Expected response:

```json
{"status":"UP"}
```
