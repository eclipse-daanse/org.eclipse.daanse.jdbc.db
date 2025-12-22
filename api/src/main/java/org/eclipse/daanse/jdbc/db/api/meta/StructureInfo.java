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
package org.eclipse.daanse.jdbc.db.api.meta;

import java.sql.DatabaseMetaData;
import java.util.List;

import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.PrimaryKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

public interface StructureInfo {
    /**
     * A list of all {@link CatalogReference} according
     * {@link DatabaseMetaData#getCatalogs()}
     *
     * @return the catalogs
     */
    List<CatalogReference> catalogs();

    /**
     * A list of all {@link SchemaReference} according
     * {@link DatabaseMetaData#getSchemas()}
     *
     * @return the schemas
     */
    List<SchemaReference> schemas();

    /**
     * A list of all {@link TableReference} according
     * {@link DatabaseMetaData#getTables(String, String, String, String[])}
     *
     * @return the tables
     */
    List<TableDefinition> tables();

    /**
     * A list of all {@link ColumnDefinition} according
     * {@link DatabaseMetaData#getColumns(String, String, String, String)}
     *
     * @return the columnDefinitions
     */
    List<ColumnDefinition> columns();

    /**
     * A list of all {@link ImportedKey} according
     * {@link DatabaseMetaData#getImportedKeys(String, String, String)}
     *
     * @return the imported keys (foreign key constraints)
     */
    List<ImportedKey> importedKeys();

    /**
     * A list of all {@link PrimaryKey} according
     * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)}
     *
     * @return the primary keys
     */
    List<PrimaryKey> primaryKeys();

}
