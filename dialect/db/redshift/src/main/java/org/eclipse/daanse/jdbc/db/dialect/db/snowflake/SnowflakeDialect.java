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
package org.eclipse.daanse.jdbc.db.dialect.db.snowflake;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.dialect.api.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.jdbc.db.dialect.db.common.Util;

public class SnowflakeDialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "SNOWFLAKE";

    public SnowflakeDialect(Connection connection) {
        super(connection);
    }

    @Override
    public String getQuoteIdentifierString() {
        return "\"";
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, null, false);
    }

    @Override
    public void quoteStringLiteral(StringBuilder buf, String s) {
        Util.singleQuoteString(s.replace("\\\\", "\\\\\\\\"), buf);
    }

    @Override
    public boolean allowsOrderByAlias() {
        return true;
    }

    @Override
    public boolean allowsSelectNotInGroupBy() {
        return false;
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return true;
    }

    @Override
    public BestFitColumnType getType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        final int scale = metaData.getScale(columnIndex + 1);
        final int columnType = metaData.getColumnType(columnIndex + 1);

        if ((columnType == Types.NUMERIC || columnType == Types.DECIMAL) && scale != 0) {
            logTypeInfo(metaData, columnIndex, BestFitColumnType.DECIMAL);
            return BestFitColumnType.DECIMAL;
        }
        return super.getType(metaData, columnIndex);
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

        StringBuilder mappedFlags = new StringBuilder();
        // Snowflake regex allowed inline modifiers
        // https://docs.snowflake.net/manuals/sql-reference/functions-regexp.html
        String[][] mapping = new String[][] { { "c", "c" }, { "i", "i" }, { "m", "m" }, { "s", "s" } };
        javaRegex = extractEmbeddedFlags(javaRegex, mapping, mappedFlags);

        final StringBuilder sb = new StringBuilder();
        sb.append(" RLIKE ( ");
        sb.append(source);
        sb.append(", ");
        quoteStringLiteral(sb, javaRegex);
        if (mappedFlags.length() > 0) {
            sb.append(", ");
            quoteStringLiteral(sb, mappedFlags.toString());
        }
        sb.append(")");
        return sb;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }
}
