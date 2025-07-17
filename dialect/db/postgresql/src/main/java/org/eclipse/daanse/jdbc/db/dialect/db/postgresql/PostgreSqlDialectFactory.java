/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.dialect.db.postgresql;

import java.sql.Connection;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import aQute.bnd.annotation.spi.ServiceProvider;

@ServiceProvider(value = DialectFactory.class, attribute = { "database.dialect.type:String='POSTGRESQL'",
    "database.product:String='POSTGRESQL'" })
@Component(service = DialectFactory.class, scope = ServiceScope.PROTOTYPE)
public class PostgreSqlDialectFactory extends AbstractDialectFactory<PostgreSqlDialect> {
    private static final String SUPPORTED_PRODUCT_NAME = "POSTGRESQL";

    @Override
    public boolean isSupportedProduct(String productName, String productVersion, Connection connection) {
        return SUPPORTED_PRODUCT_NAME.equalsIgnoreCase(productVersion) && !isDatabase("GREENPLUM", connection)
            && !isDatabase("NETEZZA", connection)
            && !isDatabase("REDSHIFT", connection);
    }

    @Override
    public Function<Connection, PostgreSqlDialect> getConstructorFunction() {
        return PostgreSqlDialect::new;
    }

}
