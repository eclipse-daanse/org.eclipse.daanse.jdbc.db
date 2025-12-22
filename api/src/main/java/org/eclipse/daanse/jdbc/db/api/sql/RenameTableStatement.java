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
 * SQL statement for renaming a table.
 * Generates: ALTER TABLE old_name RENAME TO new_name
 * Or: RENAME TABLE old_name TO new_name (MySQL syntax)
 */
public non-sealed interface RenameTableStatement extends SqlStatement {

    /**
     * The current table reference (to be renamed)
     *
     * @return the old table reference
     */
    TableReference oldTable();

    /**
     * The new name for the table
     *
     * @return the new table name
     */
    String newTableName();
}
