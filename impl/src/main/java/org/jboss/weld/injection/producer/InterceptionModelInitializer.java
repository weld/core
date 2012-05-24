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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED;
import static org.jboss.weld.util.Interceptors.filterInterceptorBindings;
import static org.jboss.weld.util.Interceptors.flattenInterceptorBindings;
import static org.jboss.weld.util.Interceptors.mergeBeanInterceptorBindings;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
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

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.bean.interceptor.SerializableContextualInterceptorReference;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.interceptor.builder.InterceptionModelBuilder;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Initializes {@link InterceptionModel} for a {@link Bean} or a non-contextual component.
 *
 * @author Marko Luksa
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InterceptionModelInitializer<T> {

    private static final InterceptorMetadata<?>[] EMPTY_INTERCEPTOR_METADATA_ARRAY = new InterceptorMetadata[0];

    private static <T> InterceptorMetadata<T>[] emptyInterceptorMetadataArray() {
        return cast(EMPTY_INTERCEPTOR_METADATA_ARRAY);
    }

    private final BeanManagerImpl manager;
    private final EnhancedAnnotatedType<T> annotatedType;
    private final Set<Class<? extends Annotation>> stereotypes;

    private Map<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>> interceptorMetadatas = new HashMap<Interceptor<?>, InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>>>();
    private List<AnnotatedMethod<?>> businessMethods;
    private final InterceptionModelBuilder<ClassMetadata<?>,?> builder;
    private boolean hasSerializationOrInvocationInterceptorMethods;

    public InterceptionModelInitializer(BeanManagerImpl manager, EnhancedAnnotatedType<T> annotatedType, Bean<?> bean) {
        this.manager = manager;
        this.annotatedType = annotatedType;
        this.builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(getClassMetadata());
        if (bean == null) {
            stereotypes = Collections.emptySet();
        } else {
            stereotypes = bean.getStereotypes();
        }
    }

    public void init() {
        initTargetClassInterceptors();
        businessMethods = Beans.getInterceptableMethods(annotatedType);

        initCdiInterceptors();
        initEjbInterceptors();

        InterceptionModel<ClassMetadata<?>, ?> interceptionModel = builder.build();
        if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods) {
            if (annotatedType.isFinal()) {
                throw new DefinitionException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, annotatedType.getJavaClass());
            }
            manager.getInterceptorModelRegistry().put(annotatedType.getJavaClass(), interceptionModel);
        }
    }

    private void initTargetClassInterceptors() {
        if (!Beans.isInterceptor(annotatedType)) {
            InterceptorMetadata<T> interceptorClassMetadata = manager.getInterceptorMetadataReader().getTargetClassInterceptorMetadata(WeldInterceptorClassMetadata.of(annotatedType));
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

    private ClassMetadata<T> getClassMetadata() {
        return manager.getInterceptorMetadataReader().getClassMetadata(annotatedType.getJavaClass());
    }

    private void initCdiInterceptors() {
        Map<Class<? extends Annotation>, Annotation> classBindingAnnotations = getClassInterceptorBindings();
        initCdiLifecycleInterceptors(classBindingAnnotations);
        initCdiBusinessMethodInterceptors(classBindingAnnotations);
    }

    private Map<Class<? extends Annotation>, Annotation> getClassInterceptorBindings() {
        return mergeBeanInterceptorBindings(manager, annotatedType, stereotypes);
    }

    private void initCdiLifecycleInterceptors(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
        if (classBindingAnnotations.size() == 0) {
            return;
        }
        initLifeCycleInterceptor(InterceptionType.POST_CONSTRUCT, classBindingAnnotations);
        initLifeCycleInterceptor(InterceptionType.PRE_DESTROY, classBindingAnnotations);
        initLifeCycleInterceptor(InterceptionType.PRE_PASSIVATE, classBindingAnnotations);
        initLifeCycleInterceptor(InterceptionType.POST_ACTIVATE, classBindingAnnotations);
    }

    private void initLifeCycleInterceptor(InterceptionType interceptionType, Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
        List<Interceptor<?>> resolvedInterceptors = manager.resolveInterceptors(interceptionType, classBindingAnnotations.values());
        if (!resolvedInterceptors.isEmpty()) {
            builder.intercept(interceptionType).with(toSerializableContextualArray(resolvedInterceptors));
        }
    }

    private void initCdiBusinessMethodInterceptors(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations) {
        for (AnnotatedMethod<?> method : businessMethods) {
            initCdiBusinessMethodInterceptor(method, getMethodBindingAnnotations(classBindingAnnotations, method));
        }
    }

    private Collection<Annotation> getMethodBindingAnnotations(Map<Class<? extends Annotation>, Annotation> classBindingAnnotations, AnnotatedMethod<?> method) {
        Set<Annotation> methodBindingAnnotations = flattenInterceptorBindings(manager, filterInterceptorBindings(manager, method.getAnnotations()), true, true);
        return mergeMethodInterceptorBindings(classBindingAnnotations, methodBindingAnnotations).values();
    }

    private void initCdiBusinessMethodInterceptor(AnnotatedMethod<?> method, Collection<Annotation> methodBindingAnnotations) {
        if (methodBindingAnnotations.size() == 0) {
            return;
        }
        initInterceptor(InterceptionType.AROUND_INVOKE, method, methodBindingAnnotations);
        initInterceptor(InterceptionType.AROUND_TIMEOUT, method, methodBindingAnnotations);
    }

    private void initInterceptor(InterceptionType interceptionType, AnnotatedMethod<?> method, Collection<Annotation> methodBindingAnnotations) {
        List<Interceptor<?>> methodBoundInterceptors = manager.resolveInterceptors(interceptionType, methodBindingAnnotations);
        if (methodBoundInterceptors != null && methodBoundInterceptors.size() > 0) {
            if (Reflections.isFinal(method.getJavaMember())) {
                throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodBoundInterceptors.get(0).getBeanClass().getName());
            }
            Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();
            builder.intercept(interceptionType, javaMethod).with(toSerializableContextualArray(methodBoundInterceptors));
        }
    }

    private void initEjbInterceptors() {
        initClassDeclaredEjbInterceptors();
        for (AnnotatedMethod<?> method : businessMethods) {
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
                builder.interceptAll().with(manager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
            }
        }
    }

    private void initMethodDeclaredEjbInterceptors(AnnotatedMethod<?> method) {
        Method javaMethod = Reflections.<AnnotatedMethod<T>>cast(method).getJavaMember();

        boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
        if (excludeClassInterceptors) {
            builder.ignoreGlobalInterceptors(javaMethod);
        }

        Class<?>[] methodDeclaredInterceptors = getMethodDeclaredInterceptors(method);
        if (methodDeclaredInterceptors != null && methodDeclaredInterceptors.length > 0) {
            if (Reflections.isFinal(method.getJavaMember())) {
                throw new DefinitionException(FINAL_INTERCEPTED_BEAN_METHOD_NOT_ALLOWED, method, methodDeclaredInterceptors[0].getName());
            }

            InterceptionType interceptionType = isTimeoutAnnotationPresentOn(method)
                    ? InterceptionType.AROUND_TIMEOUT
                    : InterceptionType.AROUND_INVOKE;
            InterceptorMetadata<?>[] interceptors = getMethodDeclaredInterceptorMetadatas(methodDeclaredInterceptors);
            builder.intercept(interceptionType, javaMethod).with(interceptors);
        }
    }

    private InterceptorMetadata<?>[] getMethodDeclaredInterceptorMetadatas(Class<?>[] methodDeclaredInterceptors) {
        List<InterceptorMetadata<?>> list = new ArrayList<InterceptorMetadata<?>>();
        for (Class<?> clazz : methodDeclaredInterceptors) {
            list.add(manager.getInterceptorMetadataReader().getInterceptorMetadata(clazz));
        }
        return list.toArray(new InterceptorMetadata[list.size()]);
    }

    private boolean isTimeoutAnnotationPresentOn(AnnotatedMethod<?> method) {
        return method.isAnnotationPresent(manager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS);
    }

    private Class<?>[] getMethodDeclaredInterceptors(AnnotatedMethod<?> method) {
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

    private InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> getInterceptorMetadata(Interceptor<?> interceptor) {
        SerializableContextualImpl<Interceptor<?>, ?> contextual = new SerializableContextualImpl(interceptor, manager.getServices().get(ContextualStore.class));
        if (interceptor instanceof InterceptorImpl) {
            InterceptorImpl<?> interceptorImpl = (InterceptorImpl<?>) interceptor;
            ClassMetadata<?> classMetadata = interceptorImpl.getInterceptorMetadata().getInterceptorClass();
            SerializableContextualInterceptorReference interceptorReference = new SerializableContextualInterceptorReference(contextual, classMetadata);
            return manager.getInterceptorMetadataReader().getInterceptorMetadata(interceptorReference);
        } else {
            //custom interceptor
            ClassMetadata<?> classMetadata = manager.getInterceptorMetadataReader().getClassMetadata(interceptor.getBeanClass());
            return new CustomInterceptorMetadata(new SerializableContextualInterceptorReference(contextual, null), classMetadata);
        }
    }

    /**
     * Merges bean interceptor bindings (including inherited ones) with method interceptor bindings. Method interceptor bindings
     * override bean interceptor bindings. The bean binding map is not modified - a copy is used.
     */
    protected Map<Class<? extends Annotation>, Annotation> mergeMethodInterceptorBindings(Map<Class<? extends Annotation>, Annotation> beanBindings,
            Collection<Annotation> methodBindingAnnotations) {

        Map<Class<? extends Annotation>, Annotation> mergedBeanBindings = new HashMap<Class<? extends Annotation>, Annotation>(beanBindings);
        // conflict detection
        Set<Class<? extends Annotation>> processedBindingTypes = new HashSet<Class<? extends Annotation>>();

        for (Annotation methodBinding : methodBindingAnnotations) {
            Class<? extends Annotation> methodBindingType = methodBinding.annotationType();
            if (processedBindingTypes.contains(methodBindingType)) {
                throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, annotatedType);
            }
            processedBindingTypes.add(methodBindingType);
            // override bean interceptor binding
            mergedBeanBindings.put(methodBindingType, methodBinding);
        }
        return mergedBeanBindings;
    }
}
