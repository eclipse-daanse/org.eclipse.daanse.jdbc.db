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
package org.eclipse.daanse.jdbc.db.record.meta;

import java.util.List;

import org.eclipse.daanse.jdbc.db.api.meta.StructureInfo;
import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;

public record StructureInfoR(List<CatalogReference> catalogs, List<SchemaReference> schemas,
        List<TableDefinition> tables, List<ColumnDefinition> columns, List<ImportedKey> importedKeys) implements StructureInfo {

}
