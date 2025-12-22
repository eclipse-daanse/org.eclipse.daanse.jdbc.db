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
package org.eclipse.daanse.jdbc.db.api.meta;

import java.sql.DatabaseMetaData;
import java.util.Optional;
import java.util.stream.Stream;

public interface IndexInfoItem {

    /**
     * Index name; null when TYPE is tableIndexStatistic
     */
    Optional<String> indexName();

    /**
     * Index type
     */
    IndexType type();

    /**
     * Column name; null when TYPE is tableIndexStatistic
     */
    Optional<String> columnName();

    /**
     * Column sequence number within index (starting at 1); zero when TYPE is tableIndexStatistic
     */
    int ordinalPosition();

    /**
     * Sort sequence: true for ascending, false for descending, empty when sort sequence is not supported
     */
    Optional<Boolean> ascending();

    /**
     * When TYPE is tableIndexStatistic, this is the number of unique values in the index;
     * otherwise, it is the number of rows in the index
     */
    long cardinality();

    /**
     * When TYPE is tableIndexStatistic, this is the number of pages used for the table;
     * otherwise, it is the number of pages used for the current index
     */
    long pages();

    /**
     * Filter condition, if any (may be null)
     */
    Optional<String> filterCondition();

    /**
     * Whether the index is non-unique (false means unique)
     */
    boolean unique();

    /**
     * Index type as defined in DatabaseMetaData
     */
    enum IndexType {
        /**
         * Indicates that this column contains table statistics that are returned in conjunction with the index descriptions
         */
        TABLE_INDEX_STATISTIC(DatabaseMetaData.tableIndexStatistic),
        /**
         * Indicates that this is a clustered index
         */
        TABLE_INDEX_CLUSTERED(DatabaseMetaData.tableIndexClustered),
        /**
         * Indicates that this is a hashed index
         */
        TABLE_INDEX_HASHED(DatabaseMetaData.tableIndexHashed),
        /**
         * Indicates that this is some other style of index
         */
        TABLE_INDEX_OTHER(DatabaseMetaData.tableIndexOther);

        private final int value;

        IndexType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static IndexType of(int value) {
            return Stream.of(IndexType.values())
                    .filter(t -> t.value == value)
                    .findFirst()
                    .orElse(TABLE_INDEX_OTHER);
        }
    }
}
