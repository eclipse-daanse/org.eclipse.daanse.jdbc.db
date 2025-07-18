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
package org.eclipse.daanse.jdbc.db.dialect.db.mssqlserver;

import java.sql.Connection;
import java.util.function.Function;

import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.api.DialectName;
import org.eclipse.daanse.jdbc.db.dialect.db.common.AbstractDialectFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = DialectFactory.class, scope = ServiceScope.SINGLETON)
@DialectName("MSSQL")
public class MicrosoftSqlServerDialectFactory extends AbstractDialectFactory<MicrosoftSqlServerDialect> {

    @Override
    public Function<Connection, MicrosoftSqlServerDialect> getConstructorFunction() {
        return MicrosoftSqlServerDialect::new;
    }

}
