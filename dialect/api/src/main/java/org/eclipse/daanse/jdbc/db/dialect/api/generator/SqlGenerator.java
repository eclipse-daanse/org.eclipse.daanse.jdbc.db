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

import java.util.List;
import java.util.Map;

import org.eclipse.daanse.jdbc.db.dialect.api.type.Datatype;

/**
 * Interface for generating SQL expressions and statements in a dialect-specific manner.
 * <p>
 * This interface provides methods for generating common SQL constructs like
 * inline datasets, ORDER BY items, regular expressions, and SQL functions.
 */
public interface SqlGenerator {

    /**
     * Wraps an expression into an 'to upper case' SQL function call.
     * <p>
     * For example: {@code "foo.bar"} becomes {@code "UPPER(foo.bar)"} or {@code "UCASE(foo.bar)"}
     *
     * @param sqlExpression expression to wrap
     * @return function-wrapped expression
     */
    StringBuilder wrapIntoSqlUpperCaseFunction(CharSequence sqlExpression);

    /**
     * Wraps an expression into an 'If Then Else' SQL function call.
     * <p>
     * For example: {@code "CASE WHEN ifValue THEN thenValue ELSE elseValue END"}
     * or {@code "IIF(ifValue, thenValue, elseValue)"}
     *
     * @param condition      the IF-condition
     * @param thenExpression the Then-expression
     * @param elseExpression the Else-expression
     * @return function-wrapped expression
     */
    StringBuilder wrapIntoSqlIfThenElseFunction(CharSequence condition, CharSequence thenExpression,
                                                 CharSequence elseExpression);

    /**
     * Generates a SQL statement to represent an inline dataset.
     * <p>
     * For example, this may generate a VALUES clause or a series of
     * {@code SELECT ... FROM dual UNION ALL} statements depending on dialect.
     *
     * @param columnNames names of the columns in the inline table
     * @param columnTypes types of the columns
     * @param valueList   list of rows, each row being an array of values
     * @return SQL for inline dataset
     */
    StringBuilder generateInline(List<String> columnNames, List<String> columnTypes,
                                  List<String[]> valueList);

    /**
     * Generates an ORDER BY item with proper NULL handling.
     *
     * @param expr             expression to order by
     * @param nullable         whether the expression can be null
     * @param ascending        true for ASC, false for DESC
     * @param collateNullsLast true to put NULLs last, false for NULLs first
     * @return ORDER BY clause item
     */
    StringBuilder generateOrderItem(CharSequence expr, boolean nullable, boolean ascending,
                                     boolean collateNullsLast);

    /**
     * Generates an ORDER BY item for a specific order value with proper NULL handling.
     *
     * @param expr             expression to order by
     * @param orderValue       the specific value to order
     * @param datatype         datatype of the order value
     * @param ascending        true for ASC, false for DESC
     * @param collateNullsLast true to put NULLs last, false for NULLs first
     * @return ORDER BY clause item
     */
    StringBuilder generateOrderItemForOrderValue(CharSequence expr, String orderValue, Datatype datatype,
                                                  boolean ascending, boolean collateNullsLast);

    /**
     * Generates a count expression, wrapping as needed for specific dialects.
     * <p>
     * For example, Greenplum may need special NULL handling.
     *
     * @param exp expression to count
     * @return count expression
     */
    StringBuilder generateCountExpression(CharSequence exp);

    /**
     * Generates a regular expression match in the dialect's syntax.
     * <p>
     * Converts a Java regex pattern to the dialect-specific syntax
     * (e.g., Oracle's REGEXP_LIKE, PostgreSQL's ~).
     *
     * @param source     source column or expression to match against
     * @param javaRegExp Java regular expression pattern
     * @return regex match expression, or null if not supported
     */
    StringBuilder generateRegularExpression(String source, String javaRegExp);

    /**
     * Generates a UNION ALL SQL statement from a list of value maps.
     *
     * @param valueList list of maps where each map represents a row
     *                  with column name to (datatype, value) entries
     * @return UNION ALL SQL statement
     */
    StringBuilder generateUnionAllSql(List<Map<String, Map.Entry<Datatype, Object>>> valueList);

    /**
     * Generates DDL to clear (truncate) a table.
     *
     * @param schemaName schema name (may be null)
     * @param tableName  table name
     * @return SQL statement to clear the table
     */
    String clearTable(String schemaName, String tableName);

    /**
     * Generates DDL to drop a table.
     *
     * @param schemaName schema name (may be null)
     * @param tableName  table name
     * @param ifExists   whether to include IF EXISTS clause
     * @return SQL statement to drop the table
     */
    String dropTable(String schemaName, String tableName, boolean ifExists);

    /**
     * Generates DDL to create a schema.
     *
     * @param schemaName schema name
     * @param ifExists   whether to include IF NOT EXISTS clause
     * @return SQL statement to create the schema
     */
    String createSchema(String schemaName, boolean ifExists);

    /**
     * Appends dialect-specific hints after the FROM clause.
     *
     * @param buf   buffer to append to
     * @param hints map of hint names to values
     */
    void appendHintsAfterFromClause(StringBuilder buf, Map<String, String> hints);
}
