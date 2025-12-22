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

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * SQL statement for creating an index.
 * Generates: CREATE [UNIQUE] INDEX [IF NOT EXISTS] index_name ON table (column1 [ASC|DESC], column2 [ASC|DESC], ...)
 */
public non-sealed interface CreateIndexStatement extends SqlStatement {

    /**
     * The name of the index to create
     *
     * @return the index name
     */
    String indexName();

    /**
     * The table on which to create the index
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The columns to include in the index, with optional sort direction.
     * Each entry is an IndexColumn specifying the column name and sort order.
     *
     * @return the list of index columns
     */
    List<IndexColumn> columns();

    /**
     * Whether this is a unique index
     *
     * @return true if unique, false otherwise
     */
    boolean unique();

    /**
     * Whether to use IF NOT EXISTS clause
     *
     * @return true to include IF NOT EXISTS
     */
    boolean ifNotExists();

    /**
     * Represents a column in an index with its sort direction
     */
    interface IndexColumn {
        /**
         * The column name
         *
         * @return the column name
         */
        String columnName();

        /**
         * The sort direction (true = ascending, false = descending, null = default/unspecified)
         *
         * @return the sort direction
         */
        Boolean ascending();
    }
}
