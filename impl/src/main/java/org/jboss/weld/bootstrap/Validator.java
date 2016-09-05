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
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_WITH_PASSIVATING_SCOPE_NOT_PASSIVATION_CAPABLE;
import static org.jboss.weld.logging.messages.ValidatorMessage.BUILTIN_BEAN_WITH_NONSERIALIZABLE_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_DISPOSER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_OBSERVER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_PRODUCER_FIELDS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATORS_CANNOT_HAVE_PRODUCER_METHODS;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.DISPOSAL_METHODS_WITHOUT_PRODUCER;
import static org.jboss.weld.logging.messages.ValidatorMessage.EVENT_METADATA_INJECTED_OUTSIDE_OF_OBSERVER;
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
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR;
import static org.jboss.weld.logging.messages.ValidatorMessage.PSEUDO_SCOPED_BEAN_HAS_CIRCULAR_REFERENCES;
import static org.jboss.weld.logging.messages.ValidatorMessage.SCOPE_ANNOTATION_ON_INJECTION_POINT;
import static org.jboss.weld.logging.messages.ValidatorMessage.UNSATISFIED_DEPENDENCY_BECAUSE_CLASS_IGNORED;
import static org.jboss.weld.logging.messages.ValidatorMessage.UNSATISFIED_DEPENDENCY_BECAUSE_QUALIFIERS_DONT_MATCH;
import static org.jboss.weld.logging.messages.ValidatorMessage.USER_TRANSACTION_INJECTION_INTO_BEAN_WITH_CONTAINER_MANAGED_TRANSACTIONS;
import static org.jboss.weld.util.Types.buildClassNameMap;

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
import java.util.Map;
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
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Scope;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.DecorableBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.AbstractDecorableBuiltInBean;
import org.jboss.weld.bean.builtin.ee.EEResourceProducerField;
import org.jboss.weld.bean.interceptor.CdiInterceptorFactory;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.InconsistentSpecializationException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnserializableDependencyException;
import org.jboss.weld.injection.producer.AbstractMemberProducer;
import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorFactory;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.literal.DecoratedLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.InterceptedLiteral;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.JtaApiAbstraction;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.HashSetSupplier;
import org.jboss.weld.util.collections.WeldCollections;
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

        if (beanManager.isPassivatingScope(bean.getScope()) && !Beans.isPassivationCapableBean(bean)) {
            throw new DeploymentException(BEAN_WITH_PASSIVATING_SCOPE_NOT_PASSIVATION_CAPABLE, bean);
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
    protected void validateRIBean(CommonBean<?> bean, BeanManagerImpl beanManager, Collection<CommonBean<?>> specializedBeans) {
        validateGeneralBean(bean, beanManager);
        if (bean instanceof NewBean) {
            return;
        }
        if (bean instanceof DecorableBean) {
            validateDecorators(beanManager, (DecorableBean<?>) bean);
        }
        if ((bean instanceof AbstractClassBean<?>)) {
            AbstractClassBean<?> classBean = (AbstractClassBean<?>) bean;
            // validate CDI-defined interceptors
            if (classBean.hasInterceptors()) {
                validateInterceptors(beanManager, classBean);
            }
        }
        // for each producer bean validate its disposer method
        if (bean instanceof AbstractProducerBean<?, ?, ?>) {
            AbstractProducerBean<?, ?, ?> producerBean = Reflections.<AbstractProducerBean<?, ?, ?>> cast(bean);
            if (producerBean.getProducer() instanceof AbstractMemberProducer<?, ?>) {
                AbstractMemberProducer<?, ?> producer = Reflections.<AbstractMemberProducer<?, ?>> cast(producerBean
                        .getProducer());
                if (producer.getDisposalMethod() != null) {
                    for (InjectionPoint ip : producer.getDisposalMethod().getInjectionPoints()) {
                        // pass the producer bean instead of the disposal method bean
                        validateInjectionPointForDefinitionErrors(ip, null, beanManager);
                        validateMetadataInjectionPoint(ip, null, ValidatorMessage.INJECTION_INTO_DISPOSER_METHOD);
                        validateEventMetadataInjectionPoint(ip);
                        validateInjectionPointForDeploymentProblems(ip, null, beanManager);
                    }
                }
            }
        }
    }

    private void validateCustomBean(Bean<?> bean, BeanManagerImpl beanManager) {
        validateGeneralBean(bean, beanManager);
        if (!(bean instanceof PassivationCapable) && beanManager.isNormalScope(bean.getScope())) {
            log.warn(ValidatorMessage.BEAN_NOT_PASSIVATION_CAPABLE, bean);
        }
    }

    private void validateInterceptors(BeanManagerImpl beanManager, AbstractClassBean<?> classBean) {
        InterceptionModel<ClassMetadata<?>> interceptionModel = beanManager.getInterceptorModelRegistry().get(classBean.getType());
        if (interceptionModel != null) {
            Set<? extends InterceptorMetadata<?>> interceptors = interceptionModel.getAllInterceptors();
            if (interceptors.size() > 0) {
                boolean passivationCapabilityCheckRequired = beanManager.isPassivatingScope(classBean.getScope());
                for (InterceptorMetadata<?> interceptorMetadata : interceptors) {
                    if (interceptorMetadata.getInterceptorFactory() instanceof CdiInterceptorFactory<?>) {
                        CdiInterceptorFactory<?> cdiInterceptorFactory = (CdiInterceptorFactory<?>) interceptorMetadata.getInterceptorFactory();
                        Interceptor<?> interceptor = cdiInterceptorFactory.getInterceptor();

                        if (passivationCapabilityCheckRequired) {
                            boolean isSerializable = (interceptor instanceof InterceptorImpl) ? ((InterceptorImpl<?>) interceptor).isSerializable() : Beans.isPassivationCapableDependency(interceptor);
                            if (!isSerializable) {
                                throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, classBean, interceptor);
                            }
                        }
                        for (InjectionPoint injectionPoint : interceptor.getInjectionPoints()) {
                            Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                            if (passivationCapabilityCheckRequired) {
                                validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
                            }
                        }
                    }
                    if (interceptorMetadata.getInterceptorFactory() instanceof ClassMetadataInterceptorFactory<?>) {
                        ClassMetadataInterceptorFactory<?> factory = (ClassMetadataInterceptorFactory<?>) interceptorMetadata.getInterceptorFactory();
                        ClassMetadata<?> classMetadata = interceptorMetadata.getInterceptorClass();
                        if (passivationCapabilityCheckRequired && !Reflections.isSerializable(classMetadata.getJavaClass())) {
                            throw new DeploymentException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_INTERCEPTOR, this, classMetadata.getJavaClass().getName());
                        }
                        for (InjectionPoint injectionPoint : factory.getInjectionTarget().getInjectionPoints()) {
                            validateInjectionPoint(injectionPoint, beanManager);
                            if (passivationCapabilityCheckRequired) {
                                Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                                validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager);
                            }
                        }
                    }
                }
            }
        }
    }

    private void validateDecorators(BeanManagerImpl beanManager, DecorableBean<?> bean) {
        if (!(beanManager.isPassivatingScope(bean.getScope()) || bean instanceof AbstractDecorableBuiltInBean<?>)) {
            return;
        }
        List<Decorator<?>> decorators = bean.getDecorators();
        if (decorators.isEmpty()) {
            return;
        }
        for (Decorator<?> decorator : decorators) {
            if (!Decorators.isPassivationCapable(decorator)) {
                if (bean instanceof AbstractDecorableBuiltInBean<?>) {
                    throw new UnserializableDependencyException(BUILTIN_BEAN_WITH_NONSERIALIZABLE_DECORATOR, decorator, bean);
                } else {
                    throw new UnserializableDependencyException(PASSIVATING_BEAN_WITH_NONSERIALIZABLE_DECORATOR, bean, decorator);
                }
            }
            for (InjectionPoint ij : decorator.getInjectionPoints()) {
                if (!ij.isDelegate()) {
                    Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(ij));
                    validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
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
        validateMetadataInjectionPoint(ij, ij.getBean(), INJECTION_INTO_NON_BEAN);
        validateEventMetadataInjectionPoint(ij);
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
        // check that UserTransaction is not injected into a SessionBean with container-managed transactions
        if (bean instanceof SessionBean<?>) {
            JtaApiAbstraction jtaApi = beanManager.getServices().get(JtaApiAbstraction.class);
            if (jtaApi.USER_TRANSACTION_CLASS.equals(ij.getType()) &&
                    (ij.getQualifiers().isEmpty() || ij.getQualifiers().contains(DefaultLiteral.INSTANCE)) &&
                    beanManager.getServices().get(EJBApiAbstraction.class).isSessionBeanWithContainerManagedTransactions(bean)) {
                throw new DefinitionException(USER_TRANSACTION_INJECTION_INTO_BEAN_WITH_CONTAINER_MANAGED_TRANSACTIONS, ij);
            }
        }
    }

    public void validateMetadataInjectionPoint(InjectionPoint ij, Bean<?> bean, ValidatorMessage message) {
        // metadata injection points
        if (ij.getType().equals(InjectionPoint.class) && bean == null) {
            throw new DefinitionException(message, ij);
        }
        if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(bean.getScope())) {
            throw new DefinitionException(INJECTION_INTO_NON_DEPENDENT_BEAN, ij);
        }
        Class<?> rawType = Reflections.getRawType(ij.getType());
        if (Bean.class.equals(rawType) || Interceptor.class.equals(rawType) || Decorator.class.equals(rawType)) {
            if (bean == null) {
                throw new DefinitionException(message, ij);
            }
            if (bean instanceof AbstractClassBean<?>) {
                checkBeanMetadataInjectionPoint(bean, ij, AnnotatedTypes.getDeclaringAnnotatedType(ij.getAnnotated()).getBaseType());
            }
            if (bean instanceof ProducerMethod<?, ?>) {
                ProducerMethod<?, ?> producerMethod = Reflections.cast(bean);
                checkBeanMetadataInjectionPoint(bean, ij, producerMethod.getAnnotated().getBaseType());
            }
        }
    }

    public void validateEventMetadataInjectionPoint(InjectionPoint ip) {
        if (EventMetadata.class.equals(ip.getType()) && ip.getQualifiers().contains(DefaultLiteral.INSTANCE)) {
            throw new DefinitionException(EVENT_METADATA_INJECTED_OUTSIDE_OF_OBSERVER, ip);
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
            throw new DeploymentException(INJECTION_POINT_HAS_UNSATISFIED_DEPENDENCIES,
                ij,
                Formats.formatAnnotations(ij.getQualifiers()),
                Formats.formatInjectionPointType(ij.getType()),
                Formats.formatAsStackTraceElement(ij),
                getUnsatisfiedDependenciesAdditionalInfo(ij, beanManager));
        }
        if (resolvedBeans.size() > 1) {
            throw new DeploymentException(INJECTION_POINT_HAS_AMBIGUOUS_DEPENDENCIES,
                ij,
                Formats.formatAnnotations(ij.getQualifiers()),
                Formats.formatInjectionPointType(ij.getType()),
                Formats.formatAsStackTraceElement(ij),
                WeldCollections.toMultiRowString(resolvedBeans));
        }
        // Account for the case this is disabled decorator
        if (!resolvedBeans.isEmpty()) {
            Bean<?> resolvedBean = (Bean<?>) resolvedBeans.iterator().next();
            if (beanManager.isNormalScope(resolvedBean.getScope())) {
                UnproxyableResolutionException ue = Proxies.getUnproxyableTypeException(ij.getType(), resolvedBean);
                if (ue != null) {
                    throw new DeploymentException(INJECTION_POINT_HAS_NON_PROXYABLE_DEPENDENCIES, ue, ij);
                }
            }
            if (bean != null && Beans.isPassivatingScope(bean, beanManager)) {
                validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
            }
        }
    }

    private String getUnsatisfiedDependenciesAdditionalInfo(InjectionPoint ij, BeanManagerImpl beanManager) {
        Set<Bean<?>> beansMatchedByType = beanManager.getBeans(ij.getType());
        if (beansMatchedByType.isEmpty()) {
            Class<?> rawType = Reflections.getRawType(ij.getType());
            if (rawType != null) {
                String missingDependency = beanManager.getMissingDependencyForClass(rawType.getName());
                if (missingDependency != null) {
                    return loggerFactory().getMessageConveyor().getMessage(
                        UNSATISFIED_DEPENDENCY_BECAUSE_CLASS_IGNORED,
                        rawType.getName(),
                        missingDependency);
                }
            }
        } else {
            return loggerFactory().getMessageConveyor().getMessage(
                UNSATISFIED_DEPENDENCY_BECAUSE_QUALIFIERS_DONT_MATCH,
                WeldCollections.toMultiRowString(beansMatchedByType));
        }
        return "";
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
        if (!Beans.isPassivationCapableDependency(resolvedBean)) {
            if (((ij.getMember() instanceof Field) && ij.isTransient())) {
                return;
            }
            if (ij.getAnnotated() instanceof AnnotatedParameter<?> && ij.getAnnotated().isAnnotationPresent(TransientReference.class)) {
                return;
            }
            throw new UnserializableDependencyException(INJECTION_POINT_HAS_NON_SERIALIZABLE_DEPENDENCY, ij.getBean(), resolvedBean);
        }
    }

    public void validateDeployment(BeanManagerImpl manager, BeanDeployment deployment) {
        validateDecorators(manager.getDecorators(), manager);
        validateInterceptors(manager.getInterceptors(), manager);
        validateBeans(manager.getBeans(), manager);
        validateEnabledDecoratorClasses(manager, deployment);
        validateEnabledInterceptorClasses(manager, deployment);
        validateEnabledAlternativeStereotypes(manager, deployment);
        validateEnabledAlternativeClasses(manager, deployment);
        validateSpecialization(manager);
        validateDisposalMethods(deployment.getBeanDeployer().getEnvironment());
        validateObserverMethods(deployment.getBeanDeployer().getEnvironment().getObservers(), manager);
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
        final Set<CommonBean<?>> specializedBeans = new HashSet<CommonBean<?>>();

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

    protected void validateBean(Bean<?> bean, Collection<CommonBean<?>> specializedBeans, BeanManagerImpl manager, List<RuntimeException> problems) {
        try {
            if (bean instanceof CommonBean<?>) {
                validateRIBean((CommonBean<?>) bean, manager, specializedBeans);
            } else {
                validateCustomBean(bean, manager);
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
        Set<CommonBean<?>> specializedBeans = new HashSet<CommonBean<?>>();
        for (Decorator<?> decorator : decorators) {
            validateDecorator(decorator, specializedBeans, manager);
        }
    }

    protected void validateDecorator(Decorator<?> decorator, Collection<CommonBean<?>> specializedBeans, BeanManagerImpl manager) {

        if (decorator.getDecoratedTypes().isEmpty()) {
            throw new DefinitionException(ValidatorMessage.NO_DECORATED_TYPES, decorator);
        }

        Decorators.checkDelegateType(decorator);

        /*
         * Validate decorators of facade built-in beans
         */
        Type delegateType = decorator.getDelegateType();
        if (delegateType instanceof ParameterizedType) {
            ParameterizedType parameterizedDelegateType = (ParameterizedType) delegateType;
            if (!Decorators.isPassivationCapable(decorator)) {
                if (Instance.class.equals(parameterizedDelegateType.getRawType()) || Provider.class.equals(parameterizedDelegateType.getRawType())) {
                    throw new UnserializableDependencyException(BUILTIN_BEAN_WITH_NONSERIALIZABLE_DECORATOR, decorator, Instance.class.getName());
                }
                if (Event.class.equals(parameterizedDelegateType.getRawType())) {
                    throw new UnserializableDependencyException(BUILTIN_BEAN_WITH_NONSERIALIZABLE_DECORATOR, decorator, Event.class.getName());
                }
            }
        }

        if (decorator instanceof WeldDecorator<?>) {

            EnhancedAnnotatedType<?> annotated = ((WeldDecorator<?>) decorator).getEnhancedAnnotated();

            if (decorator instanceof DecoratorImpl<?>) {
                // Discovered decorator bean - abstract methods and delegate injection point are validated during bean initialization
                validateRIBean((CommonBean<?>) decorator, manager, specializedBeans);

                // Following checks are not legal for custom decorator beans as we cannot rely on decorator bean class methods
                if (!BeanMethods.getObserverMethods(annotated).isEmpty()) {
                    throw new DefinitionException(DECORATORS_CANNOT_HAVE_OBSERVER_METHODS, decorator);
                }
                while (annotated != null && annotated.getJavaClass() != Object.class) {
                    if (!annotated.getDeclaredEnhancedMethods(Produces.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_METHODS, decorator);
                    }
                    if (!annotated.getDeclaredEnhancedFields(Produces.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_PRODUCER_FIELDS, decorator);
                    }
                    if (!annotated.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                        throw new DefinitionException(DECORATORS_CANNOT_HAVE_DISPOSER_METHODS, decorator);
                    }
                    annotated = annotated.getEnhancedSuperclass();
                }

            } else {
                // Custom decorator bean
                validateGeneralBean(decorator, manager);
                Decorators.findDelegateInjectionPoint(annotated, decorator.getInjectionPoints());
            }
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
            Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledBeans(namedAccessibleBeans.get(name), beanManager, registry));
            if (resolvedBeans.size() > 1) {
                throw new DeploymentException(AMBIGUOUS_EL_NAME, name, resolvedBeans);
            }
            if (accessibleNamespaces.contains(name)) {
                throw new DeploymentException(BEAN_NAME_IS_PREFIX, name);
            }
        }
    }

    private void validateEnabledInterceptorClasses(BeanManagerImpl beanManager, BeanDeployment deployment) {
        BeansXml beansXml = deployment.getBeanDeploymentArchive().getBeansXml();
        if (beansXml != null && !beansXml.getEnabledInterceptors().isEmpty()) {
            Set<String> interceptorBeanClasses = new HashSet<String>();
            for (Interceptor<?> interceptor : beanManager.getAccessibleInterceptors()) {
                interceptorBeanClasses.add(interceptor.getBeanClass().getName());
            }
            for (Metadata<String> interceptorClassName : beansXml.getEnabledInterceptors()) {
                if (!interceptorBeanClasses.contains(interceptorClassName.getValue())) {
                    throw new DeploymentException(INTERCEPTOR_NOT_ANNOTATED_OR_REGISTERED, interceptorClassName);
                }
            }
        }
    }

    private void validateEnabledDecoratorClasses(BeanManagerImpl beanManager, BeanDeployment deployment) {
        BeansXml beansXml = deployment.getBeanDeploymentArchive().getBeansXml();
        if (beansXml != null && !beansXml.getEnabledDecorators().isEmpty()) {
            Set<String> decoratorBeanClasses = new HashSet<String>();
            for (Decorator<?> bean : beanManager.getAccessibleDecorators()) {
                decoratorBeanClasses.add(bean.getBeanClass().getName());
            }
            for (Metadata<String> decoratorClassName : beansXml.getEnabledDecorators()) {
                if (!decoratorBeanClasses.contains(decoratorClassName.getValue())) {
                    throw new DeploymentException(DECORATOR_CLASS_NOT_BEAN_CLASS_OF_DECORATOR, decoratorClassName, WeldCollections.toMultiRowString(decoratorBeanClasses));
                }
            }
        }
    }

    private void validateEnabledAlternativeStereotypes(BeanManagerImpl beanManager, BeanDeployment deployment) {
        BeansXml beansXml = deployment.getBeanDeploymentArchive().getBeansXml();
        if (beansXml != null && !beansXml.getEnabledAlternativeStereotypes().isEmpty()) {
            // prepare lookup structure

            Map<String, Class<? extends Annotation>> loadedStereotypes = buildClassNameMap(beanManager.getEnabled().getAlternativeStereotypes());

            for (Metadata<String> definition : beansXml.getEnabledAlternativeStereotypes()) {
                Class<? extends Annotation> stereotype = loadedStereotypes.get(definition.getValue());
                if (!beanManager.isStereotype(stereotype)) {
                    throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE, definition);
                }
                if (!isAlternative(beanManager, stereotype)) {
                    throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_ANNOTATED, definition);
                }
            }
        }
    }

    private void validateEnabledAlternativeClasses(BeanManagerImpl beanManager, BeanDeployment deployment) {
        BeansXml beansXml = deployment.getBeanDeploymentArchive().getBeansXml();
        if (beansXml != null && !beansXml.getEnabledAlternativeClasses().isEmpty()) {

            // prepare lookup structure
            Map<String, Class<?>> loadedClasses = buildClassNameMap(beanManager.getEnabled().getAlternativeClasses());

            // lookup structure for validation of alternatives
            Multimap<Class<?>, Bean<?>> beansByClass = HashMultimap.create();
            for (Bean<?> bean : beanManager.getAccessibleBeans()) {
                if (!(bean instanceof NewBean)) {
                    beansByClass.put(bean.getBeanClass(), bean);
                }
            }
            for (Metadata<String> definition : beansXml.getEnabledAlternativeClasses()) {
                Class<?> enabledClass = loadedClasses.get(definition.getValue());
                if (enabledClass.isAnnotation() || enabledClass.isInterface()) {
                    throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_CLASS, definition);
                } else {
                    // check that the class is a bean class of at least one alternative
                    boolean alternativeBeanFound = false;
                    for (Bean<?> bean : beansByClass.get(enabledClass)) {
                        if (bean.isAlternative()) {
                            alternativeBeanFound = true;
                        }
                    }
                    if (!alternativeBeanFound) {
                        throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_ANNOTATED, definition);
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
            throw new DefinitionException(DISPOSAL_METHODS_WITHOUT_PRODUCER, WeldCollections.toMultiRowString(beans));
        }
    }

    protected void validateObserverMethods(Iterable<ObserverInitializationContext<?, ?>> observers, BeanManagerImpl beanManager) {
        for (ObserverInitializationContext<?, ?> omi : observers) {
            for (InjectionPoint ip : omi.getObserver().getInjectionPoints()) {
                validateInjectionPointForDefinitionErrors(ip, ip.getBean(), beanManager);
                validateMetadataInjectionPoint(ip, null, INJECTION_INTO_NON_BEAN);
                validateInjectionPointForDeploymentProblems(ip, ip.getBean(), beanManager);
            }
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
            throw new DeploymentException(PSEUDO_SCOPED_BEAN_HAS_CIRCULAR_REFERENCES, WeldCollections.toMultiRowString(realDependencyPath));
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
        if (bean instanceof DecorableBean<?>) {
            final List<Decorator<?>> decorators = Reflections.<DecorableBean<?>>cast(bean).getDecorators();
            if (!decorators.isEmpty()) {
                for (final Decorator<?> decorator : decorators) {
                    reallyValidatePseudoScopedBean(decorator, beanManager, dependencyPath, validatedBeans);
                }
            }
        }
        if (bean instanceof AbstractProducerBean<?, ?, ?> && !(bean instanceof EEResourceProducerField<?, ?>)) {
            AbstractProducerBean<?, ?, ?> producer = (AbstractProducerBean<?, ?, ?>) bean;
            if (!beanManager.isNormalScope(producer.getDeclaringBean().getScope()) && !producer.getAnnotated().isStatic()) {
                reallyValidatePseudoScopedBean(producer.getDeclaringBean(), beanManager, dependencyPath, validatedBeans);
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
                    boolean normalScoped = beanManager.isNormalScope(bean.getScope());
                    if (!normalScoped) {
                        reallyValidatePseudoScopedBean(bean, beanManager, dependencyPath, validatedBeans);
                    }
                }
            }
        }
    }

    @Override
    public void cleanup() {
    }

}
