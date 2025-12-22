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
import java.sql.JDBCType;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public interface ColumnMetaData {

    /**
     * SQL type from java.sql.Types
     */
    JDBCType dataType();

    /**
     * Data source dependent type name
     */
    String typeName();

    /**
     * Column size. For char/date types this is the maximum number of characters.
     * For numeric/decimal types this is precision.
     */
    OptionalInt columnSize();

    /**
     * The number of fractional digits. Null is returned for data types where
     * DECIMAL_DIGITS is not applicable.
     */
    OptionalInt decimalDigits();

    /**
     * Radix (typically either 10 or 2)
     */
    OptionalInt numPrecRadix();

    /**
     * Is NULL allowed (raw JDBC value)
     * @deprecated Use {@link #nullability()} instead
     */
    @Deprecated
    OptionalInt nullable();

    /**
     * Is NULL allowed
     */
    Nullability nullability();

    /**
     * For char types the maximum number of bytes in the column
     */
    OptionalInt charOctetLength();

    /**
     * Comment describing column (may be null)
     */
    Optional<String> remarks();

    /**
     * Default value for the column, which should be interpreted as a string when
     * the value is enclosed in single quotes (may be null)
     */
    Optional<String> columnDefault();

    /**
     * Indicates whether this column is auto incremented
     */
    AutoIncrement autoIncrement();

    /**
     * Indicates whether this is a generated column
     */
    GeneratedColumn generatedColumn();

    /**
     * Column nullability
     */
    enum Nullability {
        /**
         * Column does not allow NULL values
         */
        NO_NULLS(DatabaseMetaData.columnNoNulls),
        /**
         * Column allows NULL values
         */
        NULLABLE(DatabaseMetaData.columnNullable),
        /**
         * Nullability unknown
         */
        UNKNOWN(DatabaseMetaData.columnNullableUnknown);

        private final int value;

        Nullability(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Nullability of(int value) {
            return Stream.of(Nullability.values())
                    .filter(n -> n.value == value)
                    .findFirst()
                    .orElse(UNKNOWN);
        }

        public static Nullability ofString(String value) {
            if ("YES".equalsIgnoreCase(value)) {
                return NULLABLE;
            } else if ("NO".equalsIgnoreCase(value)) {
                return NO_NULLS;
            }
            return UNKNOWN;
        }
    }

    /**
     * Column auto-increment status
     */
    enum AutoIncrement {
        /**
         * Column is auto incremented
         */
        YES,
        /**
         * Column is not auto incremented
         */
        NO,
        /**
         * Unknown if column is auto incremented
         */
        UNKNOWN;

        public static AutoIncrement ofString(String value) {
            if ("YES".equalsIgnoreCase(value)) {
                return YES;
            } else if ("NO".equalsIgnoreCase(value)) {
                return NO;
            }
            return UNKNOWN;
        }
    }

    /**
     * Column generated status
     */
    enum GeneratedColumn {
        /**
         * Column is a generated column
         */
        YES,
        /**
         * Column is not a generated column
         */
        NO,
        /**
         * Unknown if column is generated
         */
        UNKNOWN;

        public static GeneratedColumn ofString(String value) {
            if ("YES".equalsIgnoreCase(value)) {
                return YES;
            } else if ("NO".equalsIgnoreCase(value)) {
                return NO;
            }
            return UNKNOWN;
        }
    }
}
