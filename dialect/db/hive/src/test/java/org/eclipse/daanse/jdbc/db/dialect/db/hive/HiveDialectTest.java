/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.dialect.db.hive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HiveDialectTest {

    private Connection connection = mock(Connection.class);
    private DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    private HiveDialect dialect;

    @BeforeEach
    protected void setUp() throws Exception {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("Apache Hive");
        when(metaData.getDatabaseProductVersion()).thenReturn("3.1.0");
        dialect = new HiveDialect(connection);
    }

    @Test
    void testGetDialectName() {
        assertEquals("hive", dialect.getDialectName());
    }

    @Test
    void testRequiresAliasForFromQuery() {
        assertTrue(dialect.requiresAliasForFromQuery());
    }

    @Test
    void testRequiresOrderByAlias() {
        assertTrue(dialect.requiresOrderByAlias());
    }

    @Test
    void testAllowsOrderByAlias() {
        assertTrue(dialect.allowsOrderByAlias());
    }

    @Test
    void testRequiresGroupByAlias() {
        assertFalse(dialect.requiresGroupByAlias());
    }

    @Test
    void testAllowsCompoundCountDistinct() {
        assertTrue(dialect.allowsCompoundCountDistinct());
    }

    @Test
    void testAllowsAs() {
        assertFalse(dialect.allowsAs());
    }

    @Test
    void testAllowsJoinOn() {
        assertFalse(dialect.allowsJoinOn());
    }

    @Test
    void testRequiresUnionOrderByExprToBeInSelectClause() {
        assertFalse(dialect.requiresUnionOrderByExprToBeInSelectClause());
    }

    @Test
    void testRequiresUnionOrderByOrdinal() {
        assertFalse(dialect.requiresUnionOrderByOrdinal());
    }

    @Test
    void testQuoteDateLiteral() {
        StringBuilder buf = new StringBuilder();
        dialect.quoteDateLiteral(buf, java.sql.Date.valueOf("2024-01-15"));
        assertEquals("'2024-01-15'", buf.toString());
    }

    @Test
    void testQuoteTimestampLiteral() {
        StringBuilder buf = new StringBuilder();
        dialect.quoteTimestampLiteral(buf, "2024-01-15 10:30:00");
        assertEquals("cast( '2024-01-15 10:30:00' as timestamp )", buf.toString());
    }

    @Test
    void testQuoteTimestampLiteral_InvalidFormat() {
        StringBuilder buf = new StringBuilder();
        assertThrows(NumberFormatException.class, () -> dialect.quoteTimestampLiteral(buf, "invalid"));
    }

    @Test
    void testGenerateOrderByNulls_AscNullsLast() {
        StringBuilder result = dialect.generateOrderByNulls("column1", true, true);
        assertEquals("ISNULL(column1) ASC, column1 ASC", result.toString());
    }

    @Test
    void testGenerateOrderByNulls_DescNullsLast() {
        StringBuilder result = dialect.generateOrderByNulls("column1", false, true);
        assertEquals("column1 DESC", result.toString());
    }

    @Test
    void testGenerateOrderByNulls_AscNullsFirst() {
        StringBuilder result = dialect.generateOrderByNulls("column1", true, false);
        assertEquals("column1 ASC", result.toString());
    }

    @Test
    void testGenerateOrderByNulls_DescNullsFirst() {
        StringBuilder result = dialect.generateOrderByNulls("column1", false, false);
        assertEquals("ISNULL(column1) DESC, column1 DESC", result.toString());
    }

}
