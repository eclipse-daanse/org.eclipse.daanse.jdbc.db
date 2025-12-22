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
package org.eclipse.daanse.jdbc.db.dialect.api.generator;

import org.eclipse.daanse.jdbc.db.dialect.api.type.QuoteStyle;

/**
 * Interface for quoting SQL identifiers (table names, column names, schema names, etc.)
 * in a dialect-specific manner.
 * <p>
 * Different databases use different quoting characters:
 * <ul>
 *   <li>Oracle, PostgreSQL, DB2: double quotes ("identifier")</li>
 *   <li>MySQL: backticks (`identifier`)</li>
 *   <li>SQL Server, Access: square brackets ([identifier])</li>
 * </ul>
 */
public interface IdentifierQuoter {

    /**
     * Encloses an identifier in quotation marks appropriate for this Dialect.
     * <p>
     * For example, {@code quoteIdentifier("emp")} yields a string containing
     * {@code "emp"} in Oracle, and a string containing {@code [emp]} in Access.
     *
     * @param val identifier to quote
     * @return quoted identifier as StringBuilder
     */
    StringBuilder quoteIdentifier(CharSequence val);

    /**
     * Appends to a buffer an identifier, quoted appropriately for this Dialect.
     *
     * @param val identifier to quote (must not be null)
     * @param buf buffer to append to
     */
    void quoteIdentifier(String val, StringBuilder buf);

    /**
     * Encloses an identifier in quotation marks appropriate for the current SQL dialect.
     * <p>
     * For example, in Oracle where identifiers are quoted using double-quotes,
     * {@code quoteIdentifier("schema", "table")} yields a string containing
     * {@code "schema"."table"}.
     *
     * @param qual qualifier (schema name); if not null, prepended with separator
     * @param name name to be quoted
     * @return quoted identifier
     */
    String quoteIdentifier(String qual, String name);

    /**
     * Appends to a buffer a list of identifiers, quoted appropriately for this Dialect.
     * <p>
     * Names in the list may be null, but there must be at least one non-null name.
     *
     * @param buf   buffer to append to
     * @param names list of names to be quoted
     */
    void quoteIdentifier(StringBuilder buf, String... names);

    /**
     * Returns the character which is used to quote identifiers, or null if quoting
     * is not supported.
     *
     * @return identifier quote string
     */
    String getQuoteIdentifierString();

    /**
     * Returns the quote style used by this dialect.
     *
     * @return the QuoteStyle enum value
     */
    default QuoteStyle getQuoteStyle() {
        String quote = getQuoteIdentifierString();
        if (quote == null || quote.isEmpty()) {
            return QuoteStyle.NONE;
        }
        return switch (quote) {
            case "\"" -> QuoteStyle.DOUBLE_QUOTE;
            case "`" -> QuoteStyle.BACKTICK;
            case "[" -> QuoteStyle.SQUARE_BRACKET;
            default -> QuoteStyle.DOUBLE_QUOTE;
        };
    }
}
