/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.weld.tests.proxy;

public interface Alpha<T> {
    Alpha<T> returnSelf();
}
