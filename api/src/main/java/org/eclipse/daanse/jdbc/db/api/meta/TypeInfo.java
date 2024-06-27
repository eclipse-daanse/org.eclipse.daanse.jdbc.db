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
import java.sql.JDBCType;
import java.util.Optional;
import java.util.stream.Stream;

public interface TypeInfo {

    String typeName();
    JDBCType dataType();
    int percision();
    Optional<String> literatPrefix();
    Optional<String> literatSuffix();
    Optional<String> createPragmas();
    Nullable nullable();
    boolean caseSensitive();
    Searchable searchable();
    boolean unsignesAttribute();
    boolean fixedPrecScale();
    boolean autoIncrement();
    Optional<String> localTypeName();
    short minimumScale();
    short maximumScale();
    int numPrecRadix();

    enum Nullable {
        NO_NULLS(DatabaseMetaData.typeNoNulls), NULLABLE(DatabaseMetaData.typeNullable),
        UNKNOWN(DatabaseMetaData.typeNullableUnknown);

        int value;

        Nullable(int value) {
            this.value = value;
        }

        public static Nullable of(int value) {
            return Stream.of(Nullable.values()).filter(n -> n.value == value).findAny().orElse(null);
        }
    }

    enum Searchable {
        PRED_NONE(DatabaseMetaData.typePredNone), PRRED_CHAR(DatabaseMetaData.typePredChar),
        PRED_BASIC(DatabaseMetaData.typePredBasic), SEARCHABLE(DatabaseMetaData.typeSearchable);

        int value;

        Searchable(int value) {
            this.value = value;
        }

        public static Searchable of(int value) {
            return Stream.of(Searchable.values()).filter(n -> n.value == value).findAny().orElse(null);
        }
    }
}
