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

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * OSGi component factory for creating {@link ConfigurableDialect} instances.
 * <p>
 * This factory is registered as a {@link DialectFactory} service and creates
 * dialect instances based on OSGi configuration. The dialect can be configured
 * via Configuration Admin using the {@link ConfigurableDialectConfig} metatype.
 * <p>
 * Unlike other dialect factories, this one creates dialects purely from
 * configuration without requiring database metadata from a JDBC connection.
 */
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

    /**
     * Creates a dialect based on the current configuration.
     * <p>
     * Note: The connection parameter is ignored since ConfigurableDialect is
     * entirely configuration-driven and does not require database metadata.
     *
     * @param connection the JDBC connection (ignored)
     * @return the configured dialect
     */
    @Override
    public Dialect createDialect(Connection connection) throws SQLException {
        return dialect;
    }

    /**
     * Returns the dialect created from the current configuration.
     * <p>
     * This is a convenience method for programmatic access to the dialect without
     * needing a JDBC connection.
     *
     * @return the configured dialect
     */
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * Returns the current configuration.
     *
     * @return the configuration
     */
    public ConfigurableDialectConfig getConfig() {
        return config;
    }
}
