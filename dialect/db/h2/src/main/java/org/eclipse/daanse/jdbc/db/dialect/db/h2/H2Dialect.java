/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.db.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the H2 database.
 */
public class H2Dialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "H2";

    public H2Dialect(Connection connection) {
        super(connection);
    }


    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

    @Override
    protected boolean deduceSupportsNullsOrdering(DatabaseMetaData metaData) throws SQLException {
        return true; // H2 supports NULLS FIRST/LAST
    }

    // Unified BitOperation methods

    @Override
    public StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        return switch (operation) {
            case AND -> buf.append("BIT_AND_AGG(").append(operand).append(")");
            case OR -> buf.append("BIT_OR_AGG(").append(operand).append(")");
            case XOR -> buf.append("BIT_XOR_AGG(").append(operand).append(")");
            case NAND -> buf.append("BIT_NAND_AGG(").append(operand).append(")");
            case NOR -> buf.append("BIT_NOR_AGG(").append(operand).append(")");
            case NXOR -> buf.append("BIT_XNOR_AGG(").append(operand).append(")");
        };
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return true; // H2 supports all bit operations
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
    public StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator, String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("LISTAGG");
        buf.append("( ");
        if (distinct) {
            buf.append("DISTINCT ");
        }
        if (coalesce != null) {
            buf.append("COALESCE(").append(operand).append(", '").append(coalesce).append("')");
        } else {
            buf.append(operand);
        }
        buf.append(", '");
        if (separator != null) {
            buf.append(separator);
        } else {
            buf.append(", ");
        }
        buf.append("'");
        if (onOverflowTruncate != null) {
            buf.append(" ON OVERFLOW TRUNCATE '").append(onOverflowTruncate).append("' WITHOUT COUNT)");
        } else {
            buf.append(")");
        }
        if (columns != null && !columns.isEmpty()) {
            buf.append(" WITHIN GROUP (ORDER BY ");
            buf.append(buildOrderedColumnsClause(columns));
            buf.append(")");
        }
        //LISTAGG(NAME, ', ') WITHIN GROUP (ORDER BY ID)
        //LISTAGG(COALESCE(NAME, 'null'), ', ') WITHIN GROUP (ORDER BY ID)
        //LISTAGG(ID, ', ') WITHIN GROUP (ORDER BY ID) OVER (ORDER BY ID)
        //LISTAGG(ID, ';' ON OVERFLOW TRUNCATE 'etc' WITHOUT COUNT) WITHIN GROUP (ORDER BY ID)
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        return buildNthValueFunction("NTH_VALUE", operand, ignoreNulls, n, columns, true);
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

    @Override
    public boolean supportsNthValueIgnoreNulls() {
        return true;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }

}
