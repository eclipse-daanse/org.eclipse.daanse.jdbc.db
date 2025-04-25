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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
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

    public MicrosoftSqlServerDialect(MetaInfo metaInfo) {
        super(metaInfo);
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

}
