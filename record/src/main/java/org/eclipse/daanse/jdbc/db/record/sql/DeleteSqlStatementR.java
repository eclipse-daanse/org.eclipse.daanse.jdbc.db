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
import org.eclipse.daanse.jdbc.db.api.sql.DeleteSqlStatement;

public record DeleteSqlStatementR(
        TableReference table,
        Optional<String> whereClause) implements DeleteSqlStatement {

    /**
     * Creates a DeleteSqlStatement without WHERE clause (deletes all rows)
     */
    public DeleteSqlStatementR(TableReference table) {
        this(table, Optional.empty());
    }
}
