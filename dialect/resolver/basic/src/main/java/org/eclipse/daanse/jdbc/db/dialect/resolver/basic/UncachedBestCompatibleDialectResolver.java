/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * History:
 *  This files came from the mondrian project. Some of the Flies
 *  (mostly the Tests) did not have License Header.
 *  But the Project is EPL Header. 2002-2022 Hitachi Vantara.
 *
 * Contributors:
 *   Hitachi Vantara.
 *   SmartCity Jena - initial  Java 8, Junit5
 */
package org.eclipse.daanse.jdbc.db.dialect.resolver.basic;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of {@link DialectResolver} that:<br>
 * <p>
 * - does not cache <br>
 * - returns Dialect with best ranking - calculated using
 *
 * @author stbischof
 */
@Component(service = DialectResolver.class)
public class UncachedBestCompatibleDialectResolver implements DialectResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(UncachedBestCompatibleDialectResolver.class);
    private List<DialectFactory> dialectFactories = new ArrayList<>();

    @Reference
    DatabaseService databaseService;


    @Reference(service = DialectFactory.class, cardinality = ReferenceCardinality.MULTIPLE)
    public void bindDialect(DialectFactory dialectFactory) {
        dialectFactories.add(dialectFactory);
    }

    public void unbindDialect(DialectFactory dialectFactory) {
        dialectFactories.remove(dialectFactory);
    }

    public void updatedDialect(DialectFactory dialectFactory) {
        unbindDialect(dialectFactory);
        bindDialect(dialectFactory);
    }

    @Override
    public Optional<Dialect> resolve(DataSource dataSource) {

        try (Connection c = dataSource.getConnection()) {
            return resolve(c);
        } catch (SQLException e) {
            LOGGER.error("connection error", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Dialect> resolve(Connection connection) {
        try {
            MetaInfo metaInfo = databaseService.createMetaInfo(connection);
            Optional<DialectFactory> dfOptional = dialectFactories.parallelStream()
                .map(calcCompatibility(metaInfo.databaseInfo().databaseProductName(), metaInfo.databaseInfo().databaseProductVersion(), metaInfo))
                .filter(compatibleDialect())
                .findFirst().map(Entry::getKey);
            if (dfOptional.isPresent()) {
                return dfOptional.get().tryCreateDialect(metaInfo);
            }
        } catch (SQLException e) {
            LOGGER.info("resolve failed", e);
        }
        return Optional.empty();
    }

    private Function<? super DialectFactory, ? extends Entry<DialectFactory, Boolean>> calcCompatibility(
        String productName, String productVersion, MetaInfo metaInfo) {
        return dialectFactory ->
            new AbstractMap.SimpleEntry<>(
                dialectFactory,
                dialectFactory.isSupportedProduct(productName, productVersion, metaInfo)
            );
    }

    private Predicate<Entry<DialectFactory, Boolean>> compatibleDialect() {
        return Entry::getValue;
    }

    protected String deduceProductVersion(DatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getDatabaseProductVersion();
    }

}
