/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.api.sql;

import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * SQL statement for deleting rows from a table.
 * Generates: DELETE FROM table [WHERE condition]
 */
public non-sealed interface DeleteSqlStatement extends SqlStatement {

    /**
     * The table from which to delete rows
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The WHERE clause condition (without the "WHERE" keyword)
     *
     * @return the condition, or empty for deleting all rows
     */
    Optional<String> whereClause();
}
