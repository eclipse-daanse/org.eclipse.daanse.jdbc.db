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
package org.eclipse.daanse.jdbc.db.dialect.db.h2;

import java.sql.Connection;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectName;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.osgi.service.component.annotations.Component;

@Component(service = DialectFactory.class)
@DialectName("H2")
public class H2DialectFactory extends AbstractDialectFactory<H2Dialect>{
    private static final String SUPPORTED_PRODUCT_NAME = "H2";

    @Override
    public boolean isSupportedProduct(String productName, String productVersion, Connection connection) {
        return SUPPORTED_PRODUCT_NAME.equalsIgnoreCase(productName);
    }

    @Override
    public Function<Connection, H2Dialect> getConstructorFunction() {
        return H2Dialect::new;
    }

}
