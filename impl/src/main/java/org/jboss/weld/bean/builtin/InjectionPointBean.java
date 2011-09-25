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
package org.jboss.weld.bean.builtin;

import org.jboss.weld.Container;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Bean for InjectionPoint metadata
 *
 * @author David Allen
 */
public class InjectionPointBean extends AbstractBuiltInBean<InjectionPoint> {

    private static final Set<Type> TYPES = Arrays2.<Type>asSet(InjectionPoint.class, Object.class);

    /**
     * Creates an InjectionPoint Web Bean for the injection of the containing bean owning
     * the field, constructor or method for the annotated item
     *
     * @param <T>     must be InjectionPoint
     * @param <S>
     * @param field   The annotated member field/parameter for the injection
     * @param manager The RI manager implementation
     */
    public InjectionPointBean(BeanManagerImpl manager) {
        super(InjectionPoint.class.getSimpleName(), manager);
    }

    public InjectionPoint create(CreationalContext<InjectionPoint> creationalContext) {
        return Container.instance(getBeanManager().getContextId()).services().get(CurrentInjectionPoint.class).peek();
    }

    public void destroy(InjectionPoint instance, CreationalContext<InjectionPoint> creationalContext) {

    }

    @Override
    public Class<InjectionPoint> getType() {
        return InjectionPoint.class;
    }

    public Set<Type> getTypes() {
        return TYPES;
    }

    @Override
    public String toString() {
        return "Implicit Bean [javax.enterprise.inject.spi.InjectionPoint] with qualifiers [@Default]";
    }

}
