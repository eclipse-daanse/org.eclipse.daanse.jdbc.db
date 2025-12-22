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

package org.eclipse.daanse.jdbc.db.dialect.db.postgresql;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.dialect.api.type.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the PostgreSQL database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class PostgreSqlDialect extends JdbcDialectImpl {

    public PostgreSqlDialect(Connection connection) {
        super(connection);
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    protected StringBuilder generateOrderByNulls(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        // Support for "ORDER BY ... NULLS LAST" was introduced in Postgres 8.3.
        if (productVersion.compareTo("8.3") >= 0) {
            return generateOrderByNullsAnsi(expr, ascending, collateNullsLast);
        } else {
            return super.generateOrderByNulls(expr, ascending, collateNullsLast);
        }
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return true;
    }

    @Override
    public StringBuilder generateRegularExpression(String source, String javaRegex) {
        try {
            Pattern.compile(javaRegex);
        } catch (PatternSyntaxException e) {
            // Not a valid Java regex. Too risky to continue.
            return null;
        }
        javaRegex = DialectUtil.cleanUnicodeAwareCaseFlag(javaRegex);
        javaRegex = javaRegex.replace("\\Q", "");
        javaRegex = javaRegex.replace("\\E", "");
        final StringBuilder sb = new StringBuilder();
        sb.append("cast(");
        sb.append(source);
        sb.append(" as text) is not null and ");
        sb.append("cast(");
        sb.append(source);
        sb.append(" as text) ~ ");
        quoteStringLiteral(sb, javaRegex);
        return sb;
    }

    @Override
    public BestFitColumnType getType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        final int precision = metaData.getPrecision(columnIndex + 1);
        final int scale = metaData.getScale(columnIndex + 1);
        final int columnType = metaData.getColumnType(columnIndex + 1);
        final String columnName = metaData.getColumnName(columnIndex + 1);

        // Check for column names starting with "m" (measure columns like "m0", "m1"):
        // In GROUPING SETS queries, Greenplum/PostgreSQL may loosen type metadata for measure
        // columns. Using OBJECT avoids data loss when converting measure values. This pattern
        // matches the handling in OracleDialect.getNumericDecimalType() for similar cases.
        // The "m" prefix check identifies measure columns in aggregation queries.
        if (columnType == Types.NUMERIC && scale == 0 && precision == 0 && columnName.startsWith("m")) {
            // In Greenplum, NUMBER/NUMERIC with no precision or scale means floating point.
            // Using OBJECT is safer than DOUBLE to avoid potential precision issues.
            logTypeInfo(metaData, columnIndex, BestFitColumnType.OBJECT);
            return BestFitColumnType.OBJECT;
        }
        return super.getType(metaData, columnIndex);
    }

    @Override
    public String getDialectName() {
        return "postgres";
    }

    @Override
    protected boolean deduceSupportsNullsOrdering(java.sql.DatabaseMetaData metaData) throws SQLException {
        // Support for "ORDER BY ... NULLS LAST" was introduced in Postgres 8.3.
        return productVersion != null && productVersion.compareTo("8.3") >= 0;
    }

    // Unified BitOperation methods

    @Override
    public StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        return switch (operation) {
            case AND -> buf.append("bit_and(").append(operand).append(")");
            case OR -> buf.append("bit_or(").append(operand).append(")");
            case XOR -> buf.append("bit_xor(").append(operand).append(")");
            case NAND -> buf.append("NOT(bit_and(").append(operand).append("))");
            case NOR -> buf.append("NOT(bit_or(").append(operand).append("))");
            case NXOR -> buf.append("NOT(bit_xor(").append(operand).append("))");
        };
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return true; // PostgreSQL supports all bit operations
    }

    @Override
    public StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName) {
        return buildPercentileFunction("PERCENTILE_DISC", percentile, desc, tableName, columnName);
    }

    @Override
    public StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName) {
        return buildPercentileFunction("PERCENTILE_CONT", percentile, desc, tableName, columnName);
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        return buildNthValueFunction("NTH_VALUE", operand, ignoreNulls, n, columns, false);
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
    public boolean supportsNthValue() {
        return true;
    }

}
