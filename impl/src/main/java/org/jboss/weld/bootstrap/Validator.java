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

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.INJECTED_FIELD_CANNOT_BE_PRODUCER;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_NOT_CLASS;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE;
import static org.jboss.weld.logging.messages.ValidatorMessage.AMBIGUOUS_EL_NAME;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_NAME_IS_PREFIX;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_SPECIALIZED_TOO_MANY_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_DISPOSER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_OBSERVER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_PRODUCER_FIELDS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_PRODUCER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.DISPOSAL_METHODS_WITHOUT_PRODUCER;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_INTO_NON_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_INTO_NON_DEPENDENT_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_HAS_WILDCARD;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_MUST_HAVE_TYPE_PARAMETER;
import static org.jboss.weld.logging.messages.ValidatorMessage.INJECTION_POINT_WITH_TYPE_VARIABLE;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTORS_CANNOT_HAVE_DISPOSER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTORS_CANNOT_HAVE_OBSERVER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTORS_CANNOT_HAVE_PRODUCER_FIELDS;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTORS_CANNOT_HAVE_PRODUCER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTOR_NOT_ANNOTATED_OR_REGISTERED;
import static org.jboss.weld.logging.messages.ValidatorMessage.INVALID_BEAN_METADATA_INJECTION_POINT_QUALIFIER;
import static org.jboss.weld.logging.messages.ValidatorMessage.INVALID_BEAN_METADATA_INJECTION_POINT_TYPE;
import static org.jboss.weld.logging.messages.ValidatorMessage.INVALID_BEAN_METADATA_INJECTION_POINT_TYPE_ARGUMENT;
import static org.jboss.weld.logging.messages.ValidatorMessage.NEW_WITH_QUALIFIERS;
import static org.jboss.weld.logging.messages.ValidatorMessage.NON_FIELD_INJECTION_POINT_CANNOT_USE_NAMED;
import static org.jboss.weld.logging.messages.ValidatorMessage.NON_SERIALIZABLE_BEAN_INJECTED_INTO_PASSIVATING_BEAN;
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.PSEUDO_SCOPED_BEAN_HAS_CIRCULAR_REFERENCES;
import static org.jboss.weld.logging.messages.ValidatorMessage.SCOPE_ANNOTATION_ON_INJECTION_POINT;
import static org.jboss.weld.logging.messages.ValidatorMessage.USER_TRANSACTION_INJECTION_INTO_BEAN_WITH_CONTAINER_MANAGED_TRANSACTIONS;
import static org.jboss.weld.util.AnnotatedTypes.getDeclaringAnnotatedType;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Decorated;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Named;
import javax.inject.Scope;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalProductException;
import org.jboss.weld.exceptions.InconsistentSpecializationException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnserializableDependencyException;
import org.jboss.weld.injection.producer.AbstractMemberProducer;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.literal.DecoratedLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.InterceptedLiteral;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.JtaApiAbstraction;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.HashSetSupplier;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.SetMultimap;

/**
 * Checks a list of beans for DeploymentExceptions and their subclasses
 *
 * @author Nicklas Karlsson
 * @author David Allen
 * @author Jozef Hartinger
 * @author Stuart Douglas
 * @author Ales Justin
 */
public class Validator implements Service {

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    protected void validateGeneralBean(Bean<?> bean, BeanManagerImpl beanManager) {
        for (InjectionPoint ij : bean.getInjectionPoints()) {
            validateInjectionPoint(ij, beanManager);
        }
        boolean normalScoped = beanManager.isNormalScope(bean.getScope());
        /*
         * Named beans are validated eagerly. If a bean is not named, it is validated for proxyability based on discovered
         * injection points.
         */
        if (normalScoped && bean.getName() != null && !Beans.isBeanProxyable(bean)) {
            UnproxyableResolutionException ue = Proxies.getUnproxyableTypesException(bean);
            if (ue != null) {
                throw new DeploymentException(ue);
            }
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
    protected void validateRIBean(RIBean<?> bean, BeanManagerImpl beanManager, Collection<RIBean<?>> specializedBeans) {
        validateGeneralBean(bean, beanManager);
        if (!(bean instanceof NewManagedBean<?>) && !(bean instanceof NewSessionBean<?>)) {
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
            // for each producer bean validate its disposer method
            if (bean instanceof AbstractProducerBean<?, ?, ?>) {
                AbstractProducerBean<?, ?, ?> producerBean = Reflections.<AbstractProducerBean<?, ?, ?>>cast(bean);
                if (producerBean.getProducer() instanceof AbstractMemberProducer<?, ?>) {
                    AbstractMemberProducer<?, ?> producer = Reflections.<AbstractMemberProducer<?, ?>>cast(producerBean.getProducer());
                    if (producer.getDisposalMethod() != null) {
                        for (InjectionPoint ip : producer.getDisposalMethod().getInjectionPoints()) {
                            // pass the producer bean instead of the disposal method bean
                            validateInjectionPointForDefinitionErrors(ip, bean, beanManager);
                            validateInjectionPointForDeploymentProblems(ip, bean, beanManager);
                        }
                    }
                }
            }
        }
    }

    private void validateInterceptors(BeanManagerImpl beanManager, AbstractClassBean<?> classBean) {
        InterceptionModel<ClassMetadata<?>, ?> interceptionModel = beanManager.getInterceptorModelRegistry().get(classBean.getType());
        if (interceptionModel != null) {
            Set<? extends InterceptorMetadata<?>> interceptors = interceptionModel.getAllInterceptors();
            if (interceptors.size() > 0) {
                boolean passivationCapabilityCheckRequired = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(classBean.getScope()).isPassivating();
                for (InterceptorMetadata<?> interceptorMetadata : interceptors) {
                    if (interceptorMetadata.getInterceptorReference().getInterceptor() instanceof SerializableContextual) {
                        SerializableContextual<Interceptor<?>, ?> serializableContextual = cast(interceptorMetadata.getInterceptorReference().getInterceptor());

                        if (passivationCapabilityCheckRequired) {
                            Interceptor<?> interceptor = serializableContextual.get();
                            boolean isSerializable = (interceptor instanceof InterceptorImpl) ? ((InterceptorImpl<?>) interceptor).isSerializable() : (interceptor instanceof PassivationCapable);
                            if (isSerializable == false)
                                throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, classBean, interceptor);
                        }
                        for (InjectionPoint injectionPoint : serializableContextual.get().getInjectionPoints()) {
                            Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
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
            boolean passivationCapabilityCheckRequired = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(classBean.getScope()).isPassivating();
            for (Decorator<?> decorator : classBean.getDecorators()) {
                if (passivationCapabilityCheckRequired) {
                    boolean isSerializable = (decorator instanceof WeldDecorator<?>) ? (((WeldDecorator<?>) decorator).getEnhancedAnnotated().isSerializable()) : (decorator instanceof PassivationCapable);
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
        validateInjectionPointForDefinitionErrors(ij, ij.getBean(), beanManager);
        validateInjectionPointForDeploymentProblems(ij, ij.getBean(), beanManager);
    }

    /**
     * Checks for definition errors associated with a given {@link InjectionPoint}
     */
    public void validateInjectionPointForDefinitionErrors(InjectionPoint ij, Bean<?> bean, BeanManagerImpl beanManager) {
        if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getQualifiers().size() > 1) {
            throw new DefinitionException(NEW_WITH_QUALIFIERS, ij);
        }
        if (ij.getType() instanceof TypeVariable<?>) {
            throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, ij);
        }
        if (!(ij.getMember() instanceof Field) && ij.getAnnotated().isAnnotationPresent(Named.class) && ij.getAnnotated().getAnnotation(Named.class).value().equals("")) {
            throw new DefinitionException(NON_FIELD_INJECTION_POINT_CANNOT_USE_NAMED, ij);
        }
        if (ij.getAnnotated().isAnnotationPresent(Produces.class)) {
            if (bean != null) {
                throw new DefinitionException(INJECTED_FIELD_CANNOT_BE_PRODUCER, ij.getAnnotated(), bean);
            } else {
                throw new DefinitionException(INJECTED_FIELD_CANNOT_BE_PRODUCER, ij.getAnnotated(), Reflections.<AnnotatedField<?>>cast(ij.getAnnotated()).getDeclaringType());
            }
        }
        boolean newBean = (bean instanceof NewManagedBean<?>) || (bean instanceof NewSessionBean<?>);
        if (!newBean) {
            checkScopeAnnotations(ij, beanManager.getServices().get(MetaAnnotationStore.class));
        }
        checkFacadeInjectionPoint(ij, Instance.class);
        checkFacadeInjectionPoint(ij, Event.class);
        // metadata injection points
        if (ij.getType().equals(InjectionPoint.class) && bean == null) {
            throw new DefinitionException(INJECTION_INTO_NON_BEAN, ij);
        }
        if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(bean.getScope())) {
            throw new DefinitionException(INJECTION_INTO_NON_DEPENDENT_BEAN, ij);
        }
        Class<?> rawType = Reflections.getRawType(ij.getType());
        if (Bean.class.equals(rawType) || Interceptor.class.equals(rawType) || Decorator.class.equals(rawType)) {
            if (bean == null) {
                throw new DefinitionException(INJECTION_INTO_NON_BEAN, ij);
            }
            if (bean instanceof AbstractClassBean<?>) {
                checkBeanMetadataInjectionPoint(bean, ij, getDeclaringAnnotatedType(ij.getAnnotated()).getBaseType());
            }
            // make sure this is PM injection point and not an injection point of the disposer method
            if (bean instanceof ProducerMethod<?, ?> && bean == ij.getBean()) {
                ProducerMethod<?, ?> producerMethod = Reflections.cast(bean);
                checkBeanMetadataInjectionPoint(bean, ij, producerMethod.getAnnotated().getBaseType());
            }
        }
        // check that UserTransaction is not injected into a SessionBean with container-managed transactions
        if (bean instanceof SessionBean<?>) {
            JtaApiAbstraction jtaApi = beanManager.getServices().get(JtaApiAbstraction.class);
            if (jtaApi.USER_TRANSACTION_CLASS.equals(rawType) && (ij.getQualifiers().isEmpty() || ij.getQualifiers().contains(DefaultLiteral.INSTANCE)) && Beans.isSessionBeanWithContainerManagedTransactions(bean, beanManager)) {
                throw new DefinitionException(USER_TRANSACTION_INJECTION_INTO_BEAN_WITH_CONTAINER_MANAGED_TRANSACTIONS, ij);
            }
        }
    }

    /**
     * Checks for deployment problems associated with a given {@link InjectionPoint}
     */
    public void validateInjectionPointForDeploymentProblems(InjectionPoint ij, Bean<?> bean, BeanManagerImpl beanManager) {
        if (ij.isDelegate()) {
            return; // do not validate delegate injection points as these are special
        }
        Set<?> resolvedBeans = beanManager.getBeanResolver().resolve(beanManager.getBeans(ij));
        if (!isInjectionPointSatisfied(ij, resolvedBeans, beanManager)) {
            throw new DeploymentException(INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES, ij, Formats.formatAnnotations(ij.getQualifiers().toArray(new Annotation[ij.getQualifiers().size()])), Formats.formatType(ij.getType()));
        }
        if (resolvedBeans.size() > 1) {
            throw new DeploymentException(INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES, ij, Formats.formatAnnotations(ij.getQualifiers().toArray(new Annotation[ij.getQualifiers().size()])), Formats.formatType(ij.getType()), resolvedBeans);
        }
        // Account for the case this is disabled decorator
        if (!resolvedBeans.isEmpty()) {
            Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
            if (beanManager.isNormalScope(resolvedBean.getScope())) {
                UnproxyableResolutionException ue = Proxies.getUnproxyableTypeException(ij.getType());
                if (ue != null) {
                    throw new DeploymentException(INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES, ue, ij);
                }
            }
            if (bean != null && Beans.isPassivatingScope(bean, beanManager)) {
                validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
            }
        }
    }

    public void validateProducers(Collection<Producer<?>> producers, BeanManagerImpl beanManager) {
        for (Producer<?> producer : producers) {
            validateProducer(producer, beanManager);
        }
    }

    public void validateProducer(Producer<?> producer, BeanManagerImpl beanManager) {
        for (InjectionPoint injectionPoint : producer.getInjectionPoints()) {
            validateInjectionPoint(injectionPoint, beanManager);
        }
    }

    private void checkScopeAnnotations(InjectionPoint ij, MetaAnnotationStore metaAnnotationStore) {
        Annotated annotated = ij.getAnnotated();
        if (annotated instanceof EnhancedAnnotated<?, ?>) {
            EnhancedAnnotated<?, ?> weldAnnotated = (EnhancedAnnotated<?, ?>) annotated;
            Set<Annotation> scopes = weldAnnotated.getMetaAnnotations(Scope.class);
            Set<Annotation> normalScopes = weldAnnotated.getMetaAnnotations(NormalScope.class);
            for (Annotation annotation : scopes) {
                logScopeOnInjectionPointWarning(ij, annotation);
            }
            for (Annotation annotation : normalScopes) {
                logScopeOnInjectionPointWarning(ij, annotation);
            }
        } else {
            for (Annotation annotation : annotated.getAnnotations()) {
                if (hasScopeMetaAnnotation(annotation)) {
                    logScopeOnInjectionPointWarning(ij, annotation);
                }
            }
        }
    }

    private void logScopeOnInjectionPointWarning(InjectionPoint ij, Annotation annotation) {
        log.warn(SCOPE_ANNOTATION_ON_INJECTION_POINT, annotation, ij);
    }

    private boolean hasScopeMetaAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotationType.isAnnotationPresent(Scope.class) || annotationType.isAnnotationPresent(NormalScope.class);
    }

    public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager) {
        if ((ij.getMember() instanceof Field) && !ij.isTransient() && !Beans.isPassivationCapableDependency(resolvedBean)) {
            if (resolvedBean.getScope().equals(Dependent.class) && resolvedBean instanceof AbstractProducerBean<?, ?, ?>) {
                throw new IllegalProductException(NON_SERIALIZABLE_BEAN_INJECTED_INTO_PASSIVATING_BEAN, ij.getBean(), resolvedBean);
            }
            throw new UnserializableDependencyException(INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY, ij.getBean(), resolvedBean);
        }
    }

    public void validateDeployment(BeanManagerImpl manager, BeanDeployerEnvironment environment) {
        validateDecorators(manager.getDecorators(), manager);
        validateInterceptors(manager.getInterceptors(), manager);
        validateBeans(manager.getBeans(), manager);
        validateEnabledDecoratorClasses(manager);
        validateEnabledInterceptorClasses(manager);
        validateEnabledAlternatives(manager);
        validateSpecialization(manager);
        validateDisposalMethods(environment);
        validateObserverMethods(environment.getObservers(), manager);
        validateBeanNames(manager);
    }

    public void validateSpecialization(BeanManagerImpl manager) {
        SpecializationAndEnablementRegistry registry = manager.getServices().get(SpecializationAndEnablementRegistry.class);
        for (Entry<AbstractBean<?, ?>> entry : registry.getBeansSpecializedInAnyDeploymentAsMultiset().entrySet()) {
            if (entry.getCount() > 1) {
                throw new InconsistentSpecializationException(BEAN_SPECIALIZED_TOO_MANY_TIMES, entry.getElement());
            }
        }
    }

    public void validateBeans(Collection<? extends Bean<?>> beans, BeanManagerImpl manager) {
        final List<RuntimeException> problems = new ArrayList<RuntimeException>();
        final Set<RIBean<?>> specializedBeans = new HashSet<RIBean<?>>();

        for (Bean<?> bean : beans) {
            validateBean(bean, specializedBeans, manager, problems);
        }
        if (!problems.isEmpty()) {
            if (problems.size() == 1) {
                throw problems.get(0);
            } else {
                throw new DeploymentException(problems);
            }
        }
    }

    protected void validateBean(Bean<?> bean, Collection<RIBean<?>> specializedBeans, BeanManagerImpl manager, List<RuntimeException> problems) {
        try {
            if (bean instanceof RIBean<?>) {
                validateRIBean((RIBean<?>) bean, manager, specializedBeans);
            } else {
                validateGeneralBean(bean, manager);
            }
        } catch (RuntimeException e) {
            problems.add(e);
        }
    }

    public void validateInterceptors(Collection<? extends Interceptor<?>> interceptors, BeanManagerImpl manager) {
        for (Interceptor<?> interceptor : interceptors) {
            validateInterceptor(interceptor, manager);
        }
    }

    protected void validateInterceptor(Interceptor<?> interceptor, BeanManagerImpl manager) {
        if (interceptor instanceof InterceptorImpl<?>) {
            EnhancedAnnotatedType<?> annotated = ((InterceptorImpl<?>) interceptor).getEnhancedAnnotated();
            if (!BeanMethods.getObserverMethods(annotated).isEmpty()) {
                throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_OBSERVER_METHODS, interceptor);
            }
            while (annotated != null && annotated.getJavaClass() != Object.class) {
                if (!annotated.getDeclaredEnhancedMethods(Produces.class).isEmpty()) {
                    throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_PRODUCER_METHODS, interceptor);
                }
                if (!annotated.getDeclaredEnhancedFields(Produces.class).isEmpty()) {
                    throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_PRODUCER_FIELDS, interceptor);
                }
                if (!annotated.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                    throw new DefinitionException(INTERCEPTORS_CANNOT_HAVE_DISPOSER_METHODS, interceptor);
                }
                annotated = annotated.getEnhancedSuperclass();
            }
        }
        for (InjectionPoint injectionPoint : interceptor.getInjectionPoints()) {
            validateInjectionPoint(injectionPoint, manager);
        }
    }

    public void validateDecorators(Collection<? extends Decorator<?>> decorators, BeanManagerImpl manager) {
        Set<RIBean<?>> specializedBeans = new HashSet<RIBean<?>>();
        for (Decorator<?> decorator : decorators) {
            validateDecorator(decorator, specializedBeans, manager);
        }
    }

    protected void validateDecorator(Decorator<?> decorator, Collection<RIBean<?>> specializedBeans, BeanManagerImpl manager) {
        if (decorator.getDecoratedTypes().isEmpty()) {
            throw new DefinitionException(ValidatorMessage.NO_DECORATED_TYPES, decorator);
        }
        if (decorator instanceof RIBean<?>) {
            validateRIBean((RIBean<?>) decorator, manager, specializedBeans);

            if (decorator instanceof WeldDecorator<?>) {
                EnhancedAnnotatedType<?> annotatated = ((WeldDecorator<?>) decorator).getEnhancedAnnotated();
                if (!BeanMethods.getObserverMethods(annotatated).isEmpty()) {
                    throw new DefinitionException(DECORATORS_CANNOT_HAVE_OBSERVER_METHODS, decorator);
                }
                while (annotatated != null && annotatated.getJavaClass() != Object.class) {
                    if (!annotatated.getDeclaredEnhancedMethods(Produces.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_METHODS, decorator);
                    }
                    if (!annotatated.getDeclaredEnhancedFields(Produces.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_FIELDS, decorator);
                    }
                    if (!annotatated.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_DISPOSER_METHODS, decorator);
                    }
                    annotatated = annotatated.getEnhancedSuperclass();
                }
            }

        } else {
            validateGeneralBean(decorator, manager);
        }
    }

    public void validateBeanNames(BeanManagerImpl beanManager) {
        SetMultimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), HashSetSupplier.<Bean<?>>instance());
        for (Bean<?> bean : beanManager.getAccessibleBeans()) {
            if (bean.getName() != null) {
                namedAccessibleBeans.put(bean.getName(), bean);
            }
        }

        List<String> accessibleNamespaces = new ArrayList<String>();
        for (String namespace : beanManager.getAccessibleNamespaces()) {
            accessibleNamespaces.add(namespace);
        }

        SpecializationAndEnablementRegistry registry = beanManager.getServices().get(SpecializationAndEnablementRegistry.class);
        for (String name : namedAccessibleBeans.keySet()) {
            Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledAndSpecializedBeans(namedAccessibleBeans.get(name), beanManager, registry));
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
        if (beanManager.getEnabled().getAlternatives().size() > 0) {
            // lookup structure for validation of alternatives
            Multimap<Class<?>, Bean<?>> beansByClass = HashMultimap.create();
            for (Bean<?> bean : beanManager.getAccessibleBeans()) {
                if (!(bean instanceof NewBean)) {
                    beansByClass.put(bean.getBeanClass(), bean);
                }
            }
            for (Metadata<Class<?>> clazz : beanManager.getEnabled().getAlternatives()) {
                if (clazz.getValue().isAnnotation()) {
                    Class<? extends Annotation> annotation = Reflections.<Metadata<Class<? extends Annotation>>>cast(clazz).getValue();
                    if (!beanManager.isStereotype(annotation)) {
                        throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE, clazz);
                    }
                    if (!isAlternative(beanManager, annotation)) {
                        throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED, clazz);
                    }
                } else if (clazz.getValue().isInterface()) {
                    throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_CLASS, clazz);
                } else {
                    // check that the class is a bean class of at least one alternative
                    boolean alternativeBeanFound = false;
                    for (Bean<?> bean : beansByClass.get(clazz.getValue())) {
                        if (bean.isAlternative()) {
                            alternativeBeanFound = true;
                        }
                    }
                    if (!alternativeBeanFound) {
                        throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED, clazz);
                    }
                }
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

    protected void validateObserverMethods(Iterable<ObserverInitializationContext<?, ?>> observers, BeanManagerImpl beanManager) {
        for (ObserverInitializationContext<?, ?> omi : observers) {
            for (InjectionPoint ip : omi.getObserver().getInjectionPoints())
                validateInjectionPoint(ip, beanManager);
        }
    }

    private static void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<?> type) {
        Type injectionPointType = injectionPoint.getType();
        if (injectionPointType instanceof Class<?> && type.equals(injectionPointType)) {
            throw new DefinitionException(INJECTION_POINT_MUST_HAVE_TYPE_PARAMETER, type, injectionPoint);
        }
        if (injectionPointType instanceof ParameterizedType && !injectionPoint.isDelegate()) {
            ParameterizedType parameterizedType = (ParameterizedType) injectionPointType;
            if (type.equals(parameterizedType.getRawType())) {
                if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable<?>) {
                    throw new DefinitionException(INJECTION_POINT_WITH_TYPE_VARIABLE, injectionPoint);
                }
                if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType) {
                    throw new DefinitionException(INJECTION_POINT_HAS_WILDCARD, type, injectionPoint);
                }
            }
        }
    }

    public static void checkBeanMetadataInjectionPoint(Object bean, InjectionPoint ip, Type expectedTypeArgument) {
        if (!(ip.getType() instanceof ParameterizedType)) {
            throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
        }
        ParameterizedType parameterizedType = (ParameterizedType) ip.getType();
        if (parameterizedType.getActualTypeArguments().length != 1) {
            throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
        }
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type typeArgument = parameterizedType.getActualTypeArguments()[0];

        if (bean == null) {
            throw new DefinitionException(INJECTION_INTO_NON_BEAN, ip);
        }
        /*
         * If an Interceptor instance is injected into a bean instance other than an interceptor instance, the container
         * automatically detects the problem and treats it as a definition error.
         */
        if (rawType.equals(Interceptor.class) && !(bean instanceof Interceptor<?>)) {
            throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
        }
        /*
         * If a Decorator instance is injected into a bean instance other than a decorator instance, the container automatically
         * detects the problem and treats it as a definition error.
         */
        if (rawType.equals(Decorator.class) && !(bean instanceof Decorator<?>)) {
            throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
        }
        Set<Annotation> qualifiers = ip.getQualifiers();
        if (qualifiers.contains(InterceptedLiteral.INSTANCE)) {
            /*
             * If a Bean instance with qualifier @Intercepted is injected into a bean instance other than an interceptor
             * instance, the container automatically detects the problem and treats it as a definition error.
             */
            if (!(bean instanceof Interceptor<?>)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_QUALIFIER, Intercepted.class, Interceptor.class, ip);
            }
            /*
             * If the injection point is a field, an initializer method parameter or a bean constructor of an interceptor, with
             * qualifier @Intercepted, then the type parameter of the injected Bean must be an unbounded wildcard.
             */
            if (!rawType.equals(Bean.class)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
            }
            if (!Reflections.isUnboundedWildcard(typeArgument)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE_ARGUMENT, typeArgument, ip);
            }
        }
        if (qualifiers.contains(DecoratedLiteral.INSTANCE)) {
            /*
             * If a Bean instance with qualifier @Decorated is injected into a bean instance other than a decorator instance,
             * the container automatically detects the problem and treats it as a definition error.
             */
            if (!(bean instanceof Decorator<?>)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_QUALIFIER, Decorated.class, Decorator.class, ip);
            }
            Decorator<?> decorator = Reflections.cast(bean);
            /*
             * If the injection point is a field, an initializer method parameter or a bean constructor of a decorator, with
             * qualifier @Decorated, then the type parameter of the injected Bean must be the same as the delegate type.
             */
            if (!rawType.equals(Bean.class)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE, ip.getType(), ip);
            }
            if (!typeArgument.equals(decorator.getDelegateType())) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE_ARGUMENT, typeArgument, ip);
            }
        }
        if (qualifiers.contains(DefaultLiteral.INSTANCE)) {
            /*
             * If the injection point is a field, an initializer method parameter or a bean constructor, with qualifier
             * @Default, then the type parameter of the injected Bean, Interceptor or Decorator must be the same as the type
             * declaring the injection point.
             *
             * If the injection point is a producer method parameter then the type parameter of the injected Bean must be the
             * same as the producer method return type.
             *
             * If the injection point is a disposer method parameter then the type parameter of the injected Bean must be the
             * same as the disposed parameter.
             */
            if (!expectedTypeArgument.equals(typeArgument)) {
                throw new DefinitionException(INVALID_BEAN_METADATA_INJECTION_POINT_TYPE_ARGUMENT, typeArgument, ip);
            }
        }
    }

    private static boolean isInjectionPointSatisfied(InjectionPoint ij, Set<?> resolvedBeans, BeanManagerImpl beanManager) {
        if (ij.getBean() instanceof Decorator<?>) {
            if (beanManager.getEnabled().isDecoratorEnabled(ij.getBean().getBeanClass())) {
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
        reallyValidatePseudoScopedBean(bean, beanManager, new LinkedHashSet<Object>(), new HashSet<Bean<?>>());
    }

    /**
     * checks if a bean has been seen before in the dependecyPath. If not, it
     * resolves the InjectionPoints and adds the resolved beans to the set of
     * beans to be validated
     */
    private static void reallyValidatePseudoScopedBean(Bean<?> bean, BeanManagerImpl beanManager, Set<Object> dependencyPath, Set<Bean<?>> validatedBeans) {
        // see if we have already seen this bean in the dependency path
        if (dependencyPath.contains(bean)) {
            // create a list that shows the path to the bean
            List<Object> realDependencyPath = new ArrayList<Object>(dependencyPath);
            realDependencyPath.add(bean);
            throw new DeploymentException(PSEUDO_SCOPED_BEAN_HAS_CIRCULAR_REFERENCES, realDependencyPath);
        }
        if (validatedBeans.contains(bean)) {
            return;
        }
        dependencyPath.add(bean);
        for (InjectionPoint injectionPoint : bean.getInjectionPoints()) {
            if (!injectionPoint.isDelegate()) {
                dependencyPath.add(injectionPoint);
                validatePseudoScopedInjectionPoint(injectionPoint, beanManager, dependencyPath, validatedBeans);
                dependencyPath.remove(injectionPoint);
            }
        }
        if (bean instanceof AbstractClassBean) {
            if (((AbstractClassBean) bean).hasDecorators()) {
                final List<Decorator<?>> decorators = ((AbstractClassBean) bean).getDecorators();
                for (final Decorator<?> decorator : decorators) {
                    reallyValidatePseudoScopedBean(decorator, beanManager, dependencyPath, validatedBeans);
                }
            }
        }
        validatedBeans.add(bean);
        dependencyPath.remove(bean);
    }

    /**
     * finds pseudo beans and adds them to the list of beans to be validated
     */
    private static void validatePseudoScopedInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager, Set<Object> dependencyPath, Set<Bean<?>> validatedBeans) {
        Set<Bean<?>> resolved = beanManager.getBeans(ij);
        Bean<?> bean = beanManager.resolve(resolved);
        if (bean != null) {
            if (!(bean instanceof AbstractBuiltInBean<?>)) {
                if (!ij.isDelegate()) {
                    boolean normalScoped = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal();
                    if (!normalScoped) {
                        reallyValidatePseudoScopedBean(bean, beanManager, dependencyPath, validatedBeans);
                    }
                }
            }
        }
    }

    public void cleanup() {
    }

}
