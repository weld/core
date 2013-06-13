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

import static org.jboss.weld.logging.messages.BeanMessage.ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_ON_NON_INITIALIZER_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.NO_DELEGATE_FOR_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.TOO_MANY_DELEGATES_FOR_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.UNABLE_TO_PROCESS;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper class for {@link javax.enterprise.inject.spi.Decorator} inspections.
 *
 * @author Marius Bogoevici
 */
public class Decorators {

    private Decorators() {
    }

    public static Map<MethodSignature, InvokableAnnotatedMethod<?>> getDecoratorMethods(BeanManagerImpl beanManager, Set<Type> decoratedTypes, EnhancedAnnotatedType<?> decoratorClass) {
        List<EnhancedAnnotatedMethod<?, ?>> decoratedMethods = Decorators.getDecoratedMethods(beanManager, decoratedTypes);
        Map<MethodSignature, InvokableAnnotatedMethod<?>> decoratorMethods = new HashMap<MethodSignature, InvokableAnnotatedMethod<?>>();
        for (EnhancedAnnotatedMethod<?, ?> method : decoratorClass.getEnhancedMethods()) {
            MethodSignatureImpl methodSignature = new MethodSignatureImpl(method);
            for (EnhancedAnnotatedMethod<?, ?> decoratedMethod : decoratedMethods) {
                if (new MethodSignatureImpl(decoratedMethod).equals(methodSignature)) {
                    decoratorMethods.put(methodSignature, InvokableAnnotatedMethod.of(decoratedMethod.slim()));
                }
            }
        }
        return decoratorMethods;
    }

    public static List<EnhancedAnnotatedMethod<?, ?>> getDecoratedMethods(BeanManagerImpl beanManager, Set<Type> decoratedTypes) {
        List<EnhancedAnnotatedMethod<?, ?>> methods = new ArrayList<EnhancedAnnotatedMethod<?, ?>>();
        for (Type type : decoratedTypes) {
            EnhancedAnnotatedType<?> weldClass = getWeldClassOfDecoratedType(beanManager, type);
            for (EnhancedAnnotatedMethod<?, ?> method : weldClass.getEnhancedMethods()) {
                if (!methods.contains(method)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    public static EnhancedAnnotatedType<?> getWeldClassOfDecoratedType(BeanManagerImpl beanManager, Type type) {
        if (type instanceof Class<?>) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) type);
        }
        if (type instanceof ParameterizedType && (((ParameterizedType) type).getRawType() instanceof Class)) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) ((ParameterizedType) type).getRawType());
        }
        throw new IllegalStateException(UNABLE_TO_PROCESS, type);
    }

    public static <T> InvokableAnnotatedMethod<?> findDecoratorMethod(WeldDecorator<T> decorator, Map<MethodSignature, InvokableAnnotatedMethod<?>> decoratorMethods, Method method) {
        // try the signature first, might be simpler
        MethodSignature key = new MethodSignatureImpl(method);
        InvokableAnnotatedMethod<?> foundMethod = decoratorMethods.get(key);
        if (foundMethod != null) {
            return foundMethod;
        }
        // try all methods
        for (InvokableAnnotatedMethod<?> decoratorMethod : decoratorMethods.values()) {
            if (method.getParameterTypes().length == decoratorMethod.getParameters().size()
                    && method.getName().equals(decoratorMethod.getJavaMember().getName())) {
                boolean parameterMatch = true;
                for (int i = 0; parameterMatch && i < method.getParameterTypes().length; i++) {
                    parameterMatch = parameterMatch && decoratorMethod.getJavaMember().getParameterTypes()[i].isAssignableFrom(method.getParameterTypes()[i]);
                }
                if (parameterMatch) {
                    return decoratorMethod;
                }
            }
        }
        return null;
    }

    public static WeldInjectionPointAttributes<?, ?> findDelegateInjectionPoint(AnnotatedType<?> type, Iterable<InjectionPoint> injectionPoints) {
        WeldInjectionPointAttributes<?, ?> result = null;
        for (InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.isDelegate()) {
                if (result != null) {
                    throw new DefinitionException(TOO_MANY_DELEGATES_FOR_DECORATOR, type);
                }
                if (injectionPoint instanceof MethodInjectionPoint<?, ?> && !injectionPoint.getAnnotated().isAnnotationPresent(Inject.class)) {
                    throw new DefinitionException(DELEGATE_ON_NON_INITIALIZER_METHOD, injectionPoint);
                }
                result = InjectionPoints.getWeldInjectionPoint(injectionPoint);
            }
        }
        if (result == null) {
            throw new DefinitionException(NO_DELEGATE_FOR_DECORATOR, type);
        }
        return result;
    }

    public static <T> T getOuterDelegate(Bean<T> bean, T instance, CreationalContext<T> creationalContext, Class<T> proxyClass, InjectionPoint originalInjectionPoint, BeanManagerImpl manager, List<Decorator<?>> decorators) {
        TargetBeanInstance beanInstance = new TargetBeanInstance(bean, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, bean, proxyClass, manager, manager.getServices().get(ContextualStore.class), decorators);
        DecorationHelper.push(decorationHelper);
        try {
            final T outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
            if (outerDelegate == null) {
                throw new WeldException(PROXY_INSTANTIATION_FAILED, bean);
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
            if(!types.contains(decoratedType)) {
                throw new DefinitionException(DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE, decoratedType, decorator);
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
    public static <T> void checkAbstractMethods(Set<Type> decoratedTypes, EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {

        if(decoratedTypes == null) {
            decoratedTypes = new HashSet<Type>(type.getInterfaceClosure());
            decoratedTypes.remove(Serializable.class);
        }

        Set<MethodSignature> signatures = new HashSet<MethodSignature>();

        for (Type decoratedType : decoratedTypes) {
            for (EnhancedAnnotatedMethod<?, ?> method : ClassTransformer.instance(beanManager).getEnhancedAnnotatedType(Reflections.getRawType(decoratedType), beanManager.getId()).getEnhancedMethods()) {
                signatures.add(method.getSignature());
            }
        }

        for (EnhancedAnnotatedMethod<?, ?> method : type.getEnhancedMethods()) {
            if (Reflections.isAbstract(((AnnotatedMethod<?>) method).getJavaMember())) {
                MethodSignature methodSignature = method.getSignature();
                if (!signatures.contains(methodSignature)) {
                    throw new DefinitionException(ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE, method.getSignature(), type);
                }
            }
        }
    }

    /**
     * Indicates whether a {@link Decorator} is passivation capable or not.
     * @return
     */
    public static boolean isPassivationCapable(Decorator<?> decorator) {
        if (decorator instanceof CustomDecoratorWrapper<?>) {
            // unwrap
            decorator = Reflections.<CustomDecoratorWrapper<?>>cast(decorator).delegate();
        }
        if (decorator instanceof DecoratorImpl<?>) {
            DecoratorImpl<?> weldDecorator = (DecoratorImpl<?>) decorator;
            return weldDecorator.getEnhancedAnnotated().isSerializable();
        }
        return decorator instanceof PassivationCapable;
    }
}
