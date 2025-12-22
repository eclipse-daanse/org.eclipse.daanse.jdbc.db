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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the Microsoft SQL Server
 * database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class MicrosoftSqlServerDialect extends JdbcDialectImpl {

    private final DateFormat df =
        new SimpleDateFormat("yyyyMMdd");

    private static final String SUPPORTED_PRODUCT_NAME = "MSSQL";

    public MicrosoftSqlServerDialect(Connection connection) {
        super(connection);
    }

    @Override
    public StringBuilder generateInline(
        List<String> columnNames,
        List<String> columnTypes,
        List<String[]> valueList) {
        return generateInlineGeneric(
            columnNames, columnTypes, valueList, null, false);
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
        if (!boolLiteral.equalsIgnoreCase("TRUE")
            && !(boolLiteral.equalsIgnoreCase("FALSE"))
            && !(boolLiteral.equalsIgnoreCase("1"))
            && !(boolLiteral.equalsIgnoreCase("0"))) {
            throw new NumberFormatException(
                "Illegal BOOLEAN literal:  " + value);
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
    protected void quoteTimestampLiteral(
        StringBuilder buf,
        String value,
        Timestamp timestamp) {
        buf.append("CONVERT(datetime, '");
        buf.append(timestamp.toString());
        // Format 120 is equivalent to "yyyy-mm-dd hh:mm:ss" in Java.
        // See http://msdn.microsoft.com/en-us/library/ms187928.aspx
        buf.append("', 120)");
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
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
    public boolean supportsPercentileDisc() {
        return true;
    }

    @Override
    public boolean supportsPercentileCont() {
        return true;
    }

    @Override
    public StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator, String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
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
        //STRING_AGG(CONVERT (NVARCHAR (MAX), EmailAddress), ';') WITHIN GROUP (ORDER BY EmailAddress ASC)
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        return buildNthValueFunction("NTH_VALUE", operand, ignoreNulls, n, columns, true);
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

}
