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

import org.eclipse.daanse.jdbc.db.api.meta.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

public record TableDefinitionR(TableReference table, TableMetaData metaData) implements TableDefinition {

}
