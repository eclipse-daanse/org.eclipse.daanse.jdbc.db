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
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.jdbc.db.dialect.api.type.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.type.Datatype;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;

/**
 * A configurable SQL dialect implementation that reads all settings from
 * configuration.
 * <p>
 * This dialect can be configured either programmatically via the builder or via
 * OSGi configuration using {@link ConfigurableDialectConfig}.
 */
public class ConfigurableDialect implements Dialect {

    private final String quoteIdentifierString;
    private final String dialectName;
    private final boolean allowsAs;
    private final boolean allowsFromQuery;
    private final boolean requiresAliasForFromQuery;
    private final boolean allowsJoinOn;
    private final boolean allowsFieldAs;
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
    private final boolean requiresUnionOrderByExprToBeInSelectClause;
    private final boolean requiresHavingAlias;
    private final boolean supportsUnlimitedValueList;
    private final boolean supportsMultiValueInExpr;
    private final boolean allowsDdl;
    private final boolean allowsRegularExpressionInWhereClause;
    private final boolean supportsBitAndAgg;
    private final boolean supportsBitOrAgg;
    private final boolean supportsBitXorAgg;
    private final boolean supportsBitNAndAgg;
    private final boolean supportsBitNOrAgg;
    private final boolean supportsBitNXorAgg;
    private final boolean supportsPercentileContAgg;
    private final boolean supportsPercentileDiscAgg;
    private final boolean supportsPercentileCont;
    private final boolean supportsPercentileDisc;
    private final boolean supportsListAgg;
    private final int maxColumnNameLength;
    private final boolean allowsDialectSharing;
    private final boolean requiresDrillthroughMaxRowsInLimit;
    private final boolean supportParallelLoading;
    private final boolean supportBatchOperations;

    /**
     * Creates a ConfigurableDialect from an OSGi configuration.
     *
     * @param config the OSGi metatype configuration
     */
    public ConfigurableDialect(ConfigurableDialectConfig config) {
        this.quoteIdentifierString = config.quoteIdentifierString();
        this.dialectName = config.dialectName();
        this.allowsAs = config.allowsAs();
        this.allowsFromQuery = config.allowsFromQuery();
        this.requiresAliasForFromQuery = config.requiresAliasForFromQuery();
        this.allowsJoinOn = config.allowsJoinOn();
        this.allowsFieldAs = config.allowsFieldAs();
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
        this.requiresUnionOrderByExprToBeInSelectClause = config.requiresUnionOrderByExprToBeInSelectClause();
        this.requiresHavingAlias = config.requiresHavingAlias();
        this.supportsUnlimitedValueList = config.supportsUnlimitedValueList();
        this.supportsMultiValueInExpr = config.supportsMultiValueInExpr();
        this.allowsDdl = config.allowsDdl();
        this.allowsRegularExpressionInWhereClause = config.allowsRegularExpressionInWhereClause();
        this.supportsBitAndAgg = config.supportsBitAndAgg();
        this.supportsBitOrAgg = config.supportsBitOrAgg();
        this.supportsBitXorAgg = config.supportsBitXorAgg();
        this.supportsBitNAndAgg = config.supportsBitNAndAgg();
        this.supportsBitNOrAgg = config.supportsBitNOrAgg();
        this.supportsBitNXorAgg = config.supportsBitNXorAgg();
        this.supportsPercentileContAgg = config.supportsPercentileContAgg();
        this.supportsPercentileDiscAgg = config.supportsPercentileDiscAgg();
        this.supportsPercentileCont = config.supportsPercentileCont();
        this.supportsPercentileDisc = config.supportsPercentileDisc();
        this.supportsListAgg = config.supportsListAgg();
        this.maxColumnNameLength = config.maxColumnNameLength();
        this.allowsDialectSharing = config.allowsDialectSharing();
        this.requiresDrillthroughMaxRowsInLimit = config.requiresDrillthroughMaxRowsInLimit();
        this.supportParallelLoading = config.supportParallelLoading();
        this.supportBatchOperations = config.supportBatchOperations();
    }

    /**
     * Creates a ConfigurableDialect from a builder.
     *
     * @param builder the builder with configuration values
     */
    private ConfigurableDialect(Builder builder) {
        this.quoteIdentifierString = builder.quoteIdentifierString;
        this.dialectName = builder.dialectName;
        this.allowsAs = builder.allowsAs;
        this.allowsFromQuery = builder.allowsFromQuery;
        this.requiresAliasForFromQuery = builder.requiresAliasForFromQuery;
        this.allowsJoinOn = builder.allowsJoinOn;
        this.allowsFieldAs = builder.allowsFieldAs;
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
        this.requiresUnionOrderByExprToBeInSelectClause = builder.requiresUnionOrderByExprToBeInSelectClause;
        this.requiresHavingAlias = builder.requiresHavingAlias;
        this.supportsUnlimitedValueList = builder.supportsUnlimitedValueList;
        this.supportsMultiValueInExpr = builder.supportsMultiValueInExpr;
        this.allowsDdl = builder.allowsDdl;
        this.allowsRegularExpressionInWhereClause = builder.allowsRegularExpressionInWhereClause;
        this.supportsBitAndAgg = builder.supportsBitAndAgg;
        this.supportsBitOrAgg = builder.supportsBitOrAgg;
        this.supportsBitXorAgg = builder.supportsBitXorAgg;
        this.supportsBitNAndAgg = builder.supportsBitNAndAgg;
        this.supportsBitNOrAgg = builder.supportsBitNOrAgg;
        this.supportsBitNXorAgg = builder.supportsBitNXorAgg;
        this.supportsPercentileContAgg = builder.supportsPercentileContAgg;
        this.supportsPercentileDiscAgg = builder.supportsPercentileDiscAgg;
        this.supportsPercentileCont = builder.supportsPercentileCont;
        this.supportsPercentileDisc = builder.supportsPercentileDisc;
        this.supportsListAgg = builder.supportsListAgg;
        this.maxColumnNameLength = builder.maxColumnNameLength;
        this.allowsDialectSharing = builder.allowsDialectSharing;
        this.requiresDrillthroughMaxRowsInLimit = builder.requiresDrillthroughMaxRowsInLimit;
        this.supportParallelLoading = builder.supportParallelLoading;
        this.supportBatchOperations = builder.supportBatchOperations;
    }

    /**
     * Creates a new builder for programmatic configuration.
     *
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
    public StringBuilder quoteIdentifier(CharSequence val) {
        StringBuilder buf = new StringBuilder(val.length() + 10);
        quoteIdentifier(val.toString(), buf);
        return buf;
    }

    @Override
    public void quoteIdentifier(String val, StringBuilder buf) {
        String q = getQuoteIdentifierString();
        if (q == null || (val.startsWith(q) && val.endsWith(q))) {
            buf.append(val);
            return;
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
            datatype.quoteValue(buf, this, value.toString());
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
    public StringBuilder generateCountExpression(CharSequence exp) {
        return new StringBuilder(exp);
    }

    @Override
    public StringBuilder generateRegularExpression(String source, String javaRegExp) {
        // Default implementation returns null (not supported)
        return null;
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
    public StringBuilder generateUnionAllSql(List<Map<String, Map.Entry<Datatype, Object>>> valueList) {
        StringBuilder buf = new StringBuilder();
        for (Map<String, Map.Entry<Datatype, Object>> m : valueList) {
            buf.append(" UNION ALL SELECT ");
            boolean first = true;
            for (Map.Entry<String, Map.Entry<Datatype, Object>> en : m.entrySet()) {
                if (!first) {
                    buf.append(", ");
                }
                first = false;
                quote(buf, en.getValue().getValue(), en.getValue().getKey());
                if (allowsAs()) {
                    buf.append(" AS ");
                } else {
                    buf.append(' ');
                }
                quoteIdentifier(en.getKey(), buf);
            }
        }
        return buf;
    }

    // ========== Bit Aggregation Functions ==========

    @Override
    public StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand) {
        if (!supportsBitAggregation(operation)) {
            throw new UnsupportedOperationException(operation + " bit aggregation not supported");
        }
        StringBuilder buf = new StringBuilder(64);
        return switch (operation) {
        case AND -> buf.append("BIT_AND(").append(operand).append(")");
        case OR -> buf.append("BIT_OR(").append(operand).append(")");
        case XOR -> buf.append("BIT_XOR(").append(operand).append(")");
        case NAND -> buf.append("BIT_NAND(").append(operand).append(")");
        case NOR -> buf.append("BIT_NOR(").append(operand).append(")");
        case NXOR -> buf.append("BIT_XNOR(").append(operand).append(")");
        };
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return switch (operation) {
        case AND -> supportsBitAndAgg;
        case OR -> supportsBitOrAgg;
        case XOR -> supportsBitXorAgg;
        case NAND -> supportsBitNAndAgg;
        case NOR -> supportsBitNOrAgg;
        case NXOR -> supportsBitNXorAgg;
        };
    }

    // ========== Percentile Functions ==========

    @Override
    public StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName) {
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
        return buf;
    }

    @Override
    public StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName) {
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
        return buf;
    }

    // ========== List Aggregation ==========

    @Override
    public StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator, String coalesce,
            String onOverflowTruncate, List<OrderedColumn> columns) {
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
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n,
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
        return buf;
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

    // ========== Hints ==========

    @Override
    public void appendHintsAfterFromClause(StringBuilder buf, Map<String, String> hints) {
        // Default: no hints
    }

    // ========== Feature Support Flags ==========

    @Override
    public String getDialectName() {
        return dialectName;
    }

    @Override
    public boolean allowsAs() {
        return allowsAs;
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
    public boolean allowsFieldAs() {
        return allowsFieldAs;
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
    public boolean requiresUnionOrderByExprToBeInSelectClause() {
        return requiresUnionOrderByExprToBeInSelectClause;
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
    public boolean allowsDdl() {
        return allowsDdl;
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return allowsRegularExpressionInWhereClause;
    }

    @Override
    public boolean supportsPercentileContAgg() {
        return supportsPercentileContAgg;
    }

    @Override
    public boolean supportsPercentileDiscAgg() {
        return supportsPercentileDiscAgg;
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
    public boolean supportParallelLoading() {
        return supportParallelLoading;
    }

    @Override
    public boolean supportBatchOperations() {
        return supportBatchOperations;
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

    /**
     * Builder for creating ConfigurableDialect instances programmatically.
     */
    public static class Builder {
        private String quoteIdentifierString = "\"";
        private String dialectName = "configurable";
        private boolean allowsAs = true;
        private boolean allowsFromQuery = true;
        private boolean requiresAliasForFromQuery = false;
        private boolean allowsJoinOn = true;
        private boolean allowsFieldAs = true;
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
        private boolean requiresUnionOrderByExprToBeInSelectClause = true;
        private boolean requiresHavingAlias = false;
        private boolean supportsUnlimitedValueList = false;
        private boolean supportsMultiValueInExpr = false;
        private boolean allowsDdl = true;
        private boolean allowsRegularExpressionInWhereClause = false;
        private boolean supportsBitAndAgg = false;
        private boolean supportsBitOrAgg = false;
        private boolean supportsBitXorAgg = false;
        private boolean supportsBitNAndAgg = false;
        private boolean supportsBitNOrAgg = false;
        private boolean supportsBitNXorAgg = false;
        private boolean supportsPercentileContAgg = false;
        private boolean supportsPercentileDiscAgg = false;
        private boolean supportsPercentileCont = false;
        private boolean supportsPercentileDisc = false;
        private boolean supportsListAgg = false;
        private int maxColumnNameLength = 128;
        private boolean allowsDialectSharing = true;
        private boolean requiresDrillthroughMaxRowsInLimit = false;
        private boolean supportParallelLoading = true;
        private boolean supportBatchOperations = true;

        public Builder quoteIdentifierString(String quoteIdentifierString) {
            this.quoteIdentifierString = quoteIdentifierString;
            return this;
        }

        public Builder dialectName(String dialectName) {
            this.dialectName = dialectName;
            return this;
        }

        public Builder allowsAs(boolean allowsAs) {
            this.allowsAs = allowsAs;
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

        public Builder allowsFieldAs(boolean allowsFieldAs) {
            this.allowsFieldAs = allowsFieldAs;
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

        public Builder requiresUnionOrderByExprToBeInSelectClause(boolean requiresUnionOrderByExprToBeInSelectClause) {
            this.requiresUnionOrderByExprToBeInSelectClause = requiresUnionOrderByExprToBeInSelectClause;
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

        public Builder allowsDdl(boolean allowsDdl) {
            this.allowsDdl = allowsDdl;
            return this;
        }

        public Builder allowsRegularExpressionInWhereClause(boolean allowsRegularExpressionInWhereClause) {
            this.allowsRegularExpressionInWhereClause = allowsRegularExpressionInWhereClause;
            return this;
        }

        public Builder supportsBitAndAgg(boolean supportsBitAndAgg) {
            this.supportsBitAndAgg = supportsBitAndAgg;
            return this;
        }

        public Builder supportsBitOrAgg(boolean supportsBitOrAgg) {
            this.supportsBitOrAgg = supportsBitOrAgg;
            return this;
        }

        public Builder supportsBitXorAgg(boolean supportsBitXorAgg) {
            this.supportsBitXorAgg = supportsBitXorAgg;
            return this;
        }

        public Builder supportsBitNAndAgg(boolean supportsBitNAndAgg) {
            this.supportsBitNAndAgg = supportsBitNAndAgg;
            return this;
        }

        public Builder supportsBitNOrAgg(boolean supportsBitNOrAgg) {
            this.supportsBitNOrAgg = supportsBitNOrAgg;
            return this;
        }

        public Builder supportsBitNXorAgg(boolean supportsBitNXorAgg) {
            this.supportsBitNXorAgg = supportsBitNXorAgg;
            return this;
        }

        public Builder supportsPercentileContAgg(boolean supportsPercentileContAgg) {
            this.supportsPercentileContAgg = supportsPercentileContAgg;
            return this;
        }

        public Builder supportsPercentileDiscAgg(boolean supportsPercentileDiscAgg) {
            this.supportsPercentileDiscAgg = supportsPercentileDiscAgg;
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

        public Builder supportParallelLoading(boolean supportParallelLoading) {
            this.supportParallelLoading = supportParallelLoading;
            return this;
        }

        public Builder supportBatchOperations(boolean supportBatchOperations) {
            this.supportBatchOperations = supportBatchOperations;
            return this;
        }

        public ConfigurableDialect build() {
            return new ConfigurableDialect(this);
        }
    }
}
