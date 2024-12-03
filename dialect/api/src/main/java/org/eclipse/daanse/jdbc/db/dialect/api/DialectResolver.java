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
*   Sergei Semenkov - initial
*/
package org.eclipse.daanse.jdbc.db.dialect.api;

import java.sql.Connection;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * A {@link DialectResolver} gives access to Dialect that is compatible with a
 * {@link DataSource}
 *
 * @author stbischof
 *
 */
public interface DialectResolver {

    /**
     * Returns a dialect that could operate on the given DataSource. The given
     * dialect MUST be FULLY compatible.
     *
     * @param dataSource
     * @return a Optional of Dialect
     */
    Optional<Dialect> resolve(DataSource dataSource);

    Optional<Dialect> resolve(Connection connection);

}
