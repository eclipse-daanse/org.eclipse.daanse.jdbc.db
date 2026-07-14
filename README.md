# org.eclipse.daanse.jdbc.db

JDBC database utilities: a neutral schema/type model, database introspection
(reading), and — temporarily co-hosted — the SQL dialect framework that is
scheduled to move to the `org.eclipse.daanse.sql` repository.

## Module map

| Module | Package root | Role |
|---|---|---|
| `api` | `org.eclipse.daanse.jdbc.db.api` | Neutral model (`api.type`, `api.sql`, `api.schema`, `api.meta`), reading SPI (`DatabaseService`, `SnapshotBuilder`, `MetaDataQueries`, `MetadataProvider`, `MetadataProviderFactory`). Stays here; the sql repo depends on it. |
| `record` | `org.eclipse.daanse.jdbc.db.record` | Record implementations of the neutral model. |
| `metadata` | `org.eclipse.daanse.jdbc.db.metadata` | Engine-specific `MetadataProvider` implementations (`information_schema`/system-catalog readers for H2, MySQL, MariaDB, PostgreSQL, SQL Server, Oracle) + OSGi `MetadataProviderFactory` services + the static `MetadataProviders` resolver. Reading only — never depends on a dialect. |
| `impl` | `org.eclipse.daanse.jdbc.db.impl` | `DatabaseServiceImpl` (falls back to plain `DatabaseMetaData` when no provider is given). |
| `importer/csv` | `org.eclipse.daanse.jdbc.db.importer.csv` | CSV import. Depends on `org.eclipse.daanse.sql.dialect.api` (the accepted future jdbc.db→sql edge). |
| `dialect/` | `org.eclipse.daanse.sql.dialect[.api\|.db.<x>]` | **Copy candidate.** SQL dialect framework, already renamed in place to its future sql-repo coordinates. Self-contained subtree (own parent + `revision`). |

### Dialect ⇄ reading: the cut

`Dialect` no longer extends `MetadataProvider`. Dialects are SQL *spelling*
(quoting, generators, capabilities, `TypeMapper`); database *reading* lives in
`metadata`. The one JDBC probe point kept on the dialect side is
`DialectInitData.fromConnection(Connection)`.

Former callers that passed a dialect as the metadata provider migrate with a
one-liner (plus a dependency on `org.eclipse.daanse.jdbc.db.metadata`):

```java
// before:  service.createMetaInfo(connection, dialect);
service.createMetaInfo(connection,
        MetadataProviders.forConnection(connection)
                .orElse(new MetadataProvider() { /* plain-DatabaseMetaData fallback */ }));
// or explicitly: service.createMetaInfo(connection, new PostgreSqlMetadataProvider());
```

All ~37 reading methods keep their signatures; nothing was removed, only moved.
Note Oracle: the plain `DatabaseMetaData` fallback can trip ORA-17027
(LONG-column streaming) — use `OracleMetadataProvider` there.

## Copy runbook: moving `dialect/` to org.eclipse.daanse.sql

1. Copy **only the active parts** into the sql repo:
   `dialect/pom.xml`, `dialect/api/`, `dialect/db/pom.xml`,
   `dialect/db/{common,test-support,clickhouse,derby,duckdb,h2,mariadb,mssqlserver,mysql,oracle,postgresql,sqlite}/`
   → `org.eclipse.daanse.sql/dialect/…`. Do **not** copy the attic directories
   (see below).
2. sql root pom: add `<module>dialect</module>`.
3. Flip the sql-repo dependencies (statement/api+impl, guard, deparser) from
   `org.eclipse.daanse.jdbc.db.dialect.*` to `org.eclipse.daanse.sql.dialect.*`
   (`${revision}`, reactor-internal). Java imports already match.
4. In jdbc.db afterwards: delete the `dialect/` tree and its `<module>` entry.
   `importer/csv` and the impl tests then resolve `org.eclipse.daanse.sql.dialect.*`
   from the snapshot repository. Build order:
   jdbc.db (api/record/metadata/impl) → sql (dialect, statement) → jdbc.db (importer).
5. Downstream repos (rolap, legacy.xmla, olap, etl, server bndruns) need a
   mechanical import/BSN sed `org.eclipse.daanse.jdbc.db.dialect` →
   `org.eclipse.daanse.sql.dialect` — separate effort.

The subtree's cross-repo dependencies (`jdbc.db.api`, `record`, `impl`,
`metadata` — the latter three test-scope only) are pinned to the literal
`0.0.1-SNAPSHOT` so they survive leaving the reactor.

## Attic

The 27 commented-out dialect modules under `dialect/db/` (access … vertica)
keep their old `org.eclipse.daanse.jdbc.db.dialect.*` package names, are not
part of the reactor, and are **not** part of the copy. Known defect kept as-is:
the redshift/snowflake sources are cross-wired.
