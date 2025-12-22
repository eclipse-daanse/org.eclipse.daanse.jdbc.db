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
import java.util.stream.Stream;

/**
 * User-defined or system function definition according to
 * {@link DatabaseMetaData#getFunctions(String, String, String)}
 */
public interface Function {

    /**
     * Reference to this function (name, schema, specific name)
     *
     * @return the function reference
     */
    FunctionReference reference();

    /**
     * The type of this function
     *
     * @return the function type
     */
    FunctionType functionType();

    /**
     * Explanatory comment on the function
     *
     * @return the remarks
     */
    Optional<String> remarks();

    /**
     * The parameters and return value of this function
     *
     * @return the list of function columns
     */
    List<FunctionColumn> columns();

    /**
     * Type of function
     */
    enum FunctionType {
        /**
         * Cannot determine if a return value or table will be returned
         */
        UNKNOWN(DatabaseMetaData.functionResultUnknown),
        /**
         * Does not return a table
         */
        NO_TABLE(DatabaseMetaData.functionNoTable),
        /**
         * Returns a table
         */
        RETURNS_TABLE(DatabaseMetaData.functionReturnsTable);

        private final int value;

        FunctionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FunctionType of(int value) {
            return Stream.of(FunctionType.values())
                    .filter(t -> t.value == value)
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }
}
