/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.Dependent;

@Dependent
public class DependentBean {
    public String value() {
        return "dependent";
    }
}
