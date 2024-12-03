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
package org.eclipse.daanse.jdbc.db.dialect.db.sybase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import java.sql.Date;

import org.eclipse.daanse.jdbc.db.dialect.db.sybase.SybaseDialect;
import org.junit.jupiter.api.Test;

/**
 * Tests for SybaseDialect
 *
 * @author Yury Bakhmutski
 */
class SybaseDialectTest{

    SybaseDialect sybaseDialectMock=mock(SybaseDialect.class);


    /**
     * Test for MONDRIAN-2259 issue.
     * Is assumed SybaseDialect methods are called.
     */
    @Test
    void testQuoteDateLiteral() {
        String input = "1997-01-03 00:00:00.0";

        doCallRealMethod().when(sybaseDialectMock).quoteDateLiteral(
            any(StringBuilder.class), any(Date.class));

        doCallRealMethod().when(sybaseDialectMock).quoteDateLiteral(
            any(StringBuilder.class), anyString());

        StringBuilder buffer = new StringBuilder();
        sybaseDialectMock.quoteDateLiteral(buffer, input);
        String actual = buffer.toString();
        String expected = "'1997-01-03'";
        assertEquals(expected, actual);
    }

}
