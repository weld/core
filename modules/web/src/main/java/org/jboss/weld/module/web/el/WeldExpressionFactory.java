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
package org.jboss.weld.module.web.el;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import org.jboss.weld.logging.ElLogger;
import org.jboss.weld.module.web.util.el.ForwardingExpressionFactory;

/**
 * @author pmuir
 */
public class WeldExpressionFactory extends ForwardingExpressionFactory {

    private final ExpressionFactory delegate;

    public WeldExpressionFactory(ExpressionFactory expressionFactory) {
        if (expressionFactory == null) {
            throw ElLogger.LOG.nullExpressionFactory();
        }
        this.delegate = expressionFactory;
    }

    @Override
    protected ExpressionFactory delegate() {
        return delegate;
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, @SuppressWarnings("rawtypes") Class expectedType) {
        return new WeldValueExpression(super.createValueExpression(context, expression, expectedType));
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, @SuppressWarnings("rawtypes") Class expectedReturnType, @SuppressWarnings("rawtypes") Class[] expectedParamTypes) {
        return new WeldMethodExpression(super.createMethodExpression(context, expression, expectedReturnType, expectedParamTypes));
    }

    // Satisfy checkstyle requirement for a hashcode method when there's an equals method
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) ||
                (obj instanceof WeldExpressionFactory
                        && delegate().equals(((WeldExpressionFactory) obj).delegate()));
    }

}
