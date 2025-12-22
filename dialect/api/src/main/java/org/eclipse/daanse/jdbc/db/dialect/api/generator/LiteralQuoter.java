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

import org.eclipse.daanse.jdbc.db.dialect.api.type.Datatype;

/**
 * Interface for quoting SQL literals (strings, numbers, dates, etc.)
 * in a dialect-specific manner.
 * <p>
 * Different databases have different requirements for literal representation,
 * especially for dates, timestamps, and special string escaping.
 */
public interface LiteralQuoter {

    /**
     * Appends to a buffer a single-quoted SQL string.
     * <p>
     * For example, in the default dialect,
     * {@code quoteStringLiteral(buf, "Can't")} appends {@code 'Can''t'} to buf.
     *
     * @param buf buffer to append to
     * @param s   literal string value
     */
    void quoteStringLiteral(StringBuilder buf, String s);

    /**
     * Appends to a buffer a numeric literal.
     * <p>
     * In the default dialect, numeric literals are printed as-is.
     *
     * @param buf   buffer to append to
     * @param value literal value
     */
    void quoteNumericLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a boolean literal.
     * <p>
     * In the default dialect, boolean literals are printed as-is.
     *
     * @param buf   buffer to append to
     * @param value literal value
     */
    void quoteBooleanLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a date literal.
     * <p>
     * For example, in the default dialect,
     * {@code quoteDateLiteral(buf, "1969-03-17")} appends {@code DATE '1969-03-17'}.
     *
     * @param buf   buffer to append to
     * @param value literal value in ISO format
     */
    void quoteDateLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a time literal.
     * <p>
     * For example, in the default dialect,
     * {@code quoteTimeLiteral(buf, "12:34:56")} appends {@code TIME '12:34:56'}.
     *
     * @param buf   buffer to append to
     * @param value literal value in ISO format
     */
    void quoteTimeLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a timestamp literal.
     * <p>
     * For example, in the default dialect,
     * {@code quoteTimestampLiteral(buf, "1969-03-17 12:34:56")} appends
     * {@code TIMESTAMP '1969-03-17 12:34:56'}.
     *
     * @param buf   buffer to append to
     * @param value literal value in ISO format
     */
    void quoteTimestampLiteral(StringBuilder buf, String value);

    /**
     * Returns a decimal literal, appropriately quoted for this dialect.
     * <p>
     * For example, in DB2, {@code quoteDecimalLiteral("12.58")} returns
     * {@code FLOAT('12.58')}.
     *
     * @param value literal value
     * @return quoted decimal literal
     */
    StringBuilder quoteDecimalLiteral(CharSequence value);

    /**
     * Quotes a value based on its datatype.
     * <p>
     * This is a polymorphic method that delegates to the appropriate
     * type-specific quoting method based on the datatype.
     *
     * @param buf      buffer to append to
     * @param value    value to quote
     * @param datatype the SQL datatype of the value
     */
    void quote(StringBuilder buf, Object value, Datatype datatype);
}
