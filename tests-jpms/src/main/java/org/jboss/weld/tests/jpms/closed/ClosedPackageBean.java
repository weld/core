/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.jpms.closed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClosedPackageBean {
    public String value() {
        return "closed";
    }
}
