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
package org.jboss.weld.injection.producer.ejb;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.jlr.ConstructorSignatureImpl;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.proxy.SerializableSubclassFactory;
import org.jboss.weld.injection.AroundConstructCallback;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.producer.ForwardingInstantiator;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

public class SerializableSessionBeanInstantiator<T> extends ForwardingInstantiator<T> {

    private final ConstructorInjectionPoint<T> subclassConstructorInjectionPoint;

    public SerializableSessionBeanInstantiator(EnhancedAnnotatedType<T> type, SessionBean<T> bean, Instantiator<T> delegate, BeanManagerImpl manager) {
        super(delegate);
        Class<T> serializableSubclass = new SerializableSubclassFactory<T>(type.getJavaClass(), bean).getProxyClass();
        EnhancedAnnotatedType<T> enhancedSubclass = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(serializableSubclass, type.slim().getIdentifier().getBdaId());
        EnhancedAnnotatedConstructor<T> constructor = enhancedSubclass.getDeclaredEnhancedConstructor(new ConstructorSignatureImpl(delegate.getConstructor()));
        this.subclassConstructorInjectionPoint = InjectionPointFactory.silentInstance().createConstructorInjectionPoint(bean, bean.getBeanClass(), constructor, manager);
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager, AroundConstructCallback<T> callback) {
        return subclassConstructorInjectionPoint.newInstance(manager, ctx, callback);
    }
}
