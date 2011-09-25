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
package org.jboss.weld.context;

import org.jboss.weld.context.api.ContextualInstance;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * @param <T>
 * @author pmuir
 */
public interface WeldCreationalContext<T> extends CreationalContext<T> {

    void push(T incompleteInstance);

    <S> WeldCreationalContext<S> getCreationalContext(Contextual<S> Contextual);

    <S> S getIncompleteInstance(Contextual<S> bean);

    boolean containsIncompleteInstance(Contextual<?> bean);

    void addDependentInstance(ContextualInstance<?> contextualInstance);

    void release();

}
