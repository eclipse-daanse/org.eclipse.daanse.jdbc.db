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
package org.eclipse.daanse.jdbc.db.dialect.api.generator;

import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;

/**
 * Interface for generating SQL aggregate function expressions in a dialect-specific manner.
 * <p>
 * This interface provides methods for generating advanced aggregate functions like
 * LISTAGG, NTH_VALUE, percentile functions, and bitwise aggregations.
 */
public interface AggregationGenerator {

    /**
     * Generates a list aggregation (string concatenation aggregate) expression.
     * <p>
     * Different databases use different functions:
     * <ul>
     *   <li>Oracle: LISTAGG</li>
     *   <li>PostgreSQL: STRING_AGG</li>
     *   <li>MySQL: GROUP_CONCAT</li>
     *   <li>SQL Server: STRING_AGG</li>
     * </ul>
     *
     * @param operand            expression to aggregate
     * @param distinct           whether to use DISTINCT
     * @param separator          separator between values
     * @param coalesce           value to use for NULLs
     * @param onOverflowTruncate behavior when result exceeds maximum length
     * @param columns            columns for ORDER BY within the aggregate
     * @return list aggregation expression
     */
    StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator,
                                   String coalesce, String onOverflowTruncate, List<OrderedColumn> columns);

    /**
     * Generates an NTH_VALUE window function expression.
     * <p>
     * Returns the value of the nth row in the window frame.
     *
     * @param operand     expression to get value from
     * @param ignoreNulls whether to ignore NULL values
     * @param n           position (1-based) of the value to return
     * @param columns     columns for ORDER BY within the window
     * @return NTH_VALUE expression
     */
    StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n,
                                       List<OrderedColumn> columns);

    /**
     * Generates a PERCENTILE_DISC aggregate expression.
     * <p>
     * Returns a value from the set that corresponds to the specified percentile.
     *
     * @param percentile percentage (0.0 to 1.0)
     * @param desc       true for descending order
     * @param tableName  table name (may be null)
     * @param columnName column name
     * @return PERCENTILE_DISC expression
     */
    StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName);

    /**
     * Generates a PERCENTILE_CONT aggregate expression.
     * <p>
     * Returns an interpolated value that would fall at the specified percentile.
     *
     * @param percentile percentage (0.0 to 1.0)
     * @param desc       true for descending order
     * @param tableName  table name (may be null)
     * @param columnName column name
     * @return PERCENTILE_CONT expression
     */
    StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName);

    /**
     * Generates a bitwise aggregation expression for the specified operation.
     * <p>
     * This is the unified method for all bitwise aggregations. Different databases
     * use different function names:
     * <ul>
     *   <li>MySQL: BIT_AND, BIT_OR, BIT_XOR</li>
     *   <li>PostgreSQL: bit_and, bit_or, bit_xor (lowercase)</li>
     *   <li>Oracle: BIT_AND_AGG, BIT_OR_AGG, BIT_XOR</li>
     *   <li>H2: BIT_AND_AGG, BIT_OR_AGG, BIT_XOR_AGG, BIT_NAND_AGG, BIT_NOR_AGG, BIT_XNOR_AGG</li>
     * </ul>
     *
     * @param operation the bitwise operation to perform
     * @param operand   expression to aggregate
     * @return bitwise aggregation expression
     * @throws UnsupportedOperationException if the operation is not supported
     */
    StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand);

    /**
     * Checks if a specific bitwise aggregation operation is supported.
     *
     * @param operation the bitwise operation to check
     * @return true if the operation is supported
     */
    boolean supportsBitAggregation(BitOperation operation);

    /**
     * Returns whether PERCENTILE_CONT aggregation is supported.
     */
    boolean supportsPercentileContAgg();

    /**
     * Returns whether PERCENTILE_DISC aggregation is supported.
     */
    boolean supportsPercentileDiscAgg();

    /**
     * Returns whether PERCENTILE_DISC function is supported.
     */
    boolean supportsPercentileDisc();

    /**
     * Returns whether PERCENTILE_CONT function is supported.
     */
    boolean supportsPercentileCont();

    /**
     * Returns whether list aggregation (STRING_AGG, LISTAGG, GROUP_CONCAT) is supported.
     */
    boolean supportsListAgg();
}
