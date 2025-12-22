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

import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.sql.CreateIndexStatement;

public record CreateIndexStatementR(
        String indexName,
        TableReference table,
        List<IndexColumn> columns,
        boolean unique,
        boolean ifNotExists) implements CreateIndexStatement {

    /**
     * Creates a non-unique index without IF NOT EXISTS
     */
    public CreateIndexStatementR(String indexName, TableReference table, List<IndexColumn> columns) {
        this(indexName, table, columns, false, false);
    }
}
