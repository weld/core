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
import java.util.List;
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
import javax.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.collections.ListToSet;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

public class DisposalMethod<X, T> {

    private final BeanManagerImpl beanManager;
    private final AbstractClassBean<X> declaringBean;

    private final MethodInjectionPoint<T, ? super X> disposalMethodInjectionPoint;
    private final AnnotatedParameter<? super X> disposesParameter;
    // indicates whether a given type of metadata is required by the disposal method
    private boolean injectionPointMetadataParameter = false;
    private boolean beanMetadataParameter = false;

    private final Set<QualifierInstance> qualifiers;

    public static <X, T> DisposalMethod<X, T> of(BeanManagerImpl manager, EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean) {
        return new DisposalMethod<X, T>(manager, method, declaringBean);
    }

    protected DisposalMethod(BeanManagerImpl beanManager, EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod, AbstractClassBean<X> declaringBean) {
        this.disposalMethodInjectionPoint = InjectionPointFactory.instance().createMethodInjectionPoint(enhancedAnnotatedMethod, declaringBean, declaringBean.getBeanClass(), true, beanManager);
        this.beanManager = beanManager;
        this.declaringBean = declaringBean;
        EnhancedAnnotatedParameter<?, ? super X> enhancedDisposesParameter = getEnhancedDisposesParameter(enhancedAnnotatedMethod);
        this.disposesParameter = enhancedDisposesParameter.slim();
        this.qualifiers = QualifierInstance.qualifiers(beanManager, enhancedDisposesParameter.getMetaAnnotations(Qualifier.class));
        initMetadataParameters(enhancedAnnotatedMethod);
        checkDisposalMethod(enhancedAnnotatedMethod, declaringBean);
    }

    private EnhancedAnnotatedParameter<?, ? super X> getEnhancedDisposesParameter(EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod) {
        return enhancedAnnotatedMethod.getEnhancedParameters(Disposes.class).get(0);
    }

    private void initMetadataParameters(EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod) {
        for (Class<?> type : enhancedAnnotatedMethod.getParameterTypesAsArray()) {
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


    public AnnotatedMethod<? super X> getAnnotated() {
        return disposalMethodInjectionPoint.getAnnotated();
    }

    public void invokeDisposeMethod(Object receiver, Object instance, CreationalContext<?> creationalContext) {
        if (receiver == null) {
            disposalMethodInjectionPoint.invokeWithSpecialValue(null, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        } else {
            disposalMethodInjectionPoint.invokeOnInstanceWithSpecialValue(receiver, Disposes.class, instance, beanManager, creationalContext, IllegalArgumentException.class);
        }
    }

    private void checkDisposalMethod(EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod, AbstractClassBean<X> declaringBean) {
        if (enhancedAnnotatedMethod.getEnhancedParameters(Disposes.class).size() > 1) {
            throw new DefinitionException(MULTIPLE_DISPOSE_PARAMS, disposalMethodInjectionPoint);
        }
        if (enhancedAnnotatedMethod.getEnhancedParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Observes", "@Disposes", disposalMethodInjectionPoint);
        }
        if (enhancedAnnotatedMethod.getAnnotation(Inject.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Intitializer", "@Disposes", disposalMethodInjectionPoint);
        }
        if (enhancedAnnotatedMethod.getAnnotation(Produces.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes", disposalMethodInjectionPoint);
        }
        if (enhancedAnnotatedMethod.getAnnotation(Specializes.class) != null) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Specialized", "@Disposes", disposalMethodInjectionPoint);
        }
        if (declaringBean instanceof SessionBean<?>) {
            boolean methodDeclaredOnTypes = false;
            // TODO use annotated item?
            for (Type type : declaringBean.getTypes()) {
                if (type instanceof Class<?>) {
                    Class<?> clazz = (Class<?>) type;
                    if (SecureReflections.isMethodExists(clazz, enhancedAnnotatedMethod.getName(), enhancedAnnotatedMethod.getParameterTypesAsArray())) {
                        methodDeclaredOnTypes = true;
                        continue;
                    }
                }
            }
            if (!methodDeclaredOnTypes) {
                throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, enhancedAnnotatedMethod, declaringBean);
            }
        }
    }

    public Type getGenericType() {
        return getDisposesParameter().getBaseType();
    }

    public Set<QualifierInstance> getQualifiers() {
        return qualifiers;
    }

    public AbstractClassBean<X> getDeclaringBean() {
        return declaringBean;
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

    public Set<InjectionPoint> getInjectionPoints() {
        return disposalMethodInjectionPoint.getInjectionPoints();
    }
}
