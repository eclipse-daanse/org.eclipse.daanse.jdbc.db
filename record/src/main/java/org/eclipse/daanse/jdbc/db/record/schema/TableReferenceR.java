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

import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

public record TableReferenceR(Optional<SchemaReference> schema, String name, String type) implements TableReference {

    public TableReferenceR(String name) {
        this(Optional.empty(), name);
    }

    public TableReferenceR(Optional<SchemaReference> schema, String name) {
        this(schema, name, "TABLE");
    }

    public TableReferenceR(String name, String type) {
        this(Optional.empty(), name, type);
    }
}
