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
package org.eclipse.daanse.jdbc.db.dialect.api.type;

/**
 * Represents the quoting style used for SQL identifiers in different database dialects.
 * <p>
 * Different databases use different characters to quote identifiers (table names,
 * column names, etc.) to allow special characters or reserved words.
 */
public enum QuoteStyle {

    /**
     * ANSI SQL standard double quotes: "identifier"
     * Used by: Oracle, PostgreSQL, DB2, H2, Derby, HSQLDB
     */
    DOUBLE_QUOTE("\"", "\""),

    /**
     * Backtick quotes: `identifier`
     * Used by: MySQL, MariaDB
     */
    BACKTICK("`", "`"),

    /**
     * Square bracket quotes: [identifier]
     * Used by: Microsoft SQL Server, Microsoft Access, Sybase
     */
    SQUARE_BRACKET("[", "]"),

    /**
     * No quoting - identifiers are used as-is.
     * Rarely used; most dialects require some form of quoting.
     */
    NONE("", "");

    private final String openQuote;
    private final String closeQuote;

    QuoteStyle(String openQuote, String closeQuote) {
        this.openQuote = openQuote;
        this.closeQuote = closeQuote;
    }

    /**
     * Returns the opening quote character(s).
     *
     * @return the opening quote string
     */
    public String openQuote() {
        return openQuote;
    }

    /**
     * Returns the closing quote character(s).
     *
     * @return the closing quote string
     */
    public String closeQuote() {
        return closeQuote;
    }

    /**
     * Quotes the given identifier using this quote style.
     *
     * @param identifier the identifier to quote
     * @return the quoted identifier
     */
    public String quote(String identifier) {
        if (this == NONE || identifier == null) {
            return identifier;
        }
        return openQuote + identifier + closeQuote;
    }

    /**
     * Appends the quoted identifier to the given StringBuilder.
     *
     * @param buf the StringBuilder to append to
     * @param identifier the identifier to quote
     */
    public void quote(StringBuilder buf, String identifier) {
        if (identifier == null) {
            return;
        }
        buf.append(openQuote).append(identifier).append(closeQuote);
    }
}
