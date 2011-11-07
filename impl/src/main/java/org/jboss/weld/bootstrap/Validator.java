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
package org.jboss.weld.bootstrap;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.AmbiguousResolutionException;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalProductException;
import org.jboss.weld.exceptions.InconsistentSpecializationException;
import org.jboss.weld.exceptions.NullableDependencyException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnserializableDependencyException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Named;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ValidatorMessage.*;
import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 *
 * @author Nicklas Karlsson
 * @author David Allen
 */
public class Validator implements Service {

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    private void validateBean(Bean<?> bean, BeanManagerImpl beanManager) {
        for (InjectionPoint ij : bean.getInjectionPoints()) {
            validateInjectionPoint(ij, beanManager);
        }
        boolean normalScoped = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal();
        if (normalScoped && !Beans.isBeanProxyable(bean)) {
            throw Proxies.getUnproxyableTypesException(bean.getTypes());
        }
        if (!normalScoped) {
            validatePseudoScopedBean(bean, beanManager);
        }
    }

    /**
     * Validate an RIBean. This includes validating whether two beans specialize
     * the same bean
     *
     * @param bean             the bean to validate
     * @param beanManager      the current manager
     * @param specializedBeans the existing specialized beans
     */
    private void validateRIBean(RIBean<?> bean, BeanManagerImpl beanManager, Collection<RIBean<?>> specializedBeans) {
        validateBean(bean, beanManager);
        if (!(bean instanceof NewManagedBean<?>) && !(bean instanceof NewSessionBean<?>)) {
            RIBean<?> abstractBean = bean;
            if (abstractBean.isSpecializing()) {
                if (specializedBeans.contains(abstractBean.getSpecializedBean())) {
                    throw new InconsistentSpecializationException(BEAN_SPECIALIZED_TOO_MANY_TIMES, bean);
                }
                specializedBeans.add(abstractBean.getSpecializedBean());
            }
            if ((bean instanceof AbstractClassBean<?>)) {
                AbstractClassBean<?> classBean = (AbstractClassBean<?>) bean;
                if (classBean.hasDecorators()) {
                    validateDecorators(beanManager, classBean);
                }
                // validate CDI-defined interceptors
                if (classBean.hasInterceptors()) {
                    validateInterceptors(beanManager, classBean);
                }
            }
        }
    }

    private void validateInterceptors(BeanManagerImpl beanManager, AbstractClassBean<?> classBean) {
        InterceptionModel<ClassMetadata<?>, ?> interceptionModel = beanManager.getInterceptorModelRegistry().get(classBean.getType());
        if (interceptionModel != null) {
            Set<? extends InterceptorMetadata<?>> interceptors = interceptionModel.getAllInterceptors();
            if (interceptors.size() > 0) {
                boolean passivationCapabilityCheckRequired = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(classBean.getScope()).isPassivating() || (classBean instanceof SessionBean<?>);
                for (InterceptorMetadata<?> interceptorMetadata : interceptors) {
                    if (interceptorMetadata.getInterceptorReference().getInterceptor() instanceof SerializableContextual) {
                        SerializableContextual<Interceptor<?>, ?> serializableContextual = cast(interceptorMetadata.getInterceptorReference().getInterceptor());

                        if (passivationCapabilityCheckRequired) {
                            Interceptor<?> interceptor = serializableContextual.get();
                            boolean isSerializable = (interceptor instanceof InterceptorImpl) ? ((InterceptorImpl<?>) interceptor).isSerializable()  : (interceptor instanceof PassivationCapable);
                            if (isSerializable == false)
                                throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, classBean, interceptor);
                        }
                        for (InjectionPoint injectionPoint : serializableContextual.get().getInjectionPoints()) {
                            Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                            validateInjectionPoint(injectionPoint, beanManager);
                            if (passivationCapabilityCheckRequired) {
                                validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
                            }
                        }
                    }
                    if (interceptorMetadata.getInterceptorReference().getInterceptor() instanceof ClassMetadata<?>) {
                        ClassMetadata<?> classMetadata = (ClassMetadata<?>) interceptorMetadata.getInterceptorReference().getInterceptor();
                        if (passivationCapabilityCheckRequired && !Reflections.isSerializable(classMetadata.getJavaClass())) {
                            throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, this, classMetadata.getJavaClass().getName());
                        }
                        InjectionTarget<Object> injectionTarget = cast(beanManager.createInjectionTarget(beanManager.createAnnotatedType(classMetadata.getJavaClass())));
                        for (InjectionPoint injectionPoint : injectionTarget.getInjectionPoints()) {
                            Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                            validateInjectionPoint(injectionPoint, beanManager);
                            if (passivationCapabilityCheckRequired) {
                                validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateDecorators(BeanManagerImpl beanManager, AbstractClassBean<?> classBean) {
        if (classBean.getDecorators().size() > 0) {
            boolean passivationCapabilityCheckRequired = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(classBean.getScope()).isPassivating() || (classBean instanceof SessionBean<?>);
            for (Decorator<?> decorator : classBean.getDecorators()) {
                if (passivationCapabilityCheckRequired) {
                    boolean isSerializable = (decorator instanceof WeldDecorator<?>) ? (((WeldDecorator<?>) decorator).getWeldAnnotated().isSerializable())  : (decorator instanceof PassivationCapable);
                    if (!isSerializable) {
                        throw new UnserializableDependencyException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR, classBean, decorator);
                    }
                }
                for (InjectionPoint ij : decorator.getInjectionPoints()) {
                    if (!ij.isDelegate()) {
                        Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(ij));
                        validateInjectionPoint(ij, beanManager);
                        if (passivationCapabilityCheckRequired) {
                            validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate an injection point
     *
     * @param ij          the injection point to validate
     * @param beanManager the bean manager
     */
    public void validateInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager) {
        if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getQualifiers().size() > 1) {
            throw new DefinitionException(NEW_WITH_QUALIFIERS, ij);
        }
        if (ij.getType().equals(InjectionPoint.class) && ij.getBean() == null) {
            throw new DefinitionException(INJECTION_INTO_NON_BEAN, ij);
        }
        if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(ij.getBean().getScope())) {
            throw new DefinitionException(INJECTION_INTO_NON_DEPENDENT_BEAN, ij);
        }
        if (ij.getType() instanceof TypeVariable<?>) {
            throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, ij);
        }
        if (!(ij.getMember() instanceof Field) && ij.getAnnotated().isAnnotationPresent(Named.class) && ij.getAnnotated().getAnnotation(Named.class).value().equals("")) {
            throw new DefinitionException(NON_FIELD_INJECTION_POINT_CANNOT_USE_NAMED, ij);
        }
        boolean newBean = (ij.getBean() instanceof NewManagedBean<?>) || (ij.getBean() instanceof NewSessionBean<?>);
        if (!newBean) {
            checkScopeAnnotations(ij, beanManager.getServices().get(MetaAnnotationStore.class));
        }
        checkFacadeInjectionPoint(ij, Instance.class);
        checkFacadeInjectionPoint(ij, Event.class);
        Annotation[] bindings = ij.getQualifiers().toArray(new Annotation[0]);
        Set<?> resolvedBeans = beanManager.getBeanResolver().resolve(beanManager.getBeans(ij));
        if (!isInjectionPointSatisfied(ij, resolvedBeans, beanManager)) {
            throw new DeploymentException(INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES, ij, Formats.formatAnnotations(bindings), Formats.formatType(ij.getType()));
        }
        if (resolvedBeans.size() > 1 && !ij.isDelegate()) {
            throw new DeploymentException(INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES, ij, Formats.formatAnnotations(bindings), Formats.formatType(ij.getType()), resolvedBeans);
        }
        // Account for the case this is disabled decorator
        if (!resolvedBeans.isEmpty()) {
            Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
            if (beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(resolvedBean.getScope()).isNormal() && !Proxies.isTypeProxyable(ij.getType())) {
                throw new UnproxyableResolutionException(INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES, ij);
            }
            if (Reflections.isPrimitive(ij.getType()) && resolvedBean.isNullable()) {
                throw new NullableDependencyException(INJECTION_POINT_HAS_NULLABLE_DEPENDENCIES, ij);
            }
            if (ij.getBean() != null && Beans.isPassivatingScope(ij.getBean(), beanManager) && (!ij.isTransient()) && !Beans.isPassivationCapableBean(resolvedBean)) {
                validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
            }
        }
    }

    public void validateInjectionTarget(InjectionTarget<?> injectionTarget, BeanManagerImpl beanManager) {
        for (InjectionPoint injectionPoint : injectionTarget.getInjectionPoints()) {
            validateInjectionPoint(injectionPoint, beanManager);
        }
    }

    private void checkScopeAnnotations(InjectionPoint ij, MetaAnnotationStore metaAnnotationStore) {
        for (Annotation annotation : ij.getAnnotated().getAnnotations()) {
            if (hasScopeMetaAnnotation(annotation)) {
                log.warn(SCOPE_ANNOTATION_ON_INJECTION_POINT, annotation, ij);
            }
        }
    }

    private boolean hasScopeMetaAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotationType.isAnnotationPresent(Scope.class) || annotationType.isAnnotationPresent(NormalScope.class);
    }

    public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager) {
        if (!ij.isTransient() && !Beans.isPassivationCapableDependency(resolvedBean)) {
            if (resolvedBean.getScope().equals(Dependent.class) && resolvedBean instanceof AbstractProducerBean<?, ?, ?>) {
                throw new IllegalProductException(NON_SERIALIZABLE_BEAN_INJECTED_INTO_PASSIVATING_BEAN, ij.getBean(), resolvedBean);
            }
            throw new UnserializableDependencyException(INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY, ij.getBean(), resolvedBean);
        }
    }

    public void validateDeployment(BeanManagerImpl manager, BeanDeployerEnvironment environment) {
        validateDecorators(manager.getDecorators(), new ArrayList<RIBean<?>>(), manager);
        validateInterceptors(manager.getInterceptors());
        validateBeans(manager.getBeans(), new ArrayList<RIBean<?>>(), manager);
        validateEnabledDecoratorClasses(manager);
        validateEnabledInterceptorClasses(manager);
        validateEnabledAlternatives(manager);
        validateDisposalMethods(environment);
        validateObserverMethods(environment.getObservers(), manager);
        validateBeanNames(manager);
    }

    public void validateBeans(Collection<? extends Bean<?>> beans, Collection<RIBean<?>> specializedBeans, BeanManagerImpl manager) {
        final List<RuntimeException> problems = new ArrayList<RuntimeException>();

        for (Bean<?> bean : beans) {
            try {
                if (bean instanceof RIBean<?>) {
                    validateRIBean((RIBean<?>) bean, manager, specializedBeans);
                } else {
                    validateBean(bean, manager);
                }
            } catch (RuntimeException e) {
                problems.add(e);
            }
        }
        if (!problems.isEmpty()) {
            if (problems.size() == 1) {
                throw problems.get(0);
            } else {
                throw new DeploymentException((List) problems);
            }
        }
    }

    public void validateInterceptors(Collection<? extends Interceptor<?>> interceptors) {
        for (Interceptor<?> interceptor : interceptors) {
            // TODO: confirm that producer methods, fields and disposers can be
            // only found on Weld interceptors?
            if (interceptor instanceof InterceptorImpl<?>) {
                WeldClass<?> annotated = ((InterceptorImpl<?>) interceptor).getWeldAnnotated();
                while (annotated != null && annotated.getJavaClass() != Object.class) {
                    if (!annotated.getDeclaredWeldMethods(Produces.class).isEmpty()) {
                        throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_PRODUCER_METHODS, interceptor);
                    }
                    if (!annotated.getDeclaredWeldFields(Produces.class).isEmpty()) {
                        throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_PRODUCER_FIELDS, interceptor);
                    }
                    if (!annotated.getDeclaredWeldMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                        throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_DISPOSER_METHODS, interceptor);
                    }
                    annotated = annotated.getWeldSuperclass();
                }
            }
        }
    }

    public void validateDecorators(Collection<? extends Decorator<?>> beans, Collection<RIBean<?>> specializedBeans, BeanManagerImpl manager) {
        for (Bean<?> bean : beans) {
            if (bean instanceof RIBean<?>) {
                validateRIBean((RIBean<?>) bean, manager, specializedBeans);

                if (bean instanceof WeldDecorator<?>) {
                    WeldClass<?> annotatated = ((WeldDecorator<?>) bean).getWeldAnnotated();
                    while (annotatated != null && annotatated.getJavaClass() != Object.class) {
                        if (!annotatated.getDeclaredWeldMethods(Produces.class).isEmpty()) {
                            throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_METHODS, bean);
                        }
                        if (!annotatated.getDeclaredWeldFields(Produces.class).isEmpty()) {
                            throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_FIELDS, bean);
                        }
                        if (!annotatated.getDeclaredWeldMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                            throw new DefinitionException(DECORATORS_CANNOT_HAVE_DISPOSER_METHODS, bean);
                        }
                        annotatated = annotatated.getWeldSuperclass();
                    }
                }

            } else {
                validateBean(bean, manager);
            }
        }
    }

    public void validateBeanNames(BeanManagerImpl beanManager) {
        SetMultimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), new Supplier<Set<Bean<?>>>() {

            public Set<Bean<?>> get() {
                return new HashSet<Bean<?>>();
            }

        });
        for (Bean<?> bean : beanManager.getAccessibleBeans()) {
            if (bean.getName() != null) {
                namedAccessibleBeans.put(bean.getName(), bean);
            }
        }

        List<String> accessibleNamespaces = new ArrayList<String>();
        for (String namespace : beanManager.getAccessibleNamespaces()) {
            accessibleNamespaces.add(namespace);
        }

        for (String name : namedAccessibleBeans.keySet()) {
            Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledAndSpecializedBeans(namedAccessibleBeans.get(name), beanManager));
            if (resolvedBeans.size() > 1) {
                throw new DeploymentException(AMBIGUOUS_EL_NAME, name, resolvedBeans);
            }
            if (accessibleNamespaces.contains(name)) {
                throw new DeploymentException(BEAN_NAME_IS_PREFIX, name);
            }
        }
    }

    private void validateEnabledInterceptorClasses(BeanManagerImpl beanManager) {
        Set<Class<?>> interceptorBeanClasses = new HashSet<Class<?>>();
        for (Interceptor<?> interceptor : beanManager.getAccessibleInterceptors()) {
            interceptorBeanClasses.add(interceptor.getBeanClass());
        }
        for (Metadata<Class<?>> enabledInterceptorClass : beanManager.getEnabled().getInterceptors()) {
            if (!interceptorBeanClasses.contains(enabledInterceptorClass.getValue())) {
                throw new DeploymentException(INTERCEPTOR_NOT_ANNOTATED_OR_REGISTERED, enabledInterceptorClass);
            }
        }
    }

    private void validateEnabledDecoratorClasses(BeanManagerImpl beanManager) {
        // TODO Move building this list to the boot or sth
        Set<Class<?>> decoratorBeanClasses = new HashSet<Class<?>>();
        for (Decorator<?> bean : beanManager.getAccessibleDecorators()) {
            decoratorBeanClasses.add(bean.getBeanClass());
        }
        for (Metadata<Class<?>> clazz : beanManager.getEnabled().getDecorators()) {
            if (!decoratorBeanClasses.contains(clazz.getValue())) {
                throw new DeploymentException(DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR, clazz, decoratorBeanClasses);
            }
        }
    }

    private void validateEnabledAlternatives(BeanManagerImpl beanManager) {
        for (Metadata<Class<? extends Annotation>> stereotype : beanManager.getEnabled().getAlternativeStereotypes()) {
            if (!beanManager.isStereotype(stereotype.getValue())) {
                throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE, stereotype);
            }
            if (!isAlternative(beanManager, stereotype.getValue())) {
                throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED, stereotype);
            }
        }
        for (Metadata<Class<?>> clazz : beanManager.getEnabled().getAlternativeClasses()) {
            if (clazz.getValue().isAnnotation() || clazz.getValue().isInterface()) {
                throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_CLASS, clazz);
            }
            WeldClass<?> weldClass = Container.instance().services().get(ClassTransformer.class).loadClass(clazz.getValue());
            if (!weldClass.isAnnotationPresent(Alternative.class)) {
                throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED, clazz);
            }
        }
    }

    private static boolean isAlternative(BeanManager beanManager, Class<? extends Annotation> stereotype) {
        for (Annotation annotation : beanManager.getStereotypeDefinition(stereotype)) {
            if (annotation.annotationType().equals(Alternative.class)) {
                return true;
            }
        }
        return false;
    }

    private void validateDisposalMethods(BeanDeployerEnvironment environment) {
        Set<DisposalMethod<?, ?>> beans = environment.getUnresolvedDisposalBeans();
        if (!beans.isEmpty()) {
            throw new DefinitionException(DISPOSAL_METHODS_WITHOUT_PRODUCER, beans);
        }
    }

    private void validateObserverMethods(Iterable<ObserverMethodImpl<?, ?>> observers, BeanManagerImpl beanManager) {
        for (ObserverMethodImpl<?, ?> omi : observers) {
            for (InjectionPoint ip : omi.getInjectionPoints())
                validateInjectionPoint(ip, beanManager);
        }
    }

    private static void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<?> type) {
        if (injectionPoint.getAnnotated().getBaseType().equals(type)) {
            if (injectionPoint.getType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
                if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable<?>) {
                    throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, injectionPoint);
                }
                if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType) {
                    throw new DefinitionException(INJECTION_POINT_HAS_WILDCARD, type, injectionPoint);
                }
            } else {
                throw new DefinitionException(INJECTION_POINT_MUST_HAVE_TYPE_PARAMETER, type, injectionPoint);
            }
        }

    }

    private static boolean isInjectionPointSatisfied(InjectionPoint ij, Set<?> resolvedBeans, BeanManagerImpl beanManager) {
        if (ij.getBean() instanceof Decorator<?>) {
            if (beanManager.getEnabled().getDecorator(ij.getBean().getBeanClass()) != null) {
                return resolvedBeans.size() > 0;
            } else {
                return true;
            }
        } else {
            return resolvedBeans.size() > 0;
        }
    }

    /**
     * Checks to make sure that pseudo scoped beans (i.e. @Dependent scoped
     * beans) have no circular dependencies
     */
    private static void validatePseudoScopedBean(Bean<?> bean, BeanManagerImpl beanManager) {
        reallyValidatePseudoScopedBean(bean, beanManager, new LinkedHashSet<Bean<?>>(), new HashSet<Bean<?>>());
    }

    /**
     * checks if a bean has been seen before in the dependecyPath. If not, it
     * resolves the InjectionPoints and adds the resolved beans to the set of
     * beans to be validated
     */
    private static void reallyValidatePseudoScopedBean(Bean<?> bean, BeanManagerImpl beanManager, Set<Bean<?>> dependencyPath, Set<Bean<?>> validatedBeans) {
        // see if we have already seen this bean in the dependency path
        if (dependencyPath.contains(bean)) {
            // create a list that shows the path to the bean
            List<Bean<?>> realDepdencyPath = new ArrayList<Bean<?>>(dependencyPath);
            realDepdencyPath.add(bean);
            throw new DeploymentException(PSEUDO_SCOPED_BEAN_HAS_CIRCULAR_REFERENCES, realDepdencyPath);
        }
        if (validatedBeans.contains(bean)) {
            return;
        }
        dependencyPath.add(bean);
        for (InjectionPoint injectionPoint : bean.getInjectionPoints()) {
            validatePseudoScopedInjectionPoint(injectionPoint, beanManager, dependencyPath, validatedBeans);
        }
        validatedBeans.add(bean);
        dependencyPath.remove(bean);
    }

    /**
     * finds pseudo beans and adds them to the list of beans to be validated
     */
    private static void validatePseudoScopedInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager, Set<Bean<?>> dependencyPath, Set<Bean<?>> validatedBeans) {
        Set<Bean<?>> resolved = beanManager.getBeans(ij);
        try {
            Bean<? extends Object> bean = beanManager.resolve(resolved);
            if (bean != null) {
                if (!(bean instanceof AbstractBuiltInBean<?>)) {
                    boolean normalScoped = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal();
                    if (!normalScoped) {
                        reallyValidatePseudoScopedBean(bean, beanManager, dependencyPath, validatedBeans);
                    }
                }
            }
        } catch (AmbiguousResolutionException e) {
            // this is handled by another validator
        }
    }

    public void cleanup() {
    }

}
