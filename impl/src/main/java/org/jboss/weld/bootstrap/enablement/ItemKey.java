/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.bootstrap.enablement;

import java.util.Objects;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.util.Preconditions;

/**
 * Identifies an enablement entry by the bean class for a discovered bean or the {@link Bean} metadata object for a synthetic
 * bean. All beans have {@code Bean} metadata, but synthetic beans may share a bean class, so their metadata objects are used
 * to distinguish individual entries.
 */
public final class ItemKey {

    private final Class<?> javaClass;
    private final Bean<?> bean;

    ItemKey(Class<?> javaClass) {
        Preconditions.checkArgumentNotNull(javaClass, "javaClass");
        this.javaClass = javaClass;
        this.bean = null;
    }

    ItemKey(Bean<?> bean) {
        Preconditions.checkArgumentNotNull(bean, "bean");
        this.javaClass = null;
        this.bean = bean;
    }

    Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaClass, bean);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemKey) {
            ItemKey that = (ItemKey) obj;
            return Objects.equals(javaClass, that.javaClass) && Objects.equals(bean, that.bean);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(bean != null ? bean : javaClass);
    }
}
