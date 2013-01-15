/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;

/**
 *
 * @author Jozef Hartinger
 *
 */
public class ProcessSyntheticAnnotatedTypeImpl<T> extends ProcessAnnotatedTypeImpl<T> implements ProcessSyntheticAnnotatedType<T> {

    private Extension source;

    public ProcessSyntheticAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<T> annotatedType, AnnotationDiscovery discovery, Extension source) {
        super(beanManager, annotatedType, ProcessSyntheticAnnotatedType.class, annotatedType.getJavaClass(), discovery);
        this.source = source;
    }

    @Override
    protected Resolvable createResolvable(Class<?> typeArgument, AnnotationDiscovery discovery) {
        return ProcessAnnotatedTypeEventResolvable.forProcessSyntheticAnnotatedType(typeArgument, discovery);
    }

    @Override
    public Extension getSource() {
        return source;
    }
}
