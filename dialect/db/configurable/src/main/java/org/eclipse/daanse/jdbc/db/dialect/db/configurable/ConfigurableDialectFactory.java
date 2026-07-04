/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jdbc.db.dialect.db.configurable;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectInitData;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = DialectFactory.class, configurationPid = Constants.PID_DIALECT)
@Designate(ocd = ConfigurableDialectConfig.class, factory = true)
public class ConfigurableDialectFactory implements DialectFactory {

    private volatile ConfigurableDialectConfig config;
    private volatile ConfigurableDialect dialect;

    @Activate
    void activate(ConfigurableDialectConfig config) {
        this.config = config;
        this.dialect = new ConfigurableDialect(config);
    }

    @Modified
    void modified(ConfigurableDialectConfig config) {
        this.config = config;
        this.dialect = new ConfigurableDialect(config);
    }

    @Override
    public Dialect createDialect(DialectInitData init) {
        return dialect;
    }

    /**
     * @return the configured dialect
     */
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * @return the configuration
     */
    public ConfigurableDialectConfig getConfig() {
        return config;
    }
}
