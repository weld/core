/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web.util.el;

import java.lang.reflect.Method;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

/**
 * @author pmuir
 */
public abstract class ForwardingExpressionFactory extends ExpressionFactory {

    protected abstract ExpressionFactory delegate();

    @Override
    public <T> T coerceToType(Object obj, Class<T> targetType) {
        return delegate().coerceToType(obj, targetType);
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
        return delegate().createMethodExpression(context, expression, expectedReturnType, expectedParamTypes);
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        return delegate().createValueExpression(instance, expectedType);
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        return delegate().createValueExpression(context, expression, expectedType);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

    @Override
    public ELResolver getStreamELResolver() {
        return delegate().getStreamELResolver();
    }

    @Override
    public Map<String, Method> getInitFunctionMap() {
        return delegate().getInitFunctionMap();
    }
}
