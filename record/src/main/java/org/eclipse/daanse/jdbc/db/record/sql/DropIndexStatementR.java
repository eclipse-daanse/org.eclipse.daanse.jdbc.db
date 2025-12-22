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
import org.eclipse.daanse.jdbc.db.api.sql.DropIndexStatement;

public record DropIndexStatementR(
        String indexName,
        Optional<TableReference> table,
        boolean ifExists) implements DropIndexStatement {

    /**
     * Creates a drop index statement without table and without IF EXISTS
     */
    public DropIndexStatementR(String indexName) {
        this(indexName, Optional.empty(), false);
    }

    /**
     * Creates a drop index statement with table and without IF EXISTS
     */
    public DropIndexStatementR(String indexName, TableReference table) {
        this(indexName, Optional.of(table), false);
    }
}
