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

import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.osgi.service.component.annotations.Component;

import aQute.bnd.annotation.spi.ServiceProvider;

@ServiceProvider(value = DialectFactory.class, attribute = { "database.dialect.type:String='H2'",
    "database.product:String='H2'" })
@Component(service = DialectFactory.class)
public class H2DialectFactory extends AbstractDialectFactory<H2Dialect>{
    private static final String SUPPORTED_PRODUCT_NAME = "H2";

    @Override
    public boolean isSupportedProduct(String productName, String productVersion, MetaInfo metaInfo) {
        return SUPPORTED_PRODUCT_NAME.equalsIgnoreCase(productName);
    }

    @Override
    public Function<MetaInfo, H2Dialect> getConstructorFunction() {
        return H2Dialect::new;
    }

}
