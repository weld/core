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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Prioritized;
import javax.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.ForwardingBean;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.WeldBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ResourceInjection;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

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

    private Beans() {
    }

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
     * Tests if a bean is capable of having its state temporarily stored to secondary storage
     *
     * @param bean The bean to inspect
     * @return True if the bean is passivation capable
     */
    public static boolean isPassivationCapableBean(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isPassivationCapableBean();
        } else {
            return bean instanceof PassivationCapable;
        }
    }

    /**
     * Tests if a bean is capable of having its state temporarily stored to secondary storage
     *
     * @param bean The bean to inspect
     * @return True if the bean is passivation capable
     */
    public static boolean isPassivationCapableDependency(Bean<?> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isPassivationCapableDependency();
        }
        return bean instanceof PassivationCapable;
    }

    /**
     * Indicates if a bean is proxyable
     *
     * @param bean The bean to test
     * @return True if proxyable, false otherwise
     */
    public static boolean isBeanProxyable(Bean<?> bean, BeanManagerImpl manager) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).isProxyable();
        } else {
            return Proxies.isTypesProxyable(bean.getTypes(), manager.getServices());
        }
    }

    public static List<EnhancedAnnotatedMethod<?, ?>> getInterceptableMethods(EnhancedAnnotatedType<?> type) {
        List<EnhancedAnnotatedMethod<?, ?>> annotatedMethods = new ArrayList<EnhancedAnnotatedMethod<?, ?>>();
        for (EnhancedAnnotatedMethod<?, ?> annotatedMethod : type.getEnhancedMethods()) {
            // note that a bridge method can be a candidate for interception in rare cases; do not discard those
            boolean businessMethod = !annotatedMethod.isStatic() && !annotatedMethod.isAnnotationPresent(Inject.class);

            if (businessMethod && !isInterceptorMethod(annotatedMethod) && !isLambdaMethod(annotatedMethod)) {
                annotatedMethods.add(annotatedMethod);
            }
        }
        return annotatedMethods;
    }

    private static boolean isInterceptorMethod(AnnotatedMethod<?> annotatedMethod) {
        for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
            if (annotatedMethod.isAnnotationPresent(InterceptionTypeRegistry.getAnnotationClass(interceptionType))) {
                return true;
            }
        }
        return false;
    }

    // Lambdas in classes will be compiled into methods declared on that given class.
    // The exact representation differs between javac (standard compiler) and EJC (Eclipse compiler).
    // We want to avoid attempting to intercept these methods - note that the detection is on a "best effort" basis.
    private static boolean isLambdaMethod(AnnotatedMethod<?> annotatedMethod) {
        Method javaMember = annotatedMethod.getJavaMember();
        return !javaMember.isBridge() && javaMember.isSynthetic();
    }

    /**
     * Checks that all the qualifiers in the set requiredQualifiers are in the set of qualifiers. Qualifier equality rules for
     * annotation members are followed.
     *
     * @param requiredQualifiers The required qualifiers
     * @param qualifiers The set of qualifiers to check
     * @return True if all matches, false otherwise
     */
    public static boolean containsAllQualifiers(Set<QualifierInstance> requiredQualifiers, Set<QualifierInstance> qualifiers) {
        return qualifiers.containsAll(requiredQualifiers);
    }

    public static boolean containsAllInterceptionBindings(Set<Annotation> expectedBindings,
            Set<QualifierInstance> existingBindings, BeanManagerImpl manager) {
        final Set<QualifierInstance> expected = manager.extractInterceptorBindingsForQualifierInstance(QualifierInstance.of(expectedBindings, manager.getServices().get(MetaAnnotationStore.class)));
        return expected.isEmpty() ? false : manager.extractInterceptorBindingsForQualifierInstance(existingBindings).containsAll(expected);
    }

    /**
     * Retains only beans which are enabled.
     *
     * @param beans The mutable set of beans to filter
     * @param beanManager The bean manager
     * @return a mutable set of enabled beans
     */
    public static <T extends Bean<?>> Set<T> removeDisabledBeans(Set<T> beans, final BeanManagerImpl beanManager) {
        if (beans.isEmpty()) {
            return beans;
        } else {
            for (Iterator<T> iterator = beans.iterator(); iterator.hasNext();) {
                if (!isBeanEnabled(iterator.next(), beanManager.getEnabled())) {
                    iterator.remove();
                }
            }
            return beans;
        }
    }

    public static boolean isBeanEnabled(Bean<?> bean, ModuleEnablement enabled) {
        if (bean.isAlternative()) {
            boolean isEnabled = false;
            if (enabled.isEnabledAlternativeClass(bean.getBeanClass())) {
                isEnabled = true;
            } else {
                for (Class<? extends Annotation> stereotype : bean.getStereotypes()) {
                    if (enabled.isEnabledAlternativeStereotype(stereotype)) {
                        isEnabled = true;
                        break;
                    }
                }
            }
            // For synthetic enabled alternatives, the ModuleEnablement may not yet be aware of them
            if (!isEnabled
                    && ((bean instanceof WeldBean && ((WeldBean<?>) bean).getPriority() != null) || bean instanceof Prioritized)) {
                isEnabled = true;
            }
            return isEnabled;
        } else if (bean instanceof AbstractProducerBean<?, ?, ?>) {
            AbstractProducerBean<?, ?, ?> receiverBean = (AbstractProducerBean<?, ?, ?>) bean;
            return isBeanEnabled(receiverBean.getDeclaringBean(), enabled);
        } else if (bean instanceof DecoratorImpl<?>) {
            return enabled.isDecoratorEnabled(bean.getBeanClass());
        } else if (bean instanceof InterceptorImpl<?>) {
            return enabled.isInterceptorEnabled(bean.getBeanClass());
        } else {
            return true;
        }
    }

    /**
     * Is alternative.
     *
     * @param annotated the annotated
     * @param mergedStereotypes merged stereotypes
     * @return true if alternative, false otherwise
     */
    public static boolean isAlternative(EnhancedAnnotated<?, ?> annotated, MergedStereotypes<?, ?> mergedStereotypes) {
        return annotated.isAnnotationPresent(Alternative.class) || mergedStereotypes.isAlternative();
    }

    public static <T> EnhancedAnnotatedConstructor<T> getBeanConstructorStrict(EnhancedAnnotatedType<T> type) {
        EnhancedAnnotatedConstructor<T> constructor = getBeanConstructor(type);
        if (constructor == null) {
            throw UtilLogger.LOG.unableToFindConstructor(type);
        }
        return constructor;
    }

    public static <T> EnhancedAnnotatedConstructor<T> getBeanConstructor(EnhancedAnnotatedType<T> type) {
        Collection<EnhancedAnnotatedConstructor<T>> initializerAnnotatedConstructors = type
                .getEnhancedConstructors(Inject.class);
        BeanLogger.LOG.foundInjectableConstructors(initializerAnnotatedConstructors, type);
        EnhancedAnnotatedConstructor<T> constructor = null;
        if (initializerAnnotatedConstructors.size() > 1) {
            throw UtilLogger.LOG.ambiguousConstructor(type, initializerAnnotatedConstructors);
        } else if (initializerAnnotatedConstructors.size() == 1) {
            constructor = initializerAnnotatedConstructors.iterator().next();
            BeanLogger.LOG.foundOneInjectableConstructor(constructor, type);
        } else if (type.getNoArgsEnhancedConstructor() != null) {
            constructor = type.getNoArgsEnhancedConstructor();
            BeanLogger.LOG.foundDefaultConstructor(constructor, type);
        }
        if (constructor != null) {
            if (!constructor.getEnhancedParameters(Disposes.class).isEmpty()) {
                throw BeanLogger.LOG.parameterAnnotationNotAllowedOnConstructor("@Disposes", constructor,
                        Formats.formatAsStackTraceElement(constructor.getJavaMember()));
            }
            if (!constructor.getEnhancedParameters(Observes.class).isEmpty()) {
                throw BeanLogger.LOG.parameterAnnotationNotAllowedOnConstructor("@Observes", constructor,
                        Formats.formatAsStackTraceElement(constructor.getJavaMember()));
            }
            if (!constructor.getEnhancedParameters(ObservesAsync.class).isEmpty()) {
                throw BeanLogger.LOG.parameterAnnotationNotAllowedOnConstructor("@ObservesAsync", constructor,
                        Formats.formatAsStackTraceElement(constructor.getJavaMember()));
            }
        }
        return constructor;
    }

    /**
     * Injects EJBs and other EE resources.
     *
     * @param resourceInjectionsHierarchy
     * @param beanInstance
     * @param ctx
     */
    public static <T> void injectEEFields(Iterable<Set<ResourceInjection<?>>> resourceInjectionsHierarchy,
            T beanInstance, CreationalContext<T> ctx) {
        for (Set<ResourceInjection<?>> resourceInjections : resourceInjectionsHierarchy) {
            for (ResourceInjection<?> resourceInjection : resourceInjections) {
                resourceInjection.injectResourceReference(beanInstance, ctx);
            }
        }
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
    public static <T> void injectBoundFields(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager,
            Iterable<? extends FieldInjectionPoint<?, ?>> injectableFields) {
        for (FieldInjectionPoint<?, ?> injectableField : injectableFields) {
            injectableField.inject(instance, manager, creationalContext);
        }
    }

    public static <T> void injectFieldsAndInitializers(T instance, CreationalContext<T> ctx, BeanManagerImpl beanManager,
            List<? extends Iterable<? extends FieldInjectionPoint<?, ?>>> injectableFields,
            List<? extends Iterable<? extends MethodInjectionPoint<?, ?>>> initializerMethods) {
        if (injectableFields.size() != initializerMethods.size()) {
            throw UtilLogger.LOG.invalidQuantityInjectableFieldsAndInitializerMethods(injectableFields, initializerMethods);
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
    public static <T> void callInitializers(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager,
            Iterable<? extends MethodInjectionPoint<?, ?>> initializerMethods) {
        for (MethodInjectionPoint<?, ?> initializer : initializerMethods) {
            initializer.invoke(instance, null, manager, creationalContext, CreationException.class);
        }
    }

    public static <T> boolean isInterceptor(AnnotatedType<T> annotatedItem) {
        return annotatedItem.isAnnotationPresent(javax.interceptor.Interceptor.class);
    }

    public static <T> boolean isDecorator(EnhancedAnnotatedType<T> annotatedItem) {
        return annotatedItem.isAnnotationPresent(Decorator.class);
    }

    public static Set<Annotation> mergeInQualifiers(BeanManagerImpl manager, Collection<Annotation> qualifiers, Annotation[] newQualifiers) {
        Set<Annotation> result = new HashSet<Annotation>();

        if (qualifiers != null && !(qualifiers.isEmpty())) {
            result.addAll(qualifiers);
        }
        if (newQualifiers != null && newQualifiers.length > 0) {
            final MetaAnnotationStore store = manager.getServices().get(MetaAnnotationStore.class);
            Set<Annotation> checkedNewQualifiers = new HashSet<Annotation>();
            for (Annotation qualifier : newQualifiers) {
                if (!store.getBindingTypeModel(qualifier.annotationType()).isValid()) {
                    throw UtilLogger.LOG.annotationNotQualifier(qualifier);
                }
                Class<? extends Annotation> annotationType = qualifier.annotationType();
                if (!annotationType.isAnnotationPresent(Repeatable.class)) {
                    for (Annotation annotation : checkedNewQualifiers) {
                        if(annotationType.equals(annotation.annotationType())) {
                            throw UtilLogger.LOG.redundantQualifier(qualifier, Arrays.toString(newQualifiers));
                        }
                    }
                }
                checkedNewQualifiers.add(qualifier);
            }
            result.addAll(checkedNewQualifiers);
        }
        return result;
    }

    /**
     * Illegal bean types are ignored except for array and primitive types and unless {@link Typed} is used.
     *
     * @return the set of bean types from an annotated element
     */
    public static Set<Type> getTypes(EnhancedAnnotated<?, ?> annotated) {
        // array and primitive types require special treatment
        if (annotated.getJavaClass().isArray() || annotated.getJavaClass().isPrimitive()) {
            return ImmutableSet.<Type>builder().addAll(annotated.getBaseType(), Object.class).build();
        } else {
            if (annotated.isAnnotationPresent(Typed.class)) {
                return ImmutableSet.<Type>builder().addAll(getTypedTypes(Reflections.buildTypeMap(annotated.getTypeClosure()),
                        annotated.getJavaClass(), annotated.getAnnotation(Typed.class))).build();
            } else {
                if (annotated.getJavaClass().isInterface()) {
                    return getLegalBeanTypes(annotated.getTypeClosure(), annotated, Object.class);
                }
                return getLegalBeanTypes(annotated.getTypeClosure(), annotated);
            }
        }
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
                throw BeanLogger.LOG.typedClassNotInHierarchy(specifiedClass.getName(), rawType, Formats.formatTypes(typeClosure.values()));
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
    public static boolean isTypeManagedBeanOrDecoratorOrInterceptor(AnnotatedType<?> annotatedType) {
        Class<?> javaClass = annotatedType.getJavaClass();
        return !javaClass.isEnum() && !Extension.class.isAssignableFrom(javaClass)
                && Reflections.isTopLevelOrStaticNestedClass(javaClass) && !Reflections.isParameterizedTypeWithWildcard(javaClass)
                && hasSimpleCdiConstructor(annotatedType);
    }

    /**
     *
     * @param classFileInfo
     * @param checkTypeModifiers - this flag reflects whether Jandex version including fix for JANDEX-37 could be used
     * @return
     */
    public static boolean isTypeManagedBeanOrDecoratorOrInterceptor(ClassFileInfo classFileInfo, boolean checkTypeModifiers) {

        boolean isTypeManagedBean = ((classFileInfo.getModifiers() & BytecodeUtils.ENUM) == 0) && !classFileInfo.isAssignableTo(Extension.class)
                && classFileInfo.hasCdiConstructor()
                && (!Modifier.isAbstract(classFileInfo.getModifiers()) || classFileInfo.isAnnotationDeclared(Decorator.class));
        if (checkTypeModifiers) {
            return isTypeManagedBean && (classFileInfo.isTopLevelClass() || Modifier.isStatic(classFileInfo.getModifiers()));
        } else {
            return isTypeManagedBean;
        }
    }

    public static boolean isDecoratorDeclaringInAppropriateConstructor(ClassFileInfo classFileInfo) {
        return !classFileInfo.hasCdiConstructor() && classFileInfo.isAnnotationDeclared(Decorator.class);
    }

    public static boolean isDecoratorDeclaringInAppropriateConstructor(AnnotatedType<?> annotatedType) {
        return !hasSimpleCdiConstructor(annotatedType) && annotatedType.isAnnotationPresent(Decorator.class);
    }

    public static boolean hasSimpleCdiConstructor(AnnotatedType<?> type) {
        for (AnnotatedConstructor<?> constructor : type.getConstructors()) {
            if (constructor.getParameters().isEmpty()) {
                return true;
            }
            if (constructor.isAnnotationPresent(Inject.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this Java class should be vetoed as a result of presence of {@link Veto} annotations.
     */
    public static boolean isVetoed(Class<?> javaClass) {
        if (javaClass.isAnnotationPresent(Vetoed.class)) {
            return true;
        }
        return isPackageVetoed(javaClass.getPackage());
    }

    public static boolean isVetoed(AnnotatedType<?> type) {
        if (type.isAnnotationPresent(Vetoed.class)) {
            return true;
        }
        return isPackageVetoed(type.getJavaClass().getPackage());
    }

    private static boolean isPackageVetoed(Package pkg) {
        return pkg != null && pkg.isAnnotationPresent(Vetoed.class);
    }

    /**
     * Generates a unique signature for {@link BeanAttributes}.
     */
    public static String createBeanAttributesId(BeanAttributes<?> attributes) {
        StringBuilder builder = new StringBuilder();
        builder.append(attributes.getName());
        builder.append(",");
        builder.append(attributes.getScope().getName());
        builder.append(",");
        builder.append(attributes.isAlternative());
        builder.append(AnnotatedTypes.createAnnotationCollectionId(attributes.getQualifiers()));
        builder.append(createTypeCollectionId(attributes.getStereotypes()));
        builder.append(createTypeCollectionId(attributes.getTypes()));
        return builder.toString();
    }

    /**
     * Generates a unique signature of a collection of types.
     */
    public static String createTypeCollectionId(Collection<? extends Type> types) {
        StringBuilder builder = new StringBuilder();
        List<? extends Type> sortedTypes = new ArrayList<Type>(types);
        Collections.sort(sortedTypes, TypeComparator.INSTANCE);
        builder.append("[");
        for (Iterator<? extends Type> iterator = sortedTypes.iterator(); iterator.hasNext();) {
            builder.append(createTypeId(iterator.next()));
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Creates a unique signature for a {@link Type}.
     */
    private static String createTypeId(Type type) {
        if (type instanceof Class<?>) {
            return Reflections.<Class<?>> cast(type).getName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            StringBuilder builder = new StringBuilder();
            builder.append(createTypeId(parameterizedType.getRawType()));
            builder.append("<");
            for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
                builder.append(createTypeId(parameterizedType.getActualTypeArguments()[i]));
                if (i != parameterizedType.getActualTypeArguments().length - 1) {
                    builder.append(",");
                }
            }
            builder.append(">");
            return builder.toString();
        }
        if (type instanceof TypeVariable<?>) {
            return Reflections.<TypeVariable<?>> cast(type).getName();
        }
        if (type instanceof GenericArrayType) {
            return createTypeId(Reflections.<GenericArrayType> cast(type).getGenericComponentType());
        }
        throw new java.lang.IllegalArgumentException("Unknown type " + type);
    }

    private static class TypeComparator implements Comparator<Type>, Serializable {
        private static final long serialVersionUID = -2162735176891985078L;
        private static final TypeComparator INSTANCE = new TypeComparator();

        @Override
        public int compare(Type o1, Type o2) {
            return createTypeId(o1).compareTo(createTypeId(o2));
        }
    }

    public static <T, S, X extends EnhancedAnnotated<T, S>> X checkEnhancedAnnotatedAvailable(X enhancedAnnotated) {
        if (enhancedAnnotated == null) {
            throw new IllegalStateException("Enhanced metadata should not be used at runtime.");
        }
        return enhancedAnnotated;
    }

    public static boolean hasBuiltinScope(Bean<?> bean) {
        return RequestScoped.class.equals(bean.getScope()) || SessionScoped.class.equals(bean.getScope()) || ApplicationScoped.class.equals(bean.getScope())
                || ConversationScoped.class.equals(bean.getScope()) || Dependent.class.equals(bean.getScope());
    }

    public static Class<? extends Annotation> getBeanDefiningAnnotationScope(AnnotatedType<?> annotatedType) {
        for (Annotation annotation : annotatedType.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(NormalScope.class) || annotation.annotationType().equals(Dependent.class)) {
                return annotation.annotationType();
            }
        }
        return null;
    }

    /**
     * @param types The initial set of types
     * @param annotated
     * @param additionalTypes Types to add to the initial set
     * @return the set of legal bean types
     */
    public static Set<Type> getLegalBeanTypes(Set<Type> types, Object baseType, Type... additionalTypes) {
        if (additionalTypes != null && additionalTypes.length > 0) {
            // Micro-optimization is not possible
            return omitIllegalBeanTypes(types, baseType).addAll(additionalTypes).build();
        }
        for (Type type : types) {
            if (Types.isIllegalBeanType(type)) {
                return omitIllegalBeanTypes(types, baseType).build();
            }
        }
        return types;
    }

    static ImmutableSet.Builder<Type> omitIllegalBeanTypes(Set<Type> types, Object baseType) {
        ImmutableSet.Builder<Type> builder = ImmutableSet.builder();
        for (Type type : types) {
            if (Types.isIllegalBeanType(type)) {
                MetadataLogger.LOG.illegalBeanTypeIgnored(type, baseType);
            } else {
                builder.add(type);
            }
        }
        return builder;
    }

    /**
     * @param contextual
     * @param contextualStore
     * @return the identifier for the given contextual
     * @see #getIdentifier(Contextual, ContextualStore, ServiceRegistry)
     */
    public static BeanIdentifier getIdentifier(Contextual<?> contextual, ContextualStore contextualStore) {
        return getIdentifier(contextual, contextualStore, null);
    }

    /**
     * @param contextual
     * @param serviceRegistry
     * @return the identifier for the given contextual
     * @see #getIdentifier(Contextual, ContextualStore, ServiceRegistry)
     */
    public static BeanIdentifier getIdentifier(Contextual<?> contextual, ServiceRegistry serviceRegistry) {
        return getIdentifier(contextual, null, serviceRegistry);
    }

    /**
     *
     * @param bean
     * @return <code>true</code> if final methods should be ignored when checking proxyability
     */
    public static boolean shouldIgnoreFinalMethods(Bean<?> bean) {
        if (bean instanceof AbstractBean<?, ?>) {
            AbstractBean<?, ?> abstractBean = (AbstractBean<?, ?>) bean;
            return abstractBean.isIgnoreFinalMethods();
        }
        return false;
    }

    public static Bean<?> unwrap(Bean<?> bean) {
        if (bean instanceof ForwardingBean) {
            ForwardingBean<?> forwarding = (ForwardingBean<?>) bean;
            return forwarding.delegate();
        }
        return bean;
    }

    /**
     * A slightly optimized way to get the bean identifier - there is not need to call ContextualStore.putIfAbsent() for passivation capable beans because it's
     * already called during bootstrap. See also {@link BeanManagerImpl#addBean(Bean)}.
     *
     * @param contextual
     * @param contextualStore
     * @param serviceRegistry
     * @return the identifier for the given contextual
     */
    private static BeanIdentifier getIdentifier(Contextual<?> contextual, ContextualStore contextualStore, ServiceRegistry serviceRegistry) {
        if (contextual instanceof RIBean<?>) {
            return ((RIBean<?>) contextual).getIdentifier();
        }
        if (contextualStore == null) {
            contextualStore = serviceRegistry.get(ContextualStore.class);
        }
        return contextualStore.putIfAbsent(contextual);
    }

}
