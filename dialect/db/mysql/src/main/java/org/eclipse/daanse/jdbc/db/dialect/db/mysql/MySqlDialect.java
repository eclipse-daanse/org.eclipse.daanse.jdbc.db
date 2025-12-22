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
package org.eclipse.daanse.jdbc.db.dialect.db.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.eclipse.daanse.jdbc.db.dialect.api.order.OrderedColumn;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.eclipse.daanse.jdbc.db.dialect.api.Dialect} for the
 * MySQL database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class MySqlDialect extends JdbcDialectImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlDialect.class);
    private static final String SUPPORTED_PRODUCT_NAME = "MYSQL";

    public MySqlDialect(Connection connection) {
        super(connection);
        try {
            if (isInfobright(connection.getMetaData())) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            LOGGER.warn("Could not get DatabaseMetadata", e);
        }
    }

    /**
     * Detects whether this database is Infobright.
     *
     * <p>
     * Infobright uses the MySQL driver and appears to be a MySQL instance. The only
     * difference is the presence of the BRIGHTHOUSE engine.
     *
     * @param metaData DatabaseMetaData
     * @return Whether this is Infobright
     */
    public static boolean isInfobright(DatabaseMetaData metaData) {
        // Infobright detection is currently disabled. A separate Infobright dialect
        // or a configurable flag could be added if Infobright support is needed.
        // Detection would require querying for the BRIGHTHOUSE engine presence.
        return false;
    }

    @Override
    protected String deduceProductName(DatabaseMetaData metaData) throws SQLException {
        final String productName = super.deduceProductName(metaData);
        if (isInfobright(metaData)) {
            return "MySQL (Infobright)";
        }
        return productName;
    }

    @Override
    protected String deduceIdentifierQuoteString(DatabaseMetaData metaData) throws SQLException {
        String quoteIdentifierString = super.deduceIdentifierQuoteString(metaData);

        if (quoteIdentifierString == null) {
            // mm.mysql.2.0.4 driver lies. We know better.
            quoteIdentifierString = "`";
        }
        return quoteIdentifierString;
    }

    @Override
    protected boolean deduceSupportsSelectNotInGroupBy(DatabaseMetaData metaData) throws SQLException {
        // MySQL 5.7+ includes ONLY_FULL_GROUP_BY in SQL_MODE by default, requiring all
        // SELECT columns to be in GROUP BY or be aggregate functions. This method
        // returns false to enforce strict GROUP BY compliance. For flexible GROUP BY
        // behavior, the ConfigurableDialect provides this as a configurable option.
        return false;
    }


    @Override
    public void appendHintsAfterFromClause(StringBuilder buf, Map<String, String> hints) {
        if (hints != null) {
            String forcedIndex = hints.get("force_index");
            if (forcedIndex != null) {
                buf.append(" FORCE INDEX (");
                buf.append(forcedIndex);
                buf.append(")");
            }
        }
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    public boolean allowsFromQuery() {
        // MySQL before 4.0 does not allow FROM
        // subqueries in the FROM clause.
        return productVersion.compareTo("4.") >= 0;
    }

    @Override
    public boolean allowsCompoundCountDistinct() {
        return true;
    }

    @Override
    public void quoteStringLiteral(StringBuilder buf, String s) {
        // Go beyond standard singleQuoteString; also quote backslash.
        buf.append('\'');
        String s0 = s.replace("'", "''");
        String s1 = s0.replace("\\", "\\\\");
        buf.append(s1);
        buf.append('\'');
    }

    @Override
    public void quoteBooleanLiteral(StringBuilder buf, String value) {
        if (!value.equalsIgnoreCase("1") && !(value.equalsIgnoreCase("0"))) {
            super.quoteBooleanLiteral(buf, value);
        } else {
            buf.append(value);
        }
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, null, false);
    }

    @Override
    protected StringBuilder generateOrderByNulls(CharSequence expr, boolean ascending, boolean collateNullsLast) {
        // In MYSQL, Null values are worth negative infinity.
        return DialectUtil.generateOrderByNullsWithIsnull(expr, ascending, collateNullsLast);
    }

    @Override
    public boolean requiresHavingAlias() {
        return true;
    }

    @Override
    public boolean supportsMultiValueInExpr() {
        return true;
    }

    private enum Scope {
        SESSION, GLOBAL
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        return true;
    }

    @Override
    public StringBuilder generateRegularExpression(String source, String javaRegex) {
        try {
            Pattern.compile(javaRegex);
        } catch (PatternSyntaxException e) {
            // Not a valid Java regex. Too risky to continue.
            return null;
        }

        // We might have to use case-insensitive matching
        javaRegex = DialectUtil.cleanUnicodeAwareCaseFlag(javaRegex);
        StringBuilder mappedFlags = new StringBuilder();
        String[][] mapping = new String[][]{{"i", "i"}};
        javaRegex = extractEmbeddedFlags(javaRegex, mapping, mappedFlags);
        boolean caseSensitive = true;
        if (mappedFlags.toString()
            .contains("i")) {
            caseSensitive = false;
        }
        final Matcher escapeMatcher = DialectUtil.ESCAPE_PATTERN.matcher(javaRegex);
        while (escapeMatcher.find()) {
            javaRegex = javaRegex.replace(escapeMatcher.group(1), escapeMatcher.group(2));
        }
        final StringBuilder sb = new StringBuilder();

        // Now build the string.
        sb.append(source);
        sb.append(" IS NOT NULL AND ");
        if (caseSensitive) {
            sb.append(source);
        } else {
            sb.append("UPPER(");
            sb.append(source);
            sb.append(")");
        }
        sb.append(" REGEXP ");
        if (caseSensitive) {
            quoteStringLiteral(sb, javaRegex);
        } else {
            quoteStringLiteral(sb, javaRegex.toUpperCase());
        }
        return sb;
    }

    /**
     * Required for MySQL 5.7+, where SQL_MODE include ONLY_FULL_GROUP_BY by
     * default. This prevent expressions like
     * <p>
     * ISNULL(RTRIM(`promotion_name`)) ASC
     * <p>
     * from being used in ORDER BY section.
     * <p>
     * ISNULL(`c0`) ASC
     * <p>
     * will be used, where `c0` is an alias of the RTRIM(`promotion_name`). And this
     * is important for the cases where we're using SQL expressions in a Level
     * definition.
     * <p>
     * Jira ticket, that describes the issue:
     * http://jira.pentaho.com/browse/MONDRIAN-2451
     *
     * @return true when MySQL version is 5.7 or larger
     */
    @Override
    public boolean requiresOrderByAlias() {
        return productVersion.compareTo("5.7") >= 0;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

    // Unified BitOperation methods

    @Override
    public StringBuilder generateBitAggregation(BitOperation operation, CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        return switch (operation) {
            case AND -> buf.append("BIT_AND(").append(operand).append(")");
            case OR -> buf.append("BIT_OR(").append(operand).append(")");
            case XOR -> buf.append("BIT_XOR(").append(operand).append(")");
            case NAND -> buf.append("NOT(BIT_AND(").append(operand).append("))");
            case NOR -> buf.append("NOT(BIT_OR(").append(operand).append("))");
            case NXOR -> buf.append("NOT(BIT_XOR(").append(operand).append("))");
        };
    }

    @Override
    public boolean supportsBitAggregation(BitOperation operation) {
        return true; // MySQL supports all bit operations
    }

    @Override
    public StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName) {
        return buildPercentileFunction("PERCENTILE_DISC", percentile, desc, tableName, columnName);
    }

    @Override
    public StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName) {
        return buildPercentileFunction("PERCENTILE_CONT", percentile, desc, tableName, columnName);
    }

    @Override
    public boolean supportsPercentileDisc() {
        return productVersion.compareTo("8.0") >= 0;
    }

    @Override
    public boolean supportsPercentileCont() {
        return productVersion.compareTo("8.0") >= 0;
    }

    @Override
    public StringBuilder generateListAgg(CharSequence operand, boolean distinct, String separator, String coalesce, String onOverflowTruncate, List<OrderedColumn> columns) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("GROUP_CONCAT");
        buf.append("( ");
        if (distinct) {
            buf.append("DISTINCT ");
        }
        buf.append(operand);
        if (columns != null && !columns.isEmpty()) {
            buf.append(" ORDER BY ");
            buf.append(buildOrderedColumnsClause(columns));
        }
        if (separator != null) {
            buf.append(" SEPARATOR '").append(separator).append("'");
        }
        buf.append(")");
        //GROUP_CONCAT(DISTINCT cate_id ORDER BY cate_id ASC SEPARATOR ' ')
        return buf;
    }

    @Override
    public StringBuilder generateNthValueAgg(CharSequence operand, boolean ignoreNulls, Integer n, List<OrderedColumn> columns) {
        return buildNthValueFunction("NTH_VALUE", operand, ignoreNulls, n, columns, false);
    }

    @Override
    public boolean supportsNthValue() {
        return true;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }

}
