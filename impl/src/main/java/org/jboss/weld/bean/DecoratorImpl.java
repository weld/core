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
package org.jboss.weld.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import javassist.util.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.DecoratorProxy;
import org.jboss.weld.bean.proxy.DecoratorProxyFactory;
import org.jboss.weld.bean.proxy.ProxyMethodHandler;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.ForwardingInjectionTarget;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import static org.jboss.weld.logging.messages.BeanMessage.ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DECORATED_TYPE_PARAMETERIZED_DELEGATE_NOT;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_ON_NON_INITIALIZER_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_TYPE_PARAMETER_MISMATCH;
import static org.jboss.weld.logging.messages.BeanMessage.NO_DELEGATE_FOR_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.TOO_MANY_DELEGATES_FOR_DECORATOR;

public class DecoratorImpl<T> extends ManagedBean<T> implements WeldDecorator<T> {


    public static <T> Decorator<T> wrap(final Decorator<T> decorator) {
        return new ForwardingDecorator<T>() {

            @Override
            public Set<Annotation> getQualifiers() {
                return delegate().getDelegateQualifiers();
            }

            @Override
            public Set<Type> getTypes() {
                return delegate().getTypes();
            }

            @Override
            protected Decorator<T> delegate() {
                return decorator;
            }

        };
    }

    /**
     * Creates a decorator bean
     *
     * @param <T>         The type
     * @param clazz       The class
     * @param beanManager the current manager
     * @return a Bean
     */
    public static <T> DecoratorImpl<T> of(WeldClass<T> clazz, BeanManagerImpl beanManager, ServiceRegistry services) {
        return new DecoratorImpl<T>(clazz, beanManager, services);
    }

    private WeldClass<?> annotatedDelegateItem;
    private Map<MethodSignature, WeldMethod<?, ?>> decoratorMethods;
    private WeldInjectionPoint<?, ?> delegateInjectionPoint;
    private FieldInjectionPoint<?, ?> delegateFieldInjectionPoint;
    private Set<Annotation> delegateBindings;
    private Type delegateType;
    private Set<Type> delegateTypes;
    private Set<Type> decoratedTypes;

    protected DecoratorImpl(WeldClass<T> type, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(type, Decorator.class.getSimpleName() + BEAN_ID_SEPARATOR + type.getName(), beanManager, services);
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        if (!isInitialized()) {
            initDelegateInjectionPoint();
            super.initialize(environment);
            initDecoratedTypes();
            initDelegateBindings();
            initDelegateType();
            checkDelegateType();
            checkAbstractMethods();
        }
    }

    protected void initDecoratedTypes() {
        this.decoratedTypes = new HashSet<Type>(getWeldAnnotated().getInterfaceClosure());
        decoratedTypes.retainAll(getTypes());
        this.decoratedTypes.remove(Serializable.class);
        this.decoratorMethods = Decorators.getDecoratorMethods(beanManager, decoratedTypes, getWeldAnnotated());
    }

    protected void initDelegateInjectionPoint() {
        this.delegateInjectionPoint = getDelegateInjectionPoints().iterator().next();
        if(delegateInjectionPoint instanceof FieldInjectionPoint) {
            this.delegateFieldInjectionPoint = (FieldInjectionPoint<?, ?>) delegateInjectionPoint;
        }
    }


    protected Class<T> createEnhancedSubclass() {
        return new DecoratorProxyFactory<T>(beanManager.getContextId(), getWeldAnnotated().getJavaClass(), delegateInjectionPoint, this).getProxyClass();
    }

    @Override
    protected boolean isSubclassed() {
        return getWeldAnnotated().isAbstract();
    }

    @Override
    protected void checkDelegateInjectionPoints() {
        for (WeldInjectionPoint<?, ?> injectionPoint : getDelegateInjectionPoints()) {
            if (injectionPoint instanceof MethodInjectionPoint<?, ?> && !injectionPoint.isAnnotationPresent(Inject.class)) {
                throw new DefinitionException(DELEGATE_ON_NON_INITIALIZER_METHOD, injectionPoint);
            }
        }
        if (getDelegateInjectionPoints().size() == 0) {
            throw new DefinitionException(NO_DELEGATE_FOR_DECORATOR, getWeldAnnotated());
        } else if (getDelegateInjectionPoints().size() > 1) {
            throw new DefinitionException(TOO_MANY_DELEGATES_FOR_DECORATOR, getWeldAnnotated());
        }
    }

    protected void initDelegateBindings() {
        this.delegateBindings = new HashSet<Annotation>();
        this.delegateBindings.addAll(this.delegateInjectionPoint.getQualifiers());
    }

    protected void initDelegateType() {
        this.delegateType = this.delegateInjectionPoint.getBaseType();
        this.delegateTypes = new HashSet<Type>();
        delegateTypes.add(delegateType);
    }

    protected void checkDelegateType() {
        Set<Type> mostSpecificDecoratedTypes = new HashSet<Type>(Arrays.asList(getWeldAnnotated().getJavaClass().getGenericInterfaces()));
        mostSpecificDecoratedTypes.remove(Serializable.class);
        for (Type decoratedType : mostSpecificDecoratedTypes) {
            if (decoratedType instanceof Class<?>) {
                if (!((Class<?>) decoratedType).isAssignableFrom(delegateInjectionPoint.getJavaClass())) {
                    throw new DefinitionException(DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE, decoratedType, this);
                }
            } else if (decoratedType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) decoratedType;
                if (!delegateInjectionPoint.isParameterizedType()) {
                    throw new DefinitionException(DECORATED_TYPE_PARAMETERIZED_DELEGATE_NOT, delegateType, this);
                }
                if (!Arrays.equals(delegateInjectionPoint.getActualTypeArguments(), parameterizedType.getActualTypeArguments())) {
                    throw new DefinitionException(DELEGATE_TYPE_PARAMETER_MISMATCH, decoratedType, this);
                }
                Type rawType = ((ParameterizedType) decoratedType).getRawType();
                if (rawType instanceof Class<?> && !((Class<?>) rawType).isAssignableFrom(delegateInjectionPoint.getJavaClass())) {
                    throw new DefinitionException(DELEGATE_MUST_SUPPORT_EVERY_DECORATED_TYPE, decoratedType, this);
                }
            }
        }
        annotatedDelegateItem = beanManager.getServices().get(ClassTransformer.class).loadClass(delegateInjectionPoint.getJavaClass());
    }

    private void checkAbstractMethods() {
        if (isSubclassed()) {
            for (WeldMethod<?, ?> method : getWeldAnnotated().getWeldMethods()) {
                if (Reflections.isAbstract(((AnnotatedMethod<?>) method).getJavaMember())) {
                    MethodSignature methodSignature = method.getSignature();
                    if (this.annotatedDelegateItem.getWeldMethod(methodSignature) == null) {
                        throw new DefinitionException(ABSTRACT_METHOD_MUST_MATCH_DECORATED_TYPE, method.getSignature(), this, getWeldAnnotated().getName());
                    }
                }
            }
        }
    }

    @Override
    public InjectionTarget<T> getInjectionTarget() {
        final InjectionTarget<T> delegate = super.getInjectionTarget();
        if(delegateFieldInjectionPoint != null) {
            return new ForwardingInjectionTarget<T>() {
                @Override
                protected InjectionTarget<T> delegate() {
                    return delegate;
                }

                @Override
                public void inject(final T instance, final CreationalContext<T> ctx) {
                    super.inject(instance, ctx);

                    if(delegateFieldInjectionPoint != null) {
                        if(instance instanceof DecoratorProxy) {

                            //this code is only applicable if the delegate is injected into a field
                            //as the proxy can't intercept the delegate when setting the field
                            //we need to now read the delegate from the field

                            //this is only needed for fields, as constructor and method injection are handed
                            //at injection time
                            final Object delegate = delegateFieldInjectionPoint.getWeldField().get(instance);
                            final ProxyMethodHandler handler = new ProxyMethodHandler(new TargetBeanInstance(delegate), DecoratorImpl.this);
                            ((ProxyObject)instance).setHandler(handler);
                        }
                    }
                }
            };
        }
        return delegate;
    }

    public Set<Annotation> getDelegateQualifiers() {
        return delegateBindings;
    }

    public Type getDelegateType() {
        return delegateType;
    }

    public Set<Type> getDecoratedTypes() {
        return decoratedTypes;
    }

    public WeldInjectionPoint<?, ?> getDelegateInjectionPoint() {
        return delegateInjectionPoint;
    }

    @Override
    public void initDecorators() {
        // No-op, decorators can't have decorators
    }


    public WeldMethod<?, ?> getDecoratorMethod(Method method) {
        return Decorators.findDecoratorMethod(this, decoratorMethods, method);
    }

    @Override
    public String toString() {
        return "Decorator [" + getBeanClass().toString() + "] decorates [" + Formats.formatTypes(getDecoratedTypes()) + "] with delegate type [" + Formats.formatType(getDelegateType()) + "] and delegate qualifiers [" + Formats.formatAnnotations(getDelegateQualifiers()) + "]";
    }
}
