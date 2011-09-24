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
package org.jboss.weld.resolution;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.inject.spi.Decorator;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Pete Muir
 */
public class TypeSafeDecoratorResolver extends TypeSafeBeanResolver<Decorator<?>> {

    public TypeSafeDecoratorResolver(BeanManagerImpl manager, Iterable<Decorator<?>> decorators) {
        super(manager, decorators);
    }

    @Override
    protected boolean matches(Resolvable resolvable, Decorator<?> bean) {
        return Reflections.matches(Collections.singleton(bean.getDelegateType()), resolvable.getTypes())
                && Beans.containsAllQualifiers(bean.getDelegateQualifiers(), resolvable.getQualifiers(), getBeanManager())
                && getBeanManager().getEnabled().getDecorator(bean.getBeanClass()) != null;
    }

    @Override
    protected Iterable<? extends Decorator<?>> getAllBeans(Resolvable resolvable) {
        return getAllBeans();
    }

    @Override
    protected Set<Decorator<?>> sortResult(Set<Decorator<?>> matchedDecorators) {
        Set<Decorator<?>> sortedBeans = new TreeSet<Decorator<?>>(getBeanManager().getEnabled().getDecoratorComparator());
        sortedBeans.addAll(matchedDecorators);
        return sortedBeans;
    }

}
