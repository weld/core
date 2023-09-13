/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.builtin.ee.StaticEEResourceProducerField;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Abstract stateless resource injection processor.
 *
 * @author Martin Kouba
 *
 * @param <S>
 * @param <C> processor context
 */
public abstract class ResourceInjectionProcessor<S extends Service, C> {

    /**
     * @param fieldInjectionPoint
     * @param beanManager
     * @return {@link ResourceInjection} for static producer field or <code>null</code> if required services are not supported
     *         or the field is not annotated
     *         with the specific marker annotation
     * @see StaticEEResourceProducerField
     */
    protected <T, X> ResourceInjection<T> createStaticProducerFieldResourceInjection(
            FieldInjectionPoint<T, X> fieldInjectionPoint, BeanManagerImpl beanManager) {

        S injectionServices = getInjectionServices(beanManager);
        C processorContext = getProcessorContext(beanManager);

        if (injectionServices != null
                && fieldInjectionPoint.getAnnotated().isAnnotationPresent(getMarkerAnnotation(processorContext))
                && accept(fieldInjectionPoint.getAnnotated(), processorContext)) {
            return createFieldResourceInjection(fieldInjectionPoint, injectionServices, processorContext);
        }
        return null;
    }

    /**
     *
     * @param declaringBean
     * @param type
     * @param manager
     * @return the set of {@link ResourceInjection}s for the specified bean and type
     */
    protected <T> Set<ResourceInjection<?>> createResourceInjections(Bean<?> declaringBean, EnhancedAnnotatedType<T> type,
            BeanManagerImpl manager) {

        S injectionServices = getInjectionServices(manager);
        C processorContext = getProcessorContext(manager);

        if (injectionServices == null) {
            return Collections.emptySet();
        }

        Class<? extends Annotation> marker = getMarkerAnnotation(processorContext);

        final Collection<EnhancedAnnotatedField<?, ? super T>> fields = type.getDeclaredEnhancedFields(marker);
        final Collection<EnhancedAnnotatedMethod<?, ? super T>> methods = type.getDeclaredEnhancedMethods(marker);

        return createResourceInjections(fields, methods, declaringBean, type.getJavaClass(), manager);
    }

    /**
     *
     * @param fieldInjectionPoint
     * @param beanManager
     * @return {@link ResourceInjection} for the given field
     */
    private <T, X> ResourceInjection<T> createFieldResourceInjection(FieldInjectionPoint<T, X> fieldInjectionPoint,
            S injectionServices, C processorContext) {
        return new FieldResourceInjection<T, X>(fieldInjectionPoint,
                Reflections.<ResourceReferenceFactory<T>> cast(getResourceReferenceFactory(
                        fieldInjectionPoint, injectionServices, processorContext)));
    }

    /**
     *
     * @param methodInjectionPoint
     * @param beanManager
     * @return {@link ResourceInjection} for the given setter method
     */
    private <T, X> ResourceInjection<T> createSetterResourceInjection(ParameterInjectionPoint<T, X> parameterInjectionPoint,
            S injectionServices,
            C processorContext) {
        return new SetterResourceInjection<T, X>(parameterInjectionPoint,
                Reflections.<ResourceReferenceFactory<T>> cast(getResourceReferenceFactory(
                        parameterInjectionPoint, injectionServices, processorContext)));
    }

    public Class<? extends Annotation> getMarkerAnnotation(BeanManagerImpl manager) {
        return getMarkerAnnotation(getProcessorContext(manager));
    }

    protected abstract <T> ResourceReferenceFactory<T> getResourceReferenceFactory(InjectionPoint injectionPoint,
            S injectionServices, C processorContext);

    protected abstract Class<? extends Annotation> getMarkerAnnotation(C processorContext);

    protected abstract C getProcessorContext(BeanManagerImpl manager);

    protected abstract S getInjectionServices(BeanManagerImpl manager);

    protected <T> Set<ResourceInjection<?>> createResourceInjections(Iterable<EnhancedAnnotatedField<?, ? super T>> fields,
            Iterable<EnhancedAnnotatedMethod<?, ? super T>> methods, Bean<?> declaringBean, Class<?> declaringClass,
            BeanManagerImpl manager) {
        ImmutableSet.Builder<ResourceInjection<?>> resourceInjections = ImmutableSet.builder();

        S injectionServices = getInjectionServices(manager);
        C processorContext = getProcessorContext(manager);

        for (EnhancedAnnotatedField<?, ? super T> field : fields) {
            if (accept(field, processorContext)) {
                resourceInjections.add(createFieldResourceInjection(
                        InjectionPointFactory.silentInstance().createFieldInjectionPoint(field, declaringBean, declaringClass,
                                manager),
                        injectionServices,
                        processorContext));
            }
        }
        for (EnhancedAnnotatedMethod<?, ?> method : methods) {
            if (method.getParameters().size() != 1) {
                throw UtilLogger.LOG.resourceSetterInjectionNotAJavabean(method);
            }
            if (accept(method, processorContext)) {
                EnhancedAnnotatedParameter<?, ?> parameter = method.getEnhancedParameters().get(0);
                resourceInjections.add(createSetterResourceInjection(
                        InjectionPointFactory.silentInstance().createParameterInjectionPoint(parameter, declaringBean,
                                declaringClass, manager),
                        injectionServices, processorContext));
            }
        }
        return resourceInjections.build();
    }

    /**
     * Allows an implementation to indicate whether it accepts a given injection point annotated with the marker annotation.
     */
    protected boolean accept(AnnotatedMember<?> member, C processorContext) {
        return true;
    }

    protected Type getResourceInjectionPointType(AnnotatedMember<?> member) {
        if (member instanceof AnnotatedField<?>) {
            return member.getBaseType();
        }
        if (member instanceof AnnotatedMethod<?>) {
            AnnotatedMethod<?> method = (AnnotatedMethod<?>) member;
            if (method.getParameters().size() != 1) {
                throw UtilLogger.LOG.resourceSetterInjectionNotAJavabean(method);
            }
            return method.getParameters().get(0).getBaseType();
        }
        throw new IllegalArgumentException("Unknown member " + member);
    }

}
