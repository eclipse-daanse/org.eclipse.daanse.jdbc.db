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

public interface IdentifierInfo {

    /**
     * Retrieves the string used to quote SQL identifiers. This method returns a
     * space " " if identifier quoting is not supported.
     *
     * @return the quoting string or a space if quoting is not supported
     */
    String quoteString();

}
