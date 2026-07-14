/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.db.configurable;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.AggregationGenerator;
import org.eclipse.daanse.jdbc.db.api.sql.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.DdlGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.FunctionGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.HintGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.OrderByGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.RegexGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.SqlGenerator;
import org.eclipse.daanse.jdbc.db.api.sql.OrderedColumn;
import org.eclipse.daanse.jdbc.db.api.type.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.api.type.Datatype;

public class ConfigurableDialect implements Dialect, SqlGenerator, DdlGenerator, OrderByGenerator, RegexGenerator,
        AggregationGenerator, FunctionGenerator, HintGenerator {

    @Override
    public SqlGenerator sqlGenerator() {
        return this;
    }

    @Override
    public DdlGenerator ddlGenerator() {
        return this;
    }

    @Override
    public OrderByGenerator orderByGenerator() {
        return this;
    }

    @Override
    public RegexGenerator regexGenerator() {
        return this;
    }

    @Override
    public AggregationGenerator aggregationGenerator() {
        return this;
    }

    @Override
    public FunctionGenerator functionGenerator() {
        return this;
    }

    @Override
    public HintGenerator hintGenerator() {
        return this;
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.capability.WindowFunctionCapabilities getWindowFunctionCapabilities() {
        return new org.eclipse.daanse.jdbc.db.dialect.api.capability.WindowFunctionCapabilities(
                supportsPercentileDisc(), supportsPercentileCont(), supportsListAgg(), supportsNthValue(),
                supportsNthValueIgnoreNulls());
    }

    private final String quoteIdentifierString;
    private final String dialectName;
    private final boolean allowsFromAlias;
    private final boolean allowsFromQuery;
    private final boolean requiresAliasForFromQuery;
    private final boolean allowsJoinOn;
    private final boolean allowsFieldAlias;
    private final boolean allowsCountDistinct;
    private final boolean allowsMultipleCountDistinct;
    private final boolean allowsCompoundCountDistinct;
    private final boolean allowsCountDistinctWithOtherAggs;
    private final boolean allowsMultipleDistinctSqlMeasures;
    private final boolean allowsInnerDistinct;
    private final boolean supportsGroupByExpressions;
    private final boolean supportsGroupingSets;
    private final boolean requiresGroupByAlias;
    private final boolean allowsSelectNotInGroupBy;
    private final boolean requiresOrderByAlias;
    private final boolean allowsOrderByAlias;
    private final boolean requiresUnionOrderByOrdinal;
    private final boolean requiresUnionOrderByExprInSelect;
    private final boolean requiresHavingAlias;
    private final boolean supportsUnlimitedValueList;
    private final boolean supportsMultiValueInExpr;
    private final boolean supportsDdl;
    private final boolean allowsRegularExpressionInWhereClause;
    private final Set<BitOperation> supportedBitAggregations;
    private final boolean supportsPercentileCont;
    private final boolean supportsPercentileDisc;
    private final boolean supportsListAgg;
    private final int maxColumnNameLength;
    private final boolean allowsDialectSharing;
    private final boolean requiresDrillthroughMaxRowsInLimit;
    private final boolean supportsParallelLoading;
    private final boolean supportsBatchOperations;
    private final org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding caseFolding;
    private volatile org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy quotingPolicy;
    private final java.util.Set<String> sqlKeywordsLower = java.util.Set.of("all", "and", "any", "as", "asc", "between",
            "by", "case", "check", "column", "create", "cross", "current_date", "current_time", "current_timestamp",
            "default", "delete", "desc", "distinct", "drop", "else", "end", "escape", "except", "exists", "false",
            "for", "foreign", "from", "full", "group", "having", "in", "inner", "insert", "intersect", "into", "is",
            "join", "key", "left", "like", "natural", "not", "null", "on", "or", "order", "outer", "primary",
            "references", "right", "select", "set", "some", "table", "then", "true", "union", "unique", "update",
            "user", "using", "values", "view", "when", "where", "with");
    private static final java.util.regex.Pattern TRIVIAL_IDENT_PATTERN = java.util.regex.Pattern
            .compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * @param config the OSGi metatype configuration
     */
    public ConfigurableDialect(ConfigurableDialectConfig config) {
        this.quoteIdentifierString = config.quoteIdentifierString();
        this.dialectName = config.dialectName();
        this.allowsFromAlias = config.allowsFromAlias();
        this.allowsFromQuery = config.allowsFromQuery();
        this.requiresAliasForFromQuery = config.requiresAliasForFromQuery();
        this.allowsJoinOn = config.allowsJoinOn();
        this.allowsFieldAlias = config.allowsFieldAlias();
        this.allowsCountDistinct = config.allowsCountDistinct();
        this.allowsMultipleCountDistinct = config.allowsMultipleCountDistinct();
        this.allowsCompoundCountDistinct = config.allowsCompoundCountDistinct();
        this.allowsCountDistinctWithOtherAggs = config.allowsCountDistinctWithOtherAggs();
        this.allowsMultipleDistinctSqlMeasures = config.allowsMultipleDistinctSqlMeasures();
        this.allowsInnerDistinct = config.allowsInnerDistinct();
        this.supportsGroupByExpressions = config.supportsGroupByExpressions();
        this.supportsGroupingSets = config.supportsGroupingSets();
        this.requiresGroupByAlias = config.requiresGroupByAlias();
        this.allowsSelectNotInGroupBy = config.allowsSelectNotInGroupBy();
        this.requiresOrderByAlias = config.requiresOrderByAlias();
        this.allowsOrderByAlias = config.allowsOrderByAlias();
        this.requiresUnionOrderByOrdinal = config.requiresUnionOrderByOrdinal();
        this.requiresUnionOrderByExprInSelect = config.requiresUnionOrderByExprInSelect();
        this.requiresHavingAlias = config.requiresHavingAlias();
        this.supportsUnlimitedValueList = config.supportsUnlimitedValueList();
        this.supportsMultiValueInExpr = config.supportsMultiValueInExpr();
        this.supportsDdl = config.supportsDdl();
        this.allowsRegularExpressionInWhereClause = config.allowsRegularExpressionInWhereClause();
        EnumSet<BitOperation> bitAggs = EnumSet.noneOf(BitOperation.class);
        if (config.supportsBitAndAgg())
            bitAggs.add(BitOperation.AND);
        if (config.supportsBitOrAgg())
            bitAggs.add(BitOperation.OR);
        if (config.supportsBitXorAgg())
            bitAggs.add(BitOperation.XOR);
        if (config.supportsBitNAndAgg())
            bitAggs.add(BitOperation.NAND);
        if (config.supportsBitNOrAgg())
            bitAggs.add(BitOperation.NOR);
        if (config.supportsBitNXorAgg())
            bitAggs.add(BitOperation.NXOR);
        this.supportedBitAggregations = Set.copyOf(bitAggs);
        this.supportsPercentileCont = config.supportsPercentileCont();
        this.supportsPercentileDisc = config.supportsPercentileDisc();
        this.supportsListAgg = config.supportsListAgg();
        this.maxColumnNameLength = config.maxColumnNameLength();
        this.allowsDialectSharing = config.allowsDialectSharing();
        this.requiresDrillthroughMaxRowsInLimit = config.requiresDrillthroughMaxRowsInLimit();
        this.supportsParallelLoading = config.supportsParallelLoading();
        this.supportsBatchOperations = config.supportsBatchOperations();
        this.caseFolding = config.caseFolding() == null
                ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.UPPER
                : config.caseFolding();
        this.quotingPolicy = config.quotingPolicy() == null
                ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS
                : config.quotingPolicy();
    }

    /**
     * @param builder the builder with configuration values
     */
    private ConfigurableDialect(Builder builder) {
        this.quoteIdentifierString = builder.quoteIdentifierString;
        this.dialectName = builder.dialectName;
        this.allowsFromAlias = builder.allowsFromAlias;
        this.allowsFromQuery = builder.allowsFromQuery;
        this.requiresAliasForFromQuery = builder.requiresAliasForFromQuery;
        this.allowsJoinOn = builder.allowsJoinOn;
        this.allowsFieldAlias = builder.allowsFieldAlias;
        this.allowsCountDistinct = builder.allowsCountDistinct;
        this.allowsMultipleCountDistinct = builder.allowsMultipleCountDistinct;
        this.allowsCompoundCountDistinct = builder.allowsCompoundCountDistinct;
        this.allowsCountDistinctWithOtherAggs = builder.allowsCountDistinctWithOtherAggs;
        this.allowsMultipleDistinctSqlMeasures = builder.allowsMultipleDistinctSqlMeasures;
        this.allowsInnerDistinct = builder.allowsInnerDistinct;
        this.supportsGroupByExpressions = builder.supportsGroupByExpressions;
        this.supportsGroupingSets = builder.supportsGroupingSets;
        this.requiresGroupByAlias = builder.requiresGroupByAlias;
        this.allowsSelectNotInGroupBy = builder.allowsSelectNotInGroupBy;
        this.requiresOrderByAlias = builder.requiresOrderByAlias;
        this.allowsOrderByAlias = builder.allowsOrderByAlias;
        this.requiresUnionOrderByOrdinal = builder.requiresUnionOrderByOrdinal;
        this.requiresUnionOrderByExprInSelect = builder.requiresUnionOrderByExprInSelect;
        this.requiresHavingAlias = builder.requiresHavingAlias;
        this.supportsUnlimitedValueList = builder.supportsUnlimitedValueList;
        this.supportsMultiValueInExpr = builder.supportsMultiValueInExpr;
        this.supportsDdl = builder.supportsDdl;
        this.allowsRegularExpressionInWhereClause = builder.allowsRegularExpressionInWhereClause;
        this.supportedBitAggregations = Set.copyOf(builder.supportedBitAggregations);
        this.supportsPercentileCont = builder.supportsPercentileCont;
        this.supportsPercentileDisc = builder.supportsPercentileDisc;
        this.supportsListAgg = builder.supportsListAgg;
        this.maxColumnNameLength = builder.maxColumnNameLength;
        this.allowsDialectSharing = builder.allowsDialectSharing;
        this.requiresDrillthroughMaxRowsInLimit = builder.requiresDrillthroughMaxRowsInLimit;
        this.supportsParallelLoading = builder.supportsParallelLoading;
        this.supportsBatchOperations = builder.supportsBatchOperations;
        this.caseFolding = builder.caseFolding == null
                ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.UPPER
                : builder.caseFolding;
        this.quotingPolicy = builder.quotingPolicy == null
                ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS
                : builder.quotingPolicy;
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding caseFolding() {
        return caseFolding;
    }

    @Override
    public org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy quotingPolicy() {
        return quotingPolicy;
    }

    /**
     * Replaces the active quoting policy. Mutates dialect state — see also
     * {@code quoteIdentifierWith(...)} for non-mutating per-call overrides.
     */
    public void setQuotingPolicy(org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy policy) {
        this.quotingPolicy = policy == null ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS
                : policy;
    }

    private boolean needsQuoting(String val) {
        if (val == null || val.isEmpty()) {
            return true;
        }
        if (!TRIVIAL_IDENT_PATTERN.matcher(val).matches()) {
            return true;
        }
        if (sqlKeywordsLower.contains(val.toLowerCase(java.util.Locale.ROOT))) {
            return true;
        }
        return caseFolding != org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.PRESERVE
                && !caseFolding.isCanonical(val);
    }

    /**
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ========== Identifier Quoting ==========

    @Override
    public String getQuoteIdentifierString() {
        return quoteIdentifierString;
    }

    @Override
    public String quoteIdentifier(CharSequence val) {
        StringBuilder buf = new StringBuilder(val.length() + 10);
        quoteIdentifier(val.toString(), buf);
        return buf.toString();
    }

    @Override
    public void quoteIdentifier(String val, StringBuilder buf) {
        emitIdentifier(val, buf, quotingPolicy);
    }

    @Override
    public void quoteIdentifierWith(String val, StringBuilder buf,
            org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy policy) {
        if (val == null) {
            return;
        }
        emitIdentifier(val, buf,
                policy == null ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS : policy);
    }

    @Override
    public String quoteIdentifierWith(CharSequence val,
            org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy policy) {
        if (val == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder(val.length() + 10);
        emitIdentifier(val.toString(), buf,
                policy == null ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS : policy);
        return buf.toString();
    }

    private void emitIdentifier(String val, StringBuilder buf,
            org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy policy) {
        String q = getQuoteIdentifierString();
        if (q == null || (val.startsWith(q) && val.endsWith(q))) {
            buf.append(val);
            return;
        }
        switch (policy) {
        case NEVER:
            buf.append(val);
            return;
        case WHEN_NEEDED:
            if (!needsQuoting(val)) {
                buf.append(val);
                return;
            }
            break;
        case ALWAYS:
        default:
            break;
        }
        String val2 = val.replace(q, q + q);
        buf.append(q).append(val2).append(q);
    }

    @Override
    public String quoteIdentifier(String qual, String name) {
        StringBuilder buf = new StringBuilder();
        quoteIdentifier(buf, qual, name);
        return buf.toString();
    }

    @Override
    public void quoteIdentifier(StringBuilder buf, String... names) {
        int nonNullNameCount = 0;
        for (String name : names) {
            if (name == null) {
                continue;
            }
            if (nonNullNameCount > 0) {
                buf.append('.');
            }
            quoteIdentifier(name, buf);
            ++nonNullNameCount;
        }
    }

    // ========== Literal Quoting ==========

    @Override
    public void quoteStringLiteral(StringBuilder buf, String s) {
        buf.append('\'');
        String escaped = s.replace("'", "''");
        buf.append(escaped);
        buf.append('\'');
    }

    @Override
    public void quoteNumericLiteral(StringBuilder buf, String value) {
        buf.append(value);
    }

    @Override
    public void quoteBooleanLiteral(StringBuilder buf, String value) {
        if (!value.equalsIgnoreCase("TRUE") && !value.equalsIgnoreCase("FALSE")) {
            throw new NumberFormatException("Illegal BOOLEAN literal: " + value);
        }
        buf.append(value);
    }

    @Override
    public void quoteDateLiteral(StringBuilder buf, String value) {
        buf.append("DATE ");
        quoteStringLiteral(buf, value);
    }

    @Override
    public void quoteTimeLiteral(StringBuilder buf, String value) {
        buf.append("TIME ");
        quoteStringLiteral(buf, value);
    }

    @Override
    public void quoteTimestampLiteral(StringBuilder buf, String value) {
        buf.append("TIMESTAMP ");
        quoteStringLiteral(buf, value);
    }

    @Override
    public StringBuilder quoteDecimalLiteral(CharSequence value) {
        return new StringBuilder(value);
    }

    @Override
    public void quote(StringBuilder buf, Object value, Datatype datatype) {
        if (value == null) {
            buf.append("null");
        } else {
            quoteLiteral(datatype, buf, value.toString());
        }
    }

    // ========== SQL Function Wrappers ==========

    @Override
    public StringBuilder wrapIntoSqlUpperCaseFunction(CharSequence sqlExpression) {
        return new StringBuilder("UPPER(").append(sqlExpression).append(")");
    }

    @Override
    public StringBuilder wrapIntoSqlIfThenElseFunction(CharSequence idCondition, CharSequence thenExpression,
            CharSequence elseExpression) {
        return new StringBuilder("CASE WHEN ").append(idCondition).append(" THEN ").append(thenExpression)
                .append(" ELSE ").append(elseExpression).append(" END");
    }

    @Override

    @Override
    public Optional<String> generateRegularExpression(String source, String javaRegExp) {
        // Default implementation returns null (not supported)
        return Optional.empty();
    }

    // ========== ORDER BY Generation ==========

    @Override
    public StringBuilder generateOrderItem(CharSequence expr, boolean nullable, boolean ascending,
            boolean collateNullsLast) {
        StringBuilder sb = new StringBuilder();
        if (nullable) {
            if (collateNullsLast) {
                sb.append("CASE WHEN ").append(expr).append(" IS NULL THEN 1 ELSE 0 END, ");
            } else {
                sb.append("CASE WHEN ").append(expr).append(" IS NULL THEN 0 ELSE 1 END, ");
            }
        }
        sb.append(expr);
        sb.append(ascending ? " ASC" : " DESC");
        return sb;
    }

    @Override
    public StringBuilder generateOrderItemForOrderValue(CharSequence expr, String orderValue, Datatype datatype,
            boolean ascending, boolean collateNullsLast) {
        StringBuilder sb = new StringBuilder("CASE WHEN ").append(expr).append(" = ");
        quote(sb, orderValue, datatype);
        if (collateNullsLast) {
            sb.append(" THEN 1 ELSE 0 END, ");
        } else {
            sb.append(" THEN 0 ELSE 1 END, ");
        }
        sb.append(expr);
        sb.append(ascending ? " ASC" : " DESC");
        return sb;
    }

    // ========== Inline Values Generation ==========

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT * FROM (VALUES ");
        for (int i = 0; i < valueList.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            String[] values = valueList.get(i);
            buf.append("(");
            for (int j = 0; j < values.length; j++) {
                String value = values[j];
                if (j > 0) {
                    buf.append(", ");
                }
                String columnType = columnTypes.get(j);
                Datatype datatype = Datatype.fromValue(columnType);
                quote(buf, value, datatype);
            }
            buf.append(")");
        }
        buf.append(") AS t (");
        for (int j = 0; j < columnNames.size(); j++) {
            if (j > 0) {
                buf.append(", ");
            }
            quoteIdentifier(columnNames.get(j), buf);
        }
        buf.append(")");
        return buf;
    }

    @Override

    // ========== Bit Aggregation Functions ==========

    @Override
    public java.util.Optional<String> generateBitAggregation(BitOperation operation, CharSequence operand) {
        if (!supportsBitAggregation(operation)) {
            return java.util.Optional.empty();
        }
        StringBuilder buf = new StringBuilder(64);
        StringBuilder result = switch (operation) {
        case AND -> buf.append("BIT_AND(").append(operand).append(")");
        case OR -> buf.append("BIT_OR(").append(operand).append(")");
        case XOR -> buf.append("BIT_XOR(").append(operand).append(")");
        case NAND -> buf.append("BIT_NAND(").append(operand).append(")");
        case NOR -> buf.append("BIT_NOR(").append(operand).append(")");
        case NXOR -> buf.append("BIT_XNOR(").append(operand).append(")");
        };
        return java.util.Optional.of(result.toString());
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return supportedBitAggregations.contains(operation);
    }

    // ========== Percentile Functions ==========

    @Override
    public java.util.Optional<String> generatePercentileDisc(double percentile, boolean desc, String tableName,
            String columnName) {
        if (!supportsPercentileDisc()) {
            throw new UnsupportedOperationException("PERCENTILE_DISC not supported");
        }
        StringBuilder buf = new StringBuilder("PERCENTILE_DISC(").append(percentile)
                .append(") WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" DESC");
        }
        buf.append(")");
        return java.util.Optional.of((buf).toString());
    }

    @Override
    public java.util.Optional<String> generatePercentileCont(double percentile, boolean desc, String tableName,
            String columnName) {
        if (!supportsPercentileCont()) {
            throw new UnsupportedOperationException("PERCENTILE_CONT not supported");
        }
        StringBuilder buf = new StringBuilder("PERCENTILE_CONT(").append(percentile)
                .append(") WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" DESC");
        }
        buf.append(")");
        return java.util.Optional.of((buf).toString());
    }

    // ========== List Aggregation ==========

    @Override
    public java.util.Optional<String> generateListAgg(CharSequence operand, boolean distinct, String separator,
            String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
        if (!supportsListAgg()) {
            throw new UnsupportedOperationException("LISTAGG not supported");
        }
        StringBuilder buf = new StringBuilder("LISTAGG(");
        if (distinct) {
            buf.append("DISTINCT ");
        }
        if (coalesce != null) {
            buf.append("COALESCE(").append(operand).append(", '").append(coalesce).append("')");
        } else {
            buf.append(operand);
        }
        buf.append(", '");
        buf.append(separator != null ? separator : ", ");
        buf.append("'");
        if (onOverflowTruncate != null) {
            buf.append(" ON OVERFLOW TRUNCATE '").append(onOverflowTruncate).append("' WITHOUT COUNT");
        }
        buf.append(")");
        if (columns != null && !columns.isEmpty()) {
            buf.append(" WITHIN GROUP (ORDER BY ");
            appendOrderedColumns(buf, columns);
            buf.append(")");
        }
        return java.util.Optional.of((buf).toString());
    }

    @Override
    public java.util.Optional<String> generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n,
            List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder("NTH_VALUE(").append(operand).append(", ")
                .append(n == null || n < 1 ? 1 : n).append(")");
        if (ignoreNulls) {
            buf.append(" IGNORE NULLS");
        } else {
            buf.append(" RESPECT NULLS");
        }
        buf.append(" OVER (");
        if (columns != null && !columns.isEmpty()) {
            buf.append("ORDER BY ");
            appendOrderedColumns(buf, columns);
        }
        buf.append(")");
        return java.util.Optional.of((buf).toString());
    }

    private void appendOrderedColumns(StringBuilder buf, List<OrderedColumn> columns) {
        boolean first = true;
        for (OrderedColumn col : columns) {
            if (!first) {
                buf.append(", ");
            }
            first = false;
            if (col.tableName() != null) {
                quoteIdentifier(buf, col.tableName(), col.columnName());
            } else {
                quoteIdentifier(buf, col.columnName());
            }
            col.sortDirection().ifPresent(dir -> buf.append(' ').append(dir.name()));
            col.nullsOrder().ifPresent(no -> buf.append(" NULLS ").append(no.name()));
        }
    }

    // ========== DDL Operations ==========

    @Override
    public String clearTable(String schemaName, String tableName) {
        return "TRUNCATE TABLE " + quoteIdentifier(schemaName, tableName);
    }

    @Override
    public String dropTable(String schemaName, String tableName, boolean ifExists) {
        StringBuilder sb = new StringBuilder("DROP TABLE ");
        if (ifExists) {
            sb.append("IF EXISTS ");
        }
        sb.append(quoteIdentifier(schemaName, tableName));
        return sb.toString();
    }

    @Override
    public String createSchema(String schemaName, boolean ifNotExists) {
        StringBuilder sb = new StringBuilder("CREATE SCHEMA ");
        if (ifNotExists) {
            sb.append("IF NOT EXISTS ");
        }
        sb.append(quoteIdentifier(schemaName));
        return sb.toString();
    }

    @Override
    public String dropSchema(String schemaName, boolean ifExists, boolean cascade) {
        StringBuilder sb = new StringBuilder("DROP SCHEMA ");
        if (ifExists && supportsDropSchemaIfExists())
            sb.append("IF EXISTS ");
        sb.append(quoteIdentifier(schemaName));
        if (requiresDropSchemaRestrict()) {
            sb.append(" RESTRICT");
        } else if (cascade && supportsDropTableCascade()) {
            sb.append(" CASCADE");
        }
        return sb.toString();
    }

    // appendHintsAfterFromClause inherited from HintGenerator default (no-op).

    // ========== Feature Support Flags ==========

    @Override
    public String name() {
        return dialectName;
    }

    @Override
    public boolean allowsFromAlias() {
        return allowsFromAlias;
    }

    @Override
    public boolean allowsFromQuery() {
        return allowsFromQuery;
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return requiresAliasForFromQuery;
    }

    @Override
    public boolean allowsJoinOn() {
        return allowsJoinOn;
    }

    @Override
    public boolean allowsFieldAlias() {
        return allowsFieldAlias;
    }

    @Override
    public boolean allowsCountDistinct() {
        return allowsCountDistinct;
    }

    @Override
    public boolean allowsMultipleCountDistinct() {
        return allowsMultipleCountDistinct;
    }

    @Override
    public boolean allowsCompoundCountDistinct() {
        return allowsCompoundCountDistinct;
    }

    @Override
    public boolean allowsCountDistinctWithOtherAggs() {
        return allowsCountDistinctWithOtherAggs;
    }

    @Override
    public boolean allowsMultipleDistinctSqlMeasures() {
        return allowsMultipleDistinctSqlMeasures;
    }

    @Override
    public boolean allowsInnerDistinct() {
        return allowsInnerDistinct;
    }

    @Override
    public boolean supportsGroupByExpressions() {
        return supportsGroupByExpressions;
    }

    @Override
    public boolean supportsGroupingSets() {
        return supportsGroupingSets;
    }

    @Override
    public boolean requiresGroupByAlias() {
        return requiresGroupByAlias;
    }

    @Override
    public boolean allowsSelectNotInGroupBy() {
        return allowsSelectNotInGroupBy;
    }

    @Override
    public boolean requiresOrderByAlias() {
        return requiresOrderByAlias;
    }

    @Override
    public boolean allowsOrderByAlias() {
        return allowsOrderByAlias;
    }

    @Override
    public boolean requiresUnionOrderByOrdinal() {
        return requiresUnionOrderByOrdinal;
    }

    @Override
    public boolean requiresUnionOrderByExprInSelect() {
        return requiresUnionOrderByExprInSelect;
    }

    @Override
    public boolean requiresHavingAlias() {
        return requiresHavingAlias;
    }

    @Override
    public boolean supportsUnlimitedValueList() {
        return supportsUnlimitedValueList;
    }

    @Override
    public boolean supportsMultiValueInExpr() {
        return supportsMultiValueInExpr;
    }

    @Override
    public boolean supportsDdl() {
        return supportsDdl;
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return allowsRegularExpressionInWhereClause;
    }

    @Override
    public boolean supportsPercentileCont() {
        return supportsPercentileCont;
    }

    @Override
    public boolean supportsPercentileDisc() {
        return supportsPercentileDisc;
    }

    @Override
    public boolean supportsListAgg() {
        return supportsListAgg;
    }

    @Override
    public int getMaxColumnNameLength() {
        return maxColumnNameLength;
    }

    @Override
    public boolean allowsDialectSharing() {
        return allowsDialectSharing;
    }

    @Override
    public boolean requiresDrillthroughMaxRowsInLimit() {
        return requiresDrillthroughMaxRowsInLimit;
    }

    @Override
    public boolean supportsParallelLoading() {
        return supportsParallelLoading;
    }

    @Override
    public boolean supportsBatchOperations() {
        return supportsBatchOperations;
    }

    // ========== Methods Not Configurable (Runtime Dependent) ==========

    @Override
    public boolean needsExponent(Object value, String valueString) {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        // Cannot be configured - requires runtime metadata
        return true;
    }

    @Override
    public BestFitColumnType getType(ResultSetMetaData metadata, int columnIndex) throws SQLException {
        // Cannot be configured - requires runtime metadata
        return BestFitColumnType.OBJECT;
    }

    // ========== Builder ==========

    public static class Builder {
        private String quoteIdentifierString = "\"";
        private String dialectName = "configurable";
        private boolean allowsFromAlias = true;
        private boolean allowsFromQuery = true;
        private boolean requiresAliasForFromQuery = false;
        private boolean allowsJoinOn = true;
        private boolean allowsFieldAlias = true;
        private boolean allowsCountDistinct = true;
        private boolean allowsMultipleCountDistinct = true;
        private boolean allowsCompoundCountDistinct = false;
        private boolean allowsCountDistinctWithOtherAggs = true;
        private boolean allowsMultipleDistinctSqlMeasures = true;
        private boolean allowsInnerDistinct = true;
        private boolean supportsGroupByExpressions = true;
        private boolean supportsGroupingSets = false;
        private boolean requiresGroupByAlias = false;
        private boolean allowsSelectNotInGroupBy = false;
        private boolean requiresOrderByAlias = false;
        private boolean allowsOrderByAlias = true;
        private boolean requiresUnionOrderByOrdinal = true;
        private boolean requiresUnionOrderByExprInSelect = true;
        private boolean requiresHavingAlias = false;
        private boolean supportsUnlimitedValueList = false;
        private boolean supportsMultiValueInExpr = false;
        private boolean supportsDdl = true;
        private boolean allowsRegularExpressionInWhereClause = false;
        private final EnumSet<BitOperation> supportedBitAggregations = EnumSet.noneOf(BitOperation.class);
        private boolean supportsPercentileCont = false;
        private boolean supportsPercentileDisc = false;
        private boolean supportsListAgg = false;
        private int maxColumnNameLength = 128;
        private boolean allowsDialectSharing = true;
        private boolean requiresDrillthroughMaxRowsInLimit = false;
        private boolean supportsParallelLoading = true;
        private boolean supportsBatchOperations = true;
        private org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding caseFolding = org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.UPPER;
        private org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy quotingPolicy = org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS;

        public Builder quoteIdentifierString(String quoteIdentifierString) {
            this.quoteIdentifierString = quoteIdentifierString;
            return this;
        }

        public Builder dialectName(String dialectName) {
            this.dialectName = dialectName;
            return this;
        }

        public Builder allowsFromAlias(boolean allowsFromAlias) {
            this.allowsFromAlias = allowsFromAlias;
            return this;
        }

        public Builder allowsFromQuery(boolean allowsFromQuery) {
            this.allowsFromQuery = allowsFromQuery;
            return this;
        }

        public Builder requiresAliasForFromQuery(boolean requiresAliasForFromQuery) {
            this.requiresAliasForFromQuery = requiresAliasForFromQuery;
            return this;
        }

        public Builder allowsJoinOn(boolean allowsJoinOn) {
            this.allowsJoinOn = allowsJoinOn;
            return this;
        }

        public Builder allowsFieldAlias(boolean allowsFieldAlias) {
            this.allowsFieldAlias = allowsFieldAlias;
            return this;
        }

        public Builder allowsCountDistinct(boolean allowsCountDistinct) {
            this.allowsCountDistinct = allowsCountDistinct;
            return this;
        }

        public Builder allowsMultipleCountDistinct(boolean allowsMultipleCountDistinct) {
            this.allowsMultipleCountDistinct = allowsMultipleCountDistinct;
            return this;
        }

        public Builder allowsCompoundCountDistinct(boolean allowsCompoundCountDistinct) {
            this.allowsCompoundCountDistinct = allowsCompoundCountDistinct;
            return this;
        }

        public Builder allowsCountDistinctWithOtherAggs(boolean allowsCountDistinctWithOtherAggs) {
            this.allowsCountDistinctWithOtherAggs = allowsCountDistinctWithOtherAggs;
            return this;
        }

        public Builder allowsMultipleDistinctSqlMeasures(boolean allowsMultipleDistinctSqlMeasures) {
            this.allowsMultipleDistinctSqlMeasures = allowsMultipleDistinctSqlMeasures;
            return this;
        }

        public Builder allowsInnerDistinct(boolean allowsInnerDistinct) {
            this.allowsInnerDistinct = allowsInnerDistinct;
            return this;
        }

        public Builder supportsGroupByExpressions(boolean supportsGroupByExpressions) {
            this.supportsGroupByExpressions = supportsGroupByExpressions;
            return this;
        }

        public Builder supportsGroupingSets(boolean supportsGroupingSets) {
            this.supportsGroupingSets = supportsGroupingSets;
            return this;
        }

        public Builder requiresGroupByAlias(boolean requiresGroupByAlias) {
            this.requiresGroupByAlias = requiresGroupByAlias;
            return this;
        }

        public Builder allowsSelectNotInGroupBy(boolean allowsSelectNotInGroupBy) {
            this.allowsSelectNotInGroupBy = allowsSelectNotInGroupBy;
            return this;
        }

        public Builder requiresOrderByAlias(boolean requiresOrderByAlias) {
            this.requiresOrderByAlias = requiresOrderByAlias;
            return this;
        }

        public Builder allowsOrderByAlias(boolean allowsOrderByAlias) {
            this.allowsOrderByAlias = allowsOrderByAlias;
            return this;
        }

        public Builder requiresUnionOrderByOrdinal(boolean requiresUnionOrderByOrdinal) {
            this.requiresUnionOrderByOrdinal = requiresUnionOrderByOrdinal;
            return this;
        }

        public Builder requiresUnionOrderByExprInSelect(boolean requiresUnionOrderByExprInSelect) {
            this.requiresUnionOrderByExprInSelect = requiresUnionOrderByExprInSelect;
            return this;
        }

        public Builder requiresHavingAlias(boolean requiresHavingAlias) {
            this.requiresHavingAlias = requiresHavingAlias;
            return this;
        }

        public Builder supportsUnlimitedValueList(boolean supportsUnlimitedValueList) {
            this.supportsUnlimitedValueList = supportsUnlimitedValueList;
            return this;
        }

        public Builder supportsMultiValueInExpr(boolean supportsMultiValueInExpr) {
            this.supportsMultiValueInExpr = supportsMultiValueInExpr;
            return this;
        }

        public Builder supportsDdl(boolean supportsDdl) {
            this.supportsDdl = supportsDdl;
            return this;
        }

        public Builder allowsRegularExpressionInWhereClause(boolean allowsRegularExpressionInWhereClause) {
            this.allowsRegularExpressionInWhereClause = allowsRegularExpressionInWhereClause;
            return this;
        }

        public Builder supportsBitAndAgg(boolean supported) {
            return setBitAggregation(BitOperation.AND, supported);
        }

        public Builder supportsBitOrAgg(boolean supported) {
            return setBitAggregation(BitOperation.OR, supported);
        }

        public Builder supportsBitXorAgg(boolean supported) {
            return setBitAggregation(BitOperation.XOR, supported);
        }

        public Builder supportsBitNAndAgg(boolean supported) {
            return setBitAggregation(BitOperation.NAND, supported);
        }

        public Builder supportsBitNOrAgg(boolean supported) {
            return setBitAggregation(BitOperation.NOR, supported);
        }

        public Builder supportsBitNXorAgg(boolean supported) {
            return setBitAggregation(BitOperation.NXOR, supported);
        }

        public Builder supportsBitAggregations(Set<BitOperation> operations) {
            this.supportedBitAggregations.clear();
            this.supportedBitAggregations.addAll(operations);
            return this;
        }

        private Builder setBitAggregation(BitOperation op, boolean supported) {
            if (supported) {
                this.supportedBitAggregations.add(op);
            } else {
                this.supportedBitAggregations.remove(op);
            }
            return this;
        }

        public Builder supportsPercentileCont(boolean supportsPercentileCont) {
            this.supportsPercentileCont = supportsPercentileCont;
            return this;
        }

        public Builder supportsPercentileDisc(boolean supportsPercentileDisc) {
            this.supportsPercentileDisc = supportsPercentileDisc;
            return this;
        }

        public Builder supportsListAgg(boolean supportsListAgg) {
            this.supportsListAgg = supportsListAgg;
            return this;
        }

        public Builder maxColumnNameLength(int maxColumnNameLength) {
            this.maxColumnNameLength = maxColumnNameLength;
            return this;
        }

        public Builder allowsDialectSharing(boolean allowsDialectSharing) {
            this.allowsDialectSharing = allowsDialectSharing;
            return this;
        }

        public Builder requiresDrillthroughMaxRowsInLimit(boolean requiresDrillthroughMaxRowsInLimit) {
            this.requiresDrillthroughMaxRowsInLimit = requiresDrillthroughMaxRowsInLimit;
            return this;
        }

        public Builder supportsParallelLoading(boolean supportsParallelLoading) {
            this.supportsParallelLoading = supportsParallelLoading;
            return this;
        }

        public Builder supportsBatchOperations(boolean supportsBatchOperations) {
            this.supportsBatchOperations = supportsBatchOperations;
            return this;
        }

        public Builder caseFolding(org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding caseFolding) {
            this.caseFolding = (caseFolding == null)
                    ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierCaseFolding.UPPER
                    : caseFolding;
            return this;
        }

        public Builder quotingPolicy(org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy policy) {
            this.quotingPolicy = (policy == null)
                    ? org.eclipse.daanse.jdbc.db.dialect.api.IdentifierQuotingPolicy.ALWAYS
                    : policy;
            return this;
        }

        public ConfigurableDialect build() {
            return new ConfigurableDialect(this);
        }
    }
}
