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
package org.eclipse.daanse.jdbc.db.dialect.db.nuodb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.jdbc.db.dialect.db.common.Util;

/**
 * Implementation of {@link Dialect} for the NuoDB database. In order to use
 * NuoDB with Hitachi Vantara Mondrian users can only use NuoDB version 2.0.4 or
 * newer.
 *
 * @author rbuck
 * @since Mar 20, 2014
 */
public class NuoDbDialect extends JdbcDialectImpl {
    private static final String SUPPORTED_PRODUCT_NAME = "NUODB";

    public NuoDbDialect(Connection connection) {
        super(connection);
    }

    /**
     * In order to generate a SQL statement to represent an inline dataset NuoDB
     * requires that you use FROM DUAL.
     *
     * @param columnNames the list of column names
     * @param columnTypes the list of column types
     * @param valueList   the value list
     * @return the generated SQL statement for an inline dataset
     */
    @Override
    public StringBuilder generateInline(List<String> columnNames, List<String> columnTypes, List<String[]> valueList) {
        return generateInlineGeneric(columnNames, columnTypes, valueList, " FROM DUAL", false);
    }

    /**
     * NuoDB does not yet support ANSI SQL:2003 for DATE literals so we have to cast
     * dates using a function.
     *
     * @param buf   Buffer to append to
     * @param date  Value as date
     */
    @Override
    protected void quoteDateLiteral(StringBuilder buf, Date date) {
        buf.append("DATE(");
        Util.singleQuoteString(date.toString(), buf);
        buf.append(")");
    }

    /**
     * The NuoDB JDBC driver lists " " as the string to use for quoting, but we know
     * better. Ideally the quotation character ought to have been "`" but if that is
     * used and a generated query uses non quoted object names, not- found
     * exceptions will occur for the object. So we here fall back to using the
     * double quote character. We ought to investigate why back-tick won't work. But
     * for now this makes all the tests work with Nuo (besides the tweaks above).
     *
     * @param metaData DatabaseMetaData
     * @return the quotation character
     * @throws SQLException
     */
    @Override
    protected String deduceIdentifierQuoteString(DatabaseMetaData metaData) throws SQLException {
        String identifierQuoteString = super.deduceIdentifierQuoteString(metaData);
        if (" ".equals(identifierQuoteString)) {
            identifierQuoteString = "\"";
        }
        return identifierQuoteString;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }
}
