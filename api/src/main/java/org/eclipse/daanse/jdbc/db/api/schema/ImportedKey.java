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
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * Imported Keys according *
 * {@link DatabaseMetaData#getImportedKeys(String, String, String)}
 */
public interface ImportedKey extends Named {

    /**
     * The primary key site of the {@link ColumnReference}
     *
     * @return the primary key column reference
     */
    ColumnReference primaryKeyColumn();

    /**
     * The foreign key site of the {@link ColumnReference}
     *
     * @return the foreign key column reference
     */
    ColumnReference foreignKeyColumn();

    /**
     * Sequence number within a foreign key (for composite keys).
     * A value of 1 represents the first column of the foreign key.
     *
     * @return sequence number within the foreign key
     */
    int keySequence();

    /**
     * What happens to the foreign key when the primary key is updated.
     *
     * @return the update rule
     */
    ReferentialAction updateRule();

    /**
     * What happens to the foreign key when the primary key is deleted.
     *
     * @return the delete rule
     */
    ReferentialAction deleteRule();

    /**
     * The name of the primary key (may be null)
     *
     * @return the primary key name
     */
    Optional<String> primaryKeyName();

    /**
     * Whether the evaluation of foreign key constraints can be deferred until commit.
     *
     * @return the deferrability
     */
    Deferrability deferrability();

    /**
     * Referential action when primary key is updated or deleted
     */
    enum ReferentialAction {
        /**
         * For update/delete: do not allow update/delete of primary key if it has been imported
         */
        NO_ACTION(DatabaseMetaData.importedKeyNoAction),
        /**
         * For update/delete: change imported key to agree with primary key update/delete
         */
        CASCADE(DatabaseMetaData.importedKeyCascade),
        /**
         * For update/delete: change imported key to NULL if its primary key has been updated/deleted
         */
        SET_NULL(DatabaseMetaData.importedKeySetNull),
        /**
         * For update/delete: change imported key to default values if its primary key has been updated/deleted
         */
        SET_DEFAULT(DatabaseMetaData.importedKeySetDefault),
        /**
         * For update/delete: same as NO_ACTION (for ODBC 2.x compatibility)
         */
        RESTRICT(DatabaseMetaData.importedKeyRestrict);

        private final int value;

        ReferentialAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ReferentialAction of(int value) {
            return Stream.of(ReferentialAction.values())
                    .filter(r -> r.value == value)
                    .findFirst()
                    .orElse(NO_ACTION);
        }
    }

    /**
     * Deferrability of constraint checking
     */
    enum Deferrability {
        /**
         * See SQL92 for definition
         */
        INITIALLY_DEFERRED(DatabaseMetaData.importedKeyInitiallyDeferred),
        /**
         * See SQL92 for definition
         */
        INITIALLY_IMMEDIATE(DatabaseMetaData.importedKeyInitiallyImmediate),
        /**
         * See SQL92 for definition
         */
        NOT_DEFERRABLE(DatabaseMetaData.importedKeyNotDeferrable);

        private final int value;

        Deferrability(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Deferrability of(int value) {
            return Stream.of(Deferrability.values())
                    .filter(d -> d.value == value)
                    .findFirst()
                    .orElse(NOT_DEFERRABLE);
        }
    }
}
