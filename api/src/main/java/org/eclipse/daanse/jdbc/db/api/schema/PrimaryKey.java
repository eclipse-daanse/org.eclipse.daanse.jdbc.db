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
package org.eclipse.daanse.jdbc.db.api.schema;

import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Optional;

/**
 * Primary Key constraint according to
 * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
 */
public interface PrimaryKey {

    /**
     * The table that this primary key belongs to
     *
     * @return the table reference
     */
    TableReference table();

    /**
     * The columns that make up this primary key, ordered by KEY_SEQ.
     * For a simple primary key, this list will contain one column.
     * For a composite primary key, this list will contain multiple columns in order.
     *
     * @return ordered list of column references
     */
    List<ColumnReference> columns();

    /**
     * The name of the primary key constraint (may be null if not named)
     *
     * @return the constraint name, or empty if unnamed
     */
    Optional<String> constraintName();
}
