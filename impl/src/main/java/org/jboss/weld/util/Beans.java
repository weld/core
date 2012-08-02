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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.decorator.Decorator;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractReceiverBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.interceptor.InterceptorBindingType;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.Enabled;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.collections.HashSetSupplier;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import static java.util.Arrays.asList;
import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_DEFAULT_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_INJECTABLE_CONSTRUCTORS;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_INJECTABLE_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_POST_CONSTRUCT_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_PRE_DESTROY_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_POST_CONSTRUCT_METHODS;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_PRE_DESTROY_METHODS;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_INITIALIZER;
import static org.jboss.weld.logging.messages.UtilMessage.AMBIGUOUS_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.UtilMessage.ANNOTATION_NOT_QUALIFIER;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_CANNOT_BE_DISPOSAL_METHOD;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_CANNOT_BE_PRODUCER;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_METHOD_IS_GENERIC;
import static org.jboss.weld.logging.messages.UtilMessage.INVALID_QUANTITY_INJECTABLE_FIELDS_AND_INITIALIZER_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.QUALIFIER_ON_FINAL_FIELD;
import static org.jboss.weld.logging.messages.UtilMessage.REDUNDANT_QUALIFIER;
import static org.jboss.weld.logging.messages.UtilMessage.TOO_MANY_POST_CONSTRUCT_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.TOO_MANY_PRE_DESTROY_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_CONSTRUCTOR;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_ANNOTATIONS;
import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * Helper class for bean inspection
 *
 * @author Pete Muir
 * @author David Allen
 * @author Marius Bogoevici
 * @author Ales Justin
 * @author Marko Luksa
 */
public class Beans {
    // TODO Convert messages
    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    /**
     * Indicates if a bean's scope type is passivating
     *
     * @param bean The bean to inspect
     * @return True if the scope is passivating, false otherwise
     */
    public static boolean isPassivatingScope(Bean<?> bean, BeanManagerImpl manager) {
        if (bean == null) {
            return false;
        } else if (bean instanceof SessionBean<?>) {
            return ((SessionBean<?>) bean).getEjbDescriptor().isStateful();
        } else {
            return manager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isPassivating();
        }
    }

    /**
     * Tests if a bean is capable of having its state temporarily stored to
     * secondary storage
     *
     * @param bean The bean to inspect
     * @return True if the bean is passivation capable
     */
    public static boolean isPassivationCapableBean(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isPassivationCapableBean();
        } else {
            return Reflections.isSerializable(bean.getBeanClass());
        }
    }

    /**
     * Tests if a bean is capable of having its state temporarily stored to
     * secondary storage
     *
     * @param bean The bean to inspect
     * @return True if the bean is passivation capable
     */
    public static boolean isPassivationCapableDependency(String contextId, Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isPassivationCapableDependency();
        } else {
            if (Container.instance(contextId).services().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal()) {
                return true;
            } else if (bean.getScope().equals(Dependent.class) && isPassivationCapableBean(bean)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Indicates if a bean is proxyable
     *
     * @param bean The bean to test
     * @param id the container id
     * @return True if proxyable, false otherwise
     */
    public static boolean isBeanProxyable(Bean<?> bean, String id) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isProxyable();
        } else {
            return Proxies.isTypesProxyable(bean.getTypes(), id);
        }
    }

    public static List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass) {
        if (weldClass.isModified()) {
            return getFieldInjectionPointsFromWeldFields(contextId, declaringBean, weldClass);
        } else {
            return getFieldInjectionPointsFromDeclaredFields(contextId, declaringBean, weldClass);
        }
    }

    private static List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPointsFromWeldFields(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass) {
        Collection<WeldField<?, ?>> allFields = weldClass.getWeldFields(Inject.class);

        List<Set<FieldInjectionPoint<?, ?>>> injectableFields = new ArrayList<Set<FieldInjectionPoint<?, ?>>>();
        Class<?> clazz = weldClass.getJavaClass();
        while (clazz != null) {
            ArraySet<FieldInjectionPoint<?, ?>> set = new ArraySet<FieldInjectionPoint<?, ?>>();
            for (WeldField<?, ?> field : allFields) {
                Class<?> declaringClass = field.getJavaMember().getDeclaringClass();
                if (declaringClass.equals(clazz)) {
                    addFieldInjectionPoint(contextId, declaringBean, weldClass, field, set);
                }
            }
            set.trimToSize();
            injectableFields.add(0, set);

            clazz = clazz.getSuperclass();
        }
        return injectableFields;
    }

    private static List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPointsFromDeclaredFields(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass) {
        List<Set<FieldInjectionPoint<?, ?>>> list = new ArrayList<Set<FieldInjectionPoint<?, ?>>>();
        WeldClass<?> c = weldClass;
        while (c != null && !c.getJavaClass().equals(Object.class)) {
            ArraySet<FieldInjectionPoint<?, ?>> injectionPoints = new ArraySet<FieldInjectionPoint<?, ?>>();
            for (WeldField<?, ?> field : c.getDeclaredWeldFields(Inject.class)) {
                addFieldInjectionPoint(contextId, declaringBean, weldClass, field, injectionPoints);
            }
            injectionPoints.trimToSize();
            list.add(0, injectionPoints);

            c = c.getWeldSuperclass();
        }
        return list;
    }

    private static void addFieldInjectionPoint(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass, WeldField<?, ?> field, Set<FieldInjectionPoint<?, ?>> injectionPoints) {
        if (isInjectableField(field)) {
            validateInjectableField(field);
            injectionPoints.add(FieldInjectionPoint.of(contextId, declaringBean, weldClass, field));
        }
    }

    private static boolean isInjectableField(WeldField<?, ?> field) {
        return !field.isStatic() && !field.isAnnotationPresent(Produces.class);
    }

    private static void validateInjectableField(WeldField<?, ?> field) {
        if (field.isFinal()) {
            throw new DefinitionException(QUALIFIER_ON_FINAL_FIELD, field);
        }
    }

    public static Set<FieldInjectionPoint<?, ?>> mergeFieldInjectionPoints(List<? extends Set<? extends FieldInjectionPoint<?, ?>>> fieldInjectionPoints) {
        ArraySet<FieldInjectionPoint<?, ?>> injectionPoints = new ArraySet<FieldInjectionPoint<?, ?>>();
        for (Set<? extends FieldInjectionPoint<?, ?>> i : fieldInjectionPoints) {
            injectionPoints.addAll(i);
        }
        return injectionPoints.trimToSize();
    }

    public static <T> List<WeldMethod<?, ? super T>> getPostConstructMethods(WeldClass<T> type) {
        WeldClass<?> t = type;
        List<WeldMethod<?, ? super T>> methods = new ArrayList<WeldMethod<?, ? super T>>();
        while (!t.getJavaClass().equals(Object.class)) {
            Collection<WeldMethod<?, ? super T>> declaredMethods = cast(t.getDeclaredWeldMethods(PostConstruct.class));
            log.trace(FOUND_POST_CONSTRUCT_METHODS, declaredMethods, type);
            if (declaredMethods.size() > 1) {
                throw new DefinitionException(TOO_MANY_POST_CONSTRUCT_METHODS, type);
            } else if (declaredMethods.size() == 1) {
                WeldMethod<?, ? super T> method = declaredMethods.iterator().next();
                log.trace(FOUND_ONE_POST_CONSTRUCT_METHOD, method, type);
                methods.add(0, method);
            }
            t = t.getWeldSuperclass();
        }
        return methods;
    }

    public static <T> List<WeldMethod<?, ? super T>> getObserverMethods(WeldClass<T> type) {
        List<WeldMethod<?, ? super T>> observerMethods = new ArrayList<WeldMethod<?, ? super T>>();
        // Keep track of all seen methods so we can ignore overridden methods
        Multimap<MethodSignature, Package> seenMethods = Multimaps.newSetMultimap(new HashMap<MethodSignature, Collection<Package>>(), new Supplier<Set<Package>>() {

            public Set<Package> get() {
                return new HashSet<Package>();
            }

        });
        WeldClass<? super T> t = type;
        while (t != null && !t.getJavaClass().equals(Object.class)) {
            for (WeldMethod<?, ? super T> method : t.getDeclaredWeldMethods()) {
                if (!isOverridden(method, seenMethods) && !method.getWeldParameters(Observes.class).isEmpty()) {
                    observerMethods.add(method);
                }
                seenMethods.put(method.getSignature(), method.getPackage());
            }
            t = t.getWeldSuperclass();
        }
        return observerMethods;
    }

    public static <T> List<WeldMethod<?, ? super T>> getPreDestroyMethods(WeldClass<T> type) {
        WeldClass<?> t = type;
        List<WeldMethod<?, ? super T>> methods = new ArrayList<WeldMethod<?, ? super T>>();
        while (!t.getJavaClass().equals(Object.class)) {
            Collection<WeldMethod<?, ? super T>> declaredMethods = cast(t.getDeclaredWeldMethods(PreDestroy.class));
            log.trace(FOUND_PRE_DESTROY_METHODS, declaredMethods, type);
            if (declaredMethods.size() > 1) {
                throw new DefinitionException(TOO_MANY_PRE_DESTROY_METHODS, type);
            } else if (declaredMethods.size() == 1) {
                WeldMethod<?, ? super T> method = declaredMethods.iterator().next();
                log.trace(FOUND_ONE_PRE_DESTROY_METHOD, method, type);
                methods.add(0, method);
            }
            t = t.getWeldSuperclass();
        }
        return methods;
    }

    public static List<WeldMethod<?, ?>> getInterceptableMethods(WeldClass<?> type) {
        List<WeldMethod<?, ?>> annotatedMethods = new ArrayList<WeldMethod<?, ?>>();
        for (WeldMethod<?, ?> annotatedMethod : type.getWeldMethods()) {
            boolean businessMethod = !annotatedMethod.isStatic()
                    && !annotatedMethod.isAnnotationPresent(Inject.class)
                    && !annotatedMethod.getJavaMember().isBridge();

            if (businessMethod && !isInterceptorMethod(annotatedMethod)) {
                annotatedMethods.add(annotatedMethod);
            }
        }
        return annotatedMethods;
    }

    private static boolean isInterceptorMethod(WeldMethod<?, ?> annotatedMethod) {
        for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
            if (annotatedMethod.isAnnotationPresent(InterceptionTypeRegistry.getAnnotationClass(interceptionType)))
                return true;
        }
        return false;
    }

    public static Set<WeldInjectionPoint<?, ?>> getEjbInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(EjbInjectionServices.class)) {
            Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
            return getInjectionPoints(contextId, declaringBean, type, ejbAnnotationType);
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getPersistenceContextInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(JpaInjectionServices.class)) {
            Class<? extends Annotation> persistenceContextAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
            return getInjectionPoints(contextId, declaringBean, type, persistenceContextAnnotationType);
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getPersistenceUnitInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(JpaInjectionServices.class)) {
            Class<? extends Annotation> persistenceUnitAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_UNIT_ANNOTATION_CLASS;
            return getInjectionPoints(contextId, declaringBean, type, persistenceUnitAnnotationType);
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getResourceInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(ResourceInjectionServices.class)) {
            Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
            return getInjectionPoints(contextId, declaringBean, type, resourceAnnotationType);
        } else {
            return Collections.emptySet();
        }
    }

    private static Set<WeldInjectionPoint<?, ?>> getInjectionPoints(String contextId, Bean<?> declaringBean, WeldClass<?> type, Class<? extends Annotation> annotationType) {
        ArraySet<WeldInjectionPoint<?, ?>> set = new ArraySet<WeldInjectionPoint<?, ?>>();
        for (WeldField<?, ?> field : type.getWeldFields(annotationType)) {
            set.add(FieldInjectionPoint.of(contextId, declaringBean, type, field));
        }
        return set.trimToSize();
    }

    public static List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass) {
        if (weldClass.isModified()) {
            return getInitializerMethodsFromWeldMethods(contextId, declaringBean, weldClass);
        } else {
            return getInitializerMethodsFromDeclaredMethods(contextId, declaringBean, weldClass);
        }
    }

    private static <T> List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethodsFromWeldMethods(String contextId, Bean<?> declaringBean, WeldClass<T> weldClass) {
        List<Set<MethodInjectionPoint<?, ?>>> initializerMethods = new ArrayList<Set<MethodInjectionPoint<?, ?>>>();

        // Keep track of all seen methods so we can ignore overridden methods
        Multimap<MethodSignature, Package> seenMethods = Multimaps.newSetMultimap(new HashMap<MethodSignature, Collection<Package>>(), HashSetSupplier.<Package>instance());

        Class<?> clazz = weldClass.getJavaClass();
        while (clazz != null) {
            ArraySet<MethodInjectionPoint<?, ?>> set = new ArraySet<MethodInjectionPoint<?, ?>>();
            for (WeldMethod<?, ?> weldMethod : weldClass.getWeldMethods()) {
                if (weldMethod.getJavaMember().getDeclaringClass().equals(clazz)) {
                    processPossibleInitializerMethod(contextId, declaringBean, weldClass, weldMethod, seenMethods, set);
                }
            }
            set.trimToSize();
            initializerMethods.add(0, set);

            clazz = clazz.getSuperclass();
        }

        return initializerMethods;
    }

    public static List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethodsFromDeclaredMethods(String contextId, Bean<?> declaringBean, WeldClass<?> weldClass) {
        List<Set<MethodInjectionPoint<?, ?>>> list = new ArrayList<Set<MethodInjectionPoint<?, ?>>>();
        // Keep track of all seen methods so we can ignore overridden methods
        Multimap<MethodSignature, Package> seenMethods = Multimaps.newSetMultimap(new HashMap<MethodSignature, Collection<Package>>(), HashSetSupplier.<Package>instance());
        WeldClass<?> clazz = weldClass;
        while (clazz != null && !clazz.getJavaClass().equals(Object.class)) {
            ArraySet<MethodInjectionPoint<?, ?>> set = new ArraySet<MethodInjectionPoint<?, ?>>();
            Collection declaredWeldMethods = clazz.getDeclaredWeldMethods();
            for (WeldMethod<?, ?> method : (Collection<WeldMethod<?, ?>>) declaredWeldMethods) {
                processPossibleInitializerMethod(contextId, declaringBean, weldClass, method, seenMethods, set);
            }
            set.trimToSize();
            list.add(0, set);
            clazz = clazz.getWeldSuperclass();
        }
        return list;
    }

    private static void processPossibleInitializerMethod(String contextId, Bean<?> declaringBean, WeldClass<?> injectionTargetClass, WeldMethod<?, ?> method, Multimap<MethodSignature, Package> seenMethods, ArraySet<MethodInjectionPoint<?, ?>> set) {
        if (isInitializerMethod(method)) {
            validateInitializerMethod(method, injectionTargetClass);
            if (!isOverridden(method, seenMethods)) {
                set.add(MethodInjectionPoint.of(contextId, declaringBean, method));
            }
        }
        seenMethods.put(method.getSignature(), method.getPackage());
    }

    private static boolean isInitializerMethod(WeldMethod<?, ?> method) {
        return method.isAnnotationPresent(Inject.class) && !method.isStatic();
    }

    private static void validateInitializerMethod(WeldMethod<?, ?> method, WeldClass<?> type) {
        if (method.getAnnotation(Produces.class) != null) {
            throw new DefinitionException(INITIALIZER_CANNOT_BE_PRODUCER, method, type);
        } else if (method.getWeldParameters(Disposes.class).size() > 0) {
            throw new DefinitionException(INITIALIZER_CANNOT_BE_DISPOSAL_METHOD, method, type);
        } else if (method.getWeldParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INVALID_INITIALIZER, method);
        } else if (method.isGeneric()) {
            throw new DefinitionException(INITIALIZER_METHOD_IS_GENERIC, method, type);
        }
    }

    private static boolean isOverridden(WeldMethod<?, ?> method, Multimap<MethodSignature, Package> seenMethods) {
        if (method.isPrivate()) {
            return false;
        } else if (method.isPackagePrivate() && seenMethods.containsKey(method.getSignature())) {
            return seenMethods.get(method.getSignature()).contains(method.getPackage());
        } else {
            return seenMethods.containsKey(method.getSignature());
        }
    }

    public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(String contextId, Bean<?> declaringBean, WeldConstructor<?> constructor) {
        ArraySet<ParameterInjectionPoint<?, ?>> injectionPoints = new ArraySet<ParameterInjectionPoint<?, ?>>();
        for (WeldParameter<?, ?> parameter : constructor.getWeldParameters()) {
            injectionPoints.add(ParameterInjectionPoint.of(contextId, declaringBean, parameter));
        }
        return injectionPoints.trimToSize();
    }

    public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(String contextId, Bean<?> declaringBean, MethodInjectionPoint<?, ?> method) {
        ArraySet<ParameterInjectionPoint<?, ?>> injectionPoints = new ArraySet<ParameterInjectionPoint<?, ?>>();
        for (WeldParameter<?, ?> parameter : method.getWeldParameters()) {
            if (parameter.isAnnotationPresent(Disposes.class)) {
                continue; // disposes parameter is not an injection point
            }
            injectionPoints.add(ParameterInjectionPoint.of(contextId, declaringBean, parameter));
        }
        return injectionPoints.trimToSize();
    }

    public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(String contextId, Bean<?> declaringBean, List<Set<MethodInjectionPoint<?, ?>>> methodInjectionPoints) {
        ArraySet<ParameterInjectionPoint<?, ?>> injectionPoints = new ArraySet<ParameterInjectionPoint<?, ?>>();
        for (Set<MethodInjectionPoint<?, ?>> i : methodInjectionPoints) {
            for (MethodInjectionPoint<?, ?> method : i) {
                for (WeldParameter<?, ?> parameter : method.getWeldParameters()) {
                    injectionPoints.add(ParameterInjectionPoint.of(contextId, declaringBean, parameter));
                }
            }
        }
        return injectionPoints.trimToSize();
    }

    private static void addFieldInjectionPoint(String contextId, WeldField<?, ?> annotatedField, Set<FieldInjectionPoint<?, ?>> injectableFields, Bean<?> declaringBean, WeldClass<?> type) {
        if (!annotatedField.isAnnotationPresent(Produces.class)) {
            if (annotatedField.isFinal()) {
                throw new DefinitionException(QUALIFIER_ON_FINAL_FIELD, annotatedField);
            }
            FieldInjectionPoint<?, ?> fieldInjectionPoint = FieldInjectionPoint.of(contextId, declaringBean, type, annotatedField);
            injectableFields.add(fieldInjectionPoint);
        }
    }

    /**
     * Checks that all the qualifiers in the set requiredQualifiers are in the set
     * of qualifiers. Qualifier equality rules for annotation members are followed.
     *
     * @param requiredQualifiers The required qualifiers
     * @param qualifiers         The set of qualifiers to check
     * @return True if all matches, false otherwise
     */
    public static boolean containsAllQualifiers(Set<QualifierInstance> requiredQualifiers, Set<QualifierInstance> qualifiers, BeanManagerImpl beanManager) {
        return qualifiers.containsAll(requiredQualifiers);
    }

    public static boolean containsAllInterceptionBindings(Set<Annotation> expectedBindings, Set<QualifierInstance> existingBindings, BeanManagerImpl manager) {
        final Set<QualifierInstance> expected = manager.extractInterceptorBindingsForQualifierInstance(QualifierInstance.qualifiers(manager, expectedBindings));
        return manager.extractInterceptorBindingsForQualifierInstance(existingBindings).containsAll(expected);
    }

    public static boolean findInterceptorBindingConflicts(Set<InterceptorBindingType> flattenedBindings) {
        Map<Class<? extends Annotation>, InterceptorBindingType> foundAnnotations = new HashMap<Class<? extends Annotation>, InterceptorBindingType>();
        for (InterceptorBindingType binding : flattenedBindings) {
            if (foundAnnotations.containsKey(binding.annotationType())) {
                if (!binding.equals(foundAnnotations.get(binding.annotationType()))) {
                    return true;
                }
            } else {
                foundAnnotations.put(binding.annotationType(), binding);
            }
        }
        return false;
    }

    /**
     * Retains only beans which have deployment type X.
     * <p/>
     * The deployment type X is
     *
     * @param beans       The beans to filter
     * @param beanManager the bean manager
     * @return The filtered beans
     */
    public static <T extends Bean<?>> Set<T> removeDisabledAndSpecializedBeans(Set<T> beans, BeanManagerImpl beanManager) {
        if (beans.size() == 0) {
            return beans;
        } else {
            Set<T> result = new HashSet<T>();
            for (T bean : beans) {
                if (isBeanEnabled(bean, beanManager.getEnabled()) && !isSpecialized(bean, beans, beanManager) && !isSuppressedBySpecialization(bean, beanManager)) {
                    result.add(bean);
                }
            }
            return result;
        }
    }

    public static boolean isBeanEnabled(Bean<?> bean, Enabled enabled) {
        if (bean.isAlternative()) {
            if (enabled.getAlternativeClass(bean.getBeanClass()) != null) {
                return true;
            } else {
                for (Class<? extends Annotation> stereotype : bean.getStereotypes()) {
                    if (enabled.getAlternativeStereotype(stereotype) != null) {
                        return true;
                    }
                }
                return false;
            }
        } else if (bean instanceof AbstractReceiverBean<?, ?, ?>) {
            AbstractReceiverBean<?, ?, ?> receiverBean = (AbstractReceiverBean<?, ?, ?>) bean;
            return isBeanEnabled(receiverBean.getDeclaringBean(), enabled);
        } else if (bean instanceof DecoratorImpl<?>) {
            return enabled.getDecorator(bean.getBeanClass()) != null;
        } else if (bean instanceof InterceptorImpl<?>) {
            return enabled.getInterceptor(bean.getBeanClass()) != null;
        } else {
            return true;
        }
    }

    /**
     * Check if any of the beans is an alternative
     *
     * @param beans the beans to check
     * @return true if any bean is an alternative
     */
    public static boolean isAlternativePresent(Set<Bean<?>> beans) {
        for (Bean<?> bean : beans) {
            if (bean.isAlternative()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is alternative.
     *
     * @param annotated the annotated
     * @param mergedStereotypes merged stereotypes
     * @return true if alternative, false otherwise
     */
    public static boolean isAlternative(WeldAnnotated<?, ?> annotated, MergedStereotypes<?, ?> mergedStereotypes) {
        return annotated.isAnnotationPresent(Alternative.class) || mergedStereotypes.isAlternative();
    }

    /**
     * Check if bean is specialized by any of beans
     *
     * @param bean the bean to check
     * @param beanManager bean manager
     * @return true if bean is specialized by some bean in all beans
     */
    public static <T extends Bean<?>> boolean isSpecialized(T bean, BeanManagerImpl beanManager) {
        BeansClosure closure = beanManager.getClosure();
        return closure.isSpecialized(bean);
    }

    /**
     * Check if bean is specialized by any of beans
     *
     * @param bean the bean to check
     * @param beans the possible specialized beans
     * @param beanManager bean manager
     * @return true if bean is specialized by some bean in beans
     */
    public static <T extends Bean<?>> boolean isSpecialized(T bean, Set<T> beans, BeanManagerImpl beanManager) {
        BeansClosure closure = beanManager.getClosure();
        Bean<?> specializedBean = closure.getSpecialized(bean);
        //noinspection SuspiciousMethodCalls
        return (specializedBean != null && beans.contains(specializedBean));
    }

    /**
     * Check if the given producer/disposer method of producer field is defined on a specialized bean and is therefore disabled.
     */
    public static boolean isSuppressedBySpecialization(Bean<?> bean, BeanManagerImpl manager) {
        if (bean instanceof AbstractReceiverBean<?, ?, ?>) {
            BeansClosure closure = manager.getClosure();
            if (closure.isSpecialized(Reflections.<AbstractReceiverBean<?, ?, ?>>cast(bean).getDeclaringBean())) {
                // if a bean is specialized, its producer methods are not enabled (WELD-977)
                return true;
            }
        }
        return false;
    }

    public static <T> ConstructorInjectionPoint<T> getBeanConstructor(String contextId, Bean<T> declaringBean, WeldClass<T> type) {
        ConstructorInjectionPoint<T> constructor = null;
        Collection<WeldConstructor<T>> initializerAnnotatedConstructors = type.getWeldConstructors(Inject.class);
        log.trace(FOUND_INJECTABLE_CONSTRUCTORS, initializerAnnotatedConstructors, type);
        if (initializerAnnotatedConstructors.size() > 1) {
            if (initializerAnnotatedConstructors.size() > 1) {
                throw new DefinitionException(AMBIGUOUS_CONSTRUCTOR, type, initializerAnnotatedConstructors);
            }
        } else if (initializerAnnotatedConstructors.size() == 1) {
            constructor = ConstructorInjectionPoint.of(contextId, declaringBean, initializerAnnotatedConstructors.iterator().next());
            log.trace(FOUND_ONE_INJECTABLE_CONSTRUCTOR, constructor, type);
        } else if (type.getNoArgsWeldConstructor() != null) {

            constructor = ConstructorInjectionPoint.of(contextId, declaringBean, type.getNoArgsWeldConstructor());
            log.trace(FOUND_DEFAULT_CONSTRUCTOR, constructor, type);
        }

        if (constructor == null) {
            throw new DefinitionException(UNABLE_TO_FIND_CONSTRUCTOR, type);
        } else {
            return constructor;
        }
    }

    /**
     * Injects EJBs and common fields
     */
    public static <T> void injectEEFields(T beanInstance, BeanManagerImpl manager, Iterable<WeldInjectionPoint<?, ?>> ejbInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> resourceInjectionPoints) {
        EjbInjectionServices ejbServices = manager.getServices().get(EjbInjectionServices.class);
        JpaInjectionServices jpaServices = manager.getServices().get(JpaInjectionServices.class);
        ResourceInjectionServices resourceServices = manager.getServices().get(ResourceInjectionServices.class);

        if (ejbServices != null) {
            for (WeldInjectionPoint<?, ?> injectionPoint : ejbInjectionPoints) {
                Object ejbInstance = ejbServices.resolveEjb(injectionPoint);
                injectionPoint.inject(beanInstance, ejbInstance);
            }
        }

        if (jpaServices != null) {
            for (WeldInjectionPoint<?, ?> injectionPoint : persistenceContextInjectionPoints) {
                Object pcInstance = jpaServices.resolvePersistenceContext(injectionPoint);
                injectionPoint.inject(beanInstance, pcInstance);
            }
            for (WeldInjectionPoint<?, ?> injectionPoint : persistenceUnitInjectionPoints) {
                Object puInstance = jpaServices.resolvePersistenceUnit(injectionPoint);
                injectionPoint.inject(beanInstance, puInstance);
            }
        }

        if (resourceServices != null) {
            for (WeldInjectionPoint<?, ?> injectionPoint : resourceInjectionPoints) {
                Object resourceInstance = resourceServices.resolveResource(injectionPoint);
                injectionPoint.inject(beanInstance, resourceInstance);
            }
        }
    }

    /**
     * Inspect an injection point, and try to retrieve a EE resource for it
     */
    public static Object resolveEEResource(BeanManagerImpl manager, WeldInjectionPoint<?, ?> injectionPoint) {
        EjbInjectionServices ejbServices = manager.getServices().get(EjbInjectionServices.class);
        JpaInjectionServices jpaServices = manager.getServices().get(JpaInjectionServices.class);
        ResourceInjectionServices resourceServices = manager.getServices().get(ResourceInjectionServices.class);

        if (ejbServices != null) {
            Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
            if (injectionPoint.isAnnotationPresent(ejbAnnotationType)) {
                return ejbServices.resolveEjb(injectionPoint);
            }
        }

        if (jpaServices != null) {
            final PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);

            Class<? extends Annotation> persistenceUnitAnnotationType = persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS;
            if (injectionPoint.isAnnotationPresent(persistenceUnitAnnotationType)) {
                return jpaServices.resolvePersistenceUnit(injectionPoint);
            }

            Class<? extends Annotation> persistenceContextAnnotationType = persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
            if (injectionPoint.isAnnotationPresent(persistenceContextAnnotationType)) {
                return jpaServices.resolvePersistenceContext(injectionPoint);
            }
        }

        if (resourceServices != null) {
            Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
            if (injectionPoint.isAnnotationPresent(resourceAnnotationType)) {
                return resourceServices.resolveResource(injectionPoint);
            }
        }
        return null;
    }

    /**
     * Gets the declared bean type
     *
     * @return The bean type
     */
    public static Type getDeclaredBeanType(Class<?> clazz) {
        Type[] actualTypeArguments = Reflections.getActualTypeArguments(clazz);
        if (actualTypeArguments.length == 1) {
            return actualTypeArguments[0];
        } else {
            return null;
        }
    }

    /**
     * Injects bound fields
     *
     * @param instance The instance to inject into
     */
    public static <T> void injectBoundFields(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager, Iterable<? extends FieldInjectionPoint<?, ?>> injectableFields) {
        for (FieldInjectionPoint<?, ?> injectableField : injectableFields) {
            injectableField.inject(instance, manager, creationalContext);
        }
    }

    public static <T> void injectFieldsAndInitializers(T instance, CreationalContext<T> ctx, BeanManagerImpl beanManager, List<? extends Iterable<? extends FieldInjectionPoint<?, ?>>> injectableFields, List<? extends Iterable<? extends MethodInjectionPoint<?, ?>>> initializerMethods) {
        if (injectableFields.size() != initializerMethods.size()) {
            throw new IllegalArgumentException(INVALID_QUANTITY_INJECTABLE_FIELDS_AND_INITIALIZER_METHODS, injectableFields, initializerMethods);
        }
        for (int i = 0; i < injectableFields.size(); i++) {
            injectBoundFields(instance, ctx, beanManager, injectableFields.get(i));
            callInitializers(instance, ctx, beanManager, initializerMethods.get(i));
        }
    }

    /**
     * Calls all initializers of the bean
     *
     * @param instance The bean instance
     */
    public static <T> void callInitializers(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager, Iterable<? extends MethodInjectionPoint<?, ?>> initializerMethods) {
        for (MethodInjectionPoint<?, ?> initializer : initializerMethods) {
            initializer.invoke(instance, manager, creationalContext, CreationException.class);
        }
    }

    public static <T> boolean isInterceptor(WeldClass<T> annotatedItem) {
        return annotatedItem.isAnnotationPresent(javax.interceptor.Interceptor.class);
    }

    public static <T> boolean isDecorator(WeldClass<T> annotatedItem) {
        return annotatedItem.isAnnotationPresent(Decorator.class);
    }

    public static Annotation[] mergeInQualifiers(String contextId, Annotation[] qualifiers, Annotation[] newQualifiers) {
        if (qualifiers == null || newQualifiers == null)
            return EMPTY_ANNOTATIONS;

        return mergeInQualifiers(contextId, asList(qualifiers), newQualifiers).toArray(Reflections.EMPTY_ANNOTATIONS);
    }

    public static Set<Annotation> mergeInQualifiers(String contextId, Collection<Annotation> qualifiers, Annotation[] newQualifiers) {
        Set<Annotation> result = new HashSet<Annotation>();

        if (qualifiers != null && qualifiers.isEmpty() == false)
            result.addAll(qualifiers);

        if (newQualifiers != null && newQualifiers.length > 0) {
            final MetaAnnotationStore store = Container.instance(contextId).services().get(MetaAnnotationStore.class);
            Set<Annotation> checkedNewQualifiers = new HashSet<Annotation>();
            for (Annotation qualifier : newQualifiers) {
                if (!store.getBindingTypeModel(qualifier.annotationType()).isValid()) {
                    throw new IllegalArgumentException(ANNOTATION_NOT_QUALIFIER, qualifier);
                }
                if (checkedNewQualifiers.contains(qualifier)) {
                    throw new IllegalArgumentException(REDUNDANT_QUALIFIER, qualifier, Arrays.asList(newQualifiers));
                }
                checkedNewQualifiers.add(qualifier);
            }
            result.addAll(checkedNewQualifiers);
        }
        //result.addAll(checkedNewQualifiers);
        return result;
    }

    public static InjectionPoint getDelegateInjectionPoint(javax.enterprise.inject.spi.Decorator<?> decorator) {
        if (decorator instanceof DecoratorImpl<?>) {
            return ((DecoratorImpl<?>) decorator).getDelegateInjectionPoint();
        } else {
            for (InjectionPoint injectionPoint : decorator.getInjectionPoints()) {
                if (injectionPoint.isDelegate())
                    return injectionPoint;
            }
        }
        return null;
    }
}
