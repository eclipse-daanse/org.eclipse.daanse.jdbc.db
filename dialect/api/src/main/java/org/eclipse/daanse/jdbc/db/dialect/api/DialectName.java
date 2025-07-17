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
package org.eclipse.daanse.jdbc.db.dialect.api;

import static java.lang.annotation.ElementType.*;

import org.osgi.service.component.annotations.ComponentPropertyType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OSGi Component Property Type for defining a dialect name.
 */
@ComponentPropertyType
@Retention(RetentionPolicy.CLASS)
@Target({ TYPE, METHOD, FIELD })
public @interface DialectName {

    /**
     * The dialect name to be used as an OSGi component property.
     */
    String value();

    /**
     * Property prefix used by OSGi DS to namespace the property.
     */
    String PREFIX_ = DaanseDialectConstants.PREFIX + ".";

}
