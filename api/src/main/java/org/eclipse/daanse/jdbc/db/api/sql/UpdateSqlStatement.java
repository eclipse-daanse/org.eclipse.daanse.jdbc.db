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

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * SQL statement for updating rows in a table.
 * Generates: UPDATE table SET column1=value1, column2=value2, ... [WHERE condition]
 */
public non-sealed interface UpdateSqlStatement extends SqlStatement {

    /**
     * The table to update
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The columns and their new values
     *
     * @return the list of column-value pairs
     */
    List<ColumnValue> setClause();

    /**
     * The WHERE clause condition (without the "WHERE" keyword)
     *
     * @return the condition, or empty for updating all rows
     */
    Optional<String> whereClause();

    /**
     * Represents a column-value pair for the SET clause
     */
    interface ColumnValue {
        /**
         * The column name
         *
         * @return the column name
         */
        String columnName();

        /**
         * The value to set. Can be a literal value, parameter placeholder (?), or expression.
         *
         * @return the value as a string
         */
        String value();
    }
}
