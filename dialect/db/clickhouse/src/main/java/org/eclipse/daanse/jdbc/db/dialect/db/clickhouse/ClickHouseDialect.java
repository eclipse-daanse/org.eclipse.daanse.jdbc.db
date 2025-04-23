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

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for ClickHouse
 */
public class ClickHouseDialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "CLICKHOUSE";

    public ClickHouseDialect(MetaInfo metaInfo) {
        super(metaInfo);
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

}
