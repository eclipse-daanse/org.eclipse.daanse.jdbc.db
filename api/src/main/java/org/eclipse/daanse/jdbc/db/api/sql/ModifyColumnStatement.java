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
 * SQL statement for modifying a column in an existing table.
 * Generates: ALTER TABLE table MODIFY/ALTER [COLUMN] column_name new_data_type [NOT NULL] [DEFAULT value]
 * Note: The exact syntax varies by database (MODIFY vs ALTER COLUMN)
 */
public non-sealed interface ModifyColumnStatement extends SqlStatement {

    /**
     * The table containing the column to modify
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The name of the column to modify
     *
     * @return the column name
     */
    String columnName();

    /**
     * The new SQL data type for the column
     *
     * @return the new data type
     */
    String newDataType();

    /**
     * Whether the column should allow NULL values
     *
     * @return true if nullable, false for NOT NULL
     */
    boolean nullable();

    /**
     * The new default value for the column
     *
     * @return the default value, or empty if no default
     */
    Optional<String> defaultValue();
}
