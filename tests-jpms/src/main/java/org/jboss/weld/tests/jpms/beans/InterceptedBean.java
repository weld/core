/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.jpms.beans;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Logged
public class InterceptedBean {
    public String work() {
        return "intercepted";
    }
}
