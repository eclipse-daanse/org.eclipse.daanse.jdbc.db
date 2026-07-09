/*
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * For more information please visit the Project: Hitachi Vantara - Mondrian
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial adapt parts of Syntax.class
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.db.mssqlserver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.daanse.jdbc.db.api.meta.IndexInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IndexInfoItem;
import org.eclipse.daanse.jdbc.db.api.schema.CheckConstraint;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnMetaData;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnPrivilege;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.Function;
import org.eclipse.daanse.jdbc.db.api.schema.FunctionColumn;
import org.eclipse.daanse.jdbc.db.api.schema.FunctionReference;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.Partition;
import org.eclipse.daanse.jdbc.db.api.schema.PartitionMethod;
import org.eclipse.daanse.jdbc.db.api.schema.PrimaryKey;
import org.eclipse.daanse.jdbc.db.api.schema.Procedure;
import org.eclipse.daanse.jdbc.db.api.schema.ProcedureColumn;
import org.eclipse.daanse.jdbc.db.api.schema.ProcedureReference;
import org.eclipse.daanse.jdbc.db.api.schema.PseudoColumn;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.Sequence;
import org.eclipse.daanse.jdbc.db.api.schema.SequenceReference;
import org.eclipse.daanse.jdbc.db.api.schema.TablePrivilege;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.schema.Trigger;
import org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerEvent;
import org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerTiming;
import org.eclipse.daanse.jdbc.db.api.schema.TriggerReference;
import org.eclipse.daanse.jdbc.db.api.schema.UniqueConstraint;
import org.eclipse.daanse.jdbc.db.api.schema.UserDefinedType;
import org.eclipse.daanse.jdbc.db.api.schema.UserDefinedTypeReference;
import org.eclipse.daanse.jdbc.db.api.schema.ViewDefinition;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.KnownFunction;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.StatementHint;
import org.eclipse.daanse.jdbc.db.api.sql.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractJdbcDialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.record.schema.CheckConstraintRecord;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnPrivilegeRecord;
import org.eclipse.daanse.jdbc.db.record.schema.FunctionColumnRecord;
import org.eclipse.daanse.jdbc.db.record.schema.FunctionRecord;
import org.eclipse.daanse.jdbc.db.record.schema.ImportedKeyRecord;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoItemRecord;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoRecord;
import org.eclipse.daanse.jdbc.db.record.schema.PartitionRecord;
import org.eclipse.daanse.jdbc.db.record.schema.PrimaryKeyRecord;
import org.eclipse.daanse.jdbc.db.record.schema.ProcedureColumnRecord;
import org.eclipse.daanse.jdbc.db.record.schema.ProcedureRecord;
import org.eclipse.daanse.jdbc.db.record.schema.PseudoColumnRecord;
import org.eclipse.daanse.jdbc.db.record.schema.SequenceRecord;
import org.eclipse.daanse.jdbc.db.record.schema.TablePrivilegeRecord;
import org.eclipse.daanse.jdbc.db.record.schema.TriggerRecord;
import org.eclipse.daanse.jdbc.db.record.schema.UniqueConstraintRecord;
import org.eclipse.daanse.jdbc.db.record.schema.UserDefinedTypeRecord;
import org.eclipse.daanse.jdbc.db.record.schema.ViewDefinitionRecord;

/**
 * @author jhyde
 * @since Nov 23, 2008
 */
public class MicrosoftSqlServerDialect extends AbstractJdbcDialect {

    private final DateFormat df = new SimpleDateFormat("yyyyMMdd");

    private static final String SUPPORTED_PRODUCT_NAME = "MSSQL";

    private volatile org.eclipse.daanse.jdbc.db.dialect.api.generator.ReturningGenerator cachedReturningGenerator;
    private volatile org.eclipse.daanse.jdbc.db.dialect.api.generator.MergeGenerator cachedMergeGenerator;
    private volatile org.eclipse.daanse.jdbc.db.dialect.api.generator.CteGenerator cachedCteGenerator;

    public MicrosoftSqlServerDialect() {
        super(org.eclipse.daanse.jdbc.db.dialect.api.DialectInitData.ansiDefaults());
    }

    /** Construct from a captured snapshot — the canonical entry point. */
    public MicrosoftSqlServerDialect(org.eclipse.daanse.jdbc.db.dialect.api.DialectInitData init) {
        super(init);
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.generator.PaginationGenerator paginationGenerator() {
        return new org.eclipse.daanse.jdbc.db.dialect.api.generator.PaginationGenerator() {
            @Override
            public java.util.Optional<String> selectPrefix(java.util.OptionalLong limit, java.util.OptionalLong offset) {
                // SQL Server expresses a pure row cap as a leading "TOP n". When an offset is
                // present it uses the trailing OFFSET/FETCH form instead (see paginate).
                if (limit.isPresent() && offset.isEmpty()) {
                    return java.util.Optional.of("TOP " + limit.getAsLong());
                }
                return java.util.Optional.empty();
            }

            @Override
            public String paginate(java.util.OptionalLong limit, java.util.OptionalLong offset) {
                // No offset → the cap is emitted as a leading TOP (selectPrefix), so nothing
                // trailing. With an offset, fall back to ANSI OFFSET/FETCH (SQL Server 2012+).
                return offset.isEmpty()
                        ? ""
                        : org.eclipse.daanse.jdbc.db.dialect.api.generator.PaginationGenerator.super.paginate(limit,
                                offset);
            }
        };
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.generator.ReturningGenerator returningGenerator() {
        var local = cachedReturningGenerator;
        if (local != null)
            return local;
        local = new org.eclipse.daanse.jdbc.db.dialect.api.generator.ReturningGenerator() {
            @Override
            public boolean supportsReturning() {
                return true;
            }

            @Override
            public java.util.Optional<String> returning(java.util.List<String> columns) {
                if (columns == null || columns.isEmpty())
                    return java.util.Optional.empty();
                if (columns.size() == 1 && "*".equals(columns.get(0))) {
                    return java.util.Optional.of(" OUTPUT INSERTED.*");
                }
                StringBuilder sb = new StringBuilder(" OUTPUT ");
                boolean first = true;
                for (String c : columns) {
                    if (!first)
                        sb.append(", ");
                    sb.append("INSERTED.").append(quoteIdentifier(c));
                    first = false;
                }
                return java.util.Optional.of(sb.toString());
            }
        };
        cachedReturningGenerator = local;
        return local;
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.generator.MergeGenerator mergeGenerator() {
        var local = cachedMergeGenerator;
        if (local != null)
            return local;
        // SQL Server major version map: 2008=10, 2012=11, 2014=12, 2016=13, 2017=14,
        // 2019=15, 2022=16.
        if (!dialectVersion.isUnknownOrAtLeast(10, 0)) {
            local = super.mergeGenerator();
            cachedMergeGenerator = local;
            return local;
        }
        local = new org.eclipse.daanse.jdbc.db.dialect.api.generator.MergeGenerator() {
            @Override
            public boolean supportsMerge() {
                return true;
            }

            @Override
            public java.util.Optional<String> upsert(UpsertSpec spec, java.util.List<String> values) {
                if (values == null || values.size() != spec.insertColumns().size()) {
                    throw new IllegalArgumentException("values must match insertColumns in length");
                }
                StringBuilder sb = new StringBuilder("MERGE INTO ").append(qualified(spec.target()))
                        .append(" AS T USING (VALUES (");
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append(values.get(i));
                }
                sb.append(")) AS S(");
                appendQuotedCsv(sb, spec.insertColumns());
                sb.append(") ON ");
                for (int i = 0; i < spec.keyColumns().size(); i++) {
                    if (i > 0)
                        sb.append(" AND ");
                    sb.append("T.").append(quoteIdentifier(spec.keyColumns().get(i))).append(" = S.")
                            .append(quoteIdentifier(spec.keyColumns().get(i)));
                }
                if (!spec.updateColumns().isEmpty()) {
                    sb.append(" WHEN MATCHED THEN UPDATE SET ");
                    boolean first = true;
                    for (String c : spec.updateColumns()) {
                        if (!first)
                            sb.append(", ");
                        sb.append("T.").append(quoteIdentifier(c)).append(" = S.").append(quoteIdentifier(c));
                        first = false;
                    }
                }
                sb.append(" WHEN NOT MATCHED THEN INSERT (");
                appendQuotedCsv(sb, spec.insertColumns());
                sb.append(") VALUES (");
                for (int i = 0; i < spec.insertColumns().size(); i++) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append("S.").append(quoteIdentifier(spec.insertColumns().get(i)));
                }
                sb.append(");");
                return java.util.Optional.of(sb.toString());
            }
        };
        cachedMergeGenerator = local;
        return local;
    }

    /**
     * SQL Server: CTE {@code WITH …} but no {@code RECURSIVE} keyword
     * (auto-detected).
     */
    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.generator.CteGenerator cteGenerator() {
        var local = cachedCteGenerator;
        if (local != null)
            return local;
        local = new org.eclipse.daanse.jdbc.db.dialect.api.generator.CteGenerator() {
            @Override
            public boolean emitsRecursiveKeyword() {
                return false;
            }
        };
        cachedCteGenerator = local;
        return local;
    }

    /** SQL Server rejects {@code CASCADE} on {@code DROP TABLE}. */
    @Override
    public boolean supportsDropTableCascade() {
        return false;
    }

    /** SQL Server rejects {@code IF NOT EXISTS} on {@code CREATE TABLE}. */
    @Override
    public boolean supportsCreateTableIfNotExists() {
        return false;
    }

    /** SQL Server rejects {@code IF NOT EXISTS} on {@code CREATE INDEX}. */
    @Override
    public boolean supportsCreateIndexIfNotExists() {
        return false;
    }

    /** SQL Server requires DROP+CREATE — no {@code OR REPLACE} on views. */
    @Override
    public boolean supportsCreateOrReplaceView() {
        return false;
    }

    /** SQL Server: {@code DROP INDEX name ON table_name}. */
    @Override
    public boolean dropIndexRequiresTable() {
        return true;
    }

    /**
     * SQL Server rejects {@code IF EXISTS} on
     * {@code ALTER TABLE … DROP CONSTRAINT}.
     */
    @Override
    public boolean supportsDropConstraintIfExists() {
        return false;
    }

    @Override
    public String createSchema(String schemaName, boolean ifNotExists) {
        if (!ifNotExists) {
            return "CREATE SCHEMA " + quoteIdentifier(schemaName);
        }
        // Single-line guarded form; works in any batch with QUOTED_IDENTIFIER on.
        return "IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = '" + schemaName.replace("'", "''")
                + "') EXEC('CREATE SCHEMA " + quoteIdentifier(schemaName) + "')";
    }

    @Override
    public String dropTable(String schemaName, String tableName, boolean ifExists) {
        if (!ifExists) {
            return "DROP TABLE " + quoteIdentifier(schemaName, tableName);
        }
        return "IF OBJECT_ID('" + (schemaName == null ? "" : schemaName + ".") + tableName
                + "', 'U') IS NOT NULL DROP TABLE " + quoteIdentifier(schemaName, tableName);
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, null, false);
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    public boolean requiresUnionOrderByOrdinal() {
        return false;
    }

    @Override
    public void quoteBooleanLiteral(StringBuilder buf, String value) {
        // avoid padding origin values with blanks to n for char(n),
        // when ANSI_PADDING=ON
        String boolLiteral = value.trim();
        if (!boolLiteral.equalsIgnoreCase("TRUE") && !(boolLiteral.equalsIgnoreCase("FALSE"))
                && !(boolLiteral.equalsIgnoreCase("1")) && !(boolLiteral.equalsIgnoreCase("0"))) {
            throw new NumberFormatException("Illegal BOOLEAN literal:  " + value);
        }
        buf.append(DialectUtil.singleQuoteString(value));
    }

    @Override
    protected void quoteDateLiteral(StringBuilder buf, Date date) {
        buf.append("CONVERT(DATE, '");
        buf.append(df.format(date));
        // Format 112 is equivalent to "yyyyMMdd" in Java.
        // See http://msdn.microsoft.com/en-us/library/ms187928.aspx
        buf.append("', 112)");
    }

    @Override
    protected void quoteTimestampLiteral(StringBuilder buf, String value, Timestamp timestamp) {
        buf.append("CONVERT(datetime, '");
        buf.append(timestamp.toString());
        // Format 120 is equivalent to "yyyy-mm-dd hh:mm:ss" in Java.
        // See http://msdn.microsoft.com/en-us/library/ms187928.aspx
        buf.append("', 120)");
    }

    @Override
    public String name() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding caseFolding() {
        return org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.PRESERVE;
    }

    /**
     * T-SQL spells statement hints as a trailing query-option clause:
     * {@code OPTION (RECOMPILE, MAXDOP 2)} (leading space) — per hint the name followed by
     * its arguments separated by spaces ({@code name args...}), hints separated by commas.
     */
    @Override
    public StringBuilder statementOption(List<StatementHint> hints) {
        if (hints.isEmpty()) {
            return new StringBuilder();
        }
        StringBuilder sb = new StringBuilder(" OPTION (");
        boolean first = true;
        for (StatementHint hint : hints) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(hint.name());
            for (String argument : hint.arguments()) {
                sb.append(' ').append(argument);
            }
        }
        return sb.append(")");
    }

    @Override
    public StringBuilder generateKnownFunction(KnownFunction function, List<? extends CharSequence> arguments) {
        return switch (function) {
        case LENGTH -> {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("LENGTH expects 1 argument(s), got " + arguments.size());
            }
            yield new StringBuilder("LEN(").append(arguments.get(0)).append(")");
        }
        case CONCAT -> {
            // T-SQL + concatenates but coerces/NULL-propagates awkwardly; CONCAT is the idiom.
            if (arguments.size() < 2) {
                throw new IllegalArgumentException("CONCAT expects 2 or more argument(s), got " + arguments.size());
            }
            StringBuilder sb = new StringBuilder("CONCAT(");
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(arguments.get(i));
            }
            yield sb.append(")");
        }
        case INDEX_OF -> {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("INDEX_OF expects 2 argument(s), got " + arguments.size());
            }
            yield new StringBuilder("CHARINDEX(").append(arguments.get(0)).append(", ").append(arguments.get(1))
                    .append(")");
        }
        case YEAR, MONTH, DAY, HOUR, MINUTE, SECOND -> {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException(
                        function.name() + " expects 1 argument(s), got " + arguments.size());
            }
            // T-SQL has no EXTRACT; DATEPART covers the whole family.
            yield new StringBuilder("DATEPART(").append(function.name().toLowerCase(java.util.Locale.ROOT))
                    .append(", ").append(arguments.get(0)).append(")");
        }
        case NOW -> {
            if (!arguments.isEmpty()) {
                throw new IllegalArgumentException("NOW expects 0 argument(s), got " + arguments.size());
            }
            // GETDATE() (datetime) chosen over SYSDATETIME() (datetime2): it is the
            // ubiquitous T-SQL spelling and its precision suffices for NOW's intent.
            yield new StringBuilder("GETDATE()");
        }
        default -> super.generateKnownFunction(function, arguments);
        };
    }

    @Override
    public java.util.Optional<String> generatePercentileDisc(double percentile, boolean desc, String tableName,
            String columnName) {
        return java.util.Optional
                .of((buildPercentileFunction("PERCENTILE_DISC", percentile, desc, tableName, columnName)).toString());
    }

    @Override
    public java.util.Optional<String> generatePercentileCont(double percentile, boolean desc, String tableName,
            String columnName) {
        return java.util.Optional
                .of((buildPercentileFunction("PERCENTILE_CONT", percentile, desc, tableName, columnName)).toString());
    }

    @Override
    public boolean supportsPercentileDisc() {
        return true;
    }

    @Override
    public boolean supportsPercentileCont() {
        return true;
    }

    @Override
    public java.util.Optional<String> generateListAgg(CharSequence operand, boolean distinct, String separator,
            String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("STRING_AGG");
        buf.append("( ");
        if (distinct) {
            buf.append("DISTINCT ");
        }
        buf.append(operand);
        buf.append(", '");
        if (separator != null) {
            buf.append(separator);
        } else {
            buf.append(", ");
        }
        buf.append("'");
        buf.append(")");
        if (columns != null && !columns.isEmpty()) {
            buf.append(" WITHIN GROUP (ORDER BY ");
            buf.append(buildOrderedColumnsClause(columns));
            buf.append(")");
        }
        // STRING_AGG(CONVERT (NVARCHAR (MAX), EmailAddress), ';') WITHIN GROUP (ORDER
        // BY EmailAddress ASC)
        return java.util.Optional.of((buf).toString());
    }

    @Override
    public java.util.Optional<String> generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n,
            List<OrderedColumn> columns) {
        return java.util.Optional
                .of((buildNthValueFunction("NTH_VALUE", operand, ignoreNulls, n, columns, true)).toString());
    }

    @Override
    public boolean supportsNthValue() {
        return true;
    }

    @Override
    public boolean supportsNthValueIgnoreNulls() {
        return true;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }

    @Override
    public List<Trigger> getAllTriggers(Connection connection, String catalog, String schema) throws SQLException {
        String sql = """
                SELECT t.name AS trigger_name, OBJECT_SCHEMA_NAME(t.parent_id) AS schema_name,
                        OBJECT_NAME(t.parent_id) AS table_name, t.is_instead_of_trigger, m.definition
                FROM sys.triggers t JOIN sys.sql_modules m ON m.object_id = t.object_id
                WHERE t.parent_id > 0 AND OBJECT_SCHEMA_NAME(t.parent_id) = ?
                ORDER BY OBJECT_NAME(t.parent_id), t.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<Trigger> triggers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    triggers.add(readTrigger(rs, schemaName));
                }
            }
        }
        return List.copyOf(triggers);
    }

    @Override
    public List<Trigger> getTriggers(Connection connection, String catalog, String schema, String tableName)
            throws SQLException {
        String sql = """
                SELECT t.name AS trigger_name, OBJECT_SCHEMA_NAME(t.parent_id) AS schema_name,
                        OBJECT_NAME(t.parent_id) AS table_name, t.is_instead_of_trigger, m.definition
                FROM sys.triggers t JOIN sys.sql_modules m ON m.object_id = t.object_id
                WHERE t.parent_id > 0 AND OBJECT_SCHEMA_NAME(t.parent_id) = ?
                    AND OBJECT_NAME(t.parent_id) = ?
                ORDER BY t.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<Trigger> triggers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    triggers.add(readTrigger(rs, schemaName));
                }
            }
        }
        return List.copyOf(triggers);
    }

    @Override
    public List<Sequence> getAllSequences(Connection connection, String catalog, String schema) throws SQLException {
        String sql = """
                SELECT s.name AS sequence_name, SCHEMA_NAME(s.schema_id) AS schema_name,
                        CAST(s.start_value AS BIGINT) AS start_value, CAST(s.increment AS BIGINT) AS increment,
                        CAST(s.minimum_value AS BIGINT) AS minimum_value, CAST(s.maximum_value AS BIGINT) AS maximum_value,
                        s.is_cycling, CAST(s.current_value AS BIGINT) AS current_value,
                        TYPE_NAME(s.system_type_id) AS data_type
                FROM sys.sequences s WHERE s.schema_id = SCHEMA_ID(?)
                ORDER BY s.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<Sequence> sequences = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("sequence_name");
                    long startValue = rs.getLong("start_value");
                    long increment = rs.getLong("increment");
                    long minValue = rs.getLong("minimum_value");
                    Optional<Long> oMinValue = rs.wasNull() ? Optional.empty() : Optional.of(minValue);
                    long maxValue = rs.getLong("maximum_value");
                    Optional<Long> oMaxValue = rs.wasNull() ? Optional.empty() : Optional.of(maxValue);
                    boolean cycle = rs.getBoolean("is_cycling");
                    long currentValue = rs.getLong("current_value");
                    Optional<Long> oCurrentValue = rs.wasNull() ? Optional.empty() : Optional.of(currentValue);
                    String dataType = rs.getString("data_type");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));

                    sequences.add(new SequenceRecord(new SequenceReference(oSchema, name), startValue, increment,
                            oMinValue, oMaxValue, cycle, oCurrentValue, Optional.ofNullable(dataType)));
                }
            }
        }
        return List.copyOf(sequences);
    }

    @Override
    public List<Partition> getAllPartitions(Connection connection, String catalog, String schema) throws SQLException {
        // SQL Server partitioning is always RANGE; sys.partitions has one row per
        // (table, index, partition_number). Restricting index_id IN (0, 1) gives one
        // row
        // per partition (heap or clustered) and avoids duplicates from nonclustered
        // indexes.
        // sys.partition_range_values supplies the boundary value at boundary_id =
        // partition_number.
        String sql = """
                SELECT OBJECT_NAME(p.object_id) AS table_name,
                        p.partition_number,
                        p.rows AS row_count,
                        prv.value AS boundary_value,
                        (SELECT TOP 1 col.name
                        FROM sys.index_columns ic
                        JOIN sys.columns col ON col.object_id = ic.object_id AND col.column_id = ic.column_id
                        WHERE ic.object_id = p.object_id AND ic.index_id = p.index_id AND ic.partition_ordinal > 0
                        ) AS partition_column
                FROM sys.partitions p
                JOIN sys.tables t ON t.object_id = p.object_id
                JOIN sys.indexes i ON i.object_id = p.object_id AND i.index_id = p.index_id
                JOIN sys.partition_schemes ps ON ps.data_space_id = i.data_space_id
                JOIN sys.partition_functions pf ON pf.function_id = ps.function_id
                LEFT JOIN sys.partition_range_values prv
                    ON prv.function_id = pf.function_id AND prv.boundary_id = p.partition_number
                WHERE SCHEMA_NAME(t.schema_id) = ? AND i.index_id IN (0, 1)
                ORDER BY OBJECT_NAME(p.object_id), p.partition_number
                """;
        String schemaName = resolveSchema(schema, connection);
        List<Partition> partitions = new ArrayList<>();
        Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    int partitionNumber = rs.getInt("partition_number");
                    long rowCount = rs.getLong("row_count");
                    Optional<Long> oRowCount = rs.wasNull() ? Optional.empty() : Optional.of(rowCount);
                    Object boundary = rs.getObject("boundary_value");
                    String column = rs.getString("partition_column");

                    TableReference tableRef = new TableReference(oSchema, tableName);
                    partitions.add(new PartitionRecord("p" + partitionNumber, tableRef, Optional.of(partitionNumber),
                            PartitionMethod.RANGE, column == null ? Optional.empty() : Optional.of(column),
                            boundary == null ? Optional.empty() : Optional.of(boundary.toString()), oRowCount,
                            Optional.empty(), Optional.empty(), Optional.empty()));
                }
            }
        } catch (SQLException e) {
            return List.of();
        }
        return List.copyOf(partitions);
    }

    @Override
    public List<CheckConstraint> getAllCheckConstraints(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT cc.name AS constraint_name, OBJECT_NAME(cc.parent_object_id) AS table_name, cc.definition
                FROM sys.check_constraints cc
                WHERE OBJECT_SCHEMA_NAME(cc.parent_object_id) = ?
                ORDER BY OBJECT_NAME(cc.parent_object_id), cc.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<CheckConstraint> constraints = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String constraintName = rs.getString("constraint_name");
                    String tableName = rs.getString("table_name");
                    String definition = rs.getString("definition");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    TableReference tableRef = new TableReference(oSchema, tableName);

                    constraints.add(new CheckConstraintRecord(constraintName, tableRef, definition));
                }
            }
        }
        return List.copyOf(constraints);
    }

    @Override
    public List<CheckConstraint> getCheckConstraints(Connection connection, String catalog, String schema,
            String tableName) throws SQLException {
        String sql = """
                SELECT cc.name AS constraint_name, OBJECT_NAME(cc.parent_object_id) AS table_name, cc.definition
                FROM sys.check_constraints cc
                WHERE OBJECT_SCHEMA_NAME(cc.parent_object_id) = ?
                    AND OBJECT_NAME(cc.parent_object_id) = ?
                ORDER BY cc.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<CheckConstraint> constraints = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String constraintName = rs.getString("constraint_name");
                    String definition = rs.getString("definition");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    TableReference tableRef = new TableReference(oSchema, tableName);

                    constraints.add(new CheckConstraintRecord(constraintName, tableRef, definition));
                }
            }
        }
        return List.copyOf(constraints);
    }

    @Override
    public List<UniqueConstraint> getAllUniqueConstraints(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT i.name AS constraint_name, OBJECT_NAME(i.object_id) AS table_name,
                        c.name AS column_name, ic.key_ordinal
                FROM sys.indexes i
                JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
                JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                WHERE i.is_unique_constraint = 1 AND OBJECT_SCHEMA_NAME(i.object_id) = ?
                ORDER BY OBJECT_NAME(i.object_id), i.name, ic.key_ordinal
                """;
        return readUniqueConstraints(connection, sql, schema, null);
    }

    @Override
    public List<UniqueConstraint> getUniqueConstraints(Connection connection, String catalog, String schema,
            String tableName) throws SQLException {
        String sql = """
                SELECT i.name AS constraint_name, OBJECT_NAME(i.object_id) AS table_name,
                        c.name AS column_name, ic.key_ordinal
                FROM sys.indexes i
                JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
                JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                WHERE i.is_unique_constraint = 1 AND OBJECT_SCHEMA_NAME(i.object_id) = ?
                    AND OBJECT_NAME(i.object_id) = ?
                ORDER BY i.name, ic.key_ordinal
                """;
        return readUniqueConstraints(connection, sql, schema, tableName);
    }

    @Override
    public Optional<List<PrimaryKey>> getAllPrimaryKeys(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT i.name AS constraint_name, OBJECT_NAME(i.object_id) AS table_name,
                        c.name AS column_name, ic.key_ordinal
                FROM sys.indexes i
                JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
                JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                WHERE i.is_primary_key = 1 AND OBJECT_SCHEMA_NAME(i.object_id) = ?
                ORDER BY OBJECT_NAME(i.object_id), ic.key_ordinal
                """;
        String schemaName = resolveSchema(schema, connection);
        Map<String, PkBuilder> pkMap = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String constraintName = rs.getString("constraint_name");
                    String tableName = rs.getString("table_name");
                    String columnName = rs.getString("column_name");

                    String key = tableName + "." + constraintName;
                    pkMap.computeIfAbsent(key, k -> new PkBuilder(tableName, constraintName, schemaName))
                            .addColumn(columnName);
                }
            }
        }
        List<PrimaryKey> result = new ArrayList<>();
        for (PkBuilder builder : pkMap.values()) {
            result.add(builder.build());
        }
        return Optional.of(List.copyOf(result));
    }

    @Override
    public Optional<List<ImportedKey>> getAllImportedKeys(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT fk.name AS fk_name, OBJECT_NAME(fk.parent_object_id) AS fk_table,
                        fk_col.name AS fk_column, OBJECT_NAME(fk.referenced_object_id) AS pk_table,
                        pk_col.name AS pk_column, fkc.constraint_column_id AS key_seq,
                        fk.delete_referential_action_desc AS delete_rule,
                        fk.update_referential_action_desc AS update_rule
                FROM sys.foreign_keys fk
                JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
                JOIN sys.columns fk_col ON fk_col.object_id = fkc.parent_object_id AND fk_col.column_id = fkc.parent_column_id
                JOIN sys.columns pk_col ON pk_col.object_id = fkc.referenced_object_id AND pk_col.column_id = fkc.referenced_column_id
                WHERE OBJECT_SCHEMA_NAME(fk.parent_object_id) = ?
                ORDER BY OBJECT_NAME(fk.parent_object_id), fk.name, fkc.constraint_column_id
                """;
        String schemaName = resolveSchema(schema, connection);
        List<ImportedKey> importedKeys = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    importedKeys.add(readImportedKey(rs, schemaName));
                }
            }
        }
        return Optional.of(List.copyOf(importedKeys));
    }

    @Override
    public Optional<List<ImportedKey>> getAllExportedKeys(Connection connection, String catalog, String schema)
            throws SQLException {
        // Symmetric to getAllImportedKeys: filter by the schema of the REFERENCED
        // table.
        String sql = """
                SELECT fk.name AS fk_name, OBJECT_NAME(fk.parent_object_id) AS fk_table,
                        fk_col.name AS fk_column, OBJECT_NAME(fk.referenced_object_id) AS pk_table,
                        pk_col.name AS pk_column, fkc.constraint_column_id AS key_seq,
                        fk.delete_referential_action_desc AS delete_rule,
                        fk.update_referential_action_desc AS update_rule
                FROM sys.foreign_keys fk
                JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
                JOIN sys.columns fk_col ON fk_col.object_id = fkc.parent_object_id AND fk_col.column_id = fkc.parent_column_id
                JOIN sys.columns pk_col ON pk_col.object_id = fkc.referenced_object_id AND pk_col.column_id = fkc.referenced_column_id
                WHERE OBJECT_SCHEMA_NAME(fk.referenced_object_id) = ?
                ORDER BY OBJECT_NAME(fk.referenced_object_id), fk.name, fkc.constraint_column_id
                """;
        String schemaName = resolveSchema(schema, connection);
        List<ImportedKey> exportedKeys = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    exportedKeys.add(readImportedKey(rs, schemaName));
                }
            }
        }
        return Optional.of(List.copyOf(exportedKeys));
    }

    @Override
    public Optional<List<IndexInfo>> getAllIndexInfo(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT i.name AS index_name, i.type_desc AS index_type, i.is_unique,
                        OBJECT_NAME(i.object_id) AS table_name, c.name AS column_name,
                        ic.key_ordinal, ic.is_descending_key
                FROM sys.indexes i
                JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
                JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                WHERE OBJECT_SCHEMA_NAME(i.object_id) = ? AND i.type > 0
                    AND i.is_primary_key = 0 AND i.is_unique_constraint = 0
                ORDER BY OBJECT_NAME(i.object_id), i.name, ic.key_ordinal
                """;
        String schemaName = resolveSchema(schema, connection);
        Map<String, List<IndexInfoItem>> tableIndexes = new LinkedHashMap<>();
        Map<String, TableReference> tableRefs = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String indexName = rs.getString("index_name");
                    String indexType = rs.getString("index_type");
                    boolean isUnique = rs.getBoolean("is_unique");
                    String columnName = rs.getString("column_name");
                    int ordinalPosition = rs.getInt("key_ordinal");
                    boolean isDescending = rs.getBoolean("is_descending_key");

                    IndexInfoItem.IndexType mappedType = mapMssqlIndexType(indexType);

                    TableReference tableRef = tableRefs.computeIfAbsent(tableName, k -> {
                        Optional<SchemaReference> oSchema = Optional
                                .of(new SchemaReference(Optional.empty(), schemaName));
                        return new TableReference(oSchema, k);
                    });

                    Optional<ColumnReference> colRef = Optional.ofNullable(columnName)
                            .map(cn -> new ColumnReference(Optional.of(tableRef), cn));

                    IndexInfoItem item = new IndexInfoItemRecord(Optional.ofNullable(indexName), mappedType, colRef,
                            ordinalPosition, Optional.of(!isDescending), 0L, 0L, Optional.empty(), isUnique);

                    tableIndexes.computeIfAbsent(tableName, k -> new ArrayList<>()).add(item);
                }
            }
        }
        List<IndexInfo> result = new ArrayList<>();
        for (Map.Entry<String, List<IndexInfoItem>> entry : tableIndexes.entrySet()) {
            result.add(new IndexInfoRecord(tableRefs.get(entry.getKey()), List.copyOf(entry.getValue())));
        }
        return Optional.of(List.copyOf(result));
    }

    @Override
    public List<ViewDefinition> getAllViewDefinitions(Connection connection, String catalog, String schema)
            throws SQLException {
        String sql = """
                SELECT v.name AS view_name, m.definition
                FROM sys.views v JOIN sys.sql_modules m ON m.object_id = v.object_id
                WHERE OBJECT_SCHEMA_NAME(v.object_id) = ? ORDER BY v.name
                """;
        String schemaName = resolveSchema(schema, connection);
        List<ViewDefinition> views = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    String definition = rs.getString("definition");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    TableReference viewRef = new TableReference(oSchema, viewName, "VIEW");

                    views.add(new ViewDefinitionRecord(viewRef, Optional.ofNullable(definition),
                            Optional.ofNullable(definition)));
                }
            }
        }
        return List.copyOf(views);
    }

    @Override
    public List<Procedure> getAllProcedures(Connection connection, String catalog, String schema) throws SQLException {
        String schemaName = resolveSchema(schema, connection);
        Map<String, List<ProcedureColumn>> paramMap = loadMssqlProcedureColumns(connection, schemaName);

        String sql = """
                SELECT p.name AS routine_name, OBJECT_SCHEMA_NAME(p.object_id) AS schema_name,
                        m.definition AS body, p.modify_date
                FROM sys.procedures p
                LEFT JOIN sys.sql_modules m ON m.object_id = p.object_id
                WHERE OBJECT_SCHEMA_NAME(p.object_id) = ?
                ORDER BY p.name
                """;
        List<Procedure> procedures = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String body = rs.getString("body");
                    Timestamp modifyDate = rs.getTimestamp("modify_date");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    List<ProcedureColumn> cols = paramMap.getOrDefault(routineName, List.of());

                    // sys.sql_modules.definition contains the full CREATE statement as stored,
                    // which is suitable as both body and fullDefinition.
                    Optional<String> bodyOpt = Optional.ofNullable(body);
                    Optional<Instant> lastMod = modifyDate == null ? Optional.empty()
                            : Optional.of(modifyDate.toInstant());
                    procedures.add(new ProcedureRecord(new ProcedureReference(oSchema, routineName),
                            Procedure.ProcedureType.NO_RESULT, Optional.empty(), cols, bodyOpt, bodyOpt, lastMod));
                }
            }
        }
        return List.copyOf(procedures);
    }

    @Override
    public List<Function> getAllFunctions(Connection connection, String catalog, String schema) throws SQLException {
        String schemaName = resolveSchema(schema, connection);
        Map<String, List<FunctionColumn>> paramMap = loadMssqlFunctionColumns(connection, schemaName);

        String sql = """
                SELECT o.name AS routine_name, o.type_desc, m.definition AS body, o.modify_date
                FROM sys.objects o
                LEFT JOIN sys.sql_modules m ON m.object_id = o.object_id
                WHERE o.type IN ('FN', 'IF', 'TF') AND OBJECT_SCHEMA_NAME(o.object_id) = ?
                ORDER BY o.name
                """;
        List<Function> functions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String typeDesc = rs.getString("type_desc");
                    String body = rs.getString("body");
                    Timestamp modifyDate = rs.getTimestamp("modify_date");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    List<FunctionColumn> cols = paramMap.getOrDefault(routineName, List.of());

                    Function.FunctionType funcType = "TABLE_VALUED_FUNCTION".equalsIgnoreCase(typeDesc)
                            || "INLINE_TABLE_VALUED_FUNCTION".equalsIgnoreCase(typeDesc)
                                    ? Function.FunctionType.RETURNS_TABLE
                                    : Function.FunctionType.NO_TABLE;

                    Optional<String> bodyOpt = Optional.ofNullable(body);
                    Optional<Instant> lastMod = modifyDate == null ? Optional.empty()
                            : Optional.of(modifyDate.toInstant());
                    functions.add(new FunctionRecord(new FunctionReference(oSchema, routineName), funcType,
                            Optional.empty(), cols, bodyOpt, bodyOpt, lastMod));
                }
            }
        }
        return List.copyOf(functions);
    }

    private Map<String, List<ProcedureColumn>> loadMssqlProcedureColumns(Connection connection, String schemaName)
            throws SQLException {
        String sql = """
                SELECT OBJECT_NAME(p.object_id) AS routine_name, p.name AS param_name,
                        p.is_output, TYPE_NAME(p.system_type_id) AS data_type,
                        p.parameter_id, p.precision, p.scale
                FROM sys.parameters p
                JOIN sys.procedures pr ON pr.object_id = p.object_id
                WHERE OBJECT_SCHEMA_NAME(p.object_id) = ? AND p.parameter_id > 0
                ORDER BY OBJECT_NAME(p.object_id), p.parameter_id
                """;
        Map<String, List<ProcedureColumn>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String paramName = rs.getString("param_name");
                    boolean isOutput = rs.getBoolean("is_output");
                    String dataType = rs.getString("data_type");
                    int paramId = rs.getInt("parameter_id");
                    int precision = rs.getInt("precision");
                    boolean precisionNull = rs.wasNull();
                    int scale = rs.getInt("scale");
                    boolean scaleNull = rs.wasNull();

                    // MSSQL param names start with @
                    String cleanName = paramName != null && paramName.startsWith("@") ? paramName.substring(1)
                            : (paramName != null ? paramName : "");

                    ProcedureColumn.ColumnType colType = isOutput ? ProcedureColumn.ColumnType.OUT
                            : ProcedureColumn.ColumnType.IN;
                    JDBCType jdbcType = mapMssqlJdbcType(dataType);

                    ProcedureColumn col = new ProcedureColumnRecord(cleanName, colType, jdbcType,
                            dataType != null ? dataType : "",
                            precisionNull ? OptionalInt.empty() : OptionalInt.of(precision),
                            scaleNull ? OptionalInt.empty() : OptionalInt.of(scale), OptionalInt.of(10),
                            ProcedureColumn.Nullability.UNKNOWN, Optional.empty(), Optional.empty(), paramId);

                    result.computeIfAbsent(routineName, k -> new ArrayList<>()).add(col);
                }
            }
        }
        return result;
    }

    private Map<String, List<FunctionColumn>> loadMssqlFunctionColumns(Connection connection, String schemaName)
            throws SQLException {
        String sql = """
                SELECT OBJECT_NAME(p.object_id) AS routine_name, p.name AS param_name,
                        p.is_output, TYPE_NAME(p.system_type_id) AS data_type,
                        p.parameter_id, p.precision, p.scale
                FROM sys.parameters p
                JOIN sys.objects o ON o.object_id = p.object_id
                WHERE o.type IN ('FN', 'IF', 'TF') AND OBJECT_SCHEMA_NAME(p.object_id) = ?
                        AND p.parameter_id > 0
                ORDER BY OBJECT_NAME(p.object_id), p.parameter_id
                """;
        Map<String, List<FunctionColumn>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String paramName = rs.getString("param_name");
                    boolean isOutput = rs.getBoolean("is_output");
                    String dataType = rs.getString("data_type");
                    int paramId = rs.getInt("parameter_id");
                    int precision = rs.getInt("precision");
                    boolean precisionNull = rs.wasNull();
                    int scale = rs.getInt("scale");
                    boolean scaleNull = rs.wasNull();

                    String cleanName = paramName != null && paramName.startsWith("@") ? paramName.substring(1)
                            : (paramName != null ? paramName : "");

                    FunctionColumn.ColumnType colType = isOutput ? FunctionColumn.ColumnType.OUT
                            : FunctionColumn.ColumnType.IN;
                    JDBCType jdbcType = mapMssqlJdbcType(dataType);

                    FunctionColumn col = new FunctionColumnRecord(cleanName, colType, jdbcType,
                            dataType != null ? dataType : "",
                            precisionNull ? OptionalInt.empty() : OptionalInt.of(precision),
                            scaleNull ? OptionalInt.empty() : OptionalInt.of(scale), OptionalInt.of(10),
                            FunctionColumn.Nullability.UNKNOWN, Optional.empty(), OptionalInt.empty(), paramId);

                    result.computeIfAbsent(routineName, k -> new ArrayList<>()).add(col);
                }
            }
        }
        return result;
    }

    private static JDBCType mapMssqlJdbcType(String dataType) {
        if (dataType == null) {
            return JDBCType.OTHER;
        }
        return switch (dataType.toLowerCase()) {
        case "int" -> JDBCType.INTEGER;
        case "bigint" -> JDBCType.BIGINT;
        case "smallint" -> JDBCType.SMALLINT;
        case "tinyint" -> JDBCType.TINYINT;
        case "bit" -> JDBCType.BIT;
        case "decimal", "numeric" -> JDBCType.DECIMAL;
        case "float" -> JDBCType.DOUBLE;
        case "real" -> JDBCType.REAL;
        case "money", "smallmoney" -> JDBCType.DECIMAL;
        case "char", "nchar" -> JDBCType.CHAR;
        case "varchar", "nvarchar" -> JDBCType.VARCHAR;
        case "text", "ntext" -> JDBCType.VARCHAR;
        case "date" -> JDBCType.DATE;
        case "time" -> JDBCType.TIME;
        case "datetime", "datetime2", "smalldatetime" -> JDBCType.TIMESTAMP;
        case "datetimeoffset" -> JDBCType.TIMESTAMP_WITH_TIMEZONE;
        case "binary" -> JDBCType.BINARY;
        case "varbinary" -> JDBCType.VARBINARY;
        case "image" -> JDBCType.LONGVARBINARY;
        case "uniqueidentifier" -> JDBCType.CHAR;
        case "xml" -> JDBCType.SQLXML;
        default -> JDBCType.OTHER;
        };
    }

    private Trigger readTrigger(ResultSet rs, String schemaName) throws SQLException {
        String triggerName = rs.getString("trigger_name");
        String tableName = rs.getString("table_name");
        boolean isInsteadOf = rs.getBoolean("is_instead_of_trigger");
        String definition = rs.getString("definition");

        Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
        TableReference tableRef = new TableReference(oSchema, tableName);

        TriggerTiming timing = isInsteadOf ? TriggerTiming.INSTEAD_OF : TriggerTiming.AFTER;
        TriggerEvent event = TriggerEvent.INSERT;

        return new TriggerRecord(new TriggerReference(tableRef, triggerName), timing, event,
                Optional.ofNullable(definition), Optional.ofNullable(definition), Optional.empty());
    }

    private ImportedKey readImportedKey(ResultSet rs, String schemaName) throws SQLException {
        String fkName = rs.getString("fk_name");
        String fkTable = rs.getString("fk_table");
        String fkColumn = rs.getString("fk_column");
        String pkTable = rs.getString("pk_table");
        String pkColumn = rs.getString("pk_column");
        int keySeq = rs.getInt("key_seq");
        String deleteRule = rs.getString("delete_rule");
        String updateRule = rs.getString("update_rule");

        // FK side
        Optional<SchemaReference> fkSchemaRef = Optional.of(new SchemaReference(Optional.empty(), schemaName));
        TableReference fkTableRef = new TableReference(fkSchemaRef, fkTable);
        ColumnReference fkColRef = new ColumnReference(Optional.of(fkTableRef), fkColumn);

        // PK side
        Optional<SchemaReference> pkSchemaRef = Optional.of(new SchemaReference(Optional.empty(), schemaName));
        TableReference pkTableRef = new TableReference(pkSchemaRef, pkTable);
        ColumnReference pkColRef = new ColumnReference(Optional.of(pkTableRef), pkColumn);

        return new ImportedKeyRecord(pkColRef, fkColRef, fkName, keySeq, mapReferentialAction(updateRule),
                mapReferentialAction(deleteRule), Optional.empty(), ImportedKey.Deferrability.NOT_DEFERRABLE);
    }

    private List<UniqueConstraint> readUniqueConstraints(Connection connection, String sql, String schema,
            String tableName) throws SQLException {
        String schemaName = resolveSchema(schema, connection);
        Map<String, UcBuilder> ucMap = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            if (tableName != null) {
                ps.setString(2, tableName);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String constraintName = rs.getString("constraint_name");
                    String table = rs.getString("table_name");
                    String columnName = rs.getString("column_name");

                    String key = table + "." + constraintName;
                    ucMap.computeIfAbsent(key, k -> new UcBuilder(table, constraintName, schemaName))
                            .addColumn(columnName);
                }
            }
        }
        List<UniqueConstraint> result = new ArrayList<>();
        for (UcBuilder builder : ucMap.values()) {
            result.add(builder.build());
        }
        return List.copyOf(result);
    }

    private String resolveSchema(String schema, Connection connection) throws SQLException {
        if (schema != null) {
            return schema;
        }
        return connection.getSchema() != null ? connection.getSchema() : "dbo";
    }

    private static ImportedKey.ReferentialAction mapReferentialAction(String action) {
        if (action == null) {
            return ImportedKey.ReferentialAction.NO_ACTION;
        }
        return switch (action.toUpperCase()) {
        case "CASCADE" -> ImportedKey.ReferentialAction.CASCADE;
        case "SET_NULL" -> ImportedKey.ReferentialAction.SET_NULL;
        case "SET_DEFAULT" -> ImportedKey.ReferentialAction.SET_DEFAULT;
        case "NO_ACTION" -> ImportedKey.ReferentialAction.NO_ACTION;
        default -> ImportedKey.ReferentialAction.NO_ACTION;
        };
    }

    private static IndexInfoItem.IndexType mapMssqlIndexType(String typeDesc) {
        if (typeDesc == null) {
            return IndexInfoItem.IndexType.TABLE_INDEX_OTHER;
        }
        return switch (typeDesc.toUpperCase()) {
        case "CLUSTERED" -> IndexInfoItem.IndexType.TABLE_INDEX_CLUSTERED;
        case "HEAP" -> IndexInfoItem.IndexType.TABLE_INDEX_STATISTIC;
        default -> IndexInfoItem.IndexType.TABLE_INDEX_OTHER;
        };
    }

    @Override
    public List<UserDefinedType> getAllUserDefinedTypes(Connection connection, String catalog, String schema)
            throws SQLException {
        String schemaName = resolveSchema(schema, connection);
        String sql = """
                SELECT t.name AS type_name, SCHEMA_NAME(t.schema_id) AS schema_name,
                        TYPE_NAME(t.system_type_id) AS base_type_name,
                        t.is_table_type
                FROM sys.types t
                WHERE t.is_user_defined = 1 AND SCHEMA_NAME(t.schema_id) = ?
                ORDER BY t.name
                """;
        List<UserDefinedType> types = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String typeName = rs.getString("type_name");
                    String baseTypeName = rs.getString("base_type_name");
                    boolean isTableType = rs.getBoolean("is_table_type");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));

                    String className = isTableType ? "TABLE_TYPE" : "ALIAS";
                    JDBCType baseType = isTableType ? JDBCType.STRUCT : mapMssqlJdbcType(baseTypeName);

                    types.add(new UserDefinedTypeRecord(new UserDefinedTypeReference(oSchema, typeName), className,
                            baseType, Optional.empty()));
                }
            }
        }
        return List.copyOf(types);
    }

    @Override
    public Optional<List<TablePrivilege>> getAllTablePrivileges(Connection connection, String catalog,
            String schemaPattern, String tableNamePattern) throws SQLException {
        // sys.database_permissions exposes GRANT / DENY / GRANT_WITH_GRANT_OPTION,
        // which JDBC's INFORMATION_SCHEMA-based view does not differentiate.
        // class = 1 → OBJECT, minor_id = 0 → whole table (column-level rows have
        // minor_id > 0).
        StringBuilder sql = new StringBuilder("""
                SELECT s.name AS schema_name, o.name AS table_name,
                        USER_NAME(p.grantor_principal_id) AS grantor,
                        pp.name AS grantee, p.permission_name, p.state_desc
                FROM sys.database_permissions p
                JOIN sys.objects o ON o.object_id = p.major_id
                JOIN sys.schemas s ON s.schema_id = o.schema_id
                JOIN sys.database_principals pp ON pp.principal_id = p.grantee_principal_id
                WHERE p.class = 1 AND p.minor_id = 0 AND s.name = ?
                """);
        boolean hasTableFilter = tableNamePattern != null && !tableNamePattern.isBlank()
                && !"%".equals(tableNamePattern);
        if (hasTableFilter) {
            sql.append("  AND o.name LIKE ?\n");
        }
        sql.append("ORDER BY o.name, p.permission_name, pp.name");

        String schemaName = resolveSchema(schemaPattern, connection);
        List<TablePrivilege> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setString(1, schemaName);
            if (hasTableFilter) {
                ps.setString(2, tableNamePattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String grantor = rs.getString("grantor");
                    String grantee = rs.getString("grantee");
                    String permission = rs.getString("permission_name");
                    String state = rs.getString("state_desc");

                    Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                    TableReference tableRef = new TableReference(oSchema, tableName);

                    // Encode DENY / GRANT_WITH_GRANT_OPTION into privilege + isGrantable.
                    String privilege = "DENY".equalsIgnoreCase(state) ? "DENY " + permission : permission;
                    String isGrantable = "GRANT_WITH_GRANT_OPTION".equalsIgnoreCase(state) ? "YES" : "NO";

                    result.add(new TablePrivilegeRecord(tableRef, Optional.ofNullable(grantor), grantee, privilege,
                            Optional.of(isGrantable)));
                }
            }
        }
        return Optional.of(List.copyOf(result));
    }

    @Override
    public Optional<List<ColumnPrivilege>> getColumnPrivileges(Connection connection, String catalog, String schema,
            String tableName, String columnNamePattern) throws SQLException {
        // Column-level permissions in sys.database_permissions have minor_id =
        // columns.column_id.
        StringBuilder sql = new StringBuilder("""
                SELECT c.name AS column_name,
                        USER_NAME(p.grantor_principal_id) AS grantor,
                        pp.name AS grantee, p.permission_name, p.state_desc
                FROM sys.database_permissions p
                JOIN sys.objects o ON o.object_id = p.major_id
                JOIN sys.schemas s ON s.schema_id = o.schema_id
                JOIN sys.columns c ON c.object_id = p.major_id AND c.column_id = p.minor_id
                JOIN sys.database_principals pp ON pp.principal_id = p.grantee_principal_id
                WHERE p.class = 1 AND p.minor_id > 0 AND s.name = ? AND o.name = ?
                """);
        boolean hasColumnFilter = columnNamePattern != null && !columnNamePattern.isBlank()
                && !"%".equals(columnNamePattern);
        if (hasColumnFilter) {
            sql.append("  AND c.name LIKE ?\n");
        }
        sql.append("ORDER BY c.name, p.permission_name, pp.name");

        String schemaName = resolveSchema(schema, connection);
        List<ColumnPrivilege> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
            if (hasColumnFilter) {
                ps.setString(3, columnNamePattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                TableReference tableRef = new TableReference(oSchema, tableName);

                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String grantor = rs.getString("grantor");
                    String grantee = rs.getString("grantee");
                    String permission = rs.getString("permission_name");
                    String state = rs.getString("state_desc");

                    String privilege = "DENY".equalsIgnoreCase(state) ? "DENY " + permission : permission;
                    String isGrantable = "GRANT_WITH_GRANT_OPTION".equalsIgnoreCase(state) ? "YES" : "NO";

                    ColumnReference colRef = new ColumnReference(Optional.of(tableRef), columnName);
                    result.add(new ColumnPrivilegeRecord(colRef, Optional.ofNullable(grantor), grantee, privilege,
                            Optional.of(isGrantable)));
                }
            }
        }
        return Optional.of(List.copyOf(result));
    }

    @Override
    public Optional<List<PseudoColumn>> getAllPseudoColumns(Connection connection, String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException {
        // Surface MSSQL pseudo columns: rowversion/timestamp columns
        // (system_type_id=189),
        // computed columns (is_computed=1), and identity columns that behave like
        // implicit row IDs.
        StringBuilder sql = new StringBuilder("""
                SELECT s.name AS schema_name, o.name AS table_name, c.name AS column_name,
                        TYPE_NAME(c.system_type_id) AS type_name, c.max_length, c.precision, c.scale,
                        c.is_nullable, c.is_computed, c.is_rowguidcol, c.system_type_id
                FROM sys.columns c
                JOIN sys.objects o ON o.object_id = c.object_id
                JOIN sys.schemas s ON s.schema_id = o.schema_id
                WHERE (c.is_computed = 1 OR c.system_type_id = 189 OR c.is_rowguidcol = 1)
                    AND o.type = 'U'
                    AND s.name = ?
                """);
        boolean hasTableFilter = tableNamePattern != null && !tableNamePattern.isBlank()
                && !"%".equals(tableNamePattern);
        if (hasTableFilter) {
            sql.append("  AND o.name LIKE ?\n");
        }
        boolean hasColumnFilter = columnNamePattern != null && !columnNamePattern.isBlank()
                && !"%".equals(columnNamePattern);
        if (hasColumnFilter) {
            sql.append("  AND c.name LIKE ?\n");
        }
        sql.append("ORDER BY o.name, c.name");

        String schemaName = resolveSchema(schemaPattern, connection);
        List<PseudoColumn> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, schemaName);
            if (hasTableFilter) {
                ps.setString(idx++, tableNamePattern);
            }
            if (hasColumnFilter) {
                ps.setString(idx++, columnNamePattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String columnName = rs.getString("column_name");
                    String typeName = rs.getString("type_name");
                    int maxLength = rs.getInt("max_length");
                    int precision = rs.getInt("precision");
                    int scale = rs.getInt("scale");
                    boolean nullable = rs.getBoolean("is_nullable");
                    boolean computed = rs.getBoolean("is_computed");
                    boolean rowguid = rs.getBoolean("is_rowguidcol");
                    int systemTypeId = rs.getInt("system_type_id");

                    TableReference tableRef = new TableReference(oSchema, tableName);
                    ColumnReference colRef = new ColumnReference(Optional.of(tableRef), columnName);

                    String usage = computed ? "COMPUTED"
                            : systemTypeId == 189 ? "ROWVERSION" : rowguid ? "ROWGUIDCOL" : "HIDDEN";

                    result.add(new PseudoColumnRecord(colRef, mapMssqlJdbcType(typeName),
                            OptionalInt.of(precision > 0 ? precision : maxLength), OptionalInt.of(scale),
                            OptionalInt.of(10), Optional.empty(), OptionalInt.empty(),
                            Optional.of(nullable ? "YES" : "NO"), usage));
                }
            }
        }
        return Optional.of(List.copyOf(result));
    }

    // Builder for aggregating composite primary key columns
    private static class PkBuilder {
        private final String tableName;
        private final String constraintName;
        private final String schemaName;
        private final List<String> columns = new ArrayList<>();

        PkBuilder(String tableName, String constraintName, String schemaName) {
            this.tableName = tableName;
            this.constraintName = constraintName;
            this.schemaName = schemaName;
        }

        void addColumn(String columnName) {
            columns.add(columnName);
        }

        PrimaryKey build() {
            Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
            TableReference tableRef = new TableReference(oSchema, tableName);
            List<ColumnReference> colRefs = columns.stream()
                    .map(col -> (ColumnReference) new ColumnReference(Optional.of(tableRef), col)).toList();
            return new PrimaryKeyRecord(tableRef, colRefs, Optional.of(constraintName));
        }
    }

    // Builder for aggregating composite unique constraint columns
    private static class UcBuilder {
        private final String tableName;
        private final String constraintName;
        private final String schemaName;
        private final List<String> columns = new ArrayList<>();

        UcBuilder(String tableName, String constraintName, String schemaName) {
            this.tableName = tableName;
            this.constraintName = constraintName;
            this.schemaName = schemaName;
        }

        void addColumn(String columnName) {
            columns.add(columnName);
        }

        UniqueConstraint build() {
            Optional<SchemaReference> oSchema = Optional.of(new SchemaReference(Optional.empty(), schemaName));
            TableReference tableRef = new TableReference(oSchema, tableName);
            List<ColumnReference> colRefs = columns.stream()
                    .map(col -> (ColumnReference) new ColumnReference(Optional.of(tableRef), col)).toList();
            return new UniqueConstraintRecord(constraintName, tableRef, colRefs);
        }
    }

    @Override
    public String createTrigger(String triggerName, org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerTiming timing,
            org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerEvent event,
            org.eclipse.daanse.jdbc.db.api.schema.TableReference table,
            org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerScope scope, String whenCondition, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body must not be blank for SQL Server CREATE TRIGGER");
        }
        StringBuilder sb = new StringBuilder("CREATE TRIGGER ");
        sb.append(quoteIdentifier(triggerName));
        sb.append(" ON ").append(qualified(table));
        // SQL Server: AFTER (or FOR), INSTEAD OF — no BEFORE.
        if (timing == org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerTiming.INSTEAD_OF) {
            sb.append(" INSTEAD OF ");
        } else {
            sb.append(" AFTER ");
        }
        sb.append(event);
        // Body should already include the {@code AS} keyword; if not, prepend.
        String trimmed = body.stripLeading();
        if (!trimmed.regionMatches(true, 0, "AS", 0, 2) && !trimmed.regionMatches(true, 0, "WITH", 0, 4)) {
            sb.append(" AS ");
        } else {
            sb.append(' ');
        }
        sb.append(body);
        return sb.toString();
    }

    // -------------------- Trigger procedures --------------------

    @Override
    public Optional<String> createTriggerProcedure(String procedureName, String schemaName, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body must not be blank for SQL Server trigger procedure");
        }
        String qualified = schemaName != null && !schemaName.isBlank()
                ? quoteIdentifier(schemaName, procedureName).toString()
                : quoteIdentifier(procedureName).toString();
        return Optional.of("CREATE OR ALTER PROCEDURE " + qualified + " AS " + body);
    }

    @Override
    public String createTriggerUsingProcedure(String triggerName, String schemaName,
            org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerTiming timing,
            org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerEvent event,
            org.eclipse.daanse.jdbc.db.api.schema.TableReference table,
            org.eclipse.daanse.jdbc.db.api.schema.Trigger.TriggerScope scope, String whenCondition,
            String procedureName) {
        String qualified = schemaName != null && !schemaName.isBlank()
                ? quoteIdentifier(schemaName, procedureName).toString()
                : quoteIdentifier(procedureName).toString();
        return createTrigger(triggerName, timing, event, table, scope, whenCondition, "AS EXEC " + qualified);
    }

    /** SQL Server: {@code DROP PROCEDURE [IF EXISTS] schema.procedureName}. */
    @Override
    public Optional<String> dropProcedure(String procedureName, String schemaName, boolean ifExists) {
        String qualified = schemaName != null && !schemaName.isBlank()
                ? quoteIdentifier(schemaName, procedureName).toString()
                : quoteIdentifier(procedureName).toString();
        return Optional.of("DROP PROCEDURE " + (ifExists ? "IF EXISTS " : "") + qualified);
    }

    // -------------------- DDL — ALTER / RENAME (SQL-Server-specific)
    // --------------------

    @Override
    public String alterTableAddColumn(TableReference table, ColumnDefinition column) {
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(qualified(table));
        sb.append(" ADD ");
        sb.append(quoteIdentifier(column.column().name()));
        sb.append(' ').append(nativeType(column.columnMetaData()));
        if (column.columnMetaData().nullability() == ColumnMetaData.Nullability.NO_NULLS) {
            sb.append(" NOT NULL");
        }
        column.columnMetaData().columnDefault().ifPresent(d -> sb.append(" DEFAULT ").append(d));
        return sb.toString();
    }

    /**
     * SQL Server: {@code ALTER TABLE x ALTER COLUMN c <type>} (no {@code TYPE}
     * keyword).
     */
    @Override
    public String alterColumnType(TableReference table, String columnName, ColumnMetaData newMeta) {
        if (newMeta == null) {
            throw new IllegalArgumentException("newMeta must not be null for ALTER COLUMN");
        }
        return new StringBuilder("ALTER TABLE ").append(qualified(table)).append(" ALTER COLUMN ")
                .append(quoteIdentifier(columnName)).append(' ').append(nativeType(newMeta)).toString();
    }

    @Override
    public String alterColumnSetNullability(TableReference table, String columnName, boolean nullable) {
        throw new UnsupportedOperationException(
                "SQL Server requires the column type when altering nullability — use the "
                        + "alterColumnSetNullability(table, column, nullable, currentMeta) overload.");
    }

    @Override
    public String alterColumnSetNullability(TableReference table, String columnName, boolean nullable,
            ColumnMetaData currentMeta) {
        if (currentMeta == null) {
            throw new IllegalArgumentException("currentMeta must not be null for SQL Server ALTER COLUMN");
        }
        return new StringBuilder("ALTER TABLE ").append(qualified(table)).append(" ALTER COLUMN ")
                .append(quoteIdentifier(columnName)).append(' ').append(nativeType(currentMeta))
                .append(nullable ? " NULL" : " NOT NULL").toString();
    }

    @Override
    public String alterColumnSetDefault(TableReference table, String columnName, String defaultExpression) {
        if (defaultExpression == null || defaultExpression.isBlank()) {
            throw new IllegalArgumentException("defaultExpression must not be blank for SET DEFAULT");
        }
        String dfName = "DF_" + table.name() + "_" + columnName;
        return new StringBuilder("ALTER TABLE ").append(qualified(table)).append(" ADD CONSTRAINT ")
                .append(quoteIdentifier(dfName)).append(" DEFAULT (").append(defaultExpression).append(") FOR ")
                .append(quoteIdentifier(columnName)).toString();
    }

    /**
     * Drops the assumed {@code DF_
     *
    <table>
     * _<col>} default-constraint produced by {@link #alterColumnSetDefault}.
     */
    @Override
    public String alterColumnDropDefault(TableReference table, String columnName) {
        String dfName = "DF_" + table.name() + "_" + columnName;
        return new StringBuilder("ALTER TABLE ").append(qualified(table)).append(" DROP CONSTRAINT ")
                .append(quoteIdentifier(dfName)).toString();
    }

    @Override
    public String renameColumn(TableReference table, String oldName, String newName) {
        return spRename(qualified(table) + "." + quoteIdentifier(oldName), newName, "COLUMN");
    }

    /** SQL Server: {@code EXEC sp_rename 'schema.table', 'new'}. */
    @Override
    public String renameTable(TableReference table, String newName) {
        return spRename(qualified(table), newName, null);
    }

    /** SQL Server: {@code EXEC sp_rename 'schema.table.idx', 'new', 'INDEX'}. */
    @Override
    public String renameIndex(String oldName, String newName, TableReference table) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null for SQL Server sp_rename INDEX");
        }
        return spRename(qualified(table) + "." + quoteIdentifier(oldName), newName, "INDEX");
    }

    /**
     * SQL Server:
     * {@code EXEC sp_rename 'schema.table.constraint', 'new', 'OBJECT'}.
     */
    @Override
    public String renameConstraint(TableReference table, String oldName, String newName) {
        return spRename(qualified(table) + "." + quoteIdentifier(oldName), newName, "OBJECT");
    }

    /**
     * @param objectType the sp_rename type ({@code COLUMN}/{@code INDEX}/
     *                   {@code OBJECT}), or {@code null} for the table-rename
     *                   overload (which omits the third argument entirely)
     */
    private String spRename(String quotedSourcePath, String newName, String objectType) {
        StringBuilder sb = new StringBuilder("EXEC sp_rename '");
        sb.append(quotedSourcePath);
        sb.append("', '").append(newName).append("'");
        if (objectType != null)
            sb.append(", '").append(objectType).append("'");
        return sb.toString();
    }
}
