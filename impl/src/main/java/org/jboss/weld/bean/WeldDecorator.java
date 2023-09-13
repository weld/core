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

package org.jboss.weld.bean;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.Decorator;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;

/**
 * Sub-interface of {@link Decorator} that contains metadata information about Weld-deployed
 * Decorators (including custom decorators).
 *
 * @author Marius Bogoevici
 */
public interface WeldDecorator<T> extends Decorator<T> {

    EnhancedAnnotatedType<?> getEnhancedAnnotated();

    /**
     * Returns the decorated method that can decorate a particular method, if one exists
     * <p/>
     * Such a method must be implement one of the decorated type methods, and can be parametrized
     *
     * @param method
     * @return
     */
    InvokableAnnotatedMethod<?> getDecoratorMethod(Method method);

}
