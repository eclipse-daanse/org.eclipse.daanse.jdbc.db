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

/**
 * MetaInfo holds all essential values of the {@link DatabaseMetaData} in an
 * easy accessible way.
 */
public interface MetaInfo {

    /**
     * Subset of the {@link MetaInfo} holds all Database related values of the
     * {@link DatabaseMetaData}
     */
    DatabaseInfo databaseInfo();

    /**
     * Subset of the {@link MetaInfo} holds all values of the
     * {@link DatabaseMetaData} that are relevant for quoting.
     */
    IdentifierInfo identifierInfo();

    /**
     * A list of all TypeInfo according {@link DatabaseMetaData#getTypeInfo()}
     *
     * @return the typeInfo
     */
    List<TypeInfo> typeInfos();

    /**
     * A list of all {@link CatalogReference} according
     * {@link DatabaseMetaData#getCatalogs()}
     *
     * @return the catalogs
     */
    List<CatalogReference> catalogs();

}
