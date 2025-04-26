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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.OrderedColumn;
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
    private static final String ESCAPE_REGEXP = "(\\\\Q([^\\\\Q]+)\\\\E)";
    private static final Pattern escapePattern = Pattern.compile(ESCAPE_REGEXP);

    private static final String SUPPORTED_PRODUCT_NAME = "MYSQL";

    public MySqlDialect(MetaInfo metaInfo) {
        super(metaInfo);
        try {
            if (isInfobright(metaInfo)) {
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
     * @param metaInfo metaInfo
     * @return Whether this is Infobright
     */
    public static boolean isInfobright(MetaInfo metaInfo) {
        //TODO add Infobright to dialect configuration
        return false;
    }

    @Override
    protected String deduceProductName(MetaInfo metaInfo) {
        final String productName = super.deduceProductName(metaInfo);
        if (isInfobright(metaInfo)) {
            return "MySQL (Infobright)";
        }
        return productName;
    }

    @Override
    protected String deduceIdentifierQuoteString(MetaInfo metaInfo) {
        String quoteIdentifierString = super.deduceIdentifierQuoteString(metaInfo);

        if (quoteIdentifierString == null) {
            // mm.mysql.2.0.4 driver lies. We know better.
            quoteIdentifierString = "`";
        }
        return quoteIdentifierString;
    }

    @Override
    protected boolean deduceSupportsSelectNotInGroupBy(MetaInfo metaInfo) throws SQLException {
        //TODO SupportsSelectNotInGroupBy to configuration
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
        // Go beyond Util.singleQuoteString; also quote backslash.
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
                return new StringBuilder("ISNULL(").append(expr).append(") DESC, ").append(expr).append(" DESC");
            }
        }
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
        final Matcher escapeMatcher = escapePattern.matcher(javaRegex);
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

    @Override
    public StringBuilder generateAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_AND(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_OR(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("BIT_XOR(").append(operand).append(")");
        return buf;
    }

    @Override
    public StringBuilder generateNAndBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(BIT_AND(").append(operand).append("))");
        return buf;
    }

    @Override
    public StringBuilder generateNOrBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(BIT_OR(").append(operand).append("))");
        return buf;
    }

    @Override
    public StringBuilder generateNXorBitAggregation(CharSequence operand) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("NOT(BIT_XOR(").append(operand).append("))");
        return buf;
    }

    @Override
    public boolean supportsBitAndAgg() {
        return true;
    }

    @Override
    public boolean supportsBitOrAgg() {
        return true;
    }

    @Override
    public boolean supportsBitXorAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNAndAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNOrAgg() {
        return true;
    }

    @Override
    public boolean supportsBitNXorAgg() {
        return true;
    }

    @Override
    public StringBuilder generatePercentileDisc(double percentile, boolean desc, String tableName, String columnName) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("PERCENTILE_DISC(").append(percentile).append(")").append(" WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" ").append(DESC);
        }
        buf.append(")");
        return buf;
    }

    @Override
    public StringBuilder generatePercentileCont(double percentile, boolean desc, String tableName, String columnName) {
        StringBuilder buf = new StringBuilder(64);
        buf.append("PERCENTILE_CONT(").append(percentile).append(")").append(" WITHIN GROUP (ORDER BY ");
        if (tableName != null) {
            quoteIdentifier(buf, tableName, columnName);
        } else {
            quoteIdentifier(buf, columnName);
        }
        if (desc) {
            buf.append(" ").append(DESC);
        }
        buf.append(")");
        return buf;
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
            boolean first = true;
            for(OrderedColumn c : columns) {
                if (!first) {
                    buf.append(", ");
                }
                if (c.getTableName() != null) {
                    quoteIdentifier(buf, c.getTableName(), c.getColumnName());
                } else {
                    quoteIdentifier(buf, c.getColumnName());
                }
                if (!c.isAscend()) {
                    buf.append(DESC);
                }
                first = false;
            }
        }
        if (separator != null) {
            buf.append(" SEPARATOR '").append(separator).append("'");
        }
        buf.append(")");
        //GROUP_CONCAT(DISTINCT cate_id ORDER BY cate_id ASC SEPARATOR ' ')
        return buf;
    }

    @Override
    public boolean supportsListAgg() {
        return true;
    }

}
