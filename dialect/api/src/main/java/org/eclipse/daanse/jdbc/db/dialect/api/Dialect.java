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

import org.eclipse.daanse.jdbc.db.dialect.api.capability.DialectCapabilitiesProvider;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.AggregationGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.IdentifierQuoter;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.LiteralQuoter;
import org.eclipse.daanse.jdbc.db.dialect.api.generator.SqlGenerator;
import org.eclipse.daanse.jdbc.db.dialect.api.type.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.type.TypeMapper;

/**
 * Helper for generating proper SQL statements in the Dialect of the Database.
 * <p>
 * This interface extends several sub-interfaces that group related functionality:
 * <ul>
 *   <li>{@link IdentifierQuoter} - Methods for quoting SQL identifiers</li>
 *   <li>{@link LiteralQuoter} - Methods for quoting SQL literals</li>
 *   <li>{@link SqlGenerator} - Methods for generating SQL statements</li>
 *   <li>{@link AggregationGenerator} - Methods for generating aggregate functions</li>
 *   <li>{@link DialectCapabilitiesProvider} - Methods for querying dialect capabilities</li>
 *   <li>{@link TypeMapper} - Methods for type mapping</li>
 * </ul>
 * <p>
 * Consumers that only need a subset of functionality can depend on the appropriate
 * sub-interface instead of the full Dialect interface.
 */
public interface Dialect extends IdentifierQuoter, LiteralQuoter, SqlGenerator,
        AggregationGenerator, DialectCapabilitiesProvider, TypeMapper {

    // ========== Capability methods (not in DialectCapabilitiesProvider) ==========

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
     * <p>
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
     * <p>
     * SELECT COUNT(DISTINCT x, y) FROM t
     *
     * @return whether Dialect allows multiple arguments to COUNT DISTINCT
     * @see #allowsCountDistinct()
     * @see #allowsMultipleCountDistinct()
     */
    boolean allowsCompoundCountDistinct();

    /**
     * Returns whether this Dialect supports distinct aggregations.
     * <p>
     * <p>
     * For example, Access does not allow
     * select count(distinct x) from t
     *
     * @return whether Dialect allows COUNT DISTINCT
     */
    boolean allowsCountDistinct();

    /**
     * Returns whether this Dialect supports more than one distinct aggregation in
     * the same query.
     * <p>
     * <p>
     * In Derby 10.1,  select couunt(distinct x) from t
     * is OK, but
     * select couunt(distinct x), count(distinct y) from t
     * gives "Multiple DISTINCT aggregates are not supported at this
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
     * <p>
     * This may be enabled for performance reasons (Vertica)
     *
     * @return whether this Dialect supports more than one distinct aggregation in
     * the same query
     */
    boolean allowsCountDistinctWithOtherAggs();

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
     * Returns whether this dialect supports common SQL Data Definition Language
     * (DDL) statements such as CREATE TABLE and
     * DROP INDEX.
     * <p>
     * <p>
     * Access seems to allow DDL iff the .mdb file is writeable.
     *
     * @return whether this Dialect supports DDL
     * @see java.sql.DatabaseMetaData#isReadOnly()
     */
    boolean allowsDdl();

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
     * <p>
     * <p>
     * For example, in such a dialect,
     * SELECT x, x FROM t GROUP BY x  would be illegal,
     * but  SELECT x AS a, x AS b FROM t ORDER BY a, b
     * <p>
     * <p>
     * would be legal.
     * <p>
     * <p>
     * <p>
     * Infobright is the only such dialect.
     *
     * @return Whether this Dialect can include expressions in the GROUP BY clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresGroupByAlias();

    /**
     * Returns true if this Dialect can include expressions in the ORDER BY clause
     * only by adding an expression to the SELECT clause and using its alias.
     * <p>
     * <p>
     * For example, in such a dialect,
     * SELECT x FROM t ORDER BY x + y  would be illegal,
     * but  SELECT x, x + y AS z FROM t ORDER BY z
     * <p>
     * <p>
     * would be legal.
     * <p>
     * <p>
     * <p>
     * MySQL, DB2 and Ingres are examples of such dialects.
     *
     * @return Whether this Dialect can include expressions in the ORDER BY clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresOrderByAlias();

    /**
     * Returns true if this Dialect can include expressions in the HAVING clause
     * only by adding an expression to the SELECT clause and using its alias.
     * <p>
     * <p>
     * For example, in such a dialect,
     * SELECT CONCAT(x) as foo FROM t HAVING CONCAT(x) LIKE "%"
     * would be illegal, but
     * SELECT CONCAT(x) as foo FROM t HAVING foo LIKE "%"
     * <p>
     * would be legal.
     * <p>
     * <p>
     * <p>
     * MySQL is an example of such dialects.
     *
     * @return Whether this Dialect can include expressions in the HAVING clause
     * only by adding an expression to the SELECT clause and using its alias
     */
    boolean requiresHavingAlias();

    /**
     * Returns true if aliases defined in the SELECT clause can be used as
     * expressions in the ORDER BY clause.
     * <p>
     * <p>
     * For example, in such a dialect,
     * SELECT x, x + y AS z FROM t ORDER BY z
     * <p>
     * would be legal.
     * <p>
     * <p>
     * <p>
     * MySQL, DB2 and Ingres are examples of dialects where this is true; Access is
     * a dialect where this is false.
     *
     * @return Whether aliases defined in the SELECT clause can be used as
     * expressions in the ORDER BY clause.
     */
    boolean allowsOrderByAlias();

    /**
     * Returns true if this dialect allows only integers in the ORDER BY clause of a
     * UNION (or other set operation) query.
     * <p>
     * <p>
     * For example,
     * <p>
     * SELECT x, y + z FROM t
     * UNION ALL
     * SELECT x, y + z FROM t
     * ORDER BY 1, 2
     * <p>
     * is allowed but
     * <p>
     * SELECT x, y, z FROM t
     * UNION ALL
     * SELECT x, y, z FROM t
     * ORDER BY x
     * <p>
     * is not.
     * <p>
     * <p>
     * Teradata is an example of a dialect with this restriction.
     *
     * @return whether this dialect allows only integers in the ORDER BY clause of a
     * UNION (or other set operation) query
     */
    boolean requiresUnionOrderByOrdinal();

    /**
     * Returns true if this dialect allows an expression in the ORDER BY clause of a
     * UNION (or other set operation) query only if it occurs in the SELECT clause.
     * <p>
     * <p>
     * For example,
     * <p>
     * SELECT x, y + z FROM t
     * UNION ALL
     * SELECT x, y + z FROM t
     * ORDER BY y + z
     * is allowed but
     * <p>
     * SELECT x, y, z FROM t
     * UNION ALL
     * SELECT x, y, z FROM t
     * ORDER BY y + z SELECT x, y, z FROM t ORDER BY y + z
     * is not.
     * <p>
     * Access is an example of a dialect with this restriction.
     *
     * @return whether this dialect allows an expression in the ORDER BY clause of a
     * UNION (or other set operation) query only if it occurs in the SELECT
     * clause
     */
    boolean requiresUnionOrderByExprToBeInSelectClause();

    /**
     * Returns true if this dialect supports multi-value IN expressions. E.g.,
     * <p>
     * WHERE (col1, col2) IN ((val1a, val2a), (val1b, val2b))
     *
     * @return true if the dialect supports multi-value IN expressions
     */
    boolean supportsMultiValueInExpr();

    /**
     * Returns whether this Dialect supports the given concurrency type in
     * combination with the given result set type.
     * <p>
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
     * Returns whether this Dialect object can be used for all connections from the
     * same data source.
     * <p>
     * The default implementation returns {@code true}, and this allows dialects to
     * be cached and reused in environments where connections are allocated from a
     * pool based on the same data source.
     * <p>
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
     * <p>
     * For example, {@code SELECT empno, first_name || ' ' || last_name FROM
     * emps GROUP BY empno} is valid because {@code empno} is the primary key of the
     * {@code emps} table, and therefore all columns are dependent on it. For a
     * given value of {@code empno}, {@code first_name || ' ' || last_name} has a
     * unique value.
     * <p>
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
     * Chooses the most appropriate type for accessing the values of a column in a
     * result set for a dialect.
     * <p>
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

    // ========== Dialect-specific methods (not inherited from parent interfaces) ==========

    /**
     * Returns the name of this dialect.
     *
     * @return dialect name
     */
    String getDialectName();

    /**
     * Returns whether this dialect supports parallel data loading.
     *
     * @return true if parallel loading is supported
     */
    boolean supportParallelLoading();

    /**
     * Returns whether this dialect supports batch operations.
     *
     * @return true if batch operations are supported
     */
    boolean supportBatchOperations();

}
