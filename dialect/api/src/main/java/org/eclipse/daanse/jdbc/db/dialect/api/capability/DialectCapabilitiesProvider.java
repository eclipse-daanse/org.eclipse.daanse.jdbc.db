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
package org.eclipse.daanse.jdbc.db.dialect.api.capability;

/**
 * Interface for querying SQL dialect capabilities.
 * <p>
 * This interface provides methods to check what features a SQL dialect supports.
 * Capabilities are grouped into cohesive record objects for easier handling.
 */
public interface DialectCapabilitiesProvider {

    /**
     * Returns the aggregate function capabilities of this dialect.
     * <p>
     * Default implementation builds capabilities from legacy boolean methods.
     *
     * @return aggregate capabilities
     */
    default AggregateCapabilities getAggregateCapabilities() {
        return new AggregateCapabilities(
            allowsCountDistinct(),
            allowsMultipleCountDistinct(),
            allowsCompoundCountDistinct(),
            allowsCountDistinctWithOtherAggs(),
            allowsInnerDistinct(),
            allowsMultipleDistinctSqlMeasures(),
            supportsGroupingSets(),
            supportsGroupByExpressions(),
            allowsSelectNotInGroupBy()
        );
    }

    /**
     * Returns the JOIN and FROM clause capabilities of this dialect.
     * <p>
     * Default implementation builds capabilities from legacy boolean methods.
     *
     * @return join capabilities
     */
    default JoinCapabilities getJoinCapabilities() {
        return new JoinCapabilities(
            allowsJoinOn(),
            allowsAs(),
            allowsFromQuery(),
            requiresAliasForFromQuery()
        );
    }

    /**
     * Returns the ORDER BY clause capabilities of this dialect.
     * <p>
     * Default implementation builds capabilities from legacy boolean methods.
     *
     * @return order by capabilities
     */
    default OrderByCapabilities getOrderByCapabilities() {
        return new OrderByCapabilities(
            allowsOrderByAlias(),
            requiresOrderByAlias(),
            requiresUnionOrderByOrdinal(),
            requiresUnionOrderByExprToBeInSelectClause(),
            requiresGroupByAlias(),
            requiresHavingAlias(),
            supportsNullsLast()
        );
    }

    /**
     * Returns the window and analytic function capabilities of this dialect.
     * <p>
     * Default implementation builds capabilities from legacy boolean methods.
     *
     * @return window function capabilities
     */
    default WindowFunctionCapabilities getWindowFunctionCapabilities() {
        return new WindowFunctionCapabilities(
            supportsPercentileDisc(),
            supportsPercentileCont(),
            supportsListAgg(),
            supportsNthValue(),
            supportsNthValueIgnoreNulls()
        );
    }

    /**
     * Returns the name of this dialect.
     *
     * @return dialect name
     */
    String getDialectName();

    /**
     * Returns the maximum column name length allowed by this dialect.
     *
     * @return maximum column name length
     */
    int getMaxColumnNameLength();

    /**
     * Returns whether this dialect allows DDL operations.
     *
     * @return true if DDL is allowed
     */
    boolean allowsDdl();

    /**
     * Returns whether this dialect allows sharing (caching) for the same data source.
     *
     * @return true if dialect sharing is allowed
     */
    boolean allowsDialectSharing();

    /**
     * Returns whether this dialect supports regular expressions in WHERE/HAVING clauses.
     *
     * @return true if regex is supported
     */
    boolean allowsRegularExpressionInWhereClause();

    /**
     * Returns whether this dialect supports multi-value IN expressions.
     * <p>
     * For example: {@code WHERE (col1, col2) IN ((val1a, val2a), (val1b, val2b))}
     *
     * @return true if multi-value IN is supported
     */
    boolean supportsMultiValueInExpr();

    /**
     * Returns whether this dialect supports unlimited value lists in IN clauses.
     *
     * @return true if unlimited value lists are supported
     */
    boolean supportsUnlimitedValueList();

    /**
     * Returns whether this dialect requires drillthrough max rows in LIMIT clause.
     *
     * @return true if required
     */
    boolean requiresDrillthroughMaxRowsInLimit();

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

    /**
     * Returns whether this dialect allows the AS keyword in field aliases.
     *
     * @return true if AS is allowed in field aliases
     */
    boolean allowsFieldAs();

    // Legacy methods - kept as abstract for backward compatibility
    // These are the methods that implementations must provide
    // The capability record getters build from these methods

    /**
     * Returns whether AS keyword is allowed in FROM clause.
     * @see JoinCapabilities#asKeyword()
     */
    boolean allowsAs();

    /**
     * Returns whether subqueries are allowed in FROM clause.
     * @see JoinCapabilities#fromQuery()
     */
    boolean allowsFromQuery();

    /**
     * Returns whether FROM subqueries require an alias.
     * @see JoinCapabilities#requiresAliasForFromQuery()
     */
    boolean requiresAliasForFromQuery();

    /**
     * Returns whether ANSI JOIN...ON syntax is supported.
     * @see JoinCapabilities#joinOn()
     */
    boolean allowsJoinOn();

    /**
     * Returns whether COUNT(DISTINCT) is supported.
     * @see AggregateCapabilities#countDistinct()
     */
    boolean allowsCountDistinct();

    /**
     * Returns whether multiple COUNT(DISTINCT) is supported in same query.
     * @see AggregateCapabilities#multipleCountDistinct()
     */
    boolean allowsMultipleCountDistinct();

    /**
     * Returns whether COUNT(DISTINCT col1, col2) is supported.
     * @see AggregateCapabilities#compoundCountDistinct()
     */
    boolean allowsCompoundCountDistinct();

    /**
     * Returns whether COUNT(DISTINCT) can be used with other aggregates.
     * @see AggregateCapabilities#countDistinctWithOtherAggs()
     */
    boolean allowsCountDistinctWithOtherAggs();

    /**
     * Returns whether DISTINCT is allowed in inner queries.
     * @see AggregateCapabilities#innerDistinct()
     */
    boolean allowsInnerDistinct();

    /**
     * Returns whether multiple distinct SQL measures are supported.
     * @see AggregateCapabilities#multipleDistinctSqlMeasures()
     */
    boolean allowsMultipleDistinctSqlMeasures();

    /**
     * Returns whether GROUPING SETS is supported.
     * @see AggregateCapabilities#groupingSets()
     */
    boolean supportsGroupingSets();

    /**
     * Returns whether expressions in GROUP BY are supported.
     * @see AggregateCapabilities#groupByExpressions()
     */
    boolean supportsGroupByExpressions();

    /**
     * Returns whether SELECT columns not in GROUP BY are allowed.
     * @see AggregateCapabilities#selectNotInGroupBy()
     */
    boolean allowsSelectNotInGroupBy();

    /**
     * Returns whether aliases can be used in ORDER BY.
     * @see OrderByCapabilities#allowsOrderByAlias()
     */
    boolean allowsOrderByAlias();

    /**
     * Returns whether ORDER BY requires SELECT aliases.
     * @see OrderByCapabilities#requiresOrderByAlias()
     */
    boolean requiresOrderByAlias();

    /**
     * Returns whether GROUP BY requires SELECT aliases.
     * @see OrderByCapabilities#requiresGroupByAlias()
     */
    boolean requiresGroupByAlias();

    /**
     * Returns whether HAVING requires SELECT aliases.
     * @see OrderByCapabilities#requiresHavingAlias()
     */
    boolean requiresHavingAlias();

    /**
     * Returns whether UNION ORDER BY requires ordinal position.
     * @see OrderByCapabilities#requiresUnionOrderByOrdinal()
     */
    boolean requiresUnionOrderByOrdinal();

    /**
     * Returns whether UNION ORDER BY expression must be in SELECT clause.
     * @see OrderByCapabilities#requiresUnionOrderByExprInSelect()
     */
    boolean requiresUnionOrderByExprToBeInSelectClause();

    /**
     * Returns whether PERCENTILE_DISC function is supported.
     * @see WindowFunctionCapabilities#percentileDisc()
     */
    boolean supportsPercentileDisc();

    /**
     * Returns whether PERCENTILE_CONT function is supported.
     * @see WindowFunctionCapabilities#percentileCont()
     */
    boolean supportsPercentileCont();

    /**
     * Returns whether list aggregation is supported (LISTAGG, GROUP_CONCAT, STRING_AGG).
     * @see WindowFunctionCapabilities#listAgg()
     */
    boolean supportsListAgg();

    /**
     * Returns whether NTH_VALUE window function is supported.
     * @see WindowFunctionCapabilities#nthValue()
     */
    default boolean supportsNthValue() {
        return false;
    }

    /**
     * Returns whether NTH_VALUE supports IGNORE NULLS / RESPECT NULLS syntax.
     * @see WindowFunctionCapabilities#nthValueIgnoreNulls()
     */
    default boolean supportsNthValueIgnoreNulls() {
        return false;
    }

    /**
     * Returns whether NULLS FIRST/LAST syntax is supported in ORDER BY.
     * @see OrderByCapabilities#supportsNullsLast()
     */
    default boolean supportsNullsLast() {
        return true;
    }
}
