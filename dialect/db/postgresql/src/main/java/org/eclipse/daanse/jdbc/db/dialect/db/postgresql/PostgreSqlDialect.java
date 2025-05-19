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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the PostgreSQL database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class PostgreSqlDialect extends JdbcDialectImpl {

    public PostgreSqlDialect(MetaInfo metaInfo) {
        super(metaInfo);
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

        // TODO - Do we need the check for "m"??
        if (columnType == Types.NUMERIC && scale == 0 && precision == 0 && columnName.startsWith("m")) {
            // In Greenplum NUMBER/NUMERIC w/ no precision or
            // scale means floating point.
            logTypeInfo(metaData, columnIndex, BestFitColumnType.OBJECT);
            return BestFitColumnType.OBJECT; // TODO - can this be DOUBLE?
        }
        return super.getType(metaData, columnIndex);
    }

    @Override
    public String getDialectName() {
        return "postgres";
    }

    @Override
    public StringBuilder generateAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("bit_and(").append(operand).append(")");
        return buf;

    }

    @Override
    public StringBuilder generateOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("bit_or(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("bit_xor(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(bit_and(").append(operand).append("))");
        return buf;
    }

    @Override
    public StringBuilder generateNOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(bit_or(").append(operand).append("))");
        return buf;
    }

    @Override
    public StringBuilder generateNXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(bit_xor(").append(operand).append("))");
        return buf;
    }

    @Override
    public boolean supportsBitAndAgg() {
        return true;
    }

    @Override
    public boolean supportsBitOrAgg() {
        return true;
    }

    @Override
    public boolean supportsBitXorAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNAndAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNOrAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNXorAgg() {
        return true;
    }

    @Override
    public StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("PERCENTILE_DISC(").append(percentile).append(")").append(" WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" ").append(DESC);
        }
        buf.append(")");
        return buf;
    }

    @Override
    public StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("PERCENTILE_CONT(").append(percentile).append(")").append(" WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" ").append(DESC);
        }
        buf.append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NTH_VALUE");
        buf.append("( ");
        buf.append(operand);
        buf.append(", ");
        if (n == null || n < 1) {
            buf.append(1);
        } else {
            buf.append(n);
        }
        buf.append(" )");
        buf.append("OVER ( ");
        if (columns != null && !columns.isEmpty()) {
            buf.append("ORDER BY ");
            buf.append(orderedColumns(columns));
        }
        buf.append(" )");
        //NTH_VALUE(employee_name, 2) OVER ( ORDER BY salary DESC )
        return buf;
    }

    private CharSequence orderedColumns(List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        boolean first = true;
        if (columns != null) {
            for(OrderedColumn c : columns) {
                if (!first) {
                    buf.append(", ");
                }
                if (c.getTableName() != null) {
                    quoteIdentifier(buf, c.getTableName(), c.getColumnName());
                } else {
                    quoteIdentifier(buf, c.getColumnName());
                }
                if (!c.isAscend()) {
                    buf.append(DESC);
                }
                first = false;
            }
        }
        return buf;
    }

    @Override
    public boolean supportsPercentileDisc() {
        return true;
    }

    @Override
    public boolean supportsPercentileCont() {
        return true;
    }

}
