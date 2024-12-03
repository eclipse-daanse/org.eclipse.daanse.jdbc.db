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
package org.eclipse.daanse.jdbc.db.dialect.db.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Util {

    private Util() {
        //constructor
    }

// TODO remove this class to common
    /**
     * Encloses a value in single-quotes, to make a SQL string value. Examples:
     * <code>singleQuoteForSql(null)</code> yields <code>NULL</code>;
     * <code>singleQuoteForSql("don't")</code> yields <code>'don''t'</code>.
     */
    public static String singleQuoteString(String val) {
        StringBuilder buf = new StringBuilder(64);
        singleQuoteString(val, buf);
        return buf.toString();
    }

    /**
     * Encloses a value in single-quotes, to make a SQL string value. Examples:
     * <code>singleQuoteForSql(null)</code> yields <code>NULL</code>;
     * <code>singleQuoteForSql("don't")</code> yields <code>'don''t'</code>.
     */
    public static void singleQuoteString(String val, StringBuilder buf) {
        buf.append('\'');

        String s0 = val.replace("'", "''");
        buf.append(s0);

        buf.append('\'');
    }

    /**
     * Closes a JDBC result set, statement, and connection, ignoring any errors.
     * If any of them are null, that's fine.
     *
     * <p>If any of them throws a {@link SQLException}, returns the first
     * such exception, but always executes all closes.</p>
     *
     * @param resultSet Result set
     * @param statement Statement
     * @param connection Connection
     */
    public static SQLException close(
        ResultSet resultSet,
        Statement statement,
        Connection connection)
    {
        SQLException firstException = null;
        if (resultSet != null) {
            try {
                if (statement == null) {
                    statement = resultSet.getStatement();
                }
                resultSet.close();
            } catch (SQLException t) {
                firstException = getfirstException(firstException, t);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException t) {
                firstException = getfirstException(firstException, t);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException t) {
                firstException = getfirstException(firstException, t);
            }
        }
        return firstException;
    }

    private static SQLException getfirstException(SQLException firstException, SQLException t) {
        if (firstException == null) {
            firstException = new SQLException();
            firstException.initCause(t);
        }
        return firstException;
    }

}
