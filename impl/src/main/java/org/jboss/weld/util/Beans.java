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
import static org.jboss.weld.logging.messages.BeanMessage.TYPED_CLASS_NOT_IN_HIERARCHY;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import javax.enterprise.inject.Requires;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Veto;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.interceptor.spi.model.InterceptionType;
import org.jboss.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.Container;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPointImpl;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.attributes.SpecialParameterInjectionPoint;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.introspector.WeldCallable;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.Enabled;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Helper class for bean inspection
 *
 * @author Pete Muir
 * @author David Allen
 * @author Marius Bogoevici
 * @author Ales Justin
 * @author Jozef Hartinger
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
    public static boolean isPassivationCapableDependency(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isPassivationCapableDependency();
        } else {
            if (Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal()) {
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
     * @return True if proxyable, false otherwise
     */
    public static boolean isBeanProxyable(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isProxyable();
        } else {
            return Proxies.isTypesProxyable(bean.getTypes());
        }
    }

    public static List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        List<Set<FieldInjectionPoint<?, ?>>> injectableFieldsList = new ArrayList<Set<FieldInjectionPoint<?, ?>>>();
        WeldClass<?> t = type;
        while (t != null && !t.getJavaClass().equals(Object.class)) {
            ArraySet<FieldInjectionPoint<?, ?>> fields = new ArraySet<FieldInjectionPoint<?, ?>>();
            injectableFieldsList.add(0, fields);
            for (WeldField<?, ?> annotatedField : t.getDeclaredWeldFields(Inject.class)) {
                if (!annotatedField.isStatic()) {
                    addFieldInjectionPoint(annotatedField, fields, declaringBean, manager);
                }
            }
            fields.trimToSize();
            t = t.getWeldSuperclass();
        }
        return injectableFieldsList;
    }

    public static <T extends WeldInjectionPoint<?, ?>> Set<T> flattenInjectionPoints(List<? extends Set<T>> fieldInjectionPoints) {
        ArraySet<T> injectionPoints = new ArraySet<T>();
        for (Set<T> i : fieldInjectionPoints) {
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

            if (businessMethod) {
                for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
                    businessMethod = !annotatedMethod.isAnnotationPresent(InterceptionTypeRegistry.getAnnotationClass(interceptionType));
                    if (!businessMethod)
                        break;
                }
                if (businessMethod)
                    annotatedMethods.add(annotatedMethod);
            }
        }
        return annotatedMethods;
    }

    public static Set<WeldInjectionPoint<?, ?>> getEjbInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(EjbInjectionServices.class)) {
            Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
            ArraySet<WeldInjectionPoint<?, ?>> ejbInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
            for (WeldField<?, ?> field : type.getWeldFields(ejbAnnotationType)) {
                ejbInjectionPoints.add(FieldInjectionPoint.of(manager.createInjectionPoint(field, declaringBean), manager));
            }
            return ejbInjectionPoints.trimToSize();
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getPersistenceContextInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(JpaInjectionServices.class)) {
            ArraySet<WeldInjectionPoint<?, ?>> jpaInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
            Class<? extends Annotation> persistenceContextAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
            for (WeldField<?, ?> field : type.getWeldFields(persistenceContextAnnotationType)) {
                jpaInjectionPoints.add(FieldInjectionPoint.of(manager.createInjectionPoint(field, declaringBean), manager));
            }
            return jpaInjectionPoints.trimToSize();
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getPersistenceUnitInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(JpaInjectionServices.class)) {
            ArraySet<WeldInjectionPoint<?, ?>> jpaInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
            Class<? extends Annotation> persistenceUnitAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_UNIT_ANNOTATION_CLASS;
            for (WeldField<?, ?> field : type.getWeldFields(persistenceUnitAnnotationType)) {
                jpaInjectionPoints.add(FieldInjectionPoint.of(manager.createInjectionPoint(field, declaringBean), manager));
            }
            return jpaInjectionPoints.trimToSize();
        } else {
            return Collections.emptySet();
        }
    }

    public static Set<WeldInjectionPoint<?, ?>> getResourceInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        if (manager.getServices().contains(ResourceInjectionServices.class)) {
            Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
            ArraySet<WeldInjectionPoint<?, ?>> resourceInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
            for (WeldField<?, ?> field : type.getWeldFields(resourceAnnotationType)) {
                resourceInjectionPoints.add(FieldInjectionPoint.of(manager.createInjectionPoint(field, declaringBean), manager));
            }
            return resourceInjectionPoints.trimToSize();
        } else {
            return Collections.emptySet();
        }
    }

    public static List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager) {
        List<Set<MethodInjectionPoint<?, ?>>> initializerMethodsList = new ArrayList<Set<MethodInjectionPoint<?, ?>>>();
        // Keep track of all seen methods so we can ignore overridden methods
        Multimap<MethodSignature, Package> seenMethods = Multimaps.newSetMultimap(new HashMap<MethodSignature, Collection<Package>>(), new Supplier<Set<Package>>() {

            public Set<Package> get() {
                return new HashSet<Package>();
            }

        });
        WeldClass<?> t = type;
        while (t != null && !t.getJavaClass().equals(Object.class)) {
            ArraySet<MethodInjectionPoint<?, ?>> initializerMethods = new ArraySet<MethodInjectionPoint<?, ?>>();
            initializerMethodsList.add(0, initializerMethods);
            for (WeldMethod<?, ?> method : t.getDeclaredWeldMethods()) {
                if (method.isAnnotationPresent(Inject.class) && !method.isStatic()) {
                    if (method.getAnnotation(Produces.class) != null) {
                        throw new DefinitionException(INITIALIZER_CANNOT_BE_PRODUCER, method, type);
                    } else if (method.getWeldParameters(Disposes.class).size() > 0) {
                        throw new DefinitionException(INITIALIZER_CANNOT_BE_DISPOSAL_METHOD, method, type);
                    } else if (method.getWeldParameters(Observes.class).size() > 0) {
                        throw new DefinitionException(INVALID_INITIALIZER, method);
                    } else if (method.isGeneric()) {
                        throw new DefinitionException(INITIALIZER_METHOD_IS_GENERIC, method, type);
                    } else {
                        if (!isOverridden(method, seenMethods)) {
                            MethodInjectionPoint<?, ?> initializerMethod = MethodInjectionPoint.of(method, declaringBean, manager);
                            initializerMethods.add(initializerMethod);
                        }
                    }
                }
                seenMethods.put(method.getSignature(), method.getPackage());
            }
            initializerMethods.trimToSize();
            t = t.getWeldSuperclass();
        }
        return initializerMethodsList;
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

    public static <X> List<ParameterInjectionPoint<?, X>> getParameterInjectionPoints(WeldCallable<?, X, ?> callable, Bean<?> declaringBean, BeanManagerImpl manager) {
        List<ParameterInjectionPoint<?, X>> parameters = new ArrayList<ParameterInjectionPoint<?, X>>();
        for (WeldParameter<?, X> parameter : callable.getWeldParameters()) {
            if (isSpecialParameter(parameter)) {
                parameters.add(SpecialParameterInjectionPoint.of(parameter, declaringBean));
            } else {
                parameters.add(ParameterInjectionPointImpl.of(manager.createInjectionPoint(parameter, declaringBean), manager));
            }
        }
        return parameters;
    }

    public static <X> Set<ParameterInjectionPoint<?, X>> filterOutSpecialParameterInjectionPoints(List<ParameterInjectionPoint<?, X>> injectionPoints) {
        ArraySet<ParameterInjectionPoint<?, X>> filtered = new ArraySet<ParameterInjectionPoint<?, X>>();
        for (ParameterInjectionPoint<?, X> parameter : injectionPoints) {
            if (parameter instanceof SpecialParameterInjectionPoint) {
                continue;
            }
            filtered.add(parameter);
        }
        return filtered.trimToSize();
    }

    public static boolean isSpecialParameter(WeldParameter<?, ?> parameter) {
        return parameter.isAnnotationPresent(Disposes.class) || parameter.isAnnotationPresent(Observes.class);
    }

    public static Set<ParameterInjectionPoint<?, ?>> flattenParameterInjectionPoints(List<Set<MethodInjectionPoint<?, ?>>> methodInjectionPoints) {
        ArraySet<ParameterInjectionPoint<?, ?>> injectionPoints = new ArraySet<ParameterInjectionPoint<?, ?>>();
        for (Set<MethodInjectionPoint<?, ?>> i : methodInjectionPoints) {
            for (MethodInjectionPoint<?, ?> method : i) {
                for (ParameterInjectionPoint<?, ?> parameter : method.getParameterInjectionPoints()) {
                    injectionPoints.add(parameter);
                }
            }
        }
        return injectionPoints.trimToSize();
    }

    private static void addFieldInjectionPoint(WeldField<?, ?> annotatedField, Set<FieldInjectionPoint<?, ?>> injectableFields, Bean<?> declaringBean, BeanManagerImpl manager) {
        if (!annotatedField.isAnnotationPresent(Produces.class)) {
            if (annotatedField.isFinal()) {
                throw new DefinitionException(QUALIFIER_ON_FINAL_FIELD, annotatedField);
            }
            FieldInjectionPoint<?, ?> fieldInjectionPoint = FieldInjectionPoint.of(manager.createInjectionPoint(annotatedField, declaringBean), manager);
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
    public static boolean containsAllQualifiers(Set<Annotation> requiredQualifiers, Set<Annotation> qualifiers, BeanManagerImpl beanManager) {
        for (Annotation requiredQualifier : requiredQualifiers) {
            QualifierModel<?> qualifierModel = beanManager.getServices().get(MetaAnnotationStore.class).getBindingTypeModel(requiredQualifier.annotationType());
            boolean matchFound = false;
            // Do a full check as we need to consider @NonBinding
            for (Annotation qualifier : qualifiers) {
                if (qualifierModel.isEqual(requiredQualifier, qualifier)) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAllInterceptionBindings(Set<Annotation> expectedBindings, Set<Annotation> existingBindings, BeanManagerImpl manager) {
        for (Annotation binding : expectedBindings) {
            InterceptorBindingModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(binding.annotationType());
            boolean matchFound = false;
            // TODO Something wrong with annotation proxy hashcode in JDK/AnnotationLiteral hashcode, so always do a full check, don't use contains
            for (Annotation otherBinding : existingBindings) {
                if (bindingType.isEqual(binding, otherBinding)) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                return false;
            }
        }
        return true;
    }

    public static boolean findInterceptorBindingConflicts(BeanManagerImpl manager, Set<Annotation> bindings) {
        Map<Class<? extends Annotation>, Annotation> foundAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
        for (Annotation binding : bindings) {
            if (foundAnnotations.containsKey(binding.annotationType())) {
                InterceptorBindingModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(binding.annotationType());
                if (!bindingType.isEqual(binding, foundAnnotations.get(binding.annotationType()), false)) {
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
     * @param beans                  The beans to filter
     * @param beanManager the bean manager
     * @return The filtered beans
     */
    public static <T extends Bean<?>> Set<T> removeDisabledAndSpecializedBeans(Set<T> beans, BeanManagerImpl beanManager) {
        if (beans.size() == 0) {
            return beans;
        } else {
            Set<T> result = new HashSet<T>();
            for (T bean : beans) {
                if (isBeanEnabled(bean, beanManager.getEnabled()) && !isSpecialized(bean, beans, beanManager)) {
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
            }
        } else if (bean instanceof DecoratorImpl<?>) {
            return enabled.getDecorator(bean.getBeanClass()) != null;
        } else if (bean instanceof InterceptorImpl<?>) {
            return enabled.getInterceptor(bean.getBeanClass()) != null;
        } else {
            return true;
        }
        return false;
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
     * Is nullable.
     *
     * @return true if nullable, false otherwise
     */
    public static boolean isNullable(WeldAnnotated<?, ?> annotated) {
        return !annotated.isPrimitive();
    }

    /**
     * Check if bean is specialized by any of beans
     *
     * @param bean the bean to check
     * @param beanManager bean manager
     * @return true if bean is specialized by some bean in all beans
     */
    public static <T extends Bean<?>> boolean isSpecialized(T bean, BeanManagerImpl beanManager) {
        BeansClosure closure = BeansClosure.getClosure(beanManager);
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
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        Bean<?> specializedBean = closure.getSpecialized(bean);
        //noinspection SuspiciousMethodCalls
        return (specializedBean != null && beans.contains(specializedBean));
    }

    public static <T> ConstructorInjectionPoint<T> getBeanConstructor(Bean<T> declaringBean, WeldClass<T> type, BeanManagerImpl manager) {
        ConstructorInjectionPoint<T> constructor = null;
        Collection<WeldConstructor<T>> initializerAnnotatedConstructors = type.getWeldConstructors(Inject.class);
        log.trace(FOUND_INJECTABLE_CONSTRUCTORS, initializerAnnotatedConstructors, type);
        if (initializerAnnotatedConstructors.size() > 1) {
            if (initializerAnnotatedConstructors.size() > 1) {
                throw new DefinitionException(AMBIGUOUS_CONSTRUCTOR, type, initializerAnnotatedConstructors);
            }
        } else if (initializerAnnotatedConstructors.size() == 1) {
            constructor = ConstructorInjectionPoint.of(initializerAnnotatedConstructors.iterator().next(), declaringBean, manager);
            log.trace(FOUND_ONE_INJECTABLE_CONSTRUCTOR, constructor, type);
        } else if (type.getNoArgsWeldConstructor() != null) {

            constructor = ConstructorInjectionPoint.of(type.getNoArgsWeldConstructor(), declaringBean, manager);
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
            if (injectionPoint.getAnnotated().isAnnotationPresent(ejbAnnotationType)) {
                return ejbServices.resolveEjb(injectionPoint);
            }
        }

        if (jpaServices != null) {
            final PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);

            Class<? extends Annotation> persistenceUnitAnnotationType = persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS;
            if (injectionPoint.getAnnotated().isAnnotationPresent(persistenceUnitAnnotationType)) {
                return jpaServices.resolvePersistenceUnit(injectionPoint);
            }

            Class<? extends Annotation> persistenceContextAnnotationType = persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
            if (injectionPoint.getAnnotated().isAnnotationPresent(persistenceContextAnnotationType)) {
                return jpaServices.resolvePersistenceContext(injectionPoint);
            }
        }

        if (resourceServices != null) {
            Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
            if (injectionPoint.getAnnotated().isAnnotationPresent(resourceAnnotationType)) {
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

    public static Annotation[] mergeInQualifiers(Annotation[] qualifiers, Annotation[] newQualifiers) {
        if (qualifiers == null || newQualifiers == null)
            return EMPTY_ANNOTATIONS;

        return mergeInQualifiers(asList(qualifiers), newQualifiers).toArray(Reflections.EMPTY_ANNOTATIONS);
    }

    public static Set<Annotation> mergeInQualifiers(Collection<Annotation> qualifiers, Annotation[] newQualifiers) {
        Set<Annotation> result = new HashSet<Annotation>();

        if (qualifiers != null && qualifiers.isEmpty() == false)
            result.addAll(qualifiers);

        if (newQualifiers != null && newQualifiers.length > 0) {
            final MetaAnnotationStore store = Container.instance().services().get(MetaAnnotationStore.class);
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

    /**
     * Bean types from an annotated element
     */
    public static Set<Type> getTypes(WeldAnnotated<?, ?> annotated) {
        Set<Type> types = new HashSet<Type>();
        // array and primitive types require special treatment
        if (annotated.getJavaClass().isArray() || annotated.getJavaClass().isPrimitive()) {
            types.add(annotated.getJavaClass());
            types.add(Object.class);
            return types;
        } else {
            if (annotated.isAnnotationPresent(Typed.class)) {
                types = new ArraySet<Type>(getTypedTypes(Reflections.buildTypeMap(annotated.getTypeClosure()), annotated.getJavaClass(), annotated.getAnnotation(Typed.class)));
            } else {
                if (annotated.getJavaClass().isInterface()) {
                    types.add(Object.class);
                }
                types.addAll(annotated.getTypeClosure());
            }
        }
        return types;
    }

    /**
     * Bean types of a session bean.
     */
    public static <T> Set<Type> getTypes(WeldAnnotated<T, ?> annotated, EjbDescriptor<T> ejbDescriptor) {
        Set<Type> types = new HashSet<Type>();
        // session beans
        Map<Class<?>, Type> typeMap = new LinkedHashMap<Class<?>, Type>();
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces()) {
            typeMap.putAll(new HierarchyDiscovery(businessInterfaceDescriptor.getInterface()).getTypeMap());
        }
        if (annotated.isAnnotationPresent(Typed.class)) {
            types.addAll(getTypedTypes(typeMap, annotated.getJavaClass(), annotated.getAnnotation(Typed.class)));
        } else {
            typeMap.put(Object.class, Object.class);
            types.addAll(typeMap.values());
        }
        return types;
    }

    /**
     * Bean types of a bean that uses the {@link Typed} annotation.
     */
    public static Set<Type> getTypedTypes(Map<Class<?>, Type> typeClosure, Class<?> rawType, Typed typed) {
        Set<Type> types = new HashSet<Type>();
        for (Class<?> specifiedClass : typed.value()) {
            Type tmp = typeClosure.get(specifiedClass);
            if (tmp != null) {
                types.add(tmp);
            } else {
                throw new DefinitionException(TYPED_CLASS_NOT_IN_HIERARCHY, specifiedClass.getName(), rawType);
            }
        }
        types.add(Object.class);
        return types;
    }

    /**
     * Indicates if the type is a simple Web Bean
     *
     * @param clazz The type to inspect
     * @return True if simple Web Bean, false otherwise
     */
    public static boolean isTypeManagedBeanOrDecoratorOrInterceptor(WeldClass<?> clazz) {
        Class<?> javaClass = clazz.getJavaClass();
        return !javaClass.isEnum() && !Extension.class.isAssignableFrom(clazz.getJavaClass())
                && !(clazz.isAnonymousClass() || (clazz.isMemberClass() && !clazz.isStatic())) && !Reflections.isParamerterizedTypeWithWildcard(javaClass)
                && hasSimpleCdiConstructor(clazz);
    }

    public static boolean hasSimpleCdiConstructor(WeldClass<?> type) {
        return type.getNoArgsWeldConstructor() != null || type.getWeldConstructors(Inject.class).size() > 0;
    }

    /**
     * Determines if this {@link AnnotatedType} should be vetoed as a result of presence of {@link Veto} and {@link Requires}
     * annotations.
     */
    public static boolean isVetoed(AnnotatedType<?> type) {
        Class<?> javaClass = type.getJavaClass();
        if (type.isAnnotationPresent(Veto.class)) {
            return true;
        }
        if (isRequirementMissing(type.getAnnotation(Requires.class), javaClass.getClassLoader())) {
            return true;
        }
        if (javaClass.getPackage() != null) {
            if (javaClass.getPackage().isAnnotationPresent(Veto.class)) {
                return true;
            }
            if (isRequirementMissing(javaClass.getPackage().getAnnotation(Requires.class), javaClass.getClassLoader())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if any of the requirements cannot be fulfilled.
     */
    public static boolean isRequirementMissing(Requires requires, ClassLoader classLoader) {
        if (requires == null) {
            return false;
        }
        ResourceLoader loader = new ClassLoaderResourceLoader(classLoader);
        for (String className : requires.value()) {
            if (!Reflections.isClassLoadable(className, loader)) {
                return true;
            }
        }
        return false;
    }
}
