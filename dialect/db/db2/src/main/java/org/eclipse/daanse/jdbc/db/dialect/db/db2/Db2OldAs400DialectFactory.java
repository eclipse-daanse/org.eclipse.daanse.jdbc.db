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
package org.eclipse.daanse.jdbc.db.dialect.db.db2;

import java.sql.Connection;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectName;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.osgi.service.component.annotations.Component;

@DialectName("DB2_OLD_AS400")
@Component(service = DialectFactory.class)
public class Db2OldAs400DialectFactory extends AbstractDialectFactory<Db2OldAs400Dialect> {
    private static final String SUPPORTED_PRODUCT_NAME = "DB2_OLD_AS400";

    @Override
    public boolean isSupportedProduct(String productName, String productVersion, Connection connection) {
        return SUPPORTED_PRODUCT_NAME.equalsIgnoreCase(productVersion);
    }

    @Override
    public Function<Connection, Db2OldAs400Dialect> getConstructorFunction() {
        return Db2OldAs400Dialect::new;
    }
}
