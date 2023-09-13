/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Abstraction over common EL operations.
 *
 * @author Jozef Hartinger
 *
 */
public interface ExpressionLanguageSupport extends Service {

    /**
     * Returns a wrapper {@link jakarta.el.ExpressionFactory} that delegates
     * {@link jakarta.el.MethodExpression} and {@link jakarta.el.ValueExpression}
     * creation to the given {@link jakarta.el.ExpressionFactory}. When a Unified
     * EL expression is evaluated using a {@link jakarta.el.MethodExpression} or
     * {@link jakarta.el.ValueExpression} returned by the wrapper
     * {@link jakarta.el.ExpressionFactory}, the container handles destruction of
     * objects with scope {@linkjakarta.enterprise.context.Dependent}.
     *
     *
     * @param expressionFactory the {@link jakarta.el.ExpressionFactory} to wrap
     * @return the wrapped {@link jakarta.el.ExpressionFactory}
     */
    ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory);

    /**
     * Creates an {@link ELResolver} for given {@link BeanManagerImpl}
     *
     * @param manager the given manager
     * @return
     */
    ELResolver createElResolver(BeanManagerImpl manager);
}
