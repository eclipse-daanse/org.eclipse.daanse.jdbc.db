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
package org.eclipse.daanse.jdbc.db.dialect.db.postgresql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.daanse.jdbc.db.api.meta.DatabaseInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IdentifierInfo;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.db.postgresql.PostgreSqlDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostgreSqlDialectTest {
    private MetaInfo metaInfo = mock(MetaInfo.class);
    private DatabaseInfo databaseInfo = mock(DatabaseInfo.class);
    private IdentifierInfo identifierInfo = mock(IdentifierInfo.class);
    private PostgreSqlDialect dialect;

    @BeforeEach
    protected void setUp() throws Exception {
        when(metaInfo.databaseInfo()).thenReturn(databaseInfo);
        when(metaInfo.identifierInfo()).thenReturn(identifierInfo);
        when(databaseInfo.databaseProductName()).thenReturn("POSTGRESQL");
        dialect = new PostgreSqlDialect(metaInfo);
    }

    @Test
    void testAllowsRegularExpressionInWhereClause() {
        assertTrue(dialect.allowsRegularExpressionInWhereClause());
    }

    @Test
    void testGenerateRegularExpression_InvalidRegex() throws Exception {
        assertNull(dialect.generateRegularExpression("table.column", "(a"), "Invalid regex should be ignored");
    }

    @Test
    void testGenerateRegularExpression_CaseInsensitive() throws Exception {
        String sql = dialect.generateRegularExpression("table.column", "(?i)|(?u).*a.*").toString();
        assertEquals("cast(table.column as text) is not null and cast(table.column as text) ~ '(?i).*a.*'", sql);
    }

    @Test
    void testGenerateRegularExpression_CaseSensitive() throws Exception {
        String sql = dialect.generateRegularExpression("table.column", ".*a.*").toString();
        assertEquals("cast(table.column as text) is not null and cast(table.column as text) ~ '.*a.*'", sql);
    }

}
//End PostgreSqlDialectTest.java