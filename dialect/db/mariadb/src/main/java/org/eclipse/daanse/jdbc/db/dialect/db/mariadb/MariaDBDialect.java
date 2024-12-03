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

package org.eclipse.daanse.jdbc.db.dialect.db.mariadb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.db.mysql.MySqlDialect;

public class MariaDBDialect extends MySqlDialect {

    private static final String SUPPORTED_PRODUCT_NAME = "MARIADB";

    public MariaDBDialect(MetaInfo metaInfo) {
        super(metaInfo);
    }

    @Override
    protected String deduceProductName(MetaInfo metaInfo) {
        // It is possible for someone to use the MariaDB JDBC driver with Infobright . .
        // .
        final String productName = super.deduceProductName(metaInfo);
        if (isInfobright(metaInfo)) {
            return "MySQL (Infobright)";
        }
        return productName;
    }

    @Override
    public String getDialectName() {
        return SUPPORTED_PRODUCT_NAME.toLowerCase();
    }
}
