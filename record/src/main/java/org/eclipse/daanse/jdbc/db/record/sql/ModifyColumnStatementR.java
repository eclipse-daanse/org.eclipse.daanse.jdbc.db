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
package org.eclipse.daanse.jdbc.db.record.sql;

import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.sql.ModifyColumnStatement;

public record ModifyColumnStatementR(
        TableReference table,
        String columnName,
        String newDataType,
        boolean nullable,
        Optional<String> defaultValue) implements ModifyColumnStatement {

    /**
     * Creates a ModifyColumnStatement for a nullable column without default value
     */
    public ModifyColumnStatementR(TableReference table, String columnName, String newDataType) {
        this(table, columnName, newDataType, true, Optional.empty());
    }

    /**
     * Creates a ModifyColumnStatement with specified nullability but no default value
     */
    public ModifyColumnStatementR(TableReference table, String columnName, String newDataType, boolean nullable) {
        this(table, columnName, newDataType, nullable, Optional.empty());
    }
}
