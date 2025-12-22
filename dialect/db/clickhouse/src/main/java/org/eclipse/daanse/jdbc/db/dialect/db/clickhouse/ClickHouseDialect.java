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

import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;
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

    // Unified BitOperation methods

    @Override
    public StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        return switch (operation) {
            case AND -> buf.append("groupBitAnd(").append(operand).append(")");
            case OR -> buf.append("groupBitOr(").append(operand).append(")");
            case XOR -> buf.append("groupBitXor(").append(operand).append(")");
            case NAND -> buf.append("NOT(groupBitAnd(").append(operand).append("))");
            case NOR -> buf.append("NOT(groupBitOr(").append(operand).append("))");
            case NXOR -> buf.append("NOT(groupBitXor(").append(operand).append("))");
        };
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return true; // ClickHouse supports all bit operations
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
        return buildNthValueFunction("nth_value", operand, ignoreNulls, n, columns, false);
    }

    @Override
    public boolean supportsNthValue() {
        return true;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }
}
