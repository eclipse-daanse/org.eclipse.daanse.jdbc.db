/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * History:
 *  This files came from the mondrian project. Some of the Flies
 *  (mostly the Tests) did not have License Header.
 *  But the Project is EPL Header. 2002-2022 Hitachi Vantara.
 *
 * Contributors:
 *   Hitachi Vantara.
 *   SmartCity Jena - initial  Java 8, Junit5
 */
package org.eclipse.daanse.jdbc.db.dialect.db.clickhouse;

import java.sql.Connection;
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for ClickHouse
 */
public class ClickHouseDialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "CLICKHOUSE";

    public ClickHouseDialect(Connection connection) {
        super(connection);
    }

    @Override
    public boolean requiresDrillthroughMaxRowsInLimit() {
        return true;
    }

    @Override
    public void quoteStringLiteral(
        StringBuilder buf,
        String s) {
        buf.append('\'');

        String s0 = s.replace("\\", "\\\\");
        s0 = s0.replace("'", "\\'");
        buf.append(s0);

        buf.append('\'');
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

    @Override
    public StringBuilder generateAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("groupBitAnd(").append(operand).append(")");
        return buf;

    }

    @Override
    public StringBuilder generateOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("groupBitOr(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("groupBitXor(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(groupBitAnd(").append(operand).append("))");
        return buf;

    }

    @Override
    public StringBuilder generateNOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(groupBitOr(").append(operand).append("))");
        return buf;
    }

    @Override
    public StringBuilder generateNXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(groupBitXor(").append(operand).append("))");
        return buf;
    }

    public boolean supportsBitAndAgg() {
        return true;
    }

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
    public StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator, String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("groupArrayArray");
        buf.append("( ");
        buf.append(operand);
        buf.append(")");
        //groupArrayArray(page_visits)
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("nth_value");
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
    public boolean supportsListAgg() {
        return true;
    }
}
