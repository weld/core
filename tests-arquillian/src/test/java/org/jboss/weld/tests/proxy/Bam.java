/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.weld.tests.proxy;

interface Bam {
    Foo asFoo();

    <T> T as(Class<T> type);
}
