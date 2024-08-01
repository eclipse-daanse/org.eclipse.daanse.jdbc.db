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
package org.eclipse.daanse.jdbc.db.rdb.emf;

import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.daanse.emf.model.rdbstructure.Column;
import org.eclipse.daanse.emf.model.rdbstructure.DatabaseCatalog;
import org.eclipse.daanse.emf.model.rdbstructure.DatabaseSchema;
import org.eclipse.daanse.emf.model.rdbstructure.Link;
import org.eclipse.daanse.emf.model.rdbstructure.RelationalDatabaseFactory;
import org.eclipse.daanse.emf.model.rdbstructure.Table;

public class EmfRdbProvider implements Supplier<DatabaseCatalog> {

    private DatabaseMetaData databaseMetaData;
    private AtomicInteger counterSchema = new AtomicInteger();

    public EmfRdbProvider(DatabaseMetaData databaseMetaData) {
        this.databaseMetaData = databaseMetaData;
    }

    @Override
    public DatabaseCatalog get() {
        return getDatabaseCatalog();
    }

    private DatabaseCatalog getDatabaseCatalog() {
        try {
            org.eclipse.daanse.emf.model.rdbstructure.DatabaseCatalog databaseCatalog =
                RelationalDatabaseFactory.eINSTANCE
                .createDatabaseCatalog();
            Collection<? extends DatabaseSchema> schemas = getSchemas();
            Collection<? extends Link> links = getLinks(schemas);
            databaseCatalog.getSchemas().addAll(schemas);
            databaseCatalog.getLinks().addAll(links);
            return databaseCatalog;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Collection<? extends Link> getLinks(Collection<? extends DatabaseSchema> schemas) throws SQLException {
        List<Link> links = new ArrayList<>();
        for (DatabaseSchema schema : schemas) {
            if (schema.getTables() != null) {
                for (Table table : schema.getTables()) {
                    try (ResultSet rs = databaseMetaData.getImportedKeys(null, schema.getName(), table.getName());) {
                        while (rs.next()) {
                            final String tableNamePk = rs.getString("PKTABLE_NAME");
                            final String columNamePk = rs.getString("PKCOLUMN_NAME");

                            final String tableNameFk = rs.getString("FKTABLE_NAME");
                            final String columNameFk = rs.getString("FKCOLUMN_NAME");

                            Optional<Column> oPrimaryKey = getColumn(schema, tableNamePk, columNamePk);
                            Optional<Column> oForeignKey = getColumn(schema, tableNameFk, columNameFk);

                            if (oPrimaryKey.isPresent() && oForeignKey.isPresent()) {
                                Link link = RelationalDatabaseFactory.eINSTANCE.createLink();
                                link.setPrimaryKey(oPrimaryKey.get());
                                link.setForeignKey(oForeignKey.get());
                                links.add(link);
                            }
                        }
                    }
                }
            }
        }
        return links;
    }

    private Optional<Column> getColumn(DatabaseSchema schema, String tableNamePk, String columNamePk) {
        Optional<Table> oTable = schema.getTables().stream().filter(t -> t.getName().equals(tableNamePk)).findFirst();
        if (oTable.isPresent()) {
            Table t = oTable.get();
            List<Column> columns = t.getColumns();
            if (columns != null) {
                return columns.stream().filter(c -> c.getName().equals(columNamePk)).findFirst();
            }
        }
        return Optional.empty();
    }

    private Collection<? extends DatabaseSchema> getSchemas() throws SQLException {

        List<DatabaseSchema> schemas = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getCatalogs()) {
            while (rs.next()) {
                final String catalogName = rs.getString("TABLE_CAT");
                schemas.addAll(getSchemas(catalogName));
            }
        }
        return schemas;
    }

    private Collection<? extends DatabaseSchema> getSchemas(String catalogName) throws SQLException {
        List<DatabaseSchema> schemas = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getSchemas(catalogName, null)) {
            while (rs.next()) {
                final String schemaName = rs.getString("TABLE_SCHEM");
                DatabaseSchema databaseSchema = RelationalDatabaseFactory.eINSTANCE.createDatabaseSchema();
                databaseSchema.setId("s_" + counterSchema.incrementAndGet());
                databaseSchema.setName(schemaName);
                databaseSchema.getTables().addAll(getTables(catalogName, schemaName, databaseSchema));
                schemas.add(databaseSchema);
            }
        }
        return schemas;
    }

    private Collection<? extends Table> getTables(String catalog, String schemaName, DatabaseSchema databaseSchema)
        throws SQLException {
        List<Table> tabes = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTables(catalog, null, null, null)) {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");
                if (tableType.equals("TABLE")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createPhysicalTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("table " + tableName);
                    tabes.add(table);
                }
                if (tableType.equals("VIEW")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createViewTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("view " + tableName);
                    tabes.add(table);
                }
                if (tableType.equals("SYSTEM TABLE")) {
                    Table table = RelationalDatabaseFactory.eINSTANCE.createSystemTable();
                    Collection<? extends Column> columns = getColumns(catalog, schemaName, table);
                    table.setName(tableName);
                    table.getColumns().addAll(columns);
                    table.setSchema(databaseSchema);
                    table.setDescription("system table " + tableName);
                    tabes.add(table);
                }

            }
        }

        return tabes;
    }

    public List<Column> getColumns(String catalog, String schema, Table table) throws SQLException {
        List<Column> columns = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getColumns(catalog, schema, table.getName(), null);) {
            while (rs.next()) {
                final String columName = rs.getString("COLUMN_NAME");
                final int dataType = rs.getInt("DATA_TYPE");
                final Optional<Integer> columnSize = Optional.ofNullable(rs.getInt("COLUMN_SIZE"));
                final Optional<Integer> decimalDigits = Optional.ofNullable(rs.getInt("DECIMAL_DIGITS"));

                JDBCType jdbcType = JDBCType.valueOf(dataType);
                Column column = RelationalDatabaseFactory.eINSTANCE.createColumn();
                column.setName(columName);
                column.setType(jdbcType.getName().toLowerCase());
                column.setTable(table);
                ArrayList<String> typeQualifiers = new ArrayList<>();
                if (columnSize.isPresent() && columnSize.get() > 0) {
                    typeQualifiers.add(columnSize.get().toString());
                    if (decimalDigits.isPresent()) {
                        typeQualifiers.add(decimalDigits.get().toString());
                    }
                }
                column.getTypeQualifiers().addAll(typeQualifiers);
                column.setDescription("");
                columns.add(column);
            }
        }
        return columns;
    }

}
