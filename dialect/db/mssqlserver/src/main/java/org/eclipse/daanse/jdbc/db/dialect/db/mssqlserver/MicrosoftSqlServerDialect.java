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
import org.eclipse.daanse.jdbc.db.dialect.api.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.jdbc.db.dialect.db.common.Util;

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
        buf.append(Util.singleQuoteString(value));
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
            boolean first = true;
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
            buf.append(")");
        }
        //STRING_AGG(CONVERT (NVARCHAR (MAX), EmailAddress), ';') WITHIN GROUP (ORDER BY EmailAddress ASC)
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
        if (ignoreNulls) {
            buf.append(" IGNORE NULLS ");
        } else {
            buf.append(" RESPECT NULLS ");
        }
        buf.append("OVER ( ");
        if (columns != null && !columns.isEmpty()) {
            buf.append("ORDER BY ");
            buf.append(orderedColumns(columns));
        }
        buf.append(" )");
        //NTH_VALUE(price,2) IGNORE NULLS OVER (ORDER BY id)
        //NTH_VALUE(price,2) IGNORE NULLS OVER ()
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
    public boolean supportsListAgg() {
        return true;
    }

}
