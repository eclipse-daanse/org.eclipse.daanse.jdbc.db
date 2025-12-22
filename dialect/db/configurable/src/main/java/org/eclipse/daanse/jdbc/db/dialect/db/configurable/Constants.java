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
package org.eclipse.daanse.jdbc.db.dialect.db.configurable;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.osgi.framework.Bundle;

/**
 * Constants for the configurable dialect {@link Bundle}.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Constant for the {@link org.osgi.framework.Constants#SERVICE_PID} of a
     * {@link Dialect} Service.
     */
    public static final String PID_DIALECT = "org.eclipse.daanse.jdbc.db.dialect.configurable";

    // ========== Identifier Quoting ==========

    public static final String PROPERTY_QUOTE_IDENTIFIER_STRING = "quoteIdentifierString";
    public static final String PROPERTY_DIALECT_NAME = "dialectName";

    // ========== SQL Syntax Features ==========

    public static final String PROPERTY_ALLOWS_AS = "allowsAs";
    public static final String PROPERTY_ALLOWS_FROM_QUERY = "allowsFromQuery";
    public static final String PROPERTY_REQUIRES_ALIAS_FOR_FROM_QUERY = "requiresAliasForFromQuery";
    public static final String PROPERTY_ALLOWS_JOIN_ON = "allowsJoinOn";
    public static final String PROPERTY_ALLOWS_FIELD_AS = "allowsFieldAs";

    // ========== COUNT and DISTINCT Features ==========

    public static final String PROPERTY_ALLOWS_COUNT_DISTINCT = "allowsCountDistinct";
    public static final String PROPERTY_ALLOWS_MULTIPLE_COUNT_DISTINCT = "allowsMultipleCountDistinct";
    public static final String PROPERTY_ALLOWS_COMPOUND_COUNT_DISTINCT = "allowsCompoundCountDistinct";
    public static final String PROPERTY_ALLOWS_COUNT_DISTINCT_WITH_OTHER_AGGS = "allowsCountDistinctWithOtherAggs";
    public static final String PROPERTY_ALLOWS_MULTIPLE_DISTINCT_SQL_MEASURES = "allowsMultipleDistinctSqlMeasures";
    public static final String PROPERTY_ALLOWS_INNER_DISTINCT = "allowsInnerDistinct";

    // ========== GROUP BY Features ==========

    public static final String PROPERTY_SUPPORTS_GROUP_BY_EXPRESSIONS = "supportsGroupByExpressions";
    public static final String PROPERTY_SUPPORTS_GROUPING_SETS = "supportsGroupingSets";
    public static final String PROPERTY_REQUIRES_GROUP_BY_ALIAS = "requiresGroupByAlias";
    public static final String PROPERTY_ALLOWS_SELECT_NOT_IN_GROUP_BY = "allowsSelectNotInGroupBy";

    // ========== ORDER BY Features ==========

    public static final String PROPERTY_REQUIRES_ORDER_BY_ALIAS = "requiresOrderByAlias";
    public static final String PROPERTY_ALLOWS_ORDER_BY_ALIAS = "allowsOrderByAlias";
    public static final String PROPERTY_REQUIRES_UNION_ORDER_BY_ORDINAL = "requiresUnionOrderByOrdinal";
    public static final String PROPERTY_REQUIRES_UNION_ORDER_BY_EXPR_IN_SELECT = "requiresUnionOrderByExprToBeInSelectClause";

    // ========== HAVING Features ==========

    public static final String PROPERTY_REQUIRES_HAVING_ALIAS = "requiresHavingAlias";

    // ========== Value List and IN Features ==========

    public static final String PROPERTY_SUPPORTS_UNLIMITED_VALUE_LIST = "supportsUnlimitedValueList";
    public static final String PROPERTY_SUPPORTS_MULTI_VALUE_IN_EXPR = "supportsMultiValueInExpr";

    // ========== DDL Features ==========

    public static final String PROPERTY_ALLOWS_DDL = "allowsDdl";

    // ========== Regular Expression Features ==========

    public static final String PROPERTY_ALLOWS_REGEX_IN_WHERE = "allowsRegularExpressionInWhereClause";

    // ========== Aggregation Function Support ==========

    public static final String PROPERTY_SUPPORTS_BIT_AND_AGG = "supportsBitAndAgg";
    public static final String PROPERTY_SUPPORTS_BIT_OR_AGG = "supportsBitOrAgg";
    public static final String PROPERTY_SUPPORTS_BIT_XOR_AGG = "supportsBitXorAgg";
    public static final String PROPERTY_SUPPORTS_BIT_NAND_AGG = "supportsBitNAndAgg";
    public static final String PROPERTY_SUPPORTS_BIT_NOR_AGG = "supportsBitNOrAgg";
    public static final String PROPERTY_SUPPORTS_BIT_NXOR_AGG = "supportsBitNXorAgg";
    public static final String PROPERTY_SUPPORTS_PERCENTILE_CONT_AGG = "supportsPercentileContAgg";
    public static final String PROPERTY_SUPPORTS_PERCENTILE_DISC_AGG = "supportsPercentileDiscAgg";
    public static final String PROPERTY_SUPPORTS_PERCENTILE_CONT = "supportsPercentileCont";
    public static final String PROPERTY_SUPPORTS_PERCENTILE_DISC = "supportsPercentileDisc";
    public static final String PROPERTY_SUPPORTS_LIST_AGG = "supportsListAgg";

    // ========== Column and Metadata ==========

    public static final String PROPERTY_MAX_COLUMN_NAME_LENGTH = "maxColumnNameLength";

    // ========== Other Features ==========

    public static final String PROPERTY_ALLOWS_DIALECT_SHARING = "allowsDialectSharing";
    public static final String PROPERTY_REQUIRES_DRILLTHROUGH_MAX_ROWS_IN_LIMIT = "requiresDrillthroughMaxRowsInLimit";
    public static final String PROPERTY_SUPPORT_PARALLEL_LOADING = "supportParallelLoading";
    public static final String PROPERTY_SUPPORT_BATCH_OPERATIONS = "supportBatchOperations";

    // ========== Quote Style Options ==========

    public static final String OPTION_QUOTE_DOUBLE = "\"";
    public static final String OPTION_QUOTE_BACKTICK = "`";
    public static final String OPTION_QUOTE_SQUARE_BRACKET = "[";
    public static final String OPTION_QUOTE_NONE = "";
}
