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

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.sql.CreatePrimaryKeyStatement;

public record CreatePrimaryKeyStatementR(
        TableReference table,
        List<String> columns,
        Optional<String> constraintName) implements CreatePrimaryKeyStatement {

    /**
     * Creates a primary key statement without a constraint name
     */
    public CreatePrimaryKeyStatementR(TableReference table, List<String> columns) {
        this(table, columns, Optional.empty());
    }

    /**
     * Creates a primary key statement with a constraint name
     */
    public CreatePrimaryKeyStatementR(TableReference table, List<String> columns, String constraintName) {
        this(table, columns, Optional.ofNullable(constraintName));
    }
}
