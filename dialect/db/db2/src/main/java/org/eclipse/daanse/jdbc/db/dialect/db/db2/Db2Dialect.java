/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * History:
 *  This files came from the mondrian project. Some of the Flies
 *  (mostly the Tests) did not have License Header.
 *  But the Project is EPL Header. 2002-2022 Hitachi Vantara.
 *
 * Contributors:
 *   Hitachi Vantara.
 *   SmartCity Jena - initial  Java 8, Junit5
 */
package org.eclipse.daanse.jdbc.db.dialect.db.db2;

import java.sql.Connection;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;

/**
 * Implementation of {@link Dialect} for the IBM DB2 database.
 *
 * @see Db2OldAs400Dialect
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
public class Db2Dialect extends JdbcDialectImpl {

    public Db2Dialect(Connection connection) {
        super(connection);
    }

    @Override
    public StringBuilder wrapIntoSqlUpperCaseFunction(CharSequence expr) {
        return new StringBuilder("UCASE(").append(expr).append(")");
    }

    @Override
    public boolean supportsGroupingSets() {
        return true;
    }

    @Override
    public StringBuilder quoteDecimalLiteral(
        CharSequence value)
    {
        return new StringBuilder("FLOAT(").append(value).append(")");
    }

    @Override
    public String getDialectName() {
        return "db2";
    }

}
