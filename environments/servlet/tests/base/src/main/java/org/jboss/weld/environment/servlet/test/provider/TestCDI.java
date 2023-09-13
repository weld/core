/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.servlet.test.provider;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
public class TestCDI extends CDI<Object> {

    /**
     * WORKAROUND - it's not possible to unset the CDIProvider via
     * {@link CDI#setCDIProvider(jakarta.enterprise.inject.spi.CDIProvider)} but it's possible to set the field
     * value directly in a subclass. However, it was probably not intended for the subclass to be able to do this.
     */
    public static void unsetCDIProvider() {
        configuredProvider = null;
    }

    @Override
    public Instance<Object> select(Annotation... qualifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUnsatisfied() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAmbiguous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(Object instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Handle<Object> getHandle() {
        return null;
    }

    @Override
    public Iterable<Handle<Object>> handles() {
        return null;
    }

    @Override
    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BeanManager getBeanManager() {
        throw new UnsupportedOperationException();
    }

}
