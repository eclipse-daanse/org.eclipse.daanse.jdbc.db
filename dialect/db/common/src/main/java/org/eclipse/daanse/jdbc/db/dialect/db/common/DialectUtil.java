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
package org.eclipse.daanse.jdbc.db.dialect.db.common;

import java.util.regex.Pattern;

public class DialectUtil {

    private static final Pattern UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN = Pattern.compile( "\\|\\(\\?u\\)" );
    private static final String EMPTY = "";

    /**
     * Regular expression pattern for matching Java's \\Q...\\E quote escape sequences.
     * Used by dialects that need to convert Java regex escapes to database-specific syntax.
     */
    public static final String ESCAPE_REGEXP = "(\\\\Q([^\\\\Q]+)\\\\E)";

    /**
     * Compiled pattern for {@link #ESCAPE_REGEXP}.
     */
    public static final Pattern ESCAPE_PATTERN = Pattern.compile(ESCAPE_REGEXP);

    private DialectUtil() {
        // constructor
    }

    /**
     * Cleans up the reqular expression from the unicode-aware case folding embedded flag expression (?u)
     *
     * @param javaRegExp
     *          the regular expression to clean up
     * @return the cleaned regular expression
     */
    public static String cleanUnicodeAwareCaseFlag( String javaRegExp ) {
        String cleaned = javaRegExp;
        if ( cleaned != null && isUnicodeCaseFlagInRegExp( cleaned ) ) {
            cleaned = UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN.matcher( cleaned ).replaceAll( EMPTY );
        }
        return cleaned;
    }

    private static boolean isUnicodeCaseFlagInRegExp( String javaRegExp ) {
        return UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN.matcher( javaRegExp ).find();
    }

    /**
     * Encloses a value in single-quotes, to make a SQL string value.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code singleQuoteString("foo")} yields {@code 'foo'}</li>
     *   <li>{@code singleQuoteString("don't")} yields {@code 'don''t'}</li>
     * </ul>
     *
     * @param val the value to quote
     * @return the quoted string
     */
    public static String singleQuoteString(String val) {
        StringBuilder buf = new StringBuilder(64);
        singleQuoteString(val, buf);
        return buf.toString();
    }

    /**
     * Appends a value enclosed in single-quotes to a StringBuilder.
     * <p>
     * Single quotes within the value are escaped by doubling them.
     *
     * @param val the value to quote
     * @param buf the buffer to append to
     */
    public static void singleQuoteString(String val, StringBuilder buf) {
        buf.append('\'');
        String escaped = val.replace("'", "''");
        buf.append(escaped);
        buf.append('\'');
    }

    /**
     * Generates ORDER BY clause with NULL handling using ISNULL function.
     * <p>
     * For databases where NULL is treated as negative infinity (smallest value),
     * this function generates appropriate ordering clauses:
     * <ul>
     *   <li>ASC + nullsLast: {@code ISNULL(expr) ASC, expr ASC} - pushes nulls to bottom</li>
     *   <li>DESC + nullsLast: {@code expr DESC} - natural behavior, nulls at top</li>
     *   <li>ASC + nullsFirst: {@code expr ASC} - natural behavior, nulls at top</li>
     *   <li>DESC + nullsFirst: {@code ISNULL(expr) DESC, expr DESC} - pushes nulls to top</li>
     * </ul>
     * Used by MySQL, Hive and similar databases.
     *
     * @param expr the expression to order by
     * @param ascending true for ASC, false for DESC
     * @param collateNullsLast true if nulls should appear last
     * @return the generated ORDER BY expression
     */
    public static StringBuilder generateOrderByNullsWithIsnull(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        if (collateNullsLast) {
            if (ascending) {
                return new StringBuilder("ISNULL(").append(expr)
                    .append(") ASC, ").append(expr).append(" ASC");
            } else {
                return new StringBuilder(expr).append(" DESC");
            }
        } else {
            if (ascending) {
                return new StringBuilder(expr).append(" ASC");
            } else {
                return new StringBuilder("ISNULL(").append(expr)
                    .append(") DESC, ").append(expr).append(" DESC");
            }
        }
    }

}
