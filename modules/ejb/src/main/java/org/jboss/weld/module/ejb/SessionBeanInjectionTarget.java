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
package org.jboss.weld.module.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.DefaultInjector;
import org.jboss.weld.injection.producer.DefaultInstantiator;
import org.jboss.weld.injection.producer.DefaultLifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.Injector;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.injection.producer.LifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.SubclassDecoratorApplyingInstantiator;
import org.jboss.weld.injection.producer.SubclassedComponentInstantiator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Types;

class SessionBeanInjectionTarget<T> extends BeanInjectionTarget<T> {

    public static <T> SessionBeanInjectionTarget<T> of(EnhancedAnnotatedType<T> type, SessionBean<T> bean,
            BeanManagerImpl beanManager) {
        LifecycleCallbackInvoker<T> invoker = DefaultLifecycleCallbackInvoker.of(type);
        Injector<T> injector;
        if (bean.getEjbDescriptor().isStateless() || bean.getEjbDescriptor().isSingleton()) {
            injector = new DynamicInjectionPointInjector<T>(type, bean, beanManager);
        } else {
            injector = new DefaultInjector<T>(type, bean, beanManager);
        }
        return new SessionBeanInjectionTarget<T>(type, bean, beanManager, injector, invoker);
    }

    private final SessionBean<T> bean;

    private SessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl beanManager,
            Injector<T> injector, LifecycleCallbackInvoker<T> invoker) {
        super(type, bean, beanManager, injector, invoker);
        this.bean = bean;
    }

    @Override
    public SessionBean<T> getBean() {
        return bean;
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Set<InjectionPoint> injectionPoints) {
        if (bean instanceof SessionBean<?>) {
            EnhancedAnnotatedType<T> implementationClass = SessionBeans.getEjbImplementationClass((SessionBean<T>) bean);

            AbstractInstantiator<T> instantiator = null;
            if (type.equals(implementationClass)) {
                instantiator = new DefaultInstantiator<T>(type, bean, beanManager);
            } else {
                // Session bean subclassed by the EJB container
                instantiator = SubclassedComponentInstantiator.forSubclassedEjb(type, implementationClass, bean, beanManager);
            }
            injectionPoints.addAll(instantiator.getConstructorInjectionPoint().getParameterInjectionPoints());
            return instantiator;
        } else {
            throw new IllegalArgumentException("Cannot create SessionBeanInjectionTarget for " + bean);
        }
    }

    @Override
    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        initializeInterceptionModel(annotatedType);

        List<Decorator<?>> decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        if (!decorators.isEmpty()) {
            Instantiator<T> instantiator = getInstantiator();
            EnhancedAnnotatedType<T> implementationClass = SessionBeans.getEjbImplementationClass(getBean());
            instantiator = SubclassedComponentInstantiator.forInterceptedDecoratedBean(implementationClass, getBean(),
                    (AbstractInstantiator<T>) instantiator, beanManager);
            instantiator = new SubclassDecoratorApplyingInstantiator<T>(getBeanManager().getContextId(), instantiator,
                    getBean(), decorators, implementationClass.getJavaClass());
            setInstantiator(instantiator);
        }

        /*
         * We only take care of @AroundConstructor interception. The EJB container deals with the other types of interception.
         */
        setupConstructorInterceptionInstantiator(beanManager.getInterceptorModelRegistry().get(getType()));
    }

    @Override
    protected void buildInterceptionModel(EnhancedAnnotatedType<T> annotatedType, AbstractInstantiator<T> instantiator) {
        /*
         * instantiator.getConstructorInjectionPoint() may represent the constructor of the SessionBean subclass which may not
         * have annotations applied
         * Therefore, use the component class constructor instead of the one from subclass.
         */
        EnhancedAnnotatedConstructor<T> constructor = annotatedType
                .getDeclaredEnhancedConstructor(instantiator.getConstructorInjectionPoint().getSignature());
        new InterceptionModelInitializer<T>(beanManager, annotatedType, constructor, getBean()).init();
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        T result = super.produce(ctx);

        if (result instanceof ProxyObject) {
            // if decorators are applied, use SessionBeanViewMethodHandler
            ProxyObject proxy = (ProxyObject) result;
            proxy.weld_setHandler(new SessionBeanViewMethodHandler(bean.getTypes(),
                    (CombinedInterceptorAndDecoratorStackMethodHandler) proxy.weld_getHandler()));
        }
        return result;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        // explicitly use bean's AnnotatedType, not InjectionPoint's as this.getAnnotated()
        // may represent the annotated type for the EJB-container subclass (see SubclassedComponentDescriptor)
        getInjector().inject(instance, ctx, beanManager, bean.getAnnotated(), this);
    }

    /**
     * This {@link MethodHandler} assures that any method invoked on a decorated {@link SessionBean} is a method that belongs to
     * a bean type of the bean. If the methods belongs to the bean implementation and the bean implementation is not a bean type
     * the handler tries to replace the method with the equivalent method defined on a bean type.
     *
     * @author Jozef Hartinger
     *
     */
    private static class SessionBeanViewMethodHandler extends CombinedInterceptorAndDecoratorStackMethodHandler {

        private static final long serialVersionUID = -8038819529432133787L;

        private final Set<Class<?>> beanTypes;

        public SessionBeanViewMethodHandler(Set<Type> types, CombinedInterceptorAndDecoratorStackMethodHandler delegate) {
            this.beanTypes = Types.getRawTypes(types);
            setOuterDecorator(delegate.getOuterDecorator());
            setInterceptorMethodHandler(delegate.getInterceptorMethodHandler());
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (beanTypes.contains(thisMethod.getDeclaringClass())) {
                return super.invoke(self, thisMethod, proceed, args);
            } else {
                Method decoratedTypeMethod = getBeanTypeMethod(thisMethod);
                return super.invoke(self, decoratedTypeMethod, proceed, args);
            }
        }

        private Method getBeanTypeMethod(Method method) {
            for (Class<?> c : beanTypes) {
                try {
                    return c.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }
            return method;
        }
    }
}
