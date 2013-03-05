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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.NON_CONTAINER_DECORATOR;
import java.util.List;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class BeanInjectionTarget<T> extends BasicInjectionTarget<T> {

    public BeanInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
    }

    @Override
    protected Injector<T> initInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        return new ResourceInjector<T>(type, bean, beanManager);
    }

    public void postConstruct(T instance) {
        if (getInstantiator().hasInterceptorSupport()) {
            InterceptionUtils.executePostConstruct(instance);
        } else {
            super.postConstruct(instance);
        }
    }

    public void preDestroy(T instance) {
        if (getInstantiator().hasInterceptorSupport()) {
            InterceptionUtils.executePredestroy(instance);
        } else {
            super.preDestroy(instance);
        }
    }

    public void dispose(T instance) {
        // No-op
    }

    protected boolean isInterceptor() {
        return (getBean() instanceof Interceptor<?>) || getType().isAnnotationPresent(javax.interceptor.Interceptor.class);
    }

    protected boolean isDecorator() {
        return (getBean() instanceof Decorator<?>) || getType().isAnnotationPresent(javax.decorator.Decorator.class);
    }

    protected boolean isInterceptionCandidate() {
        return !isInterceptor() && !isDecorator();
    }

    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(annotatedType.getJavaClass())) {
            new InterceptionModelInitializer<T>(beanManager, annotatedType, getBean()).init();
        }
        boolean hasInterceptors = isInterceptionCandidate() && (beanManager.getInterceptorModelRegistry().containsKey(getType().getJavaClass()));

        List<Decorator<?>> decorators = null;
        if (getBean() != null && isInterceptionCandidate()) {
            decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        }
        boolean hasDecorators = decorators != null && !decorators.isEmpty();
        if (hasDecorators) {
            checkDecoratedMethods(annotatedType, decorators);
        }

        if (hasInterceptors || hasDecorators) {
            if (!(getInstantiator() instanceof DefaultInstantiator<?>)) {
                throw new java.lang.IllegalStateException("Unexpected instantiator " + getInstantiator());
            }
            DefaultInstantiator<T> delegate = (DefaultInstantiator<T>) getInstantiator();
            setInstantiator(new SubclassedComponentInstantiator<T>(annotatedType, getBean(), delegate, beanManager));

            if (hasDecorators) {
                setInstantiator(new SubclassDecoratorApplyingInstantiator<T>(getInstantiator(), getBean(), decorators));
            }
            if (hasInterceptors) {
                setInstantiator(new InterceptorApplyingInstantiator<T>(annotatedType, this.getInstantiator(), beanManager, delegate.getConstructor().getAnnotated().getJavaMember()));
            }
        }
    }

    protected void checkDecoratedMethods(EnhancedAnnotatedType<T> type, List<Decorator<?>> decorators) {
        if (type.isFinal()) {
            throw new DeploymentException(FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED, this);
        }
        for (Decorator<?> decorator : decorators) {
            EnhancedAnnotatedType<?> decoratorClass;
            if (decorator instanceof DecoratorImpl<?>) {
                DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
                decoratorClass = decoratorBean.getBeanManager().getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(decoratorBean.getAnnotated());
            } else if (decorator instanceof CustomDecoratorWrapper<?>) {
                decoratorClass = ((CustomDecoratorWrapper<?>) decorator).getEnhancedAnnotated();
            } else {
                throw new IllegalStateException(NON_CONTAINER_DECORATOR, decorator);
            }

            for (EnhancedAnnotatedMethod<?, ?> decoratorMethod : decoratorClass.getEnhancedMethods()) {
                EnhancedAnnotatedMethod<?, ?> method = type.getEnhancedMethod(decoratorMethod.getSignature());
                if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal()) {
                    throw new DeploymentException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, method, decoratorMethod);
                }
            }
        }
    }

    @Override
    protected LifecycleCallbackInvoker<T> initInvoker(EnhancedAnnotatedType<T> type) {
        if (isInterceptor()) {
            return NoopLifecycleCallbackInvoker.getInstance();
        } else {
            return new DefaultLifecycleCallbackInvoker<T>(type);
        }
    }
}
