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

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the H2 database.
 */
public class H2Dialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "H2";

    public H2Dialect(MetaInfo metaInfo) {
        super(metaInfo);
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
