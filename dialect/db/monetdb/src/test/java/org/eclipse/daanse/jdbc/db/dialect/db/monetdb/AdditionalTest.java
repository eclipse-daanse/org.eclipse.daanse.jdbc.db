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
package org.eclipse.daanse.jdbc.db.dialect.db.monetdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.daanse.jdbc.db.dialect.api.type.BestFitColumnType;
import org.junit.jupiter.api.Test;

class AdditionalTest {

    @Test
    void testMonetBooleanColumn() throws SQLException {
        ResultSetMetaData resultSet = mock(ResultSetMetaData.class);
        Connection connection = mock(Connection.class);;
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(resultSet.getColumnType(1)).thenReturn(Types.BOOLEAN);
        MonetDbDialect monetDbDialect = new MonetDbDialect(connection);
        BestFitColumnType type = monetDbDialect.getType(resultSet, 0);
        assertEquals(BestFitColumnType.OBJECT, type);
    }
}
