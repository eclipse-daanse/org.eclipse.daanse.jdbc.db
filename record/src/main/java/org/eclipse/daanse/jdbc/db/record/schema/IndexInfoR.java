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
package org.eclipse.daanse.jdbc.db.record.schema;

import java.util.List;

import org.eclipse.daanse.jdbc.db.api.meta.IndexInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IndexInfoItem;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

public record IndexInfoR(TableReference tableReference, List<IndexInfoItem> indexInfoItems) implements IndexInfo {

}
