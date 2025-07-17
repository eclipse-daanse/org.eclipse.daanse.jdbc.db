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
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.OrderedColumn;
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
        buf.append("BIT_XOR_AGG(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_NAND_AGG(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_NOR_AGG(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_XNOR_AGG(").append(operand).append(")");
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
            if (columns != null) {
                buf.append(orderedColumns(columns));
            }
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
    public boolean supportsPercentileDisc() {
        return true;
    }

    @Override
    public boolean supportsPercentileCont() {
        return true;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }

}
