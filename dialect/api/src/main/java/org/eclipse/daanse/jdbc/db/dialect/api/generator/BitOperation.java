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

/**
 * Represents bitwise aggregation operations supported by SQL dialects.
 * <p>
 * Each operation represents a different way to aggregate bit values across rows:
 * <ul>
 *   <li>{@link #AND} - Bitwise AND of all values (result bit is 1 only if all input bits are 1)</li>
 *   <li>{@link #OR} - Bitwise OR of all values (result bit is 1 if any input bit is 1)</li>
 *   <li>{@link #XOR} - Bitwise XOR of all values (result bit is 1 if odd number of input bits are 1)</li>
 *   <li>{@link #NAND} - Bitwise NOT AND (negation of AND)</li>
 *   <li>{@link #NOR} - Bitwise NOT OR (negation of OR)</li>
 *   <li>{@link #NXOR} - Bitwise NOT XOR (negation of XOR, also known as XNOR)</li>
 * </ul>
 */
public enum BitOperation {

    /**
     * Bitwise AND aggregation.
     * SQL functions: BIT_AND (MySQL), bit_and (PostgreSQL), BIT_AND_AGG (Oracle, H2)
     */
    AND,

    /**
     * Bitwise OR aggregation.
     * SQL functions: BIT_OR (MySQL), bit_or (PostgreSQL), BIT_OR_AGG (Oracle, H2)
     */
    OR,

    /**
     * Bitwise XOR aggregation.
     * SQL functions: BIT_XOR (MySQL, Oracle), bit_xor (PostgreSQL), BIT_XOR_AGG (H2)
     */
    XOR,

    /**
     * Bitwise NAND (NOT AND) aggregation.
     * SQL functions: BIT_NAND_AGG (H2)
     */
    NAND,

    /**
     * Bitwise NOR (NOT OR) aggregation.
     * SQL functions: BIT_NOR_AGG (H2)
     */
    NOR,

    /**
     * Bitwise NXOR (NOT XOR, also known as XNOR) aggregation.
     * SQL functions: BIT_XNOR_AGG (H2)
     */
    NXOR
}
