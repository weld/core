/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.bean.interceptor.SerializableContextualInterceptorReference;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.interceptor.InterceptorBindingType;
import org.jboss.weld.interceptor.builder.InterceptionModelBuilder;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Initializes {@link InterceptionModel} for a {@link Bean} or a non-contextual component.
 *
 * @param <T>
 */
public class InterceptionModelInitializer<T> {

    private static final InterceptorMetadata<?>[] EMPTY_INTERCEPTOR_METADATA_ARRAY = new InterceptorMetadata[0];

    private static <T> InterceptorMetadata<T>[] emptyInterceptorMetadataArray() {
        return cast(EMPTY_INTERCEPTOR_METADATA_ARRAY);
    }

    private final BeanManagerImpl beanManager;
    private final WeldClass<T> annotatedType;
    private final Set<Class<? extends Annotation>> stereotypes;
    private final Class<T> type;
    private final ContextualStore contextualStore;

    private boolean hasSerializationOrInvocationInterceptorMethods;
    private Map<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> interceptorMetadatas = new HashMap<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
    private List<WeldMethod<?,?>> businessMethods;
    private InterceptionModelBuilder<ClassMetadata<?>,?> builder;

    /**
     *
     * @param beanManager
     * @param annotatedType
     * @param type
     */
    public InterceptionModelInitializer(BeanManagerImpl beanManager, WeldClass<T> annotatedType, Class<T> type) {
        this(beanManager, annotatedType, null, type);
    }

    /**
     *
     * @param beanManager
     * @param annotatedType
     * @param stereotypes
     * @param type
     * @param contextualStore
     */
    public InterceptionModelInitializer(BeanManagerImpl beanManager, WeldClass<T> annotatedType, Bean<?> bean, Class<T> type) {
        this.beanManager = beanManager;
        this.annotatedType = annotatedType;
        this.stereotypes = bean != null ? bean.getStereotypes() : Collections.<Class<? extends Annotation>>emptySet();
        this.type = type;
        this.contextualStore = beanManager.getServices().get(ContextualStore.class);
    }

    public void init() {
        initTargetClassInterceptors();
        businessMethods = Beans.getInterceptableMethods(annotatedType);
        builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(getClassMetadata());

        initEjbInterceptors();
        initCdiInterceptors();

        InterceptionModel<ClassMetadata<?>, ?> interceptionModel = builder.build();
        if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods) {
            if (annotatedType.isFinal()) {
                throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, type);
            }
            beanManager.getInterceptorModelRegistry().put(type, interceptionModel);
        }
    }

    private ClassMetadata<T> getClassMetadata() {
        return beanManager.getInterceptorMetadataReader().getClassMetadata(type);
    }

    private void initCdiInterceptors() {
        Set<InterceptorBindingType> classBindings = getClassInterceptorBindings();
        initCdiLifecycleInterceptors(classBindings);
        initCdiBusinessMethodInterceptors(classBindings);
    }

    private Set<InterceptorBindingType> getClassInterceptorBindings() {
        Set<InterceptorBindingType> classBindingAnnotations = new HashSet<InterceptorBindingType>();
        classBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(annotatedType.getAnnotations()));
        for (Class<? extends Annotation> annotation : stereotypes) {
            classBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(beanManager.getStereotypeDefinition(annotation)));
        }
        return classBindingAnnotations;
    }

    private void initCdiLifecycleInterceptors(Set<InterceptorBindingType> classBindings) {
        if (classBindings.size() == 0) {
            return;
        }
        if (Beans.findInterceptorBindingConflicts(classBindings)) {
            throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, type);
        }

        initLifeCycleInterceptor(InterceptionType.POST_CONSTRUCT, classBindings);
        initLifeCycleInterceptor(InterceptionType.PRE_DESTROY, classBindings);
        initLifeCycleInterceptor(InterceptionType.PRE_PASSIVATE, classBindings);
        initLifeCycleInterceptor(InterceptionType.POST_ACTIVATE, classBindings);
    }

    private void initLifeCycleInterceptor(InterceptionType interceptionType, Set<InterceptorBindingType> interceptorBindingTypes) {
        List<Interceptor<?>> resolvedInterceptors = beanManager.resolveInterceptors(interceptionType, interceptorBindingTypes);
        builder.intercept(interceptionType).with(toSerializableContextualArray(resolvedInterceptors));
    }

    private void initCdiBusinessMethodInterceptors(Set<InterceptorBindingType> classBindings) {
        for (WeldMethod<?, ?> method : businessMethods) {
            initCdiBusinessMethodInterceptor(method, getMethodInterceptorBindings(classBindings, method));
        }
    }

    private Set<InterceptorBindingType> getMethodInterceptorBindings(Set<InterceptorBindingType> classBindingAnnotations, WeldMethod<?, ?> method) {
        Set<InterceptorBindingType> methodBindingAnnotations = new HashSet<InterceptorBindingType>();
        methodBindingAnnotations.addAll(classBindingAnnotations);
        methodBindingAnnotations.addAll(beanManager.extractAndFlattenInterceptorBindings(method.getAnnotations()));
        return methodBindingAnnotations;
    }

    private void initCdiBusinessMethodInterceptor(WeldMethod<?, ?> method, Set<InterceptorBindingType> methodInterceptorBindings) {
        if (methodInterceptorBindings.size() == 0) {
            return;
        }
        if (Beans.findInterceptorBindingConflicts(methodInterceptorBindings)) {
            throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, type + "." + method.getName() + "()");
        }

        initInterceptor(InterceptionType.AROUND_INVOKE, method, methodInterceptorBindings);
        initInterceptor(InterceptionType.AROUND_TIMEOUT, method, methodInterceptorBindings);
    }

    private void initInterceptor(InterceptionType interceptionType, WeldMethod<?, ?> method, Set<InterceptorBindingType> interceptorBindingTypes) {
        List<Interceptor<?>> methodBoundInterceptors = beanManager.resolveInterceptors(interceptionType, interceptorBindingTypes);
        if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
            if (method.isFinal()) {
                throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
            }
            Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();
            builder.intercept(interceptionType, javaMethod).with(toSerializableContextualArray(methodBoundInterceptors));
        }
    }

    private void initEjbInterceptors() {
        initClassDeclaredEjbInterceptors();
        for (WeldMethod<?, ?> method : businessMethods) {
            initMethodDeclaredEjbInterceptors(method);
        }
    }

    private void initClassDeclaredEjbInterceptors() {
        Class<?>[] classDeclaredInterceptors = null;
        if (annotatedType.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
            Annotation interceptorsAnnotation = annotatedType.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass());
            classDeclaredInterceptors = SecureReflections.extractValues(interceptorsAnnotation);
        }

        if (classDeclaredInterceptors != null) {
            for (Class<?> clazz : classDeclaredInterceptors) {
                builder.interceptAll().with(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void initMethodDeclaredEjbInterceptors(WeldMethod<?, ?> method) {
        Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();

        boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
        if (excludeClassInterceptors) {
            builder.ignoreGlobalInterceptors(javaMethod);
        }

        Class<?>[] methodDeclaredInterceptors = getMethodDeclaredInterceptors(method);
        if (methodDeclaredInterceptors != null && methodDeclaredInterceptors.length > 0) {
            if (method.isFinal()) {
                throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodDeclaredInterceptors[0].getName());
            }

            InterceptionType interceptionType = isTimeoutAnnotationPresentOn(method)
                    ? InterceptionType.AROUND_TIMEOUT
                    : InterceptionType.AROUND_INVOKE;
            InterceptorMetadata[] interceptors = getMethodDeclaredInterceptorMetadatas(methodDeclaredInterceptors);
            builder.intercept(interceptionType, javaMethod).with(interceptors);
        }
    }

    @SuppressWarnings("rawtypes")
    private InterceptorMetadata[] getMethodDeclaredInterceptorMetadatas(Class<?>[] methodDeclaredInterceptors) {
        List<InterceptorMetadata<?>> list = new ArrayList<InterceptorMetadata<?>>();
        for (Class<?> clazz : methodDeclaredInterceptors) {
            list.add(beanManager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
        }
        return list.toArray(new InterceptorMetadata[list.size()]);
    }

    private boolean isTimeoutAnnotationPresentOn(WeldMethod<?, ?> method) {
        return method.isAnnotationPresent(beanManager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS);
    }

    private Class<?>[] getMethodDeclaredInterceptors(WeldMethod<?, ?> method) {
        Class<?>[] methodDeclaredInterceptors = null;
        if (method.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass())) {
            methodDeclaredInterceptors = SecureReflections.extractValues(method.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass()));
        }
        return methodDeclaredInterceptors;
    }

    private InterceptorMetadata<SerializableContextual<?, ?>>[] toSerializableContextualArray(List<Interceptor<?>> interceptors) {
        List<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> serializableContextuals = new ArrayList<InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
        for (Interceptor<?> interceptor : interceptors) {
            serializableContextuals.add(getCachedInterceptorMetadata(interceptor));
        }
        return serializableContextuals.toArray(InterceptionModelInitializer.<SerializableContextual<?, ?>>emptyInterceptorMetadataArray());
    }

    private InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> getCachedInterceptorMetadata(Interceptor<?> interceptor) {
        InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> interceptorMetadata = interceptorMetadatas.get(interceptor);
        if (interceptorMetadata == null) {
            interceptorMetadata = getInterceptorMetadata(interceptor);
            interceptorMetadatas.put(interceptor, interceptorMetadata);
        }
        return interceptorMetadata;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> getInterceptorMetadata(Interceptor<?> interceptor) {
        SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(interceptor, contextualStore);
        if (interceptor instanceof InterceptorImpl) {
            InterceptorImpl interceptorImpl = (InterceptorImpl) interceptor;
            WeldInterceptorClassMetadata classMetadata = WeldInterceptorClassMetadata.of(interceptorImpl.getWeldAnnotated());
            SerializableContextualInterceptorReference interceptorReference = new SerializableContextualInterceptorReference(contextual, classMetadata);
            return beanManager.getInterceptorMetadataReader().getInterceptorMetadata(interceptorReference);
        } else {
            //custom interceptor
            ClassMetadata<?> classMetadata = beanManager.getInterceptorMetadataReader().getClassMetadata(interceptor.getBeanClass());
            return new CustomInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, null), classMetadata);
        }
    }

    private void initTargetClassInterceptors() {
        if (!Beans.isInterceptor(annotatedType)) {
            InterceptorMetadata<T> interceptorClassMetadata = beanManager.getInterceptorMetadataReader().getTargetClassInterceptorMetadata(WeldInterceptorClassMetadata.of(annotatedType));
            hasSerializationOrInvocationInterceptorMethods = interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_INVOKE)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.AROUND_TIMEOUT)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_PASSIVATE)
                    || interceptorClassMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_ACTIVATE);
        } else {
            // an interceptor does not have lifecycle methods of its own, but it intercepts the methods of the
            // target class
            hasSerializationOrInvocationInterceptorMethods = false;
        }
    }

}
