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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import org.eclipse.daanse.jdbc.db.api.meta.DatabaseInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IdentifierInfo;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.db.redshift.RedshiftDialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedshiftDialectTest {
    private MetaInfo metaInfo = mock(MetaInfo.class);
    private DatabaseInfo databaseInfo = mock(DatabaseInfo.class);
    private IdentifierInfo identifierInfo = mock(IdentifierInfo.class);
    private RedshiftDialect dialect;

    @BeforeEach
    protected void setUp() throws Exception {
        when(metaInfo.databaseInfo()).thenReturn(databaseInfo);
        when(metaInfo.identifierInfo()).thenReturn(identifierInfo);
        when(databaseInfo.databaseProductName()).thenReturn("REDSHIFT");
        dialect = new RedshiftDialect(metaInfo);
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
        String sql = dialect.generateRegularExpression("table.column", "(?is)|(?u).*a.*").toString();
        assertEquals("REGEXP_INSTR(table.column,'.*a.*',1,1,0,'i') > 0", sql);
    }

    @Test
    void testGenerateRegularExpression_CaseSensitive() throws Exception {
        String sql = dialect.generateRegularExpression("table.column", ".*a.*").toString();
        assertEquals("REGEXP_INSTR(table.column,'.*a.*',1,1,0,'c') > 0", sql);
    }
}
