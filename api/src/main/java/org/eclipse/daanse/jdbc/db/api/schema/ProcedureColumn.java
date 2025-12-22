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

/**
 * Represents a parameter or column of a stored procedure.
 * Based on {@link DatabaseMetaData#getProcedureColumns(String, String, String, String)}
 */
public interface ProcedureColumn extends Named {

    /**
     * The type of this column/parameter
     *
     * @return the column type
     */
    ColumnType columnType();

    /**
     * SQL type from java.sql.Types
     *
     * @return the JDBC type
     */
    JDBCType dataType();

    /**
     * Data source dependent type name
     *
     * @return the type name
     */
    String typeName();

    /**
     * Precision for numeric types, length for string types
     *
     * @return the precision/length
     */
    OptionalInt precision();

    /**
     * Scale for numeric types
     *
     * @return the scale
     */
    OptionalInt scale();

    /**
     * Radix (typically 10 or 2)
     *
     * @return the radix
     */
    OptionalInt radix();

    /**
     * Whether NULL values are allowed
     *
     * @return the nullability
     */
    Nullability nullable();

    /**
     * Comment describing the parameter/column
     *
     * @return the remarks
     */
    Optional<String> remarks();

    /**
     * Default value for the column
     *
     * @return the default value
     */
    Optional<String> columnDefault();

    /**
     * Ordinal position of this parameter (starting at 1)
     *
     * @return the ordinal position
     */
    int ordinalPosition();

    /**
     * Type of procedure column
     */
    enum ColumnType {
        /**
         * Unknown type
         */
        UNKNOWN(DatabaseMetaData.procedureColumnUnknown),
        /**
         * IN parameter
         */
        IN(DatabaseMetaData.procedureColumnIn),
        /**
         * INOUT parameter
         */
        INOUT(DatabaseMetaData.procedureColumnInOut),
        /**
         * OUT parameter
         */
        OUT(DatabaseMetaData.procedureColumnOut),
        /**
         * Return value of a function
         */
        RETURN(DatabaseMetaData.procedureColumnReturn),
        /**
         * Result column in a ResultSet
         */
        RESULT(DatabaseMetaData.procedureColumnResult);

        private final int value;

        ColumnType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ColumnType of(int value) {
            return Stream.of(ColumnType.values())
                    .filter(t -> t.value == value)
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }

    /**
     * Nullability of the column
     */
    enum Nullability {
        /**
         * Does not allow NULL values
         */
        NO_NULLS(DatabaseMetaData.procedureNoNulls),
        /**
         * Allows NULL values
         */
        NULLABLE(DatabaseMetaData.procedureNullable),
        /**
         * Nullability unknown
         */
        UNKNOWN(DatabaseMetaData.procedureNullableUnknown);

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
    }
}
