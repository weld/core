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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint.MethodInjectionPointType;
import org.jboss.weld.injection.MethodInvocationStrategy;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

public class DisposalMethod<X, T> {

    private static final Set<Class<? extends Annotation>> SPECIAL_PARAM_MARKERS = Collections.singleton(Disposes.class);

    private static final String DISPOSER_ANNOTATION = "@Disposes";
    private final BeanManagerImpl beanManager;
    private final AbstractClassBean<X> declaringBean;

    private final MethodInjectionPoint<T, ? super X> disposalMethodInjectionPoint;
    private final AnnotatedParameter<? super X> disposesParameter;

    private final Set<QualifierInstance> requiredQualifiers;

    private final MethodInvocationStrategy invocationStrategy;

    public static <X, T> DisposalMethod<X, T> of(BeanManagerImpl manager, EnhancedAnnotatedMethod<T, ? super X> method,
            AbstractClassBean<X> declaringBean) {
        return new DisposalMethod<X, T>(manager, method, declaringBean);
    }

    protected DisposalMethod(BeanManagerImpl beanManager, EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod,
            AbstractClassBean<X> declaringBean) {
        this.disposalMethodInjectionPoint = InjectionPointFactory.instance().createMethodInjectionPoint(
                MethodInjectionPointType.DISPOSER,
                enhancedAnnotatedMethod, declaringBean, declaringBean.getBeanClass(), SPECIAL_PARAM_MARKERS, beanManager);
        this.beanManager = beanManager;
        this.declaringBean = declaringBean;
        EnhancedAnnotatedParameter<?, ? super X> enhancedDisposesParameter = getEnhancedDisposesParameter(
                enhancedAnnotatedMethod);
        this.disposesParameter = enhancedDisposesParameter.slim();
        this.requiredQualifiers = getRequiredQualifiers(enhancedDisposesParameter);
        checkDisposalMethod(enhancedAnnotatedMethod, declaringBean);
        this.invocationStrategy = MethodInvocationStrategy.forDisposer(disposalMethodInjectionPoint, beanManager);
    }

    private EnhancedAnnotatedParameter<?, ? super X> getEnhancedDisposesParameter(
            EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod) {
        return enhancedAnnotatedMethod.getEnhancedParameters(Disposes.class).get(0);
    }

    public AnnotatedParameter<? super X> getDisposesParameter() {
        return disposesParameter;
    }

    public AnnotatedMethod<? super X> getAnnotated() {
        return disposalMethodInjectionPoint.getAnnotated();
    }

    public void invokeDisposeMethod(Object receiver, Object instance, CreationalContext<?> creationalContext) {
        invocationStrategy.invoke(receiver, disposalMethodInjectionPoint, instance, beanManager, creationalContext);
    }

    private void checkDisposalMethod(EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod,
            AbstractClassBean<X> declaringBean) {
        if (enhancedAnnotatedMethod.getEnhancedParameters(Disposes.class).size() > 1) {
            throw BeanLogger.LOG
                    .multipleDisposeParams(disposalMethodInjectionPoint,
                            Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
        }
        if (enhancedAnnotatedMethod.getEnhancedParameters(Observes.class).size() > 0) {
            throw BeanLogger.LOG.inconsistentAnnotationsOnMethod("@Observes", DISPOSER_ANNOTATION, disposalMethodInjectionPoint,
                    Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
        }
        if (enhancedAnnotatedMethod.getAnnotation(Inject.class) != null) {
            throw BeanLogger.LOG.inconsistentAnnotationsOnMethod("@Inject", DISPOSER_ANNOTATION, disposalMethodInjectionPoint,
                    Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
        }
        if (enhancedAnnotatedMethod.getAnnotation(Produces.class) != null) {
            throw BeanLogger.LOG.inconsistentAnnotationsOnMethod("@Produces", DISPOSER_ANNOTATION, disposalMethodInjectionPoint,
                    Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
        }
        if (enhancedAnnotatedMethod.getAnnotation(Specializes.class) != null) {
            throw BeanLogger.LOG.inconsistentAnnotationsOnMethod("@Specialized", DISPOSER_ANNOTATION,
                    disposalMethodInjectionPoint,
                    Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
        }
        if (declaringBean instanceof SessionBean<?>) {
            SessionBean<?> sessionBean = (SessionBean<?>) declaringBean;
            Set<MethodSignature> localBusinessMethodSignatures = sessionBean.getLocalBusinessMethodSignatures();
            Set<MethodSignature> remoteBusinessMethodSignatures = sessionBean.getRemoteBusinessMethodSignatures();
            if (!localBusinessMethodSignatures.contains(enhancedAnnotatedMethod.getSignature())
                    || remoteBusinessMethodSignatures
                            .contains(enhancedAnnotatedMethod.getSignature())) {
                throw BeanLogger.LOG.methodNotBusinessMethod("Disposer", enhancedAnnotatedMethod, declaringBean,
                        Formats.formatAsStackTraceElement(enhancedAnnotatedMethod.getJavaMember()));
            }
        }
        for (ParameterInjectionPoint<?, ?> ip : disposalMethodInjectionPoint.getParameterInjectionPoints()) {
            Class<?> rawType = Reflections.getRawType(ip.getType());
            if (Bean.class.equals(rawType) || Interceptor.class.equals(rawType) || Decorator.class.equals(rawType)) {
                Validator.checkBeanMetadataInjectionPoint(this, ip, getDisposesParameter().getBaseType());
            }
        }
    }

    public Type getGenericType() {
        return getDisposesParameter().getBaseType();
    }

    public Set<QualifierInstance> getRequiredQualifiers() {
        return requiredQualifiers;
    }

    public AbstractClassBean<X> getDeclaringBean() {
        return declaringBean;
    }

    @Override
    public String toString() {
        return "Disposer method [" + getDisposesParameter().getDeclaringCallable() + "]";
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return disposalMethodInjectionPoint.getInjectionPoints();
    }

    /**
     * A disposer method is bound to a producer if the producer is assignable to the disposed parameter.
     *
     * @param enhancedDisposedParameter
     * @return the set of required qualifiers for the given disposed parameter
     */
    private Set<QualifierInstance> getRequiredQualifiers(EnhancedAnnotatedParameter<?, ? super X> enhancedDisposedParameter) {
        Set<Annotation> disposedParameterQualifiers = enhancedDisposedParameter.getMetaAnnotations(Qualifier.class);
        if (disposedParameterQualifiers.isEmpty()) {
            disposedParameterQualifiers = Collections.<Annotation> singleton(Default.Literal.INSTANCE);
        }
        return beanManager.getServices().get(MetaAnnotationStore.class).getQualifierInstances(disposedParameterQualifiers);
    }
}
