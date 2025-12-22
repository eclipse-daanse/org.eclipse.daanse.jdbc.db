/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.dialect.api.order;

import java.util.Optional;

/**
 * Represents a column in an ORDER BY clause or aggregate function ordering.
 *
 * @param columnName the name of the column
 * @param tableName the name of the table (may be null for unqualified columns)
 * @param sortDirection empty = use database default, otherwise ASC or DESC
 * @param nullsOrder empty = use database default, otherwise FIRST or LAST
 */
public record OrderedColumn(String columnName, String tableName,
                            Optional<SortDirection> sortDirection, Optional<NullsOrder> nullsOrder) {

    /**
     * Creates an OrderedColumn with explicit direction but database default for nulls.
     *
     * @param columnName the name of the column
     * @param tableName the name of the table (may be null for unqualified columns)
     * @param sortDirection ASC or DESC
     */
    public OrderedColumn(String columnName, String tableName, SortDirection sortDirection) {
        this(columnName, tableName, Optional.of(sortDirection), Optional.empty());
    }

    /**
     * Creates an OrderedColumn using database defaults for direction and nulls.
     *
     * @param columnName the name of the column
     * @param tableName the name of the table (may be null for unqualified columns)
     */
    public OrderedColumn(String columnName, String tableName) {
        this(columnName, tableName, Optional.empty(), Optional.empty());
    }

    // Factory methods for convenience

    /**
     * Creates an ascending OrderedColumn.
     */
    public static OrderedColumn asc(String columnName) {
        return new OrderedColumn(columnName, null, SortDirection.ASC);
    }

    /**
     * Creates an ascending OrderedColumn with table name.
     */
    public static OrderedColumn asc(String tableName, String columnName) {
        return new OrderedColumn(columnName, tableName, SortDirection.ASC);
    }

    /**
     * Creates a descending OrderedColumn.
     */
    public static OrderedColumn desc(String columnName) {
        return new OrderedColumn(columnName, null, SortDirection.DESC);
    }

    /**
     * Creates a descending OrderedColumn with table name.
     */
    public static OrderedColumn desc(String tableName, String columnName) {
        return new OrderedColumn(columnName, tableName, SortDirection.DESC);
    }

    /**
     * Creates an OrderedColumn with full control over direction and nulls ordering.
     */
    public static OrderedColumn of(String columnName, SortDirection direction, NullsOrder nullsOrder) {
        return new OrderedColumn(columnName, null, Optional.ofNullable(direction), Optional.ofNullable(nullsOrder));
    }

    /**
     * Creates an OrderedColumn with full control including table name.
     */
    public static OrderedColumn of(String tableName, String columnName, SortDirection direction, NullsOrder nullsOrder) {
        return new OrderedColumn(columnName, tableName, Optional.ofNullable(direction), Optional.ofNullable(nullsOrder));
    }
}
