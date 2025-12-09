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
package org.eclipse.daanse.jdbc.db.dialect.db.opensearch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.DialectFactory;
import org.eclipse.daanse.jdbc.db.dialect.db.opensearch.OpenSearchDialectFactory;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

class ServiceTest {
    @Test
    void serviceExists(@InjectService List<DialectFactory> dialects) throws Exception {

        assertThat(dialects).isNotNull().isNotEmpty().anyMatch(OpenSearchDialectFactory.class::isInstance);
    }
}
