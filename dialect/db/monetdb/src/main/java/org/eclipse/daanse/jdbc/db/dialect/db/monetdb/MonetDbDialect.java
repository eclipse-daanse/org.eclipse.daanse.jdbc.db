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
package org.eclipse.daanse.jdbc.db.dialect.db.monetdb;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.daanse.jdbc.db.dialect.api.type.BestFitColumnType;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the MonetDB database.
 *
 * @author pstoellberger
 * @since Nov 10, 2012
 */
public class MonetDbDialect extends JdbcDialectImpl {
    private static final String DOT = "\\.";

    private static final String SUPPORTED_PRODUCT_NAME = "MONETDB";

    public MonetDbDialect(Connection connection) {
        super(connection);
    }

    @Override
    public boolean allowsMultipleDistinctSqlMeasures() {
        return false;
    }

    @Override
    public boolean allowsCountDistinct() {
        // MonetDB before Aug 2011-SP2 bugfix release (11.5.7) has the issue
        // http://bugs.monetdb.org/2890
        // So we uses count distinct only started from V11.5.7+
        return compareVersions(productVersion, "11.5.7") >= 0;
    }

    @Override
    public boolean allowsCountDistinctWithOtherAggs() {
        return false;
    }

    @Override
    public boolean allowsMultipleCountDistinct() {
        return false;
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    public boolean allowsCompoundCountDistinct() {
        return false;
    }

    @Override
    public boolean supportsGroupByExpressions() {
        return false;
    }

    @Override
    public void quoteStringLiteral(StringBuilder buf, String s) {
        // Go beyond standard singleQuoteString; also quote backslash, like MySQL.
        buf.append('\'');
        String s0 = s.replace("'", "''");
        String s1 = s0.replace("\\", "\\\\");
        buf.append(s1);
        buf.append('\'');
    }

    @Override
    public BestFitColumnType getType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        final int columnType = metaData.getColumnType(columnIndex + 1);
        final int precision = metaData.getPrecision(columnIndex + 1);
        final int scale = metaData.getScale(columnIndex + 1);

        if (columnType == Types.NUMERIC || columnType == Types.DECIMAL) {
            if (scale == 0 && precision == 0) {
                // MonetDB marks doesn't return precision and scale for agg
                // decimal data types, so we'll assume it's a double.
                logTypeInfo(metaData, columnIndex, BestFitColumnType.DOUBLE);
                return BestFitColumnType.DOUBLE;
            }
        } else if (columnType == Types.BOOLEAN) {
            return BestFitColumnType.OBJECT;
        }
        return super.getType(metaData, columnIndex);
    }

    /**
     * Compares two MonetDB versions that contain only digits separated by dots.
     *
     * Examples of MonetDB versions:
     *
     *
     * 11.17.17
     * 11.5.3
     *
     *
     * @param v1 the first version be compared
     * @param v2 the second version to be compared
     * @return the value 0 if two versions are equal; a value less than 0 if the
     * first version number is less than the second one; and a value greater
     * than 0 if the first version number is greater than the second one.
     */
    public int compareVersions(String v1, String v2) {
        int result = v1.compareTo(v2);
        if (result == 0) {
            return result;
        }
        // MonetDB versions consists of didgits separated by dots. E.g.: 11.17.17,
        // 11.5.3
        // So parsing parts as String and then compare them as Integer.
        String[] parts1 = v1.split(DOT);
        String[] parts2 = v2.split(DOT);

        int partsCount = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < partsCount; i++) {
            result = Integer.valueOf(parts1[i])
                .compareTo(Integer.valueOf(parts2[i]));
            if (result != 0) {
                return result;
            }
        }

        result = parts1.length - parts2.length;
        return result;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

}
