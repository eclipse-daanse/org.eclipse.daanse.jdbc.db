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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.CatalogReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class DatabaseServiceImplH2Test {

    private DatabaseService databaseService = new DatabaseServiceImpl();

    private String catalogName = UUID.randomUUID().toString().toUpperCase();

    private DataSource ds() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:memFS:" + catalogName);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    @Test
    void createMetaDataTest() throws SQLException {
        DataSource ds = ds();
        MetaInfo metaInfo = databaseService.createMetaInfo(ds);
        assertThat(metaInfo).isNotNull();
    }

    @Test
    void getTypeInfoTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<TypeInfo> types = databaseService.getTypeInfo(databaseMetaData);
        assertThat(types).isNotNull().isNotEmpty();
    }

    @Test
    void getCatalogsTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<CatalogReference> catalogs = databaseService.getCatalogs(databaseMetaData);

        assertThat(catalogs).isNotNull().isNotEmpty();
    }

    @Test
    void getSchemasTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<SchemaReference> schemas = databaseService.getSchemas(databaseMetaData);
        assertThat(schemas).isNotNull().isNotEmpty();
    }

    @Test
    void getTablesTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<TableDefinition> tables = databaseService.getTableDefinitions(databaseMetaData);
        assertThat(tables).isNotNull().isNotEmpty();
    }

    @Test
    void getTableTypesTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();
        List<String> tableTypes = databaseService.getTableTypes(databaseMetaData);
        assertThat(tableTypes).isNotNull().isNotEmpty();
    }

    @Test
    void tableExistsTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();

        //
        boolean constantsTableExists = databaseService.tableExists(databaseMetaData, new TableReferenceR("CONSTANTS"));
        assertThat(constantsTableExists).isTrue();

        //
        constantsTableExists = databaseService.tableExists(databaseMetaData, new TableReferenceR("CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR("CONSTANTS", "BASE TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        Optional<SchemaReference> oSchema = Optional.of(new SchemaReferenceR("INFORMATION_SCHEMA"));
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR(oSchema, "CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();

        //
        Optional<CatalogReference> oCatalog = Optional.of(new CatalogReferenceR(catalogName));
        oSchema = Optional.of(new SchemaReferenceR(oCatalog, "INFORMATION_SCHEMA"));
        constantsTableExists = databaseService.tableExists(databaseMetaData,
                new TableReferenceR(oSchema, "CONSTANTS", "TABLE"));
        assertThat(constantsTableExists).isTrue();
    }

    @Test
    void tableColumnTest() throws SQLException {
        DataSource ds = ds();
        DatabaseMetaData databaseMetaData = ds.getConnection().getMetaData();

        //
        boolean exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR("USER_NAME"));
        assertThat(exists).isTrue();

        //
        Optional<TableReference> oTable = Optional.of(new TableReferenceR("USERS"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        oTable = Optional.of(new TableReferenceR("USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        oTable = Optional.of(new TableReferenceR("USERS", "BASE TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //
        Optional<SchemaReference> oSchema = Optional.of(new SchemaReferenceR("INFORMATION_SCHEMA"));
        oTable = Optional.of(new TableReferenceR(oSchema, "USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();

        //

        Optional<CatalogReference> oCatalog = Optional.of(new CatalogReferenceR(catalogName));
        oSchema = Optional.of(new SchemaReferenceR(oCatalog, "INFORMATION_SCHEMA"));
        oTable = Optional.of(new TableReferenceR(oSchema, "USERS", "TABLE"));
        exists = databaseService.columnExists(databaseMetaData, new ColumnReferenceR(oTable, "USER_NAME"));
        assertThat(exists).isTrue();
    }

}
