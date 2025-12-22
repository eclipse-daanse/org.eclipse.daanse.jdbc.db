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
 * SQL statement for dropping a primary key constraint from a table.
 * Generates: ALTER TABLE table DROP PRIMARY KEY
 * Or: ALTER TABLE table DROP CONSTRAINT constraint_name (if constraint name is specified)
 */
public non-sealed interface DropPrimaryKeyStatement extends SqlStatement {

    /**
     * The table to drop the primary key from
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The optional constraint name. If provided, uses DROP CONSTRAINT syntax.
     * If not provided, uses DROP PRIMARY KEY syntax (MySQL style).
     *
     * @return the constraint name, or empty to use DROP PRIMARY KEY
     */
    Optional<String> constraintName();

    /**
     * Whether to use IF EXISTS clause
     *
     * @return true to include IF EXISTS
     */
    boolean ifExists();
}
