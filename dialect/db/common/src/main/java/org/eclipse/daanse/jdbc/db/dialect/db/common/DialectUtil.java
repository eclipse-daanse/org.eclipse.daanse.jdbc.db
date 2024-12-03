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
package org.eclipse.daanse.jdbc.db.dialect.db.common;

import java.util.regex.Pattern;

public class DialectUtil {

    private static final Pattern UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN = Pattern.compile( "\\|\\(\\?u\\)" );
    private static final String EMPTY = "";

    private DialectUtil() {
        // constructor
    }

    /**
     * Cleans up the reqular expression from the unicode-aware case folding embedded flag expression (?u)
     *
     * @param javaRegExp
     *          the regular expression to clean up
     * @return the cleaned regular expression
     */
    public static String cleanUnicodeAwareCaseFlag( String javaRegExp ) {
        String cleaned = javaRegExp;
        if ( cleaned != null && isUnicodeCaseFlagInRegExp( cleaned ) ) {
            cleaned = UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN.matcher( cleaned ).replaceAll( EMPTY );
        }
        return cleaned;
    }

    private static boolean isUnicodeCaseFlagInRegExp( String javaRegExp ) {
        return UNICODE_CASE_FLAG_IN_JAVA_REG_EXP_PATTERN.matcher( javaRegExp ).find();
    }

}

//End DialectUtil.java
