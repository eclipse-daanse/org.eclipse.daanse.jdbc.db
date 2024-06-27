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

import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.meta.TableDefinition.TableMetaData;

public record TableMetaDataR(Optional<String> remarks, Optional<String> typeCatalog, Optional<String> typeSchema,
        Optional<String> typeName, Optional<String> selfReferencingColumnName, Optional<String> referencingGenerationa)
        implements TableMetaData {
}
