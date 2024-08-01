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
package org.eclipse.daanse.jdbc.db.rdb.emf;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.eclipse.daanse.rdb.structure.api.model.DatabaseCatalog;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class DatabaseServiceImplH2Test {

    private String catalogName = UUID.randomUUID().toString().toUpperCase();

    private DataSource ds() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:memFS:" + catalogName);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    @Test
    void test() throws SQLException {
        DataSource ds = ds();
        EmfRdbProvider emfRdbProvider = new EmfRdbProvider(ds.getConnection().getMetaData());
        DatabaseCatalog databaseCatalog = emfRdbProvider.get();
        assertThat(databaseCatalog).isNotNull();
    }

}
