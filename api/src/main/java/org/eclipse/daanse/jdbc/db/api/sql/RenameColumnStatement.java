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
 * SQL statement for renaming a column in an existing table.
 * Generates: ALTER TABLE table RENAME COLUMN old_name TO new_name
 */
public non-sealed interface RenameColumnStatement extends SqlStatement {

    /**
     * The table containing the column to rename
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The current name of the column
     *
     * @return the old column name
     */
    String oldColumnName();

    /**
     * The new name for the column
     *
     * @return the new column name
     */
    String newColumnName();
}
