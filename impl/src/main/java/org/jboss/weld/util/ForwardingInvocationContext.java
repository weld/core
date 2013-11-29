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
package org.jboss.weld.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * Forwarding implementation of {@link InvocationContext}.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ForwardingInvocationContext implements InvocationContext {

    protected abstract InvocationContext delegate();

    @Override
    public Object getTarget() {
        return delegate().getTarget();
    }

    @Override
    public Method getMethod() {
        return delegate().getMethod();
    }

    @Override
    public Constructor<?> getConstructor() {
        return delegate().getConstructor();
    }

    @Override
    public Object[] getParameters() throws IllegalStateException {
        return delegate().getParameters();
    }

    @Override
    public void setParameters(Object[] params) throws IllegalStateException, IllegalArgumentException {
        delegate().setParameters(params);
    }

    @Override
    public Map<String, Object> getContextData() {
        return delegate().getContextData();
    }

    @Override
    public Object getTimer() {
        return delegate().getTimer();
    }

    @Override
    public Object proceed() throws Exception {
        return delegate().proceed();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForwardingInvocationContext) {
            return delegate().equals(ForwardingInvocationContext.class.cast(obj).delegate());
        }
        return delegate().equals(obj);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
