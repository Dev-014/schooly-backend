# pgAdmin Connection Guide

Use this guide to view the project PostgreSQL database in pgAdmin.

## Connection Details

The active database connection is defined in:

`src/main/resources/application.properties`

Use these values in pgAdmin:

| pgAdmin field | Value |
| --- | --- |
| Name | `schooly-render-db` |
| Host name/address | `dpg-d7fo3mmrnols73f6i3jg-a.oregon-postgres.render.com` |
| Port | `5432` |
| Maintenance database | `school_erp_db_8r8k` |
| Username | `school_erp_db_8r8k_user` |
| Password | Use `spring.datasource.password` from `application.properties` |

## Steps

1. Open pgAdmin.
2. Right-click `Servers`.
3. Select `Register > Server`.
4. Open the `General` tab.
5. Enter a name, for example `schooly-render-db`.
6. Open the `Connection` tab.
7. Enter the host, port, database, username, and password from the table above.
8. Enable `Save password` if you want pgAdmin to remember it.
9. Click `Save`.

## Where to Find Tables

After connecting, browse this path in pgAdmin:

```text
Servers
  > schooly-render-db
  > Databases
  > school_erp_db_8r8k
  > Schemas
  > public
  > Tables
```

You should see tables such as:

- `schools`
- `class`
- `student`
- `staff`
- `fee_invoice`
- `payment`
- `student_attendance`
- `users`
- `user_school_roles`
- `student_parents`
- `auth_sessions`

## Common Issues

### Connection timeout

The database host may not allow connections from your current IP address, or the database provider may restrict public access. Check the database provider's network settings.

### Password authentication failed

Confirm that the password matches `spring.datasource.password` in `application.properties`. If the password was rotated, update pgAdmin with the new value.

### Database does not appear

Make sure the maintenance database is set to:

```text
school_erp_db_8r8k
```

### SSL error

If pgAdmin requires SSL settings for the hosted database, open the `SSL` tab while registering the server and try setting SSL mode to `Require`.

## Security Notes

- Do not share database credentials in chat, screenshots, or commits.
- Rotate the password if it has been exposed.
- Move credentials out of `application.properties` and use environment variables or a secrets manager.
