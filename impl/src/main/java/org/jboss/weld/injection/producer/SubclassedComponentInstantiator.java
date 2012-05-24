/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedConstructorImpl;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.ProxyClassConstructorInjectionPointWrapper;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;

/**
 * Instantiates an enhanced subclass of a given component class. This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SubclassedComponentInstantiator<T> implements Instantiator<T> {

    private final ConstructorInjectionPoint<T> proxyClassConstructorInjectionPoint;

    public SubclassedComponentInstantiator(AnnotatedType<T> type, Bean<T> bean, SimpleInstantiator<T> delegate, BeanManagerImpl manager) {
        EnhancedAnnotatedConstructor<T> constructorForEnhancedSubclass = initEnhancedSubclass(manager, type, bean, delegate.getConstructor());
        this.proxyClassConstructorInjectionPoint = new ProxyClassConstructorInjectionPointWrapper<T>(bean, type.getJavaClass(), constructorForEnhancedSubclass, delegate.getConstructor(), manager);
    }

    protected EnhancedAnnotatedConstructor<T> initEnhancedSubclass(BeanManagerImpl manager, AnnotatedType<T> type, Bean<?> bean, ConstructorInjectionPoint<T> originalConstructorInjectionPoint) {
        ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);
        EnhancedAnnotatedType<T> enhancedSubclass = transformer.getEnhancedAnnotatedType(createEnhancedSubclass(type, bean));
        return EnhancedAnnotatedConstructorImpl.of(enhancedSubclass.getDeclaredEnhancedConstructor(originalConstructorInjectionPoint.getSignature()),
                enhancedSubclass,
                transformer);
    }

    protected Class<T> createEnhancedSubclass(AnnotatedType<T> type, Bean<?> bean) {
        Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
        for (AnnotatedMethod<?> method : Beans.getInterceptableMethods(type)) {
            enhancedMethodSignatures.add(new MethodSignatureImpl(method));
        }
        Set<Type> types = null;
        if (bean == null) {
            // TODO we may need to really discover types here
            types = Collections.<Type>singleton(type.getJavaClass());
        } else {
            types = bean.getTypes();
        }
        return new InterceptedSubclassFactory<T>(type.getJavaClass(), types, bean, enhancedMethodSignatures).getProxyClass();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return proxyClassConstructorInjectionPoint.newInstance(manager, ctx);
    }

    @Override
    public String toString() {
        return "SubclassedComponentInstantiator for " + proxyClassConstructorInjectionPoint.getType();
    }

    @Override
    public boolean hasInterceptors() {
        return false;
    }

    @Override
    public boolean hasDecorators() {
        return false;
    }
}
