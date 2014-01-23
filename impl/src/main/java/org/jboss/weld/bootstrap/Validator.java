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
import javax.enterprise.inject.spi.InjectionTarget;
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
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.injection.producer.AbstractMemberProducer;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.interceptor.reader.PlainInterceptorFactory;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DecoratedLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.InterceptedLiteral;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.LogMessageCallback;
import org.jboss.weld.logging.ValidatorLogger;
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

    protected void validateGeneralBean(Bean<?> bean, BeanManagerImpl beanManager) {
        for (InjectionPoint ij : bean.getInjectionPoints()) {
            validateInjectionPoint(ij, beanManager);
        }
        boolean normalScoped = beanManager.isNormalScope(bean.getScope());
        /*
         * Named beans are validated eagerly. If a bean is not named, it is validated for proxyability based on discovered
         * injection points.
         */
        if (normalScoped && bean.getName() != null && !Beans.isBeanProxyable(bean, beanManager)) {
            UnproxyableResolutionException ue = Proxies.getUnproxyableTypesException(bean, beanManager.getServices());
            if (ue != null) {
                throw new DeploymentException(ue);
            }
        }
        if (!normalScoped) {
            validatePseudoScopedBean(bean, beanManager);
        }

        if (beanManager.isPassivatingScope(bean.getScope()) && !Beans.isPassivationCapableBean(bean)) {
            throw ValidatorLogger.LOG.beanWithPassivatingScopeNotPassivationCapable(bean);
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
                        validateMetadataInjectionPoint(ip, null, ValidatorLogger.INJECTION_INTO_DISPOSER_METHOD_CALLBACK);
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
            ValidatorLogger.LOG.beanNotPassivationCapable(bean);
        }
    }

    private void validateInterceptors(BeanManagerImpl beanManager, AbstractClassBean<?> classBean) {
        InterceptionModel<?> interceptionModel = beanManager.getInterceptorModelRegistry().get(classBean.getAnnotated());
        if (interceptionModel != null) {
            Set<? extends InterceptorClassMetadata<?>> interceptors = interceptionModel.getAllInterceptors();
            if (interceptors.size() > 0) {
                boolean passivationCapabilityCheckRequired = beanManager.isPassivatingScope(classBean.getScope());
                for (InterceptorClassMetadata<?> interceptorMetadata : interceptors) {
                    // in the case of CDI interceptors we only need to additionally validate passivation capability (if required)
                    if (interceptorMetadata.getInterceptorFactory() instanceof CdiInterceptorFactory<?> && passivationCapabilityCheckRequired) {
                        CdiInterceptorFactory<?> cdiInterceptorFactory = (CdiInterceptorFactory<?>) interceptorMetadata.getInterceptorFactory();
                        Interceptor<?> interceptor = cdiInterceptorFactory.getInterceptor();

                        boolean isSerializable = (interceptor instanceof InterceptorImpl) ? ((InterceptorImpl<?>) interceptor).isSerializable() : Beans.isPassivationCapableDependency(interceptor);
                        if (!isSerializable) {
                            throw ValidatorLogger.LOG.passivatingBeanWithNonserializableInterceptor(classBean, interceptor);
                        }
                        if (interceptor instanceof InterceptorImpl) {
                            beanManager = ((InterceptorImpl<?>) interceptor).getBeanManager();
                        }
                        for (InjectionPoint injectionPoint : interceptor.getInjectionPoints()) {
                            Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                            validateInterceptorDecoratorInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager, classBean);
                        }
                    }
                    if (interceptorMetadata.getInterceptorFactory() instanceof PlainInterceptorFactory<?>) {
                        PlainInterceptorFactory<?> factory = (PlainInterceptorFactory<?>) interceptorMetadata.getInterceptorFactory();
                        Class<?> interceptorClass = interceptorMetadata.getJavaClass();
                        if (passivationCapabilityCheckRequired && !Reflections.isSerializable(interceptorClass)) {
                            throw ValidatorLogger.LOG.passivatingBeanWithNonserializableInterceptor(this, interceptorClass.getName());
                        }
                        // if we can't get to the interceptor's BeanManager, we will use the bean's BM instead
                        InjectionTarget<?> injectionTarget = factory.getInjectionTarget();
                        if (injectionTarget instanceof BasicInjectionTarget<?>) {
                            beanManager = ((BasicInjectionTarget<?>) injectionTarget).getBeanManager();
                        }
                        for (InjectionPoint injectionPoint : factory.getInjectionTarget().getInjectionPoints()) {
                            validateInjectionPoint(injectionPoint, beanManager);
                            if (passivationCapabilityCheckRequired) {
                                Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(injectionPoint));
                                validateInterceptorDecoratorInjectionPointPassivationCapable(injectionPoint, resolvedBean, beanManager, classBean);
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
                    throw ValidatorLogger.LOG.builtinBeanWithNonserializableDecorator(decorator, bean);
                } else {
                    throw ValidatorLogger.LOG.passivatingBeanWithNonserializableDecorator(bean, decorator);
                }
            }
            if (decorator instanceof DecoratorImpl) {
                beanManager = ((DecoratorImpl<?>) decorator).getBeanManager();
            }
            for (InjectionPoint ij : decorator.getInjectionPoints()) {
                if (!ij.isDelegate()) {
                    Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(ij));
                    validateInterceptorDecoratorInjectionPointPassivationCapable(ij, resolvedBean, beanManager, bean);
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
        validateMetadataInjectionPoint(ij, ij.getBean(), ValidatorLogger.INJECTION_INTO_NON_DEPENDENT_BEAN_CALLBACK);
        validateEventMetadataInjectionPoint(ij);
        validateInjectionPointForDeploymentProblems(ij, ij.getBean(), beanManager);
    }

    /**
     * Checks for definition errors associated with a given {@link InjectionPoint}
     */
    public void validateInjectionPointForDefinitionErrors(InjectionPoint ij, Bean<?> bean, BeanManagerImpl beanManager) {
        if (ij.getAnnotated().getAnnotation(New.class) != null && ij.getQualifiers().size() > 1) {
            throw ValidatorLogger.LOG.newWithQualifiers(ij);
            // throw new DefinitionException(NEW_WITH_QUALIFIERS, ij);
        }
        if (ij.getType() instanceof TypeVariable<?>) {
            throw ValidatorLogger.LOG.injectionPointWithTypeVariable(ij);
        }
        if (!(ij.getMember() instanceof Field) && ij.getAnnotated().isAnnotationPresent(Named.class) && ij.getAnnotated().getAnnotation(Named.class).value().equals("")) {
            throw ValidatorLogger.LOG.nonFieldInjectionPointCannotUseNamed(ij);
        }
        if (ij.getAnnotated().isAnnotationPresent(Produces.class)) {
            if (bean != null) {
                throw BeanLogger.LOG.injectedFieldCannotBeProducer(ij.getAnnotated(), bean);
            } else {
                throw BeanLogger.LOG.injectedFieldCannotBeProducer(ij.getAnnotated(), Reflections.<AnnotatedField<?>>cast(ij.getAnnotated()).getDeclaringType());
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
                throw ValidatorLogger.LOG.userTransactionInjectionIntoBeanWithContainerManagedTransactions(ij);
            }
        }
    }

    public void validateMetadataInjectionPoint(InjectionPoint ij, Bean<?> bean, LogMessageCallback messageCallback) {
        // metadata injection points
        if (ij.getType().equals(InjectionPoint.class) && bean == null) {
            throw new DefinitionException(messageCallback.invoke(ij));
        }
        if (ij.getType().equals(InjectionPoint.class) && !Dependent.class.equals(bean.getScope())) {
            throw new DefinitionException(ValidatorLogger.LOG.injectionIntoNonDependentBean(ij));
        }
        Class<?> rawType = Reflections.getRawType(ij.getType());
        if (Bean.class.equals(rawType) || Interceptor.class.equals(rawType) || Decorator.class.equals(rawType)) {
            if (bean == null) {
                throw new DefinitionException(messageCallback.invoke(ij));
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
            throw ValidatorLogger.LOG.eventMetadataInjectedOutsideOfObserver(ip);
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
            throw ValidatorLogger.LOG.injectionPointHasUnsatisfiedDependencies(
                ij,
                Formats.formatAnnotations(ij.getQualifiers()),
                Formats.formatInjectionPointType(ij.getType()),
                Formats.formatAsStackTraceElement(ij),
                getUnsatisfiedDependenciesAdditionalInfo(ij, beanManager));
        }
        if (resolvedBeans.size() > 1) {
            throw ValidatorLogger.LOG.injectionPointHasAmbiguousDependencies(
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
                UnproxyableResolutionException ue = Proxies.getUnproxyableTypeException(ij.getType(), resolvedBean, beanManager.getServices());
                if (ue != null) {
                    throw ValidatorLogger.LOG.injectionPointHasNonProxyableDependencies(ij, ue);
                }
            }
            if (bean != null && Beans.isPassivatingScope(bean, beanManager)) {
                validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
            }
        }
    }

    private String getUnsatisfiedDependenciesAdditionalInfo(InjectionPoint ij, BeanManagerImpl beanManager) {
        Set<Bean<?>> beansMatchedByType = beanManager.getBeans(ij.getType(), AnyLiteral.INSTANCE);
        if (beansMatchedByType.isEmpty()) {
            Class<?> rawType = Reflections.getRawType(ij.getType());
            if (rawType != null) {
                MissingDependenciesRegistry missingDependenciesRegistry = beanManager.getServices().get(MissingDependenciesRegistry.class);
                String missingDependency = missingDependenciesRegistry.getMissingDependencyForClass(rawType.getName());
                if (missingDependency != null) {
                    return ValidatorLogger.LOG.unsatisfiedDependencyBecauseClassIgnored(
                        rawType.getName(),
                        missingDependency);
                }
            }
        } else {
            return ValidatorLogger.LOG.unsatisfiedDependencyBecauseQualifiersDontMatch(
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
        ValidatorLogger.LOG.scopeAnnotationOnInjectionPoint(annotation, ij);
    }

    private boolean hasScopeMetaAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotationType.isAnnotationPresent(Scope.class) || annotationType.isAnnotationPresent(NormalScope.class);
    }

    private boolean isInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager) {
        if (!Beans.isPassivationCapableDependency(resolvedBean)) {
            if (((ij.getMember() instanceof Field) && ij.isTransient())) {
                return true;
            }
            if (ij.getAnnotated() instanceof AnnotatedParameter<?> && ij.getAnnotated().isAnnotationPresent(TransientReference.class)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager) {
        if (!isInjectionPointPassivationCapable(ij, resolvedBean, beanManager)) {
            throw ValidatorLogger.LOG.injectionPointHasNonSerializableDependency(ij.getBean(), resolvedBean);
        }
    }

    public void validateInterceptorDecoratorInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager, Bean<?> bean) {
        if (!isInjectionPointPassivationCapable(ij, resolvedBean, beanManager)) {
            throw ValidatorLogger.LOG.interceptorDecoratorInjectionPointHasNonSerializableDependency(bean, ij.getBean(), resolvedBean);
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
                throw ValidatorLogger.LOG.beanSpecializedTooManyTimes(entry.getElement());
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
                throw ValidatorLogger.LOG.interceptorsCannotHaveObserverMethods(interceptor);
            }
            if (!interceptor.getScope().equals(Dependent.class)) {
                throw ValidatorLogger.LOG.interceptorMustBeDependent(interceptor);
            }
            while (annotated != null && annotated.getJavaClass() != Object.class) {
                if (!annotated.getDeclaredEnhancedMethods(Produces.class).isEmpty()) {
                    throw ValidatorLogger.LOG.interceptorsCannotHaveProducerMethods(interceptor);
                }
                if (!annotated.getDeclaredEnhancedFields(Produces.class).isEmpty()) {
                    throw ValidatorLogger.LOG.interceptorsCannotHaveProducerFields(interceptor);
                }
                if (!annotated.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                    throw ValidatorLogger.LOG.interceptorsCannotHaveDisposerMethods(interceptor);
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
            throw ValidatorLogger.LOG.noDecoratedTypes(decorator);
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
                    throw ValidatorLogger.LOG.builtinBeanWithNonserializableDecorator(decorator, Instance.class.getName());
                }
                if (Event.class.equals(parameterizedDelegateType.getRawType())) {
                    throw ValidatorLogger.LOG.builtinBeanWithNonserializableDecorator(decorator, Event.class.getName());
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
                    throw ValidatorLogger.LOG.decoratorsCannotHaveObserverMethods(decorator);
                }
                while (annotated != null && annotated.getJavaClass() != Object.class) {
                    if (!annotated.getDeclaredEnhancedMethods(Produces.class).isEmpty()) {
                        throw ValidatorLogger.LOG.decoratorsCannotHaveProducerMethods(decorator);
                    }
                    if (!annotated.getDeclaredEnhancedFields(Produces.class).isEmpty()) {
                        throw ValidatorLogger.LOG.decoratorsCannotHaveProducerFields(decorator);
                    }
                    if (!annotated.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class).isEmpty()) {
                        throw ValidatorLogger.LOG.decoratorsCannotHaveDisposerMethods(decorator);
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
                throw ValidatorLogger.LOG.ambiguousElName(name, resolvedBeans);
            }
            if (accessibleNamespaces.contains(name)) {
                throw ValidatorLogger.LOG.beanNameIsPrefix(name);
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
                    throw ValidatorLogger.LOG.interceptorClassDoesNotMatchInterceptorBean(interceptorClassName);
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
                    throw ValidatorLogger.LOG.decoratorClassNotBeanClassOfDecorator(decoratorClassName, WeldCollections.toMultiRowString(decoratorBeanClasses));
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
                    throw ValidatorLogger.LOG.alternativeStereotypeNotStereotype(definition);
                }
                if (!isAlternative(beanManager, stereotype)) {
                    throw ValidatorLogger.LOG.alternativeStereotypeNotAnnotated(definition);
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
                    throw ValidatorLogger.LOG.alternativeBeanClassNotClass(definition);
                } else {
                    // check that the class is a bean class of at least one alternative
                    boolean alternativeBeanFound = false;
                    for (Bean<?> bean : beansByClass.get(enabledClass)) {
                        if (bean.isAlternative()) {
                            alternativeBeanFound = true;
                        }
                    }
                    if (!alternativeBeanFound) {
                        throw ValidatorLogger.LOG.alternativeBeanClassNotAnnotated(definition);
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
            throw ValidatorLogger.LOG.disposalMethodsWithoutProducer(WeldCollections.toMultiRowString(beans));
        }
    }

    protected void validateObserverMethods(Iterable<ObserverInitializationContext<?, ?>> observers, BeanManagerImpl beanManager) {
        for (ObserverInitializationContext<?, ?> omi : observers) {
            for (InjectionPoint ip : omi.getObserver().getInjectionPoints()) {
                validateInjectionPointForDefinitionErrors(ip, ip.getBean(), beanManager);
                validateMetadataInjectionPoint(ip, null, ValidatorLogger.INJECTION_INTO_NON_DEPENDENT_BEAN_CALLBACK);
                validateInjectionPointForDeploymentProblems(ip, ip.getBean(), beanManager);
            }
        }
    }

    private static void checkFacadeInjectionPoint(InjectionPoint injectionPoint, Class<?> type) {
        Type injectionPointType = injectionPoint.getType();
        if (injectionPointType instanceof Class<?> && type.equals(injectionPointType)) {
            throw ValidatorLogger.LOG.injectionPointMustHaveTypeParameter(type, injectionPoint);
        }
        if (injectionPointType instanceof ParameterizedType && !injectionPoint.isDelegate()) {
            ParameterizedType parameterizedType = (ParameterizedType) injectionPointType;
            if (type.equals(parameterizedType.getRawType())) {
                if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable<?>) {
                    throw ValidatorLogger.LOG.injectionPointWithTypeVariable(injectionPoint);
                }
                if (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType) {
                    throw ValidatorLogger.LOG.injectionPointHasWildcard(type, injectionPoint);
                }
            }
        }
    }

    public static void checkBeanMetadataInjectionPoint(Object bean, InjectionPoint ip, Type expectedTypeArgument) {
        if (!(ip.getType() instanceof ParameterizedType)) {
            throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
        }
        ParameterizedType parameterizedType = (ParameterizedType) ip.getType();
        if (parameterizedType.getActualTypeArguments().length != 1) {
            throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
        }
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type typeArgument = parameterizedType.getActualTypeArguments()[0];

        if (bean == null) {
            throw new DefinitionException(ValidatorLogger.LOG.injectionIntoNonBean(ip));
        }
        /*
         * If an Interceptor instance is injected into a bean instance other than an interceptor instance, the container
         * automatically detects the problem and treats it as a definition error.
         */
        if (rawType.equals(Interceptor.class) && !(bean instanceof Interceptor<?>)) {
            throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
        }
        /*
         * If a Decorator instance is injected into a bean instance other than a decorator instance, the container automatically
         * detects the problem and treats it as a definition error.
         */
        if (rawType.equals(Decorator.class) && !(bean instanceof Decorator<?>)) {
            throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
        }
        Set<Annotation> qualifiers = ip.getQualifiers();
        if (qualifiers.contains(InterceptedLiteral.INSTANCE)) {
            /*
             * If a Bean instance with qualifier @Intercepted is injected into a bean instance other than an interceptor
             * instance, the container automatically detects the problem and treats it as a definition error.
             */
            if (!(bean instanceof Interceptor<?>)) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointQualifier(Intercepted.class, Interceptor.class, ip);
            }
            /*
             * If the injection point is a field, an initializer method parameter or a bean constructor of an interceptor, with
             * qualifier @Intercepted, then the type parameter of the injected Bean must be an unbounded wildcard.
             */
            if (!rawType.equals(Bean.class)) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
            }
            if (!Reflections.isUnboundedWildcard(typeArgument)) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointTypeArgument(typeArgument, ip);
            }
        }
        if (qualifiers.contains(DecoratedLiteral.INSTANCE)) {
            /*
             * If a Bean instance with qualifier @Decorated is injected into a bean instance other than a decorator instance,
             * the container automatically detects the problem and treats it as a definition error.
             */
            if (!(bean instanceof Decorator<?>)) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointQualifier(Decorated.class, Decorator.class, ip);
            }
            Decorator<?> decorator = Reflections.cast(bean);
            /*
             * If the injection point is a field, an initializer method parameter or a bean constructor of a decorator, with
             * qualifier @Decorated, then the type parameter of the injected Bean must be the same as the delegate type.
             */
            if (!rawType.equals(Bean.class)) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointType(ip.getType(), ip);
            }
            if (!typeArgument.equals(decorator.getDelegateType())) {
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointTypeArgument(typeArgument, ip);
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
                throw ValidatorLogger.LOG.invalidBeanMetadataInjectionPointTypeArgument(typeArgument, ip);
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
     * checks if a bean has been seen before in the dependencyPath. If not, it
     * resolves the InjectionPoints and adds the resolved beans to the set of
     * beans to be validated
     */
    private static void reallyValidatePseudoScopedBean(Bean<?> bean, BeanManagerImpl beanManager, Set<Object> dependencyPath, Set<Bean<?>> validatedBeans) {
        // see if we have already seen this bean in the dependency path
        if (dependencyPath.contains(bean)) {
            // create a list that shows the path to the bean
            List<Object> realDependencyPath = new ArrayList<Object>(dependencyPath);
            realDependencyPath.add(bean);
            throw ValidatorLogger.LOG.pseudoScopedBeanHasCircularReferences(WeldCollections.toMultiRowString(realDependencyPath));
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
