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
package org.jboss.weld.bean.builtin.ee;

import static org.jboss.weld.util.reflection.Reflections.cast;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ResourceInjection;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A resource producer field that is static (not injected).
 *
 * @author Jozef Hartinger
 *
 * @param <X>
 * @param <T>
 */
public class StaticEEResourceProducerField<X, T> extends EEResourceProducerField<X, T> {

    public static <X, T> StaticEEResourceProducerField<X, T> of(BeanAttributes<T> attributes,
            EnhancedAnnotatedField<T, ? super X> field, AbstractClassBean<X> declaringBean,
            DisposalMethod<X, ?> disposalMethod, BeanManagerImpl manager, ServiceRegistry services) {
        return new StaticEEResourceProducerField<X, T>(attributes, field, declaringBean, disposalMethod, manager, services);
    }

    private final ResourceInjection<T> resourceInjection;

    protected StaticEEResourceProducerField(BeanAttributes<T> attributes, EnhancedAnnotatedField<T, ? super X> field,
            AbstractClassBean<X> declaringBean, DisposalMethod<X, ?> disposalMethod, BeanManagerImpl manager,
            ServiceRegistry services) {
        super(attributes, field, declaringBean, disposalMethod, manager, services);
        this.resourceInjection = getResourceInjection(field, declaringBean, manager);
    }

    protected ResourceInjection<T> getResourceInjection(EnhancedAnnotatedField<T, ? super X> field,
            AbstractClassBean<X> declaringBean, BeanManagerImpl manager) {
        FieldInjectionPoint<T, X> injectionPoint = cast(InjectionPointFactory.silentInstance().createFieldInjectionPoint(field, declaringBean, declaringBean.getBeanClass(), manager));
        return ResourceInjectionFactory.instance().getStaticProducerFieldResourceInjection(injectionPoint, beanManager);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        if (resourceInjection == null) {
            return null; // may happen if a resource IP is defined in an environment that does not support resource injection
        }
        return resourceInjection.getResourceReference(creationalContext);
    }
}
