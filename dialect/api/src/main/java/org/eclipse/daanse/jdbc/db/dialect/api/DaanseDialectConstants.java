/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.dialect.api;

/**
 * Holds constants for Daanse Dialect related OSGi properties.
 */
public final class DaanseDialectConstants {

    private DaanseDialectConstants() {
        // Utility class, no instantiation
    }

    /**
     * The base prefix for all Daanse-related OSGi properties.
     */
    public static final String PREFIX = "org.eclipse.daanse";

    /**
     * The property key for the dialect name.
     * Equivalent to "org.eclipse.daanse.dialect.name".
     */
    public static final String DIALECT_NAME_PROPERTY = PREFIX + ".dialect.name";
}
