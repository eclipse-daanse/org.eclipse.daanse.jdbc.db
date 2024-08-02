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
package org.eclipse.daanse.jdbc.db.api;

import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * Service that helps to communicate with the Database.
 *
 */
public interface DatabaseService {

    /**
     * Opens a Connection and close this directly
     *
     * @param dataSource to be used to get the {@link DatabaseMetaData} from an the
     *                   {@link Connection} and may snapshot them.
     * @return MetaInfo copy of the essential values of the
     *         {@link DatabaseMetaData}.
     * @throws SQLException
     */
    MetaInfo createMetaInfo(DataSource dataSource) throws SQLException;

    /**
     * Used the Connection and does not close is.
     *
     * @param connection to be used to get the {@link DatabaseMetaData} from an the
     *                   {@link Connection} and may snapshot them.
     * @return MetaInfo copy of the essential values of the
     *         {@link DatabaseMetaData}.
     * @throws SQLException
     */
    MetaInfo createMetaInfo(Connection connection) throws SQLException;

    /**
     * Creates a new {@link SqlStatementGenerator} that respects the
     * {@link MetaInfo} of the {@link Connection} e.g for Quoting.
     *
     * @param metaInfo {@link MetaInfo}
     * @return SqlStatementGenerator
     */
    SqlStatementGenerator createSqlStatementGenerator(MetaInfo metaInfo);

    /**
     * returns a {@link List} of {@link ColumnDefinition}s for the given
     * {@link ColumnReference}
     *
     * @param databaseMetaData
     * @param column
     * @return
     * @throws SQLException
     */

    List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, ColumnReference column)
            throws SQLException;

    /**
     * returns a {@link List} of {@link ColumnDefinition}s for the given
     * {@link TableReference}
     *
     * @param databaseMetaData
     * @param table
     * @return
     * @throws SQLException
     */

    List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException;

    /**
     * Checks if a Table with the given {@link Parameter}s exist.
     *
     * @param databaseMetaData
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param types
     * @return
     * @throws SQLException
     */
    boolean tableExists(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException;

    /**
     * Checks if a Table with the given {@link TableReference} exist.
     *
     * @param databaseMetaData
     * @param table
     * @return
     * @throws SQLException
     */
    boolean tableExists(DatabaseMetaData databaseMetaData, TableReference table) throws SQLException;

    /**
     * returns all {@link TableDefinition}s .
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param types
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, List<String> types)
            throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param catalog
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, CatalogReference catalog)
            throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param schema
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, SchemaReference schema)
            throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param catalog
     * @param types
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, CatalogReference catalog,
            List<String> types) throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param schema
     * @param types
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, SchemaReference schema,
            List<String> types) throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param table
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException;

    /**
     * returns all {@link TableDefinition}s with the given filter.
     *
     * @param databaseMetaData
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param types
     * @return
     * @throws SQLException
     */
    List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException;

    /**
     * returns a {@link List} of TableTypes {@link DatabaseMetaData#getTableTypes()}
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<String> getTableTypes(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * return a {@link List} of Schemas {@link DatabaseMetaData#getSchemas()}
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<SchemaReference> getSchemas(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * return a {@link List} of Schemas {@link DatabaseMetaData#getSchemas()} for a
     * given {@link CatalogReference}.
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<SchemaReference> getSchemas(DatabaseMetaData databaseMetaData, CatalogReference catalog) throws SQLException;


    /**
     * return a {@link List} of {@link ColumnDefinition}
     * {@link DatabaseMetaData#getColumns(String, String, String, String)}.
     *
     * @param databaseMetaData
     * @return {@link List<ColumnDefinition>}
     * @throws SQLException
     */
    List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * return a {@link List} of {@link ColumnDefinition}
     * {@link DatabaseMetaData#getColumns(String, String, String, String)} for a
     * given filter.
     *
     * @param databaseMetaData
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param columnNamePattern
     * @return
     * @throws SQLException
     */
    List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException;

    /**
     * checks if a Column with the given filter exists.
     *
     * @param databaseMetaData
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param columnNamePattern
     * @return
     * @throws SQLException
     */
    boolean columnExists(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException;

    /**
     * checks if the {@link ColumnReference} exist.
     *
     * @param databaseMetaData
     * @param column
     * @return
     * @throws SQLException
     */
    boolean columnExists(DatabaseMetaData databaseMetaData, ColumnReference column) throws SQLException;

    /**
     * returns a {@link List} of {@link CatalogReference} like
     * {@link DatabaseMetaData#getCatalogs()} the Database.
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<CatalogReference> getCatalogs(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * return a list of {@link TypeInfo} according
     * {@link DatabaseMetaData#getTypeInfo()}
     *
     * @param databaseMetaData
     * @return
     * @throws SQLException
     */
    List<TypeInfo> getTypeInfo(DatabaseMetaData databaseMetaData) throws SQLException;

    /**
     * returns a {@link List} of {@link ImportedKey}s according
     * {@link DatabaseMetaData#getImportedKeys(String, String, String)}
     *
     * @param databaseMetaData
     * @param catalog
     * @param schema
     * @param tableName
     * @return
     * @throws SQLException
     */
    List<ImportedKey> getImportedKeys(DatabaseMetaData databaseMetaData, String catalog, String schema,
            String tableName) throws SQLException;

    /**
     * returns a {@link List} of {@link ImportedKey}s according
     * {@link DatabaseMetaData#getImportedKeys(String, String, String)}
     *
     * @param databaseMetaData
     * @param table
     * @return
     * @throws SQLException
     */
    List<ImportedKey> getImportedKeys(DatabaseMetaData databaseMetaData, TableReference table) throws SQLException;



}
