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

package org.jboss.weld.util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper class for {@linkjakarta.enterprise.inject.spi.Decorator} inspections.
 *
 * @author Marius Bogoevici
 */
public class Decorators {

    private Decorators() {
    }

    /**
     * Determines the set of {@link InvokableAnnotatedMethod}s representing decorated methods of the specified decorator. A
     * decorated method
     * is any method declared by a decorated type which is implemented by the decorator.
     *
     * @param beanManager the bean manager
     * @param decorator the specified decorator
     * @return the set of {@link InvokableAnnotatedMethod}s representing decorated methods of the specified decorator
     */
    public static Set<InvokableAnnotatedMethod<?>> getDecoratorMethods(BeanManagerImpl beanManager,
            WeldDecorator<?> decorator) {
        ImmutableSet.Builder<InvokableAnnotatedMethod<?>> builder = ImmutableSet.builder();
        for (Type type : decorator.getDecoratedTypes()) {
            EnhancedAnnotatedType<?> weldClass = getEnhancedAnnotatedTypeOfDecoratedType(beanManager, type);
            for (EnhancedAnnotatedMethod<?, ?> method : weldClass.getDeclaredEnhancedMethods()) {
                if (decorator.getEnhancedAnnotated().getEnhancedMethod(method.getSignature()) != null) {
                    builder.add(InvokableAnnotatedMethod.of(method.slim()));
                }
            }
        }
        return builder.build();
    }

    private static EnhancedAnnotatedType<?> getEnhancedAnnotatedTypeOfDecoratedType(BeanManagerImpl beanManager, Type type) {
        if (type instanceof Class<?>) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) type);
        }
        if (type instanceof ParameterizedType && (((ParameterizedType) type).getRawType() instanceof Class)) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) ((ParameterizedType) type).getRawType());
        }
        throw BeanLogger.LOG.unableToProcessDecoratedType(type);
    }

    public static WeldInjectionPointAttributes<?, ?> findDelegateInjectionPoint(AnnotatedType<?> type,
            Iterable<InjectionPoint> injectionPoints) {
        WeldInjectionPointAttributes<?, ?> result = null;
        for (InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.isDelegate()) {
                if (result != null) {
                    throw BeanLogger.LOG.tooManyDelegateInjectionPoints(type);
                }
                result = InjectionPoints.getWeldInjectionPoint(injectionPoint);
            }
        }
        if (result == null) {
            throw BeanLogger.LOG.noDelegateInjectionPoint(type);
        }
        return result;
    }

    public static <T> T getOuterDelegate(Bean<T> bean, T instance, CreationalContext<T> creationalContext, Class<T> proxyClass,
            InjectionPoint originalInjectionPoint, BeanManagerImpl manager, List<Decorator<?>> decorators) {
        TargetBeanInstance beanInstance = new TargetBeanInstance(bean, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, bean, proxyClass, manager,
                manager.getServices().get(ContextualStore.class), decorators);
        DecorationHelper.push(decorationHelper);
        try {
            final T outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
            if (outerDelegate == null) {
                throw new WeldException(BeanLogger.LOG.proxyInstantiationFailed(bean));
            }
            return outerDelegate;
        } finally {
            DecorationHelper.pop();
        }
    }

    /**
     * Check whether the delegate type implements or extends all decorated types.
     *
     * @param decorator
     * @throws DefinitionException If the delegate type doesn't implement or extend all decorated types
     */
    public static void checkDelegateType(Decorator<?> decorator) {

        Set<Type> types = new HierarchyDiscovery(decorator.getDelegateType()).getTypeClosure();

        for (Type decoratedType : decorator.getDecoratedTypes()) {
            if (!types.contains(decoratedType)) {
                throw BeanLogger.LOG.delegateMustSupportEveryDecoratedType(decoratedType, decorator);
            }
        }
    }

    /**
     * Check all abstract methods are declared by the decorated types.
     *
     * @param type
     * @param beanManager
     * @param delegateType
     * @throws DefinitionException If any of the abstract methods is not declared by the decorated types
     */
    public static <T> void checkAbstractMethods(Set<Type> decoratedTypes, EnhancedAnnotatedType<T> type,
            BeanManagerImpl beanManager) {

        if (decoratedTypes == null) {
            decoratedTypes = new HashSet<Type>(type.getInterfaceClosure());
            decoratedTypes.remove(Serializable.class);
        }

        Set<MethodSignature> signatures = new HashSet<MethodSignature>();

        for (Type decoratedType : decoratedTypes) {
            for (EnhancedAnnotatedMethod<?, ?> method : ClassTransformer.instance(beanManager)
                    .getEnhancedAnnotatedType(Reflections.getRawType(decoratedType), beanManager.getId())
                    .getEnhancedMethods()) {
                signatures.add(method.getSignature());
            }
        }

        for (EnhancedAnnotatedMethod<?, ?> method : type.getEnhancedMethods()) {
            if (Reflections.isAbstract(((AnnotatedMethod<?>) method).getJavaMember())) {
                MethodSignature methodSignature = method.getSignature();
                if (!signatures.contains(methodSignature)) {
                    throw BeanLogger.LOG.abstractMethodMustMatchDecoratedType(method,
                            Formats.formatAsStackTraceElement(method.getJavaMember()));
                }
            }
        }
    }

    /**
     * Indicates whether a {@link Decorator} is passivation capable or not.
     *
     * @return
     */
    public static boolean isPassivationCapable(Decorator<?> decorator) {
        if (decorator instanceof CustomDecoratorWrapper<?>) {
            // unwrap
            decorator = Reflections.<CustomDecoratorWrapper<?>> cast(decorator).delegate();
        }
        if (decorator instanceof DecoratorImpl<?>) {
            DecoratorImpl<?> weldDecorator = (DecoratorImpl<?>) decorator;
            return weldDecorator.getEnhancedAnnotated().isSerializable();
        }
        return decorator instanceof PassivationCapable;
    }
}
