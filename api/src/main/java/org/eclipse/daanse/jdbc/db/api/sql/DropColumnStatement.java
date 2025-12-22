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

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * SQL statement for dropping a column from an existing table.
 * Generates: ALTER TABLE table DROP [COLUMN] [IF EXISTS] column_name
 */
public non-sealed interface DropColumnStatement extends SqlStatement {

    /**
     * The table from which the column will be dropped
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The name of the column to drop
     *
     * @return the column name
     */
    String columnName();

    /**
     * Whether to use IF EXISTS clause
     *
     * @return true to include IF EXISTS
     */
    boolean ifExists();
}
