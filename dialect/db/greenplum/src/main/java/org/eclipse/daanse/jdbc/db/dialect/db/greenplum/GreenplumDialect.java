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
package org.eclipse.daanse.jdbc.db.dialect.db.greenplum;

import java.sql.Connection;

import org.eclipse.daanse.jdbc.db.dialect.db.postgresql.PostgreSqlDialect;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;

/**
 * Implementation of {@link Dialect} for the GreenplumSQL database.
 *
 * @author Millersoft
 * @since Dec 23, 2009
 */
public class GreenplumDialect extends PostgreSqlDialect {

    private static final String SUPPORTED_PRODUCT_NAME = "GREENPLUM";

    public GreenplumDialect(MetaInfo metaInfo) {
        super(metaInfo);
    }

    @Override
    public boolean supportsGroupingSets() {
        return true;
    }

    @Override
    public boolean requiresGroupByAlias() {
        return true;
    }

    @Override
    public boolean requiresAliasForFromQuery() {
        return true;
    }

    @Override
    public boolean allowsCountDistinct() {
        return true;
    }

    @Override
    public StringBuilder generateCountExpression(CharSequence exp) {
        return wrapIntoSqlIfThenElseFunction(
            new StringBuilder(exp).append(" ISNULL"),
            "'0'",
            new StringBuilder("TEXT(").append(exp).append(")"));
    }

    @Override
    public boolean allowsRegularExpressionInWhereClause() {
        // Support for regexp was added in GP 3.2+
        return productVersion.compareTo("3.2") >= 0;
    }

    @Override
    public boolean allowsInnerDistinct() {
        return false;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }

}