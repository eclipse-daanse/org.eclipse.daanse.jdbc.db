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
package org.eclipse.daanse.jdbc.db.api.schema;

import java.util.Optional;

/**
 * Reference to a user-defined or system function in a database
 */
public interface FunctionReference extends Named {

    /**
     * The schema containing this function
     *
     * @return the schema reference, or empty if not applicable
     */
    Optional<SchemaReference> schema();

    /**
     * The specific name of this function (unique within its schema)
     *
     * @return the specific name
     */
    String specificName();
}
