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

package org.eclipse.daanse.jdbc.db.dialect.db.redshift;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.jdbc.db.dialect.db.common.Util;
import org.eclipse.daanse.jdbc.db.dialect.db.postgresql.PostgreSqlDialect;

/**
 * User: cboyden Date: 2/8/13
 */
public class RedshiftDialect extends PostgreSqlDialect {
    private static final String SUPPORTED_PRODUCT_NAME = "REDSHIFT";

    public RedshiftDialect(MetaInfo metaInfo) {
        super(metaInfo);
    }

    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, null, false);
    }

    @Override
    public void quoteStringLiteral(StringBuilder buf, String value) {
        // '\' to '\\'
        Util.singleQuoteString(value.replace("\\\\", "\\\\\\\\"), buf);
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
        String[][] mapping = new String[][] { { "i", "i" } };
        javaRegex = extractEmbeddedFlags(javaRegex, mapping, mappedFlags);
        boolean caseSensitive = true;
        if (mappedFlags.toString()
                .contains("i")) {
            caseSensitive = false;
        }

        // Now build the string.
        final StringBuilder sb = new StringBuilder();
        // https://docs.aws.amazon.com/redshift/latest/dg/REGEXP_INSTR.html
        sb.append("REGEXP_INSTR(");
        sb.append(source);
        sb.append(",");
        quoteStringLiteral(sb, javaRegex);
        sb.append(",1,1,0,");
        sb.append(caseSensitive ? "'c'" : "'i'");
        sb.append(") > 0");

        return sb;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }
}
