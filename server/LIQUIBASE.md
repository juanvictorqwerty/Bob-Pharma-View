# Liquibase Database Migrations

This project uses Liquibase for database schema version control and migrations.

## Prerequisites

1. Ensure PostgreSQL is running
2. Configure database credentials in `.env` file
3. Liquibase configuration is in `liquibase.yaml` (reads from .env)

## Configuration

### liquibase.yaml
The Liquibase configuration file references environment variables from `.env`:
- `DB_HOST`, `DB_PORT`, `DB_NAME` - Database connection
- `DB_USER`, `DB_PASSWORD` - Database credentials

**Note:** `liquibase.yaml` is in `.gitignore` for security. Copy from template if needed.

### Changelog Structure
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog
└── changes/
    └── 001-add-last-renewed-at-to-token.yaml  # Migration files
```

## Usage

### Check Migration Status
```bash
mvn liquibase:status
```
Shows which migrations have been applied and which are pending.

### Apply Pending Migrations
```bash
mvn liquibase:update
```
Applies all pending migrations to the database.

### View SQL Without Executing
```bash
mvn liquibase:updateSQL
```
Generates the SQL that would be executed without running it.

### Rollback Last Migration
```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```
Rolls back the last N migrations.

### Rollback to Specific Date
```bash
mvn liquibase:rollback -Dliquibase.rollbackDate="2024-01-01T00:00:00"
```

### Clear All Migrations (Dangerous!)
```bash
mvn liquibase:clearCheckSums
```
Clears the checksums in the DATABASECHANGELOG table (use only if needed).

## Creating New Migrations

### 1. Create a new changeset file
Create a new YAML file in `src/main/resources/db/changelog/changes/`:
- Use sequential numbering: `002-add-something.yaml`, `003-update-something.yaml`
- Include proper changeSet metadata

### 2. Example Changeset
```yaml
databaseChangeLog:
  - changeSet:
      id: 002-add-something
      author: your-name
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone_number
                  type: varchar(20)
                  constraints:
                    nullable: true
        - addIndex:
            tableName: users
            indexName: idx_users_phone
            columns:
              - column:
                  name: phone_number
```

### 3. Apply the migration
```bash
mvn liquibase:update
```

## Important Notes

- **Never** modify an already-applied changeset - create a new one instead
- Always include `id` and `author` in changeSets
- Use `validate` mode in Hibernate (`ddl-auto: validate`) to let Liquibase manage schema
- The `DATABASECHANGELOG` table tracks applied migrations
- The `DATABASECHANGELOGLOCK` table prevents concurrent migrations

## Troubleshooting

### "Database URL has not been specified"
Ensure `liquibase.yaml` exists and has correct database credentials.

### "Change set already exists"
The changeset ID/author combination must be unique. Use a different ID.

### "Validation failed"
Hibernate schema doesn't match Liquibase migrations. Check entity definitions.

## Current Migrations

- `001-add-last-renewed-at-to-token` - Adds `last_renewed_at` column to token table