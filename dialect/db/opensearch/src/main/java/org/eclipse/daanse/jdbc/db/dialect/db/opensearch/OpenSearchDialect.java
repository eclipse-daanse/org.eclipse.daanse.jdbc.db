/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.dialect.db.opensearch;

import java.sql.Connection;

import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

public class OpenSearchDialect extends JdbcDialectImpl {

    private static final String SUPPORTED_PRODUCT_NAME = "OPENSEARCH";

    public OpenSearchDialect(Connection connection) {
        super(connection);
    }

    @Override
    public StringBuilder generateOrderByNulls(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        return generateOrderByNullsAnsi(expr, ascending, collateNullsLast);
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }
}
