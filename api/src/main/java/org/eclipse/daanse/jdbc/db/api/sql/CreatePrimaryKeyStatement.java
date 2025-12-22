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
 * SQL statement for adding a primary key constraint to a table.
 * Generates: ALTER TABLE table ADD [CONSTRAINT constraint_name] PRIMARY KEY (column1, column2, ...)
 */
public non-sealed interface CreatePrimaryKeyStatement extends SqlStatement {

    /**
     * The table to add the primary key to
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The columns that make up the primary key
     *
     * @return the list of column names
     */
    List<String> columns();

    /**
     * The optional constraint name for the primary key
     *
     * @return the constraint name, or empty for an unnamed constraint
     */
    Optional<String> constraintName();
}
