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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

/**
 * OSGi metatype configuration for a configurable SQL dialect.
 * <p>
 * This interface defines all configurable aspects of a SQL dialect that can be
 * set programmatically or via OSGi configuration. It covers identifier quoting,
 * SQL syntax features, and dialect-specific behaviors.
 */
@ObjectClassDefinition(name = ConfigurableDialectConfig.L10N_OCD_NAME, description = ConfigurableDialectConfig.L10N_OCD_DESCRIPTION, localization = ConfigurableDialectConfig.OCD_LOCALIZATION)
public @interface ConfigurableDialectConfig {

    // ========== Localization Constants ==========

    String OCD_LOCALIZATION = "OSGI-INF/l10n/org.eclipse.daanse.jdbc.db.dialect.configurable.ocd";
    String L10N_PREFIX = "%";
    String L10N_POSTFIX_NAME = ".name";
    String L10N_POSTFIX_DESCRIPTION = ".description";
    String L10N_POSTFIX_OPTION = ".option";
    String L10N_POSTFIX_LABEL = ".label";

    String L10N_OCD_NAME = L10N_PREFIX + "ocd.dialect" + L10N_POSTFIX_NAME;
    String L10N_OCD_DESCRIPTION = L10N_PREFIX + "ocd.dialect" + L10N_POSTFIX_DESCRIPTION;

    // ========== L10N for Identifier Quoting ==========

    String L10N_QUOTE_ID_NAME = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING + L10N_POSTFIX_NAME;
    String L10N_QUOTE_ID_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING
            + L10N_POSTFIX_DESCRIPTION;
    String L10N_QUOTE_ID_OPTION_DOUBLE = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING + L10N_POSTFIX_OPTION
            + ".double" + L10N_POSTFIX_LABEL;
    String L10N_QUOTE_ID_OPTION_BACKTICK = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING
            + L10N_POSTFIX_OPTION + ".backtick" + L10N_POSTFIX_LABEL;
    String L10N_QUOTE_ID_OPTION_BRACKET = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING + L10N_POSTFIX_OPTION
            + ".bracket" + L10N_POSTFIX_LABEL;
    String L10N_QUOTE_ID_OPTION_NONE = L10N_PREFIX + Constants.PROPERTY_QUOTE_IDENTIFIER_STRING + L10N_POSTFIX_OPTION
            + ".none" + L10N_POSTFIX_LABEL;

    String L10N_DIALECT_NAME_NAME = L10N_PREFIX + Constants.PROPERTY_DIALECT_NAME + L10N_POSTFIX_NAME;
    String L10N_DIALECT_NAME_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_DIALECT_NAME + L10N_POSTFIX_DESCRIPTION;

    // ========== L10N for SQL Syntax Features ==========

    String L10N_ALLOWS_AS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWS_AS + L10N_POSTFIX_NAME;
    String L10N_ALLOWS_AS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWS_AS + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWS_FROM_QUERY_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWS_FROM_QUERY + L10N_POSTFIX_NAME;
    String L10N_ALLOWS_FROM_QUERY_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWS_FROM_QUERY
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_REQUIRES_ALIAS_FOR_FROM_QUERY_NAME = L10N_PREFIX + Constants.PROPERTY_REQUIRES_ALIAS_FOR_FROM_QUERY
            + L10N_POSTFIX_NAME;
    String L10N_REQUIRES_ALIAS_FOR_FROM_QUERY_DESCRIPTION = L10N_PREFIX
            + Constants.PROPERTY_REQUIRES_ALIAS_FOR_FROM_QUERY + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWS_JOIN_ON_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWS_JOIN_ON + L10N_POSTFIX_NAME;
    String L10N_ALLOWS_JOIN_ON_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWS_JOIN_ON + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWS_FIELD_AS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWS_FIELD_AS + L10N_POSTFIX_NAME;
    String L10N_ALLOWS_FIELD_AS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWS_FIELD_AS
            + L10N_POSTFIX_DESCRIPTION;

    // ========== Default Values ==========

    String DEFAULT_QUOTE_IDENTIFIER_STRING = "\"";
    String DEFAULT_DIALECT_NAME = "configurable";
    boolean DEFAULT_ALLOWS_AS = true;
    boolean DEFAULT_ALLOWS_FROM_QUERY = true;
    boolean DEFAULT_REQUIRES_ALIAS_FOR_FROM_QUERY = false;
    boolean DEFAULT_ALLOWS_JOIN_ON = true;
    boolean DEFAULT_ALLOWS_FIELD_AS = true;
    boolean DEFAULT_ALLOWS_COUNT_DISTINCT = true;
    boolean DEFAULT_ALLOWS_MULTIPLE_COUNT_DISTINCT = true;
    boolean DEFAULT_ALLOWS_COMPOUND_COUNT_DISTINCT = false;
    boolean DEFAULT_ALLOWS_COUNT_DISTINCT_WITH_OTHER_AGGS = true;
    boolean DEFAULT_ALLOWS_MULTIPLE_DISTINCT_SQL_MEASURES = true;
    boolean DEFAULT_ALLOWS_INNER_DISTINCT = true;
    boolean DEFAULT_SUPPORTS_GROUP_BY_EXPRESSIONS = true;
    boolean DEFAULT_SUPPORTS_GROUPING_SETS = false;
    boolean DEFAULT_REQUIRES_GROUP_BY_ALIAS = false;
    boolean DEFAULT_ALLOWS_SELECT_NOT_IN_GROUP_BY = false;
    boolean DEFAULT_REQUIRES_ORDER_BY_ALIAS = false;
    boolean DEFAULT_ALLOWS_ORDER_BY_ALIAS = true;
    boolean DEFAULT_REQUIRES_UNION_ORDER_BY_ORDINAL = true;
    boolean DEFAULT_REQUIRES_UNION_ORDER_BY_EXPR_IN_SELECT = true;
    boolean DEFAULT_REQUIRES_HAVING_ALIAS = false;
    boolean DEFAULT_SUPPORTS_UNLIMITED_VALUE_LIST = false;
    boolean DEFAULT_SUPPORTS_MULTI_VALUE_IN_EXPR = false;
    boolean DEFAULT_ALLOWS_DDL = true;
    boolean DEFAULT_ALLOWS_REGEX_IN_WHERE = false;
    boolean DEFAULT_SUPPORTS_BIT_AND_AGG = false;
    boolean DEFAULT_SUPPORTS_BIT_OR_AGG = false;
    boolean DEFAULT_SUPPORTS_BIT_XOR_AGG = false;
    boolean DEFAULT_SUPPORTS_BIT_NAND_AGG = false;
    boolean DEFAULT_SUPPORTS_BIT_NOR_AGG = false;
    boolean DEFAULT_SUPPORTS_BIT_NXOR_AGG = false;
    boolean DEFAULT_SUPPORTS_PERCENTILE_CONT_AGG = false;
    boolean DEFAULT_SUPPORTS_PERCENTILE_DISC_AGG = false;
    boolean DEFAULT_SUPPORTS_PERCENTILE_CONT = false;
    boolean DEFAULT_SUPPORTS_PERCENTILE_DISC = false;
    boolean DEFAULT_SUPPORTS_LIST_AGG = false;
    int DEFAULT_MAX_COLUMN_NAME_LENGTH = 128;
    boolean DEFAULT_ALLOWS_DIALECT_SHARING = true;
    boolean DEFAULT_REQUIRES_DRILLTHROUGH_MAX_ROWS_IN_LIMIT = false;
    boolean DEFAULT_SUPPORT_PARALLEL_LOADING = true;
    boolean DEFAULT_SUPPORT_BATCH_OPERATIONS = true;

    // ========== Identifier Quoting ==========

    @AttributeDefinition(name = L10N_QUOTE_ID_NAME, description = L10N_QUOTE_ID_DESCRIPTION, defaultValue = DEFAULT_QUOTE_IDENTIFIER_STRING, options = {
            @Option(label = L10N_QUOTE_ID_OPTION_DOUBLE, value = Constants.OPTION_QUOTE_DOUBLE),
            @Option(label = L10N_QUOTE_ID_OPTION_BACKTICK, value = Constants.OPTION_QUOTE_BACKTICK),
            @Option(label = L10N_QUOTE_ID_OPTION_BRACKET, value = Constants.OPTION_QUOTE_SQUARE_BRACKET),
            @Option(label = L10N_QUOTE_ID_OPTION_NONE, value = Constants.OPTION_QUOTE_NONE) })
    String quoteIdentifierString() default DEFAULT_QUOTE_IDENTIFIER_STRING;

    // ========== Dialect Identification ==========

    @AttributeDefinition(name = L10N_DIALECT_NAME_NAME, description = L10N_DIALECT_NAME_DESCRIPTION, defaultValue = DEFAULT_DIALECT_NAME)
    String dialectName() default DEFAULT_DIALECT_NAME;

    // ========== SQL Syntax Features ==========

    @AttributeDefinition(name = L10N_ALLOWS_AS_NAME, description = L10N_ALLOWS_AS_DESCRIPTION, defaultValue = DEFAULT_ALLOWS_AS
            + "")
    boolean allowsAs() default DEFAULT_ALLOWS_AS;

    @AttributeDefinition(name = L10N_ALLOWS_FROM_QUERY_NAME, description = L10N_ALLOWS_FROM_QUERY_DESCRIPTION, defaultValue = DEFAULT_ALLOWS_FROM_QUERY
            + "")
    boolean allowsFromQuery() default DEFAULT_ALLOWS_FROM_QUERY;

    @AttributeDefinition(name = L10N_REQUIRES_ALIAS_FOR_FROM_QUERY_NAME, description = L10N_REQUIRES_ALIAS_FOR_FROM_QUERY_DESCRIPTION, defaultValue = DEFAULT_REQUIRES_ALIAS_FOR_FROM_QUERY
            + "")
    boolean requiresAliasForFromQuery() default DEFAULT_REQUIRES_ALIAS_FOR_FROM_QUERY;

    @AttributeDefinition(name = L10N_ALLOWS_JOIN_ON_NAME, description = L10N_ALLOWS_JOIN_ON_DESCRIPTION, defaultValue = DEFAULT_ALLOWS_JOIN_ON
            + "")
    boolean allowsJoinOn() default DEFAULT_ALLOWS_JOIN_ON;

    @AttributeDefinition(name = L10N_ALLOWS_FIELD_AS_NAME, description = L10N_ALLOWS_FIELD_AS_DESCRIPTION, defaultValue = DEFAULT_ALLOWS_FIELD_AS
            + "")
    boolean allowsFieldAs() default DEFAULT_ALLOWS_FIELD_AS;

    // ========== COUNT and DISTINCT Features ==========

    @AttributeDefinition(name = "%allowsCountDistinct.name", description = "%allowsCountDistinct.description", defaultValue = DEFAULT_ALLOWS_COUNT_DISTINCT
            + "")
    boolean allowsCountDistinct() default DEFAULT_ALLOWS_COUNT_DISTINCT;

    @AttributeDefinition(name = "%allowsMultipleCountDistinct.name", description = "%allowsMultipleCountDistinct.description", defaultValue = DEFAULT_ALLOWS_MULTIPLE_COUNT_DISTINCT
            + "")
    boolean allowsMultipleCountDistinct() default DEFAULT_ALLOWS_MULTIPLE_COUNT_DISTINCT;

    @AttributeDefinition(name = "%allowsCompoundCountDistinct.name", description = "%allowsCompoundCountDistinct.description", defaultValue = DEFAULT_ALLOWS_COMPOUND_COUNT_DISTINCT
            + "")
    boolean allowsCompoundCountDistinct() default DEFAULT_ALLOWS_COMPOUND_COUNT_DISTINCT;

    @AttributeDefinition(name = "%allowsCountDistinctWithOtherAggs.name", description = "%allowsCountDistinctWithOtherAggs.description", defaultValue = DEFAULT_ALLOWS_COUNT_DISTINCT_WITH_OTHER_AGGS
            + "")
    boolean allowsCountDistinctWithOtherAggs() default DEFAULT_ALLOWS_COUNT_DISTINCT_WITH_OTHER_AGGS;

    @AttributeDefinition(name = "%allowsMultipleDistinctSqlMeasures.name", description = "%allowsMultipleDistinctSqlMeasures.description", defaultValue = DEFAULT_ALLOWS_MULTIPLE_DISTINCT_SQL_MEASURES
            + "")
    boolean allowsMultipleDistinctSqlMeasures() default DEFAULT_ALLOWS_MULTIPLE_DISTINCT_SQL_MEASURES;

    @AttributeDefinition(name = "%allowsInnerDistinct.name", description = "%allowsInnerDistinct.description", defaultValue = DEFAULT_ALLOWS_INNER_DISTINCT
            + "")
    boolean allowsInnerDistinct() default DEFAULT_ALLOWS_INNER_DISTINCT;

    // ========== GROUP BY Features ==========

    @AttributeDefinition(name = "%supportsGroupByExpressions.name", description = "%supportsGroupByExpressions.description", defaultValue = DEFAULT_SUPPORTS_GROUP_BY_EXPRESSIONS
            + "")
    boolean supportsGroupByExpressions() default DEFAULT_SUPPORTS_GROUP_BY_EXPRESSIONS;

    @AttributeDefinition(name = "%supportsGroupingSets.name", description = "%supportsGroupingSets.description", defaultValue = DEFAULT_SUPPORTS_GROUPING_SETS
            + "")
    boolean supportsGroupingSets() default DEFAULT_SUPPORTS_GROUPING_SETS;

    @AttributeDefinition(name = "%requiresGroupByAlias.name", description = "%requiresGroupByAlias.description", defaultValue = DEFAULT_REQUIRES_GROUP_BY_ALIAS
            + "")
    boolean requiresGroupByAlias() default DEFAULT_REQUIRES_GROUP_BY_ALIAS;

    @AttributeDefinition(name = "%allowsSelectNotInGroupBy.name", description = "%allowsSelectNotInGroupBy.description", defaultValue = DEFAULT_ALLOWS_SELECT_NOT_IN_GROUP_BY
            + "")
    boolean allowsSelectNotInGroupBy() default DEFAULT_ALLOWS_SELECT_NOT_IN_GROUP_BY;

    // ========== ORDER BY Features ==========

    @AttributeDefinition(name = "%requiresOrderByAlias.name", description = "%requiresOrderByAlias.description", defaultValue = DEFAULT_REQUIRES_ORDER_BY_ALIAS
            + "")
    boolean requiresOrderByAlias() default DEFAULT_REQUIRES_ORDER_BY_ALIAS;

    @AttributeDefinition(name = "%allowsOrderByAlias.name", description = "%allowsOrderByAlias.description", defaultValue = DEFAULT_ALLOWS_ORDER_BY_ALIAS
            + "")
    boolean allowsOrderByAlias() default DEFAULT_ALLOWS_ORDER_BY_ALIAS;

    @AttributeDefinition(name = "%requiresUnionOrderByOrdinal.name", description = "%requiresUnionOrderByOrdinal.description", defaultValue = DEFAULT_REQUIRES_UNION_ORDER_BY_ORDINAL
            + "")
    boolean requiresUnionOrderByOrdinal() default DEFAULT_REQUIRES_UNION_ORDER_BY_ORDINAL;

    @AttributeDefinition(name = "%requiresUnionOrderByExprToBeInSelectClause.name", description = "%requiresUnionOrderByExprToBeInSelectClause.description", defaultValue = DEFAULT_REQUIRES_UNION_ORDER_BY_EXPR_IN_SELECT
            + "")
    boolean requiresUnionOrderByExprToBeInSelectClause() default DEFAULT_REQUIRES_UNION_ORDER_BY_EXPR_IN_SELECT;

    // ========== HAVING Features ==========

    @AttributeDefinition(name = "%requiresHavingAlias.name", description = "%requiresHavingAlias.description", defaultValue = DEFAULT_REQUIRES_HAVING_ALIAS
            + "")
    boolean requiresHavingAlias() default DEFAULT_REQUIRES_HAVING_ALIAS;

    // ========== Value List and IN Features ==========

    @AttributeDefinition(name = "%supportsUnlimitedValueList.name", description = "%supportsUnlimitedValueList.description", defaultValue = DEFAULT_SUPPORTS_UNLIMITED_VALUE_LIST
            + "")
    boolean supportsUnlimitedValueList() default DEFAULT_SUPPORTS_UNLIMITED_VALUE_LIST;

    @AttributeDefinition(name = "%supportsMultiValueInExpr.name", description = "%supportsMultiValueInExpr.description", defaultValue = DEFAULT_SUPPORTS_MULTI_VALUE_IN_EXPR
            + "")
    boolean supportsMultiValueInExpr() default DEFAULT_SUPPORTS_MULTI_VALUE_IN_EXPR;

    // ========== DDL Features ==========

    @AttributeDefinition(name = "%allowsDdl.name", description = "%allowsDdl.description", defaultValue = DEFAULT_ALLOWS_DDL
            + "")
    boolean allowsDdl() default DEFAULT_ALLOWS_DDL;

    // ========== Regular Expression Features ==========

    @AttributeDefinition(name = "%allowsRegularExpressionInWhereClause.name", description = "%allowsRegularExpressionInWhereClause.description", defaultValue = DEFAULT_ALLOWS_REGEX_IN_WHERE
            + "")
    boolean allowsRegularExpressionInWhereClause() default DEFAULT_ALLOWS_REGEX_IN_WHERE;

    // ========== Aggregation Function Support ==========

    @AttributeDefinition(name = "%supportsBitAndAgg.name", description = "%supportsBitAndAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_AND_AGG
            + "")
    boolean supportsBitAndAgg() default DEFAULT_SUPPORTS_BIT_AND_AGG;

    @AttributeDefinition(name = "%supportsBitOrAgg.name", description = "%supportsBitOrAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_OR_AGG
            + "")
    boolean supportsBitOrAgg() default DEFAULT_SUPPORTS_BIT_OR_AGG;

    @AttributeDefinition(name = "%supportsBitXorAgg.name", description = "%supportsBitXorAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_XOR_AGG
            + "")
    boolean supportsBitXorAgg() default DEFAULT_SUPPORTS_BIT_XOR_AGG;

    @AttributeDefinition(name = "%supportsBitNAndAgg.name", description = "%supportsBitNAndAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_NAND_AGG
            + "")
    boolean supportsBitNAndAgg() default DEFAULT_SUPPORTS_BIT_NAND_AGG;

    @AttributeDefinition(name = "%supportsBitNOrAgg.name", description = "%supportsBitNOrAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_NOR_AGG
            + "")
    boolean supportsBitNOrAgg() default DEFAULT_SUPPORTS_BIT_NOR_AGG;

    @AttributeDefinition(name = "%supportsBitNXorAgg.name", description = "%supportsBitNXorAgg.description", defaultValue = DEFAULT_SUPPORTS_BIT_NXOR_AGG
            + "")
    boolean supportsBitNXorAgg() default DEFAULT_SUPPORTS_BIT_NXOR_AGG;

    @AttributeDefinition(name = "%supportsPercentileContAgg.name", description = "%supportsPercentileContAgg.description", defaultValue = DEFAULT_SUPPORTS_PERCENTILE_CONT_AGG
            + "")
    boolean supportsPercentileContAgg() default DEFAULT_SUPPORTS_PERCENTILE_CONT_AGG;

    @AttributeDefinition(name = "%supportsPercentileDiscAgg.name", description = "%supportsPercentileDiscAgg.description", defaultValue = DEFAULT_SUPPORTS_PERCENTILE_DISC_AGG
            + "")
    boolean supportsPercentileDiscAgg() default DEFAULT_SUPPORTS_PERCENTILE_DISC_AGG;

    @AttributeDefinition(name = "%supportsPercentileCont.name", description = "%supportsPercentileCont.description", defaultValue = DEFAULT_SUPPORTS_PERCENTILE_CONT
            + "")
    boolean supportsPercentileCont() default DEFAULT_SUPPORTS_PERCENTILE_CONT;

    @AttributeDefinition(name = "%supportsPercentileDisc.name", description = "%supportsPercentileDisc.description", defaultValue = DEFAULT_SUPPORTS_PERCENTILE_DISC
            + "")
    boolean supportsPercentileDisc() default DEFAULT_SUPPORTS_PERCENTILE_DISC;

    @AttributeDefinition(name = "%supportsListAgg.name", description = "%supportsListAgg.description", defaultValue = DEFAULT_SUPPORTS_LIST_AGG
            + "")
    boolean supportsListAgg() default DEFAULT_SUPPORTS_LIST_AGG;

    // ========== Column and Metadata ==========

    @AttributeDefinition(name = "%maxColumnNameLength.name", description = "%maxColumnNameLength.description", defaultValue = DEFAULT_MAX_COLUMN_NAME_LENGTH
            + "")
    int maxColumnNameLength() default DEFAULT_MAX_COLUMN_NAME_LENGTH;

    // ========== Other Features ==========

    @AttributeDefinition(name = "%allowsDialectSharing.name", description = "%allowsDialectSharing.description", defaultValue = DEFAULT_ALLOWS_DIALECT_SHARING
            + "")
    boolean allowsDialectSharing() default DEFAULT_ALLOWS_DIALECT_SHARING;

    @AttributeDefinition(name = "%requiresDrillthroughMaxRowsInLimit.name", description = "%requiresDrillthroughMaxRowsInLimit.description", defaultValue = DEFAULT_REQUIRES_DRILLTHROUGH_MAX_ROWS_IN_LIMIT
            + "")
    boolean requiresDrillthroughMaxRowsInLimit() default DEFAULT_REQUIRES_DRILLTHROUGH_MAX_ROWS_IN_LIMIT;

    @AttributeDefinition(name = "%supportParallelLoading.name", description = "%supportParallelLoading.description", defaultValue = DEFAULT_SUPPORT_PARALLEL_LOADING
            + "")
    boolean supportParallelLoading() default DEFAULT_SUPPORT_PARALLEL_LOADING;

    @AttributeDefinition(name = "%supportBatchOperations.name", description = "%supportBatchOperations.description", defaultValue = DEFAULT_SUPPORT_BATCH_OPERATIONS
            + "")
    boolean supportBatchOperations() default DEFAULT_SUPPORT_BATCH_OPERATIONS;
}
