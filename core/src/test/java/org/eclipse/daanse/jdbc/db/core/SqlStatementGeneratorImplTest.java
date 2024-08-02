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
package org.eclipse.daanse.jdbc.db.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.JDBCType;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.record.meta.DatabaseInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.IdentifierInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.MetaInfoR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnMetaDataR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.eclipse.daanse.jdbc.db.record.sql.CreateContainerSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.CreateSchemaSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.DropContainerSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.DropSchemaSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.InsertSqlStatementR;
import org.eclipse.daanse.jdbc.db.record.sql.TruncateTableSqlStatementR;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SqlStatementGeneratorImplTest {

    static TypeInfo typeInfoVarchar;

    static TypeInfo typeInfoInt;

    @BeforeAll
    static void beforeAll() {
        typeInfoVarchar = Mockito.mock(TypeInfo.class);
        typeInfoInt = Mockito.mock(TypeInfo.class);
        when(typeInfoVarchar.typeName()).thenReturn("varchar");
        when(typeInfoVarchar.dataType()).thenReturn(JDBCType.VARCHAR);
        when(typeInfoInt.typeName()).thenReturn("int");
        when(typeInfoInt.dataType()).thenReturn(JDBCType.INTEGER);
    }

    private SqlStatementGenerator generator = new SqlStatementGeneratorImpl(new MetaInfoR(
            new DatabaseInfoR("", "", 0, 0), null, new IdentifierInfoR("#"), List.of(typeInfoVarchar, typeInfoInt)));

    @Test
    void dropTableNoSchemaNoExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName", "TABLE"), false));
        assertThat(sql).isEqualTo("DROP TABLE #theTableName#");
    }

    @Test
    void dropTableWithSchemaNoExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "TABLE"),
                false));
        assertThat(sql).isEqualTo("DROP TABLE #theSchemaName#.#theTableName#");
    }

    @Test
    void dropTableNoSchemaWithExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName", "TABLE"), true));
        assertThat(sql).isEqualTo("DROP TABLE IF EXISTS #theTableName#");
    }

    @Test
    void dropTableWithSchemaWithExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "TABLE"),
                true));
        assertThat(sql).isEqualTo("DROP TABLE IF EXISTS #theSchemaName#.#theTableName#");
    }

    @Test
    void dropViewNoSchemaNoExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName", "VIEW"), false));
        assertThat(sql).isEqualTo("DROP VIEW #theTableName#");
    }

    @Test
    void dropViewWithSchemaNoExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "VIEW"),
                false));
        assertThat(sql).isEqualTo("DROP VIEW #theSchemaName#.#theTableName#");
    }

    @Test
    void dropViewNoSchemaWithExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName", "VIEW"), true));
        assertThat(sql).isEqualTo("DROP VIEW IF EXISTS #theTableName#");
    }

    @Test
    void dropViewWithSchemaWithExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "VIEW"), true));
        assertThat(sql).isEqualTo("DROP VIEW IF EXISTS #theSchemaName#.#theTableName#");
    }

    @Test
    void dropSchemaWithExist() {
        String sql = generator
                .getSqlOfStatement(new DropSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), true));
        assertThat(sql).isEqualTo("DROP SCHEMA IF EXISTS #theSchemaName#");
    }

    @Test
    void dropSchemaNoExist() {
        String sql = generator
                .getSqlOfStatement(new DropSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), false));
        assertThat(sql).isEqualTo("DROP SCHEMA #theSchemaName#");
    }

    @Test
    void createSchemaWithExist() {
        String sql = generator
                .getSqlOfStatement(new CreateSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), true));
        assertThat(sql).isEqualTo("CREATE SCHEMA IF NOT EXISTS #theSchemaName#");
    }

    @Test
    void createSchemaNoExist() {
        String sql = generator
                .getSqlOfStatement(new CreateSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), false));
        assertThat(sql).isEqualTo("CREATE SCHEMA #theSchemaName#");
    }

    @Test
    void truncateTableNoSchema() {
        String sql = generator.getSqlOfStatement(
                new TruncateTableSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName", "TABLE")));
        assertThat(sql).isEqualTo("TRUNCATE TABLE #theTableName#");
    }

    @Test
    void truncateTableWithSchema() {
        String sql = generator.getSqlOfStatement(new TruncateTableSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "TABLE")));
        assertThat(sql).isEqualTo("TRUNCATE TABLE #theSchemaName#.#theTableName#");
    }

    @Test
    void createTableWithSchema() {

        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableDefinitionR(new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")),
                        "theTableName", "TABLE")),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnMetaDataR(JDBCType.INTEGER, Optional.empty(), Optional.empty(), Optional.empty()))),
                true));

        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# int)");

    }

    @Test
    void createTableWithColumDetails() {
        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableDefinitionR(new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")),
                        "theTableName", "TABLE")),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnMetaDataR(JDBCType.VARCHAR, Optional.of(200), Optional.empty(), Optional.empty()))),
                true));
        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# varchar(200))");

    }

    @Test
    void createTableWithMiltiColumn() {
        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableDefinitionR(new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")),
                        "theTableName", "TABLE")),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnMetaDataR(JDBCType.INTEGER, Optional.empty(), Optional.empty(), Optional.empty())),
                        new ColumnDefinitionR(new ColumnReferenceR("Col2"), new ColumnMetaDataR(JDBCType.INTEGER,
                                Optional.empty(), Optional.empty(), Optional.empty()))),
                true));
        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# int, #Col2# int)");
    }

    @Test
    void insertTableWithMiltiColumn() {
        String sql = generator.getSqlOfStatement(new InsertSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "TABLE"),
                List.of(new ColumnReferenceR("Col1"), new ColumnReferenceR("Col2")), List.of("?", "?")));
        assertThat(sql).isEqualTo("INSERT INTO #theSchemaName#.#theTableName#(#Col1#, #Col2#) VALUES (?, ?)");

    }

    @Test
    void insertTableWithOneColumn() {
        String sql = generator.getSqlOfStatement(new InsertSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName", "TABLE"),
                List.of(new ColumnReferenceR("Col1")), List.of("?")));
        assertThat(sql).isEqualTo("INSERT INTO #theSchemaName#.#theTableName#(#Col1#) VALUES (?)");
    }

}
