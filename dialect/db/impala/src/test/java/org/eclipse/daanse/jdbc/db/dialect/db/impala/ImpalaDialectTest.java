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
package org.eclipse.daanse.jdbc.db.dialect.db.impala;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Andrey Khayrutdinov
 */
class ImpalaDialectTest {
    private Connection connection = mock(Connection.class);
    private DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    private static ImpalaDialect impalaDialect;

    @BeforeEach
    protected void setUp() throws Exception {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("IMPALA");
        impalaDialect = new ImpalaDialect(connection);
    }

    @Test
    void testAllowsRegularExpressionInWhereClause() {
        assertTrue(impalaDialect.allowsRegularExpressionInWhereClause());
    }

    @Test
    void testGenerateRegularExpression_InvalidRegex() throws Exception {
        assertNull(impalaDialect.generateRegularExpression("table.column", "(a"), "Invalid regex should be ignored");
    }

    @Test
    void testGenerateRegularExpression_CaseInsensitive() throws Exception {
        String sql = impalaDialect.generateRegularExpression("table.column", "(?i)|(?u).*a.*").toString();
        assertSqlWithRegex(false, sql, "'.*A.*'");
    }

    @Test
    void testGenerateRegularExpression_CaseSensitive() throws Exception {
        String sql = impalaDialect.generateRegularExpression("table.column", ".*1.*").toString();
        assertSqlWithRegex(true, sql, "'.*1.*'");
    }

    private void assertSqlWithRegex(boolean isCaseSensitive, String sql, String quotedRegex) throws Exception {
        assertNotNull(sql, "Sql should be generated");
        assertEquals(isCaseSensitive, !sql.contains("UPPER"), sql);
        assertTrue(sql.contains("cast(table.column as string)"), sql);
        assertTrue(sql.contains("REGEXP"), sql);
        assertTrue(sql.contains(quotedRegex), sql);
    }
}
