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
package org.eclipse.daanse.jdbc.db.dialect.db.googlebigquery;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * This is the Mondrian dialect for Google BigQuery. It was tested against
 * google-api-services-bigquery-v2-rev355-1.22.0 in Q1 2018.
 *
 * @author lucboudreau
 */
public class GoogleBigQueryDialect extends JdbcDialectImpl {
    private static final String SUPPORTED_PRODUCT_NAME = "GOOGLEBIGQUERY";

    public GoogleBigQueryDialect(MetaInfo metaInfo) {
        super(metaInfo);
    }

    @Override
    public boolean allowsOrderByAlias() {
        return true;
    }

    @Override
    public boolean allowsAs() {
        return true;
    }

    @Override
    public boolean allowsDdl() {
        // Although DDL is supported, kinda, BQ uses its own type system.
        // Varchars are Strings and other small changes. We disable DDL
        // here so that the TCK doesn't attempt to create temporary tables.
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        return false;
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, null, false);
    }

    @Override
    public void quoteIdentifier(String val, StringBuilder buf) {
        // We have to turn spaces into underscores. BQ won't ever allow a
        // column name, nor its alias, to contain spaces. So in the schema,
        // won't be spaces coming into this function, whereas from a
        // drillthrough operation, it might. A level name with a space
        // may be used as an alias, so we need to override it here.
        // Same with non alpha characters, we have to remove them.
        super.quoteIdentifier(val.replace(' ', '_')
                .replaceAll("[^A-Za-z0-9\\_\\.`]", ""), buf);
    }

    @Override
    public void quoteStringLiteral(StringBuilder buf, String s) {
        // BQQ requires single quotes to be doubled and backslash
        // do be doubled too.
        buf.append('\'');
        s = s.replace("\\", "\\\\");
        s = s.replace("'", "\\'");
        buf.append(s);
        buf.append('\'');
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        // BQ supports regular expressions.
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
        javaRegex = DialectUtil.cleanUnicodeAwareCaseFlag(javaRegex);
        javaRegex = javaRegex.replace("\\Q", "");
        javaRegex = javaRegex.replace("\\E", "");
        final StringBuilder sb = new StringBuilder();
        sb.append("cast(");
        sb.append(source);
        sb.append(" as string) is not null and ");
        sb.append("REGEXP_CONTAINS( cast(");
        sb.append(source);
        sb.append(" as string), r");
        quoteStringLiteral(sb, javaRegex);
        sb.append(")");
        return sb;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

}
