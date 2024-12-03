/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2019 Hitachi Vantara and others.
 * Copyright (C) 2021 Sergei Semenkov
 * All rights reserved.
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
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.jdbc.db.dialect.api;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Helper for generating proper SQL statements in the Dialect of the Database.
 */
public interface Dialect {

    /**
     * Warps an {@link CharSequence} expression into an 'to upper case' SQL function
     * call.
     *
     *
     * "foo.bar" -> "UPPER(foo.bar)"
     * "foo.bar" -> "UCASE(foo.bar)"
     *
     * @param sqlExpression to wrap
     * @return function wrapped version
     */
    StringBuilder wrapIntoSqlUpperCaseFunction(CharSequence sqlExpression);

    /**
     * Warps an {@link CharSequence} expression into an 'If Then Else' SQL function
     * call.
     *
     * "case when ifValue then thenValue else elseValue end"
     * ""Iif( ifValue , thenValue, elseValue)
     *
     *
     *
     * @param idCondition    {@link CharSequence} with the IF-Condition
     * @param thenExpression {@link CharSequence} with the Then-Expression
     * @param elseExpression {@link CharSequence} with the Else-Expression
     * @return function wrapped version
     */
    StringBuilder wrapIntoSqlIfThenElseFunction(CharSequence idCondition, CharSequence thenExpression,
                                                CharSequence elseExpression);

    /**
     * Encloses an identifier in quotation marks appropriate for this Dialect.
     *
     *
     * For example, quoteIdentifier("emp") yields a string containing
     * "emp" in Oracle, and a string containing [emp] in
     * Access.
     *
     * @param val Identifier
     * @return Quoted identifier
     */
    StringBuilder quoteIdentifier(CharSequence val);

    /**
     * Appends to a buffer an identifier, quoted appropriately for this Dialect.
     *
     * @param val identifier to quote (must not be null).
     * @param buf Buffer
     */
    void quoteIdentifier(String val, StringBuilder buf);

    /**
     * Encloses an identifier in quotation marks appropriate for the current SQL
     * dialect. For example, in Oracle, where the identifiers are quoted using
     * double-quotes, quoteIdentifier("schema","table") yields a string
     * containing "schema"."table".
     *
     * @param qual Qualifier. If it is not null, qual. is
     *             prepended.
     * @param name Name to be quoted.
     * @return Quoted identifier
     */
    String quoteIdentifier(String qual, String name);

    /**
     * Appends to a buffer a list of identifiers, quoted appropriately for this
     * Dialect.
     *
     *
     * Names in the list may be null, but there must be at least one non-null name
     * in the list.
     *
     *
     * @param buf   Buffer
     * @param names List of names to be quoted
     */
    void quoteIdentifier(StringBuilder buf, String... names);

    /**
     * Returns the character which is used to quote identifiers, or null if quoting
     * is not supported.
     *
     * @return identifier quote
     */
    String getQuoteIdentifierString();

    /**
     * Appends to a buffer a single-quoted SQL string.
     *
     *
     * For example, in the default dialect,
     * quoteStringLiteral(buf, "Can't") appends "'Can''t'"
     * to buf.
     *
     * @param buf Buffer to append to
     * @param s   Literal
     */
    void quoteStringLiteral(StringBuilder buf, String s);

    /**
     * Appends to a buffer a numeric literal.
     *
     *
     * In the default dialect, numeric literals are printed as is.
     *
     * @param buf   Buffer to append to
     * @param value Literal
     */
    void quoteNumericLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a boolean literal.
     *
     *
     * In the default dialect, boolean literals are printed as is.
     *
     * @param buf   Buffer to append to
     * @param value Literal
     */
    void quoteBooleanLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a date literal.
     *
     *
     * For example, in the default dialect,
     * quoteStringLiteral(buf, "1969-03-17") appends
     * DATE '1969-03-17'.
     *
     * @param buf   Buffer to append to
     * @param value Literal
     */
    void quoteDateLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a time literal.
     *
     *
     * For example, in the default dialect,
     * quoteStringLiteral(buf, "12:34:56") appends
     * TIME '12:34:56'.
     *
     * @param buf   Buffer to append to
     * @param value Literal
     */
    void quoteTimeLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a timestamp literal.
     *
     *
     * For example, in the default dialect,
     * quoteStringLiteral(buf, "1969-03-17 12:34:56") appends
     * TIMESTAMP '1969-03-17 12:34:56'.
     *
     * @param buf   Buffer to append to
     * @param value Literal
     */
    void quoteTimestampLiteral(StringBuilder buf, String value);

    /**
     * Appends to a buffer a decimal literal.
     *
     *
     * For example, in the default dialect,
     * quoteDecimalLiteral(buf, "12.58") appends
     * FLOAT('12.58') for DB2.
     *
     * @param value Literal
     */

    StringBuilder quoteDecimalLiteral(CharSequence value);

    /**
     * Returns whether this Dialect requires subqueries in the FROM clause to have
     * an alias.
     *
     * @return whether dialewct requires subqueries to have an alias
     * @see #allowsFromQuery()
     */
    boolean requiresAliasForFromQuery();

    /**
     * Returns whether the SQL dialect allows "AS" in the FROM clause. If so,
     * "SELECT * FROM t AS alias" is a valid query.
     *
     * @return whether dialect allows AS in FROM clause
     */
    boolean allowsAs();

    /**
     * Returns whether this Dialect allows a subquery in the from clause, for
     * example
     *
     * SELECT * FROM (SELECT * FROM t) AS
     * x
     *
     * @return whether Dialect allows subquery in FROM clause
     * @see #requiresAliasForFromQuery()
     */
    boolean allowsFromQuery();

    /**
     * Returns whether this Dialect allows multiple arguments to the
     * COUNT(DISTINCT ...) aggregate function, for example
     *
     * SELECT COUNT(DISTINCT x, y) FROM t
     *
     * @return whether Dialect allows multiple arguments to COUNT DISTINCT
     * @see #allowsCountDistinct()
     * @see #allowsMultipleCountDistinct()
     */
    boolean allowsCompoundCountDistinct();

    /**
     * Returns whether this Dialect supports distinct aggregations.
     *
     *
     * For example, Access does not allow
     * select count(distinct x) from t
     *
     * @return whether Dialect allows COUNT DISTINCT
     */
    boolean allowsCountDistinct();

    /**
     * Returns whether this Dialect supports more than one distinct aggregation in
     * the same query.
     *
     *
     * In Derby 10.1,  select couunt(distinct x) from t
     *  is OK, but
     * select couunt(distinct x), count(distinct y) from t
     *  gives "Multiple DISTINCT aggregates are not supported at this
     * time."
     *
     * @return whether this Dialect supports more than one distinct aggregation in
     * the same query
     */
    boolean allowsMultipleCountDistinct();

    /**
     * Returns whether this Dialect has performant support of distinct SQL measures
     * in the same query.
     *
     * @return whether this dialect supports multiple count(distinct subquery)
     * measures in one query.
     */
    boolean allowsMultipleDistinctSqlMeasures();

    /**
     * Returns whether this Dialect supports distinct aggregations with other
     * aggregations in the same query.
     *
     * This may be enabled for performance reasons (Vertica)
     *
     * @return whether this Dialect supports more than one distinct aggregation in
     * the same query
     */
    boolean allowsCountDistinctWithOtherAggs();

    /**
     * Generates a SQL statement to represent an inline dataset.
     *
     *
     * For example, for Oracle, generates
     *
     *
     * SELECT 1 AS FOO, 'a' AS BAR FROM dual
     * UNION ALL
     * SELECT 2 AS FOO, 'b' AS BAR FROM dual
     *
     *
     *
     * For ANSI SQL, generates:
     *
     *
     * VALUES (1, 'a'), (2, 'b')
     *
     *
     * @param columnNames List of column names
     * @param columnTypes List of column types ("String" or "Numeric")
     * @param valueList   List of rows values
     * @return SQL string
     */
    StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList);

    /**
     * If Double values need to include additional exponent in its string
     * represenation. This is to make sure that Double literals will be interpreted
     * as doubles by LucidDB.
     *
     * @param value       Double value to generate string for
     * @param valueString java string representation for this value.
     * @return whether an additional exponent "E0" needs to be appended
     */
    boolean needsExponent(Object value, String valueString);

    /**
     * Appends to a buffer a value quoted for its type.
     *
     * @param buf      Buffer to append to
     * @param value    Value
     * @param datatype Datatype of value
     */
    void quote(StringBuilder buf, Object value, Datatype datatype);

    /**
     * Returns whether this dialect supports common SQL Data Definition Language
     * (DDL) statements such as CREATE TABLE and
     * DROP INDEX.
     *
     *
     * Access seems to allow DDL iff the .mdb file is writeable.
     *
     * @return whether this Dialect supports DDL
     * @see java.sql.DatabaseMetaData#isReadOnly()
     */
    boolean allowsDdl();

    /**
     * Generates an item for an ORDER BY clause, sorting in the required direction,
     * and ensuring that NULL values collate either before or after all non-NULL
     * values, depending on the collateNullsLast parameter.
     *
     * @param expr             Expression
     * @param nullable         Whether expression may have NULL values
     * @param ascending        Whether to sort expression ascending
     * @param collateNullsLast Whether the null values should be sorted first or
     *                         last.
     * @return Expression modified so that NULL values collate last
     */
    StringBuilder generateOrderItem(CharSequence expr, boolean nullable, boolean ascending, boolean collateNullsLast);

    /**
     * Returns whether this Dialect supports expressions in the GROUP BY clause.
     * Derby/Cloudscape and Infobright do not.
     *
     * @return Whether this Dialect allows expressions in the GROUP BY clause
     */
    boolean supportsGroupByExpressions();

    /**
     * Returns whether this Dialect allows the GROUPING SETS construct in the GROUP
     * BY clause. Currently Greenplum, IBM DB2, Oracle, and Teradata.
     *
     * @return Whether this Dialect allows GROUPING SETS clause
     */
    boolean supportsGroupingSets();

    /**
     * Returns whether this Dialect places no limit on the number of rows which can
     * appear as elements of an IN or VALUES expression.
     *
     * @return whether value list length is unlimited
     */
    boolean supportsUnlimitedValueList();

    /**
     * Returns true if this Dialect can include expressions in the GROUP BY clause
     * only by adding an expression to the SELECT clause and using its alias.
     *
     *
     * For example, in such a dialect,
     * SELECT x, x FROM t GROUP BY x  would be illegal,
     * but  SELECT x AS a, x AS b FROM t ORDER BY a, b
     *
     *
     * would be legal.
     *
     *
     *
     * Infobright is the only such dialect.
     *
     *
     * @return Whether this Dialect can include expressions in the GROUP BY clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresGroupByAlias();

    /**
     * Returns true if this Dialect can include expressions in the ORDER BY clause
     * only by adding an expression to the SELECT clause and using its alias.
     *
     *
     * For example, in such a dialect,
     * SELECT x FROM t ORDER BY x + y  would be illegal,
     * but  SELECT x, x + y AS z FROM t ORDER BY z
     *
     *
     * would be legal.
     *
     *
     *
     * MySQL, DB2 and Ingres are examples of such dialects.
     *
     *
     * @return Whether this Dialect can include expressions in the ORDER BY clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresOrderByAlias();

    /**
     * Returns true if this Dialect can include expressions in the HAVING clause
     * only by adding an expression to the SELECT clause and using its alias.
     *
     *
     * For example, in such a dialect,
     * SELECT CONCAT(x) as foo FROM t HAVING CONCAT(x) LIKE "%"
     *  would be illegal, but
     * SELECT CONCAT(x) as foo FROM t HAVING foo LIKE "%"
     *
     * would be legal.
     *
     *
     *
     * MySQL is an example of such dialects.
     *
     *
     * @return Whether this Dialect can include expressions in the HAVING clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresHavingAlias();

    /**
     * Returns true if aliases defined in the SELECT clause can be used as
     * expressions in the ORDER BY clause.
     *
     *
     * For example, in such a dialect,
     * SELECT x, x + y AS z FROM t ORDER BY z
     *
     * would be legal.
     *
     *
     *
     * MySQL, DB2 and Ingres are examples of dialects where this is true; Access is
     * a dialect where this is false.
     *
     *
     * @return Whether aliases defined in the SELECT clause can be used as
     * expressions in the ORDER BY clause.
     */
    boolean allowsOrderByAlias();

    /**
     * Returns true if this dialect allows only integers in the ORDER BY clause of a
     * UNION (or other set operation) query.
     *
     *
     * For example,
     *
     * SELECT x, y + z FROM t
     * UNION ALL
     * SELECT x, y + z FROM t
     * ORDER BY 1, 2
     *
     * is allowed but
     *
     * SELECT x, y, z FROM t
     * UNION ALL
     * SELECT x, y, z FROM t
     * ORDER BY x
     *
     * is not.
     *
     *
     * Teradata is an example of a dialect with this restriction.
     *
     * @return whether this dialect allows only integers in the ORDER BY clause of a
     * UNION (or other set operation) query
     */
    boolean requiresUnionOrderByOrdinal();

    /**
     * Returns true if this dialect allows an expression in the ORDER BY clause of a
     * UNION (or other set operation) query only if it occurs in the SELECT clause.
     *
     *
     * For example,
     *
     * SELECT x, y + z FROM t
     * UNION ALL
     * SELECT x, y + z FROM t
     * ORDER BY y + z
     * is allowed but
     *
     * SELECT x, y, z FROM t
     * UNION ALL
     * SELECT x, y, z FROM t
     * ORDER BY y + z SELECT x, y, z FROM t ORDER BY y + z
     * is not.
     *
     * Access is an example of a dialect with this restriction.
     *
     * @return whether this dialect allows an expression in the ORDER BY clause of a
     * UNION (or other set operation) query only if it occurs in the SELECT
     * clause
     */
    boolean requiresUnionOrderByExprToBeInSelectClause();

    /**
     * Returns true if this dialect supports multi-value IN expressions. E.g.,
     *
     * WHERE (col1, col2) IN ((val1a, val2a), (val1b, val2b))
     *
     * @return true if the dialect supports multi-value IN expressions
     */
    boolean supportsMultiValueInExpr();

    /**
     * Returns whether this Dialect supports the given concurrency type in
     * combination with the given result set type.
     *
     * The result is similar to
     * {@link java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)},
     * except that the JdbcOdbc bridge in JDK 1.6 overstates its abilities. See bug
     * 1690406.
     *
     * @param type        defined in {@link java.sql.ResultSet}
     * @param concurrency type defined in {@link java.sql.ResultSet}
     * @return true if so; false otherwise
     */
    boolean supportsResultSetConcurrency(int type, int concurrency);

    /**
     * Returns the maximum length of the name of a database column or query alias
     * allowed by this dialect.
     *
     * @return maximum number of characters in a column name
     * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()
     */
    int getMaxColumnNameLength();

    /**
     * Assembles and returns a string containing any hints that should be appended
     * after the FROM clause in a SELECT statement, based on any hints provided. Any
     * unrecognized or unsupported hints will be ignored.
     *
     * @param buf   The Stringbuffer to which the dialect-specific syntax for any
     *              relevant table hints may be appended. Must not be null.
     * @param hints A map of table hints provided in the schema definition
     */
    void appendHintsAfterFromClause(StringBuilder buf, Map<String, String> hints);

    /**
     * Returns whether this Dialect object can be used for all connections from the
     * same data source.
     *
     * The default implementation returns {@code true}, and this allows dialects to
     * be cached and reused in environments where connections are allocated from a
     * pool based on the same data source.
     *
     * Data sources are deemed 'equal' by the same criteria used by Java
     * collections, namely the {@link Object#equals(Object)} and
     * {@link Object#hashCode()} methods.
     *
     * @return Whether this dialect can be used for other connections created from
     * the same data source
     */
    boolean allowsDialectSharing();

    /**
     * Returns whether the database currently permits queries to include in the
     * SELECT clause expressions that are not listed in the GROUP BY clause. The SQL
     * standard allows this if the database can deduce that the expression is
     * functionally dependent on columns in the GROUP BY clause.
     *
     * For example, {@code SELECT empno, first_name || ' ' || last_name FROM
     * emps GROUP BY empno} is valid because {@code empno} is the primary key of the
     * {@code emps} table, and therefore all columns are dependent on it. For a
     * given value of {@code empno}, {@code first_name || ' ' || last_name} has a
     * unique value.
     *
     * Most databases do not, MySQL is an example of one that does (if the
     * functioality is enabled).
     *
     * @return Whether this Dialect allows SELECT clauses to contain columns that
     * are not in the GROUP BY clause
     */
    boolean allowsSelectNotInGroupBy();

    /**
     * Returns whether this dialect supports "ANSI-style JOIN syntax",
     * {@code FROM leftTable JOIN rightTable ON conditon}.
     *
     * @return Whether this dialect supports FROM-JOIN-ON syntax.
     */
    boolean allowsJoinOn();

    /**
     * Informs Mondrian if the dialect supports regular expressions when creating
     * the 'where' or the 'having' clause.
     *
     * @return True if regular expressions are supported.
     */
    boolean allowsRegularExpressionInWhereClause();

    /**
     * Some databases, like Greenplum, don't include nulls as part of the results of
     * a COUNT sql call. This allows dialects to wrap the count expression in
     * something before it is used in the query.
     *
     * @param exp The expression to wrap.
     * @return A valid expression to use for a count operation.
     */
    StringBuilder generateCountExpression(CharSequence exp);

    /**
     * Must generate a String representing a regular expression match operation
     * between a string literal and a Java regular expression. The string literal
     * might be a column identifier or some other identifier, but the implementation
     * must presume that it is already escaped and fit for use. The regular
     * expression is not escaped and must be adapted to the proper dialect rules.
     * Postgres / Greenplum example:
     * generateRegularExpression(
     * "'foodmart'.'customer_name'", "(?i).*oo.*") ->
     * 'foodmart'.'customer_name' ~ "(?i).*oo.*"
     * Oracle example:
     * generateRegularExpression(
     * "'foodmart'.'customer_name'", ".*oo.*") ->
     * REGEXP_LIKE('foodmart'.'customer_name', ".*oo.*")
     *
     * Dialects are allowed to return null if the dialect cannot convert that
     * particular regular expression into something that the database would support.
     *
     * @param source     A String identifying the column to match against.
     * @param javaRegExp A Java regular expression to match against.
     * @return A dialect specific matching operation, or null if the dialect cannot
     * convert that particular regular expression into something that the
     * database would support.
     */
    StringBuilder generateRegularExpression(String source, String javaRegExp);

    /**
     * Chooses the most appropriate type for accessing the values of a column in a
     * result set for a dialect.
     *
     * Dialect-specific nuances involving type representation should be encapsulated
     * in implementing methods. For example, if a dialect has implicit rules
     * involving scale or precision, they should be handled within this method so
     * the client can simply retrieve the "best fit" SqlStatement.Type for the
     * column.
     *
     * @param metadata    Results set metadata
     * @param columnIndex Column ordinal (0-based)
     * @return the most appropriate SqlStatement.Type for the column
     */
    BestFitColumnType getType(ResultSetMetaData metadata, int columnIndex) throws SQLException;

    boolean requiresDrillthroughMaxRowsInLimit();

    /**
     * Returns whether the SQL dialect allows "AS" in the 'field' clause. If so,
     * "SELECT field1.table1 AS alias FROM t" is a valid query.
     *
     * @return whether dialect allows 'AS alias' in field clause
     */
    boolean allowsFieldAs();

    /**
     * Returns whether the SQL dialect allows 'distinct' in inner queries clause. If
     * so, "select count(m0) from (select distinct f.z as m0 from f, dim1, dim2
     * where dim1.k = f.k1 and dim2.k = f.k2) as dummyname" is a valid query.
     *
     * @return whether dialect allows 'distinct' in inner queries
     */
    boolean allowsInnerDistinct();

    String getDialectName();

    String clearTable(String schemaName, String tableName);

    String dropTable(String schemaName, String tableName, boolean ifExists);

    boolean supportParallelLoading();

    boolean supportBatchOperations();

    String createSchema(String schemaName, boolean ifExists);

    StringBuilder generateUnionAllSql(List<Map<String, Map.Entry<Datatype, Object>>> valueList);
}
