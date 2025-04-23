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
package org.eclipse.daanse.jdbc.db.dialect.db.oracle;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the Oracle database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class OracleDialect extends JdbcDialectImpl {

    private static final String ESCAPE_REGEXP = "(\\\\Q([^\\\\Q]+)\\\\E)";
    private static final Pattern escapePattern = Pattern.compile(ESCAPE_REGEXP);
    private static final String SUPPORTED_PRODUCT_NAME = "ORACLE";

    public OracleDialect(MetaInfo metaInfo) {
        super(metaInfo);
    }

    @Override
    public boolean allowsAs() {
        return false;
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, " from dual", false);
    }

    @Override
    public boolean supportsGroupingSets() {
        return true;
    }

    @Override
    public StringBuilder generateOrderByNulls(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        return generateOrderByNullsAnsi(expr, ascending, collateNullsLast);
    }

    @Override
    public boolean allowsJoinOn() {
        return false;
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
        StringBuilder mappedFlags = new StringBuilder();
        String[][] mapping = new String[][] { { "c", "c" }, { "i", "i" }, { "m", "m" } };
        javaRegex = extractEmbeddedFlags(javaRegex, mapping, mappedFlags);

        final Matcher escapeMatcher = escapePattern.matcher(javaRegex);
        while (escapeMatcher.find()) {
            javaRegex = javaRegex.replace(escapeMatcher.group(1), escapeMatcher.group(2));
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(source);
        sb.append(" IS NOT NULL AND ");
        sb.append("REGEXP_LIKE(");
        sb.append(source);
        sb.append(", ");
        quoteStringLiteral(sb, javaRegex);
        sb.append(", ");
        quoteStringLiteral(sb, mappedFlags.toString());
        sb.append(")");
        return sb;
    }

    /**
     * Chooses the most appropriate type for accessing the values of a column in a
     * result set.
     *
     * The OracleDialect implementation handles some of the specific quirks of
     * Oracle: e.g. scale = -127 has special meaning with NUMERIC types and may
     * indicate a FLOAT value if precision is non-zero.
     *
     * @param metaData    Resultset metadata
     * @param columnIndex index of the column in the result set
     * @return For Types.NUMERIC and Types.DECIMAL, getType() will return a
     *         Type.INT, Type.DOUBLE, or Type.OBJECT based on scale, precision, and
     *         column name.
     *
     * @throws SQLException
     */
    @Override
    public BestFitColumnType getType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        final int columnType = metaData.getColumnType(columnIndex + 1);
        final int precision = metaData.getPrecision(columnIndex + 1);
        final int scale = metaData.getScale(columnIndex + 1);
        final String columnName = metaData.getColumnName(columnIndex + 1);
        BestFitColumnType type;

        if (columnType == Types.NUMERIC || columnType == Types.DECIMAL) {
            type = getNumericDecimalType(columnType, precision, scale, columnName);
        } else {
            type = super.getType(metaData, columnIndex);
        }
        logTypeInfo(metaData, columnIndex, type);
        return type;
    }

    private BestFitColumnType getNumericDecimalType(final int columnType, final int precision, final int scale, final String columnName) {
        if (scale == -127 && precision != 0) {
            // non zero precision w/ -127 scale means float in Oracle.
            return BestFitColumnType.DOUBLE;
        } else if (columnType == Types.NUMERIC && (scale == 0 || scale == -127) && precision == 0
            && columnName.startsWith("m")) {
            // In GROUPING SETS queries, Oracle
            // loosens the type of columns compared to mere GROUP BY
            // queries. We need integer GROUP BY columns to remain integers,
            // otherwise the segments won't be found; but if we convert
            // measure (whose column names are like "m0", "m1") to integers,
            // data loss will occur.
            return BestFitColumnType.OBJECT;
        } else if (scale == -127 && precision == 0) {
            return BestFitColumnType.INT;
        } else if (scale == 0 && (precision == 38 || precision == 0)) {
            // NUMBER(38, 0) is conventionally used in
            // Oracle for integers of unspecified precision, so let's be
            // bold and assume that they can fit into an int.
            return BestFitColumnType.INT;
        } else if (scale == 0 && precision <= 9) {
            // An int (up to 2^31 = 2.1B) can hold any NUMBER(10, 0) value
            // (up to 10^9 = 1B).
            return BestFitColumnType.INT;
        } else {
            return BestFitColumnType.DOUBLE;
        }
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

    @Override
    public StringBuilder generateAndBitAggregation(CharSequence operand) {
    	StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_AND_AGG(").append(operand).append(")");
        return buf;

    }

    @Override
    public StringBuilder generateOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_OR_AGG(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_XOR(").append(operand).append(")");
        return buf;
    }

}
