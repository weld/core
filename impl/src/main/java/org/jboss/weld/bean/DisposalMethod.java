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

import static org.jboss.weld.logging.messages.BeanMessage.INCONSISTENT_ANNOTATIONS_ON_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.METHOD_NOT_BUSINESS_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.MULTIPLE_DISPOSE_PARAMS;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

public class DisposalMethod<X, T> extends AbstractReceiverBean<X, T, Method> {

    protected MethodInjectionPoint<T, ? super X> disposalMethodInjectionPoint;
    private AnnotatedParameter<? super X> disposesParameter;
    // indicates whether a given type of metadata is required by the disposal method
    private boolean injectionPointMetadataParameter = false;
    private boolean beanMetadataParameter = false;

    private volatile EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod;

    public static <X, T> DisposalMethod<X, T> of(BeanManagerImpl manager, EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean) {
        return new DisposalMethod<X, T>(manager, method, declaringBean);
    }

    protected DisposalMethod(BeanManagerImpl beanManager, EnhancedAnnotatedMethod<T, ? super X> disposalMethod, AbstractClassBean<X> declaringBean) {
        super(BeanAttributesFactory.forDisposerMethod(disposalMethod, beanManager), new StringBuilder().append(DisposalMethod.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(declaringBean.getEnhancedAnnotated().getName()).append(disposalMethod.getSignature().toString()).toString(), declaringBean, beanManager, beanManager.getServices());
        this.disposalMethodInjectionPoint = InjectionPointFactory.instance().createMethodInjectionPoint(disposalMethod, declaringBean, declaringBean.getBeanClass(), true, beanManager);
        this.enhancedAnnotatedMethod = disposalMethod;
        initType(disposalMethod);
        addInjectionPoints(InjectionPoints.filterOutSpecialParameterInjectionPoints(disposalMethodInjectionPoint.getParameterInjectionPoints()));
        checkDisposalMethod();
    }

    private void initDisposesParameter() {
        this.disposesParameter = getEnhancedAnnotated().getEnhancedParameters(Disposes.class).get(0).slim();
    }

    private void initMetadataParameters() {
        for (Class<?> type : getEnhancedAnnotated().getParameterTypesAsArray()) {
            if (InjectionPoint.class.equals(type)) {
                injectionPointMetadataParameter = true;
            }
            if (Bean.class.equals(Reflections.getRawType(type))) {
                beanMetadataParameter = true;
            }
        }
    }

    public AnnotatedParameter<? super X> getDisposesParameter() {
        return disposesParameter;
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        initDisposesParameter();
        initMetadataParameters();
    }

    protected void initType(EnhancedAnnotatedMethod<T, ? super X> method) {
        this.type = cast(method.getEnhancedParameters(Disposes.class).get(0).getJavaClass());
    }

    @Override
    public AnnotatedMethod<? super X> getAnnotated() {
        return disposalMethodInjectionPoint.getAnnotated();
    }

    @Override
    public EnhancedAnnotatedMethod<T, ? super X> getEnhancedAnnotated() {
        return Beans.checkEnhancedAnnotatedAvailable(enhancedAnnotatedMethod);
    }

    @Override
    public void cleanupAfterBoot() {
        super.cleanupAfterBoot();
        this.enhancedAnnotatedMethod = null;
    }

    @Override
    public boolean isPassivationCapableBean() {
        // Not relevant
        return false;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        // Not relevant
        return false;
    }

    @Override
    public boolean isProxyable() {
        return true;
    }

    public T create(CreationalContext<T> creationalContext) {
        // Not Relevant
        return null;
    }

    public void invokeDisposeMethod(Object instance, CreationalContext<?> creationalContext) {
        Object receiverInstance = getReceiver(creationalContext, creationalContext);
        if (receiverInstance == null) {
            disposalMethodInjectionPoint.invokeWithSpecialValue(null, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        } else {
            disposalMethodInjectionPoint.invokeOnInstanceWithSpecialValue(receiverInstance, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        }
    }

    private void checkDisposalMethod() {
        if (getEnhancedAnnotated().getEnhancedParameters(Disposes.class).size() > 1) {
            throw new DefinitionException(MULTIPLE_DISPOSE_PARAMS, disposalMethodInjectionPoint);
        }
        if (getEnhancedAnnotated().getEnhancedParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Observes", "@Disposes", disposalMethodInjectionPoint);
        }
        if (getEnhancedAnnotated().getAnnotation(Inject.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Intitializer", "@Disposes", disposalMethodInjectionPoint);
        }
        if (getEnhancedAnnotated().getAnnotation(Produces.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes", disposalMethodInjectionPoint);
        }
        if (getEnhancedAnnotated().getAnnotation(Specializes.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Specialized", "@Disposes", disposalMethodInjectionPoint);
        }
        if (getDeclaringBean() instanceof SessionBean<?>) {
            boolean methodDeclaredOnTypes = false;
            // TODO use annotated item?
            for (Type type : getDeclaringBean().getTypes()) {
                if (type instanceof Class<?>) {
                    Class<?> clazz = (Class<?>) type;
                    if (SecureReflections.isMethodExists(clazz, getEnhancedAnnotated().getName(), getEnhancedAnnotated().getParameterTypesAsArray())) {
                        methodDeclaredOnTypes = true;
                        continue;
                    }
                }
            }
            if (!methodDeclaredOnTypes) {
                throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, getEnhancedAnnotated(), getDeclaringBean());
            }
        }
    }

    @Override
    protected void checkType() {

    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        // No-op. Producer method dependent objects are destroyed in producer method bean
    }

    @Override
    public AbstractBean<?, ?> getSpecializedBean() {
        // Doesn't support specialization
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    public boolean hasInjectionPointMetadataParameter() {
        return injectionPointMetadataParameter;
    }

    public boolean hasBeanMetadataParameter() {
        return beanMetadataParameter;
    }

    @Override
    public String toString() {
        return "Disposer method [" + getDisposesParameter().getDeclaringCallable() + "]";
    }

    @Override
    public boolean hasDefaultProducer() {
        return true; // cannot be replaced by an extension
    }
}
