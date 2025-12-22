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
package org.eclipse.daanse.jdbc.db.dialect.api.type;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Interface for mapping JDBC types to the best Java types for reading result set values.
 * <p>
 * Different databases may represent the same logical type differently in JDBC metadata.
 * This interface provides dialect-specific type mapping for optimal value retrieval.
 */
public interface TypeMapper {

    /**
     * Determines the best Java type to use when reading a column's value from a ResultSet.
     * <p>
     * The returned type indicates which getter method to use:
     * <ul>
     *   <li>{@link BestFitColumnType#INT} - use {@code ResultSet.getInt()}</li>
     *   <li>{@link BestFitColumnType#LONG} - use {@code ResultSet.getLong()}</li>
     *   <li>{@link BestFitColumnType#DOUBLE} - use {@code ResultSet.getDouble()}</li>
     *   <li>{@link BestFitColumnType#DECIMAL} - use {@code ResultSet.getBigDecimal()}</li>
     *   <li>{@link BestFitColumnType#STRING} - use {@code ResultSet.getString()}</li>
     *   <li>{@link BestFitColumnType#OBJECT} - use {@code ResultSet.getObject()}</li>
     * </ul>
     *
     * @param metaData    result set metadata
     * @param columnIndex 1-based column index
     * @return the best Java type for reading this column
     * @throws SQLException if metadata cannot be retrieved
     */
    BestFitColumnType getType(ResultSetMetaData metaData, int columnIndex) throws SQLException;

    /**
     * Returns whether a double value needs an exponent suffix (e.g., "E0").
     * <p>
     * Some databases like LucidDB require this for proper parsing.
     *
     * @param value       the double value
     * @param valueString the string representation of the value
     * @return true if an exponent suffix is needed
     */
    boolean needsExponent(Object value, String valueString);

    /**
     * Checks if the dialect supports a specific ResultSet type and concurrency mode.
     *
     * @param type        ResultSet type (e.g., TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE)
     * @param concurrency ResultSet concurrency (e.g., CONCUR_READ_ONLY, CONCUR_UPDATABLE)
     * @return true if the combination is supported
     */
    boolean supportsResultSetConcurrency(int type, int concurrency);
}
