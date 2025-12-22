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
 * Stored procedure definition according to
 * {@link DatabaseMetaData#getProcedures(String, String, String)}
 */
public interface Procedure {

    /**
     * Reference to this procedure (name, schema, specific name)
     *
     * @return the procedure reference
     */
    ProcedureReference reference();

    /**
     * The type of this procedure
     *
     * @return the procedure type
     */
    ProcedureType procedureType();

    /**
     * Explanatory comment on the procedure
     *
     * @return the remarks
     */
    Optional<String> remarks();

    /**
     * The parameters/columns of this procedure
     *
     * @return the list of procedure columns
     */
    List<ProcedureColumn> columns();

    /**
     * Type of stored procedure
     */
    enum ProcedureType {
        /**
         * Cannot determine if a return value will be returned
         */
        UNKNOWN(DatabaseMetaData.procedureResultUnknown),
        /**
         * Does not return a return value
         */
        NO_RESULT(DatabaseMetaData.procedureNoResult),
        /**
         * Returns a return value
         */
        RETURNS_RESULT(DatabaseMetaData.procedureReturnsResult);

        private final int value;

        ProcedureType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ProcedureType of(int value) {
            return Stream.of(ProcedureType.values())
                    .filter(t -> t.value == value)
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }
}
