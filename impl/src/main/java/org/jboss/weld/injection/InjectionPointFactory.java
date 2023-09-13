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
package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.injection.MethodInjectionPoint.MethodInjectionPointType;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.InferringFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.InferringParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Factory class that producer {@link InjectionPoint} instances for fields, parameters, methods and constructors. The
 * {@link ProcessInjectionPoint} event is fired for each created injection point unless the {@link #silentInstance()} is used.
 *
 * @author Jozef Hartinger
 *
 */
public class InjectionPointFactory {

    private InjectionPointFactory() {
    }

    private static final InjectionPointFactory INSTANCE = new InjectionPointFactory();
    private static final InjectionPointFactory SILENT_INSTANCE = new InjectionPointFactory() {

        @Override
        protected <T, X> FieldInjectionPointAttributes<T, X> processInjectionPoint(
                FieldInjectionPointAttributes<T, X> injectionPointAttributes, Class<?> declaringComponentClass,
                BeanManagerImpl manager) {
            // NOOP
            return injectionPointAttributes;
        }

        @Override
        protected <T, X> ParameterInjectionPointAttributes<T, X> processInjectionPoint(
                ParameterInjectionPointAttributes<T, X> injectionPointAttributes, Class<?> declaringComponentClass,
                BeanManagerImpl manager) {
            // NOOP
            return injectionPointAttributes;
        }
    };

    /**
     * Returns the default {@link InjectionPointFactory} singleton.
     *
     * @return the default {@link InjectionPointFactory} singleton
     */
    public static InjectionPointFactory instance() {
        return INSTANCE;
    }

    /**
     * Returns an {@link InjectionPointFactory} instance that never produces a {@link ProcessInjectionPoint} event. This is used
     * for creating observer method injection points of extensions and proxy classes.
     *
     * @return an {@link InjectionPointFactory} instance
     */
    public static InjectionPointFactory silentInstance() {
        return SILENT_INSTANCE;
    }

    /**
     * Notifies CDI extension of a given {@link InjectionPoint}.
     */
    protected <T, X> FieldInjectionPointAttributes<T, X> processInjectionPoint(
            FieldInjectionPointAttributes<T, X> injectionPointAttributes, Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        return manager.getContainerLifecycleEvents().fireProcessInjectionPoint(injectionPointAttributes,
                declaringComponentClass, manager);
    }

    /**
     * Notifies CDI extension of a given {@link InjectionPoint}.
     */
    protected <T, X> ParameterInjectionPointAttributes<T, X> processInjectionPoint(
            ParameterInjectionPointAttributes<T, X> injectionPointAttributes, Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        return manager.getContainerLifecycleEvents().fireProcessInjectionPoint(injectionPointAttributes,
                declaringComponentClass, manager);
    }

    /*
     * Creation of basic InjectionPoints
     */

    /**
     * Creates a new {@link FieldInjectionPoint} and fires the {@link ProcessInjectionPoint} event.
     *
     * @param field
     * @param declaringBean
     * @param declaringComponentClass used for resolution of type variables of the injection point type
     * @param manager
     * @return
     */
    public <T, X> FieldInjectionPoint<T, X> createFieldInjectionPoint(EnhancedAnnotatedField<T, X> field,
            Bean<?> declaringBean, Class<?> declaringComponentClass, BeanManagerImpl manager) {
        FieldInjectionPointAttributes<T, X> attributes = InferringFieldInjectionPointAttributes.of(field, declaringBean,
                declaringComponentClass, manager);
        attributes = processInjectionPoint(attributes, declaringComponentClass, manager);
        return new FieldInjectionPoint<T, X>(attributes);
    }

    /**
     * Creates a new {@link ParameterInjectionPoint} and fires the {@link ProcessInjectionPoint} event.
     *
     * @param parameter
     * @param declaringBean
     * @param declaringComponentClass used for resolution of type variables of the injection point type
     * @param manager
     * @return
     */
    public <T, X> ParameterInjectionPoint<T, X> createParameterInjectionPoint(EnhancedAnnotatedParameter<T, X> parameter,
            Bean<?> declaringBean, Class<?> declaringComponentClass, BeanManagerImpl manager) {
        ParameterInjectionPointAttributes<T, X> attributes = InferringParameterInjectionPointAttributes.of(parameter,
                declaringBean, declaringComponentClass, manager);
        attributes = processInjectionPoint(attributes, declaringComponentClass, manager);
        return new ParameterInjectionPointImpl<T, X>(attributes);
    }

    /*
     * Creation of callable InjectionPoints
     */

    public <T> ConstructorInjectionPoint<T> createConstructorInjectionPoint(Bean<T> declaringBean,
            EnhancedAnnotatedType<T> type, BeanManagerImpl manager) {
        EnhancedAnnotatedConstructor<T> constructor = Beans.getBeanConstructorStrict(type);
        return createConstructorInjectionPoint(declaringBean, type.getJavaClass(), constructor, manager);
    }

    public <T> ConstructorInjectionPoint<T> createConstructorInjectionPoint(Bean<T> declaringBean,
            Class<?> declaringComponentClass, EnhancedAnnotatedConstructor<T> constructor, BeanManagerImpl manager) {
        return new ConstructorInjectionPoint<T>(constructor, declaringBean, declaringComponentClass, this, manager);
    }

    public <T, X> MethodInjectionPoint<T, X> createMethodInjectionPoint(MethodInjectionPointType methodInjectionPointType,
            EnhancedAnnotatedMethod<T, X> enhancedMethod, Bean<?> declaringBean,
            Class<?> declaringComponentClass, Set<Class<? extends Annotation>> specialParameterMarkers,
            BeanManagerImpl manager) {
        if (enhancedMethod.isStatic()) {
            return new StaticMethodInjectionPoint<T, X>(methodInjectionPointType, enhancedMethod, declaringBean,
                    declaringComponentClass, specialParameterMarkers, this, manager);
        } else {
            return new VirtualMethodInjectionPoint<T, X>(methodInjectionPointType, enhancedMethod, declaringBean,
                    declaringComponentClass, specialParameterMarkers, this, manager);
        }
    }

    /*
     * Utility methods for field InjectionPoints
     */
    public List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPoints(Bean<?> declaringBean, EnhancedAnnotatedType<?> type,
            BeanManagerImpl manager) {
        List<Set<FieldInjectionPoint<?, ?>>> injectableFieldsList = new ArrayList<Set<FieldInjectionPoint<?, ?>>>();

        if (type.slim() instanceof UnbackedAnnotatedType<?>) {
            // external AnnotatedTypes require special treatment
            Collection<EnhancedAnnotatedField<?, ?>> allFields = type.getEnhancedFields(Inject.class);

            for (Class<?> clazz = type.getJavaClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                ImmutableSet.Builder<FieldInjectionPoint<?, ?>> fields = ImmutableSet.builder();
                for (EnhancedAnnotatedField<?, ?> field : allFields) {
                    if (!field.isStatic() && field.getJavaMember().getDeclaringClass().equals(clazz)) {
                        addFieldInjectionPoint(field, fields, declaringBean, type.getJavaClass(), manager);
                    }
                }
                injectableFieldsList.add(0, fields.build());
            }
        } else {
            for (EnhancedAnnotatedType<?> t = type; t != null && !t.getJavaClass().equals(Object.class); t = t
                    .getEnhancedSuperclass()) {
                ImmutableSet.Builder<FieldInjectionPoint<?, ?>> fields = ImmutableSet.builder();
                for (EnhancedAnnotatedField<?, ?> annotatedField : t.getDeclaredEnhancedFields(Inject.class)) {
                    if (!annotatedField.isStatic()) {
                        addFieldInjectionPoint(annotatedField, fields, declaringBean, t.getJavaClass(), manager);
                    }
                }
                injectableFieldsList.add(0, fields.build());
            }
        }
        return ImmutableList.copyOf(injectableFieldsList);
    }

    private void addFieldInjectionPoint(EnhancedAnnotatedField<?, ?> annotatedField,
            ImmutableSet.Builder<FieldInjectionPoint<?, ?>> injectableFields, Bean<?> declaringBean,
            Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        if (annotatedField.isFinal()) {
            throw UtilLogger.LOG.qualifierOnFinalField(annotatedField);
        }
        injectableFields.add(createFieldInjectionPoint(annotatedField, declaringBean, declaringComponentClass, manager));
    }

    /*
     * Utility methods for parameter InjectionPoints
     */

    public <X> List<ParameterInjectionPoint<?, X>> getParameterInjectionPoints(EnhancedAnnotatedCallable<?, X, ?> callable,
            Bean<?> declaringBean, Class<?> declaringComponentClass, BeanManagerImpl manager, boolean observerOrDisposer) {
        List<ParameterInjectionPoint<?, X>> parameters = new ArrayList<ParameterInjectionPoint<?, X>>();

        /*
         * bean that the injection point belongs to this is null for observer and disposer methods
         */
        Bean<?> bean = null;
        if (!observerOrDisposer) {
            bean = declaringBean;
        }

        for (EnhancedAnnotatedParameter<?, X> parameter : callable.getEnhancedParameters()) {
            if (isSpecialParameter(parameter)) {
                parameters.add(SpecialParameterInjectionPoint.of(parameter, bean, declaringBean.getBeanClass(), manager));
            } else {
                parameters.add(createParameterInjectionPoint(parameter, bean, declaringComponentClass, manager));
            }
        }
        return ImmutableList.copyOf(parameters);
    }

    private boolean isSpecialParameter(EnhancedAnnotatedParameter<?, ?> parameter) {
        return parameter.isAnnotationPresent(Disposes.class) || parameter.isAnnotationPresent(Observes.class)
                || parameter.isAnnotationPresent(ObservesAsync.class);
    }
}
