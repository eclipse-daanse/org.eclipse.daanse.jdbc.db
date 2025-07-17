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
package org.eclipse.daanse.jdbc.db.dialect.db.hive;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.jdbc.db.dialect.db.common.Util;

/**
 * Implementation of {@link Dialect} for the Hive database.
 *
 * @author Hongwei Fu
 * @since Jan 10, 2011
 */
public class HiveDialect extends JdbcDialectImpl {
    private static final int MAX_COLUMN_NAME_LENGTH = 128;

    private static final String SUPPORTED_PRODUCT_NAME = "HIVE";

    public HiveDialect(Connection connection) {
        super(connection);
    }

    @Override
    protected String deduceIdentifierQuoteString(DatabaseMetaData metaData) {
        return null;
    }

    @Override
    public boolean supportsResultSetConcurrency(
        int type,
        int concurrency) {
            return false;
    }

    @Override
    protected boolean deduceReadOnly(DatabaseMetaData metaData) throws SQLException {
        return metaData.isReadOnly();
    }

    @Override
    protected int deduceMaxColumnNameLength(DatabaseMetaData metaData) throws SQLException {
        return metaData.getMaxCatalogNameLength();
    }

    @Override
    public boolean allowsCompoundCountDistinct() {
        return true;
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    public boolean requiresOrderByAlias() {
        return true;
    }

    @Override
    public boolean allowsOrderByAlias() {
        return true;
    }

    @Override
    public boolean requiresGroupByAlias() {
        return false;
    }

    @Override
    public boolean requiresUnionOrderByExprToBeInSelectClause() {
        return false;
    }

    @Override
    public boolean requiresUnionOrderByOrdinal() {
        return false;
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return new StringBuilder("select * from (")
            .append(generateInlineGeneric(columnNames, columnTypes, valueList, " from dual", false))
            .append(") x limit ").append(valueList.size());
    }

    @Override
    protected void quoteDateLiteral(StringBuilder buf, Date date) {
        // Hive doesn't support Date type; treat date as a string '2008-01-23'
        Util.singleQuoteString(date.toString(), buf);
    }

    @Override
    protected StringBuilder generateOrderByNulls(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        // In Hive, Null values are worth negative infinity.
        if (collateNullsLast) {
            if (ascending) {
                return new StringBuilder("ISNULL(").append(expr)
                    .append(") ASC, ").append(expr).append(" ASC");
            } else {
                return new StringBuilder(expr).append(" DESC");
            }
        } else {
            if (ascending) {
                return new StringBuilder(expr).append(" ASC");
            } else {
                return new StringBuilder("ISNULL(").append(expr)
                    .append(") DESC, ").append(expr).append(" DESC");
            }
        }
    }

    @Override
    public boolean allowsAs() {
        return false;
    }

    @Override
    public boolean allowsJoinOn() {
        return false;
    }

    @Override
    public void quoteTimestampLiteral(StringBuilder buf, String value) {
        try {
            Timestamp.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new NumberFormatException("Illegal TIMESTAMP literal:  " + value);
        }
        buf.append("cast( ");
        Util.singleQuoteString(value, buf);
        buf.append(" as timestamp )");
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

}
