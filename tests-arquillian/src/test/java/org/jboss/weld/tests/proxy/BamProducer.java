/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.weld.tests.proxy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@Dependent
public class BamProducer {
    @Produces
    @Named("bam")
    @ApplicationScoped
    Bam produceBam() {
        return new Foo();
    }
}
