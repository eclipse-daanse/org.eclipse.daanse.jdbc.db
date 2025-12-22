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
package org.eclipse.daanse.jdbc.db.api.sql;

public sealed interface SqlStatement permits DropContainerSqlStatement, CreateSqlStatement, CreateSchemaSqlStatement,
        TruncateTableSqlStatement, DropSchemaSqlStatement, InsertSqlStatement, CreateConstraintStatement,
        DropConstraintStatement, CreateIndexStatement, DropIndexStatement, CreatePrimaryKeyStatement,
        DropPrimaryKeyStatement, AddColumnStatement, DropColumnStatement, ModifyColumnStatement,
        RenameColumnStatement, RenameTableStatement, UpdateSqlStatement, DeleteSqlStatement {

}
