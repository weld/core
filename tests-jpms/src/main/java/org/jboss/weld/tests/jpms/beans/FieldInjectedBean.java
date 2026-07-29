/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FieldInjectedBean {
    @Inject
    private DependentBean dependent;

    public String delegated() {
        return dependent.value();
    }
}
