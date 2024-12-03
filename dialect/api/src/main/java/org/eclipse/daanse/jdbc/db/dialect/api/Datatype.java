/*
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * For more information please visit the Project: Hitachi Vantara - Mondrian
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial adapt parts of Syntax.class
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.api;

import java.util.stream.Stream;

/**
 * Datatype of a column.
 */
public enum Datatype {
    STRING("String") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteStringLiteral(buf, value);
        }
    },

    NUMERIC("Numeric") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteNumericLiteral(buf, value);
        }

        @Override
        public boolean isNumeric() {
            return true;
        }
    },

    INTEGER("Integer") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteNumericLiteral(buf, value);
        }

        @Override
        public boolean isNumeric() {
            return true;
        }
    },

    BOOLEAN("Boolean") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteBooleanLiteral(buf, value);
        }
    },

    DATE("Date") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteDateLiteral(buf, value);
        }
    },

    TIME("Time") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteTimeLiteral(buf, value);
        }
    },

    TIMESTAMP("Timestamp") {
        @Override
        public void quoteValue(
            StringBuilder buf, Dialect dialect, String value) {
            dialect.quoteTimestampLiteral(buf, value);
        }
    };

    private String value;

    Datatype(java.lang.String value) {
        this.value = value;
    }

    public java.lang.String getValue() {
        return value;
    }

    /**
     * Appends to a buffer a value of this type, in the appropriate format
     * for this dialect.
     *
     * @param buf     Buffer
     * @param dialect Dialect
     * @param value   Value
     */
    public abstract void quoteValue(
        StringBuilder buf,
        Dialect dialect,
        String value);

    /**
     * Returns whether this is a numeric datatype.
     *
     * @return whether this is a numeric datatype.
     */
    public boolean isNumeric() {
        return false;
    }

    public static Datatype fromValue(String v) {
        return Stream.of(Datatype.values())
            .filter(e -> (e.getValue().equals(v)))
            .findFirst().orElse(NUMERIC);
        // TODO:  care about fallback
//            .orElseThrow(() -> new IllegalArgumentException(
//                new StringBuilder("Datatype enum Illegal argument ").append(v)
//                    .toString())
//            );
    }
}
