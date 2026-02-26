/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.weld.tests.proxy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class StringAlphaProducer {
    @Produces
    @ApplicationScoped
    Alpha<String> produceStringAlpha() {
        return new AlphaImpl();
    }
}
