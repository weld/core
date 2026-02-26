/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.weld.tests.proxy;

public class AlphaImpl implements Alpha<String> {
    @Override
    public Alpha<String> returnSelf() {
        return this;
    }
}
