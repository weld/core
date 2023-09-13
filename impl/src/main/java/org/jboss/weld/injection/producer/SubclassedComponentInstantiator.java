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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ProxyClassConstructorInjectionPointWrapper;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * Instantiates an enhanced subclass of a given component class. This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SubclassedComponentInstantiator<T> extends AbstractInstantiator<T> {

    public static <T> SubclassedComponentInstantiator<T> forSubclassedEjb(EnhancedAnnotatedType<T> componentType,
            EnhancedAnnotatedType<T> subclass, Bean<T> bean, BeanManagerImpl manager) {
        final EnhancedAnnotatedConstructor<T> componentConstructor = Beans.getBeanConstructor(componentType);
        final EnhancedAnnotatedConstructor<T> subclassConstructor = findMatchingConstructor(componentConstructor.getSignature(),
                subclass);
        final ConstructorInjectionPoint<T> cip = InjectionPointFactory.instance().createConstructorInjectionPoint(bean,
                componentType.getJavaClass(), subclassConstructor, manager);
        return new SubclassedComponentInstantiator<T>(cip, componentConstructor.getJavaMember());
    }

    public static <T> SubclassedComponentInstantiator<T> forInterceptedDecoratedBean(EnhancedAnnotatedType<T> type,
            Bean<T> bean, AbstractInstantiator<T> delegate, BeanManagerImpl manager) {
        return new SubclassedComponentInstantiator<T>(type, bean, delegate.getConstructorInjectionPoint(), manager);
    }

    private static <T> EnhancedAnnotatedConstructor<T> findMatchingConstructor(ConstructorSignature componentConstructor,
            EnhancedAnnotatedType<T> subclass) {
        return subclass.getDeclaredEnhancedConstructor(componentConstructor);
    }

    private final ConstructorInjectionPoint<T> proxyClassConstructorInjectionPoint;
    private final Constructor<T> componentClassConstructor;

    private SubclassedComponentInstantiator(ConstructorInjectionPoint<T> proxyClassConstructorInjectionPoint,
            Constructor<T> componentClassConstructor) {
        this.proxyClassConstructorInjectionPoint = proxyClassConstructorInjectionPoint;
        this.componentClassConstructor = componentClassConstructor;
    }

    protected SubclassedComponentInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean,
            ConstructorInjectionPoint<T> originalConstructor, BeanManagerImpl manager) {
        EnhancedAnnotatedConstructor<T> constructorForEnhancedSubclass = initEnhancedSubclass(manager, type, bean,
                originalConstructor);
        this.proxyClassConstructorInjectionPoint = new ProxyClassConstructorInjectionPointWrapper<T>(bean, type.getJavaClass(),
                constructorForEnhancedSubclass, originalConstructor, manager);
        this.componentClassConstructor = originalConstructor.getAnnotated().getJavaMember();
    }

    protected EnhancedAnnotatedConstructor<T> initEnhancedSubclass(BeanManagerImpl manager, EnhancedAnnotatedType<T> type,
            Bean<?> bean, ConstructorInjectionPoint<T> originalConstructorInjectionPoint) {
        ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);
        EnhancedAnnotatedType<T> enhancedSubclass = transformer
                .getEnhancedAnnotatedType(createEnhancedSubclass(type, bean, manager), type.slim().getIdentifier().getBdaId());
        return findMatchingConstructor(originalConstructorInjectionPoint.getSignature(), enhancedSubclass);
    }

    protected Class<T> createEnhancedSubclass(EnhancedAnnotatedType<T> type, Bean<?> bean, BeanManagerImpl manager) {
        Set<InterceptionModel> models = getInterceptionModelsForType(type, manager, bean);
        Set<MethodSignature> enhancedMethodSignatures = new HashSet<MethodSignature>();
        Set<MethodSignature> interceptedMethodSignatures = (models == null) ? enhancedMethodSignatures
                : new HashSet<MethodSignature>();

        for (AnnotatedMethod<?> method : Beans.getInterceptableMethods(type)) {
            enhancedMethodSignatures.add(MethodSignatureImpl.of(method));
            if (models != null) {
                for (InterceptionModel model : models) {
                    if (!model.getInterceptors(InterceptionType.AROUND_INVOKE, method.getJavaMember()).isEmpty()) {
                        interceptedMethodSignatures.add(MethodSignatureImpl.of(method));
                        break;
                    }
                }
            }
        }

        Set<Type> types = null;
        if (bean == null) {
            types = Collections.<Type> singleton(type.getJavaClass());
        } else {
            types = bean.getTypes();
        }
        return new InterceptedSubclassFactory<T>(manager.getContextId(), type.getJavaClass(), types, bean,
                enhancedMethodSignatures, interceptedMethodSignatures).getProxyClass();
    }

    private Set<InterceptionModel> getInterceptionModelsForType(EnhancedAnnotatedType<T> type, BeanManagerImpl manager,
            Bean<?> bean) {
        // if the bean has decorators consider all methods as intercepted
        if (bean != null && !manager.resolveDecorators(bean.getTypes(), bean.getQualifiers()).isEmpty()) {
            return null;
        }
        SlimAnnotatedTypeStore store = manager.getServices().get(SlimAnnotatedTypeStore.class);
        Set<InterceptionModel> models = new HashSet<InterceptionModel>();
        WeldCollections.addIfNotNull(models, manager.getInterceptorModelRegistry().get(type.slim()));
        for (SlimAnnotatedType<?> slimType : store.get(type.getJavaClass())) {
            WeldCollections.addIfNotNull(models, manager.getInterceptorModelRegistry().get(slimType));
        }
        for (InterceptionModel model : models) {
            if (model.hasTargetClassInterceptors()
                    && model.getTargetClassInterceptorMetadata().isEligible(InterceptionType.AROUND_INVOKE)) {
                // this means that all methods are intercepted
                // returning null here means that all methods will be overridden and will delegate to MethodHandler
                return null;
            }
        }
        return models;
    }

    @Override
    public String toString() {
        return "SubclassedComponentInstantiator for " + proxyClassConstructorInjectionPoint.getType();
    }

    @Override
    public boolean hasInterceptorSupport() {
        return false;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return false;
    }

    /**
     * Note that this method return a {@link ConstructorInjectionPoint} that represents the constructor of an enhanced subclass.
     * Use {@link #getConstructor()} to get the matching component class constructor.
     */
    @Override
    public ConstructorInjectionPoint<T> getConstructorInjectionPoint() {
        return proxyClassConstructorInjectionPoint;
    }

    @Override
    public Constructor<T> getConstructor() {
        return componentClassConstructor;
    }
}
