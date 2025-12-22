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
package org.eclipse.daanse.jdbc.db.dialect.db.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDialectFactory<T extends Dialect> implements DialectFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDialectFactory.class);

    @Override
    public Dialect createDialect(Connection connection) throws SQLException {
            return getConstructorFunction().apply(connection);
        }

    /**
     * Helper method to determine if a connection would work with
     * a given database product. This can be used to differenciate
     * between databases which use the same driver as others.
     *
     * <p>It will first try to use
     * {@link DatabaseMetaData#getDatabaseProductName()} and match the
     * name of DatabaseProduct passed as an argument.
     *
     * <p>If that fails, it will try to execute <code>select version();</code>
     * and obtains some information directly from the server.
     *
     * @param databaseProduct Database product instance
     * @param connection Connection
     * @return true if a match was found. false otherwise.
     */
    protected static boolean isDatabase(
        String databaseProduct,
        Connection connection)
    {
        String dbProduct = databaseProduct.toLowerCase();

        try {
            if (connection.getMetaData().getDatabaseProductName()
                .toLowerCase().contains(dbProduct))
            {
                LOGGER.debug("Using {} dialect", databaseProduct);
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.debug("NOT Using {} dialect.", databaseProduct);
            return false;
        }
    }

    public abstract Function<Connection, T> getConstructorFunction();
}
