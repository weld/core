/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.util.reflection.Reflections;

/**
 * Forwarding implementation of {@link Context}
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ForwardingContext implements Context {

    protected abstract Context delegate();

    @Override
    public Class<? extends Annotation> getScope() {
        return delegate().getScope();
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        return delegate().get(contextual, creationalContext);
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return delegate().get(contextual);
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForwardingContext) {
            ForwardingContext that = (ForwardingContext) obj;
            return delegate().equals(that.delegate());
        }
        return delegate().equals(obj);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

    public static Context unwrap(Context context) {
        if (context instanceof ForwardingContext) {
            return Reflections.<ForwardingContext> cast(context).delegate();
        }
        return context;
    }

}
