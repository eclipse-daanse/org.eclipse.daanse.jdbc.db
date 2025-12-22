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
 * SQL statement for dropping an index.
 * Generates: DROP INDEX [IF EXISTS] index_name [ON table]
 * Note: Some databases require the table name (e.g., MySQL), others don't (e.g., PostgreSQL)
 */
public non-sealed interface DropIndexStatement extends SqlStatement {

    /**
     * The name of the index to drop
     *
     * @return the index name
     */
    String indexName();

    /**
     * The table the index belongs to (required for some databases like MySQL)
     *
     * @return the table reference, or empty if not needed
     */
    Optional<TableReference> table();

    /**
     * Whether to use IF EXISTS clause
     *
     * @return true to include IF EXISTS
     */
    boolean ifExists();
}
