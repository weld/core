/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.ACCESSIBLE_BDAS;
import static org.jboss.weld.probe.Strings.ALTERNATIVES;
import static org.jboss.weld.probe.Strings.ANNOTATED_METHOD;
import static org.jboss.weld.probe.Strings.APPLICATION;
import static org.jboss.weld.probe.Strings.AS_STRING;
import static org.jboss.weld.probe.Strings.BDAS;
import static org.jboss.weld.probe.Strings.BDA_ID;
import static org.jboss.weld.probe.Strings.BEANS;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.BEAN_DISCOVERY_MODE;
import static org.jboss.weld.probe.Strings.CHILDREN;
import static org.jboss.weld.probe.Strings.CIDS;
import static org.jboss.weld.probe.Strings.CLASS;
import static org.jboss.weld.probe.Strings.CONFIGURATION;
import static org.jboss.weld.probe.Strings.CONTAINER;
import static org.jboss.weld.probe.Strings.CONTEXTS;
import static org.jboss.weld.probe.Strings.CONTEXT_ID;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DECLARED_OBSERVERS;
import static org.jboss.weld.probe.Strings.DECLARED_PRODUCERS;
import static org.jboss.weld.probe.Strings.DECLARING_BEAN;
import static org.jboss.weld.probe.Strings.DECLARING_CLASS;
import static org.jboss.weld.probe.Strings.DECORATORS;
import static org.jboss.weld.probe.Strings.DEFAULT_VALUE;
import static org.jboss.weld.probe.Strings.DEPENDENCIES;
import static org.jboss.weld.probe.Strings.DEPENDENTS;
import static org.jboss.weld.probe.Strings.DESCRIPTION;
import static org.jboss.weld.probe.Strings.DISPOSAL_METHOD;
import static org.jboss.weld.probe.Strings.EJB_NAME;
import static org.jboss.weld.probe.Strings.ENABLEMENT;
import static org.jboss.weld.probe.Strings.EVENT_INFO;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.INSTANCES;
import static org.jboss.weld.probe.Strings.INTERCEPTED_BEAN;
import static org.jboss.weld.probe.Strings.INTERCEPTORS;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.LAST_PAGE;
import static org.jboss.weld.probe.Strings.METHOD;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.NAME;
import static org.jboss.weld.probe.Strings.OBSERVED_TYPE;
import static org.jboss.weld.probe.Strings.OBSERVERS;
import static org.jboss.weld.probe.Strings.PAGE;
import static org.jboss.weld.probe.Strings.PRIORITY;
import static org.jboss.weld.probe.Strings.PRIORITY_RANGE;
import static org.jboss.weld.probe.Strings.PROBE_COMPONENT;
import static org.jboss.weld.probe.Strings.PRODUCER_FIELD;
import static org.jboss.weld.probe.Strings.PRODUCER_INFO;
import static org.jboss.weld.probe.Strings.PRODUCER_METHOD;
import static org.jboss.weld.probe.Strings.PROPERTIES;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.Strings.RECEPTION;
import static org.jboss.weld.probe.Strings.REQUIRED_TYPE;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.Strings.SESSION_BEAN_TYPE;
import static org.jboss.weld.probe.Strings.START;
import static org.jboss.weld.probe.Strings.STEREOTYPES;
import static org.jboss.weld.probe.Strings.TIME;
import static org.jboss.weld.probe.Strings.TIMESTAMP;
import static org.jboss.weld.probe.Strings.TOTAL;
import static org.jboss.weld.probe.Strings.TX_PHASE;
import static org.jboss.weld.probe.Strings.TYPE;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.Strings.VALUE;
import static org.jboss.weld.probe.Strings.VERSION;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.context.AbstractConversationContext;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.experimental.Prioritized;
import org.jboss.weld.injection.producer.ProducerFieldProducer;
import org.jboss.weld.injection.producer.ProducerMethodProducer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.Components.Dependency;
import org.jboss.weld.probe.Json.JsonArrayBuilder;
import org.jboss.weld.probe.Json.JsonObjectBuilder;
import org.jboss.weld.probe.ProbeObserver.EventInfo;
import org.jboss.weld.probe.Queries.Page;
import org.jboss.weld.probe.Resource.Representation;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.collections.Sets;
import org.jboss.weld.util.reflection.Formats;

/**
 * Lots of utility methods for creating JSON data.
 *
 * @author Martin Kouba
 */
final class JsonObjects {

    private JsonObjects() {
    }

    /**
     *
     * @param beanManager
     * @return the root resource representation
     */
    static String createDeploymentJson(BeanManagerImpl beanManager, Probe probe) {

        Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchivesMap = Container.instance(beanManager).beanDeploymentArchives();
        JsonObjectBuilder deploymentBuilder = Json.objectBuilder();

        // WELD VERSION
        deploymentBuilder.add(VERSION, Formats.version(WeldBootstrap.class.getPackage()));

        // BEAN DEPLOYMENT ARCHIVES
        JsonArrayBuilder bdasBuilder = Json.arrayBuilder();
        List<BeanDeploymentArchive> bdas = new ArrayList<BeanDeploymentArchive>(beanDeploymentArchivesMap.keySet());
        Collections.sort(bdas, probe.getBdaComparator());
        for (BeanDeploymentArchive bda : bdas) {
            JsonObjectBuilder bdaBuilder = createSimpleBdaJson(bda.getId());
            // If beans.xml is not found it's likely an implicit bean archive
            BeansXml beansXml = bda.getBeansXml();
            bdaBuilder.add(BEAN_DISCOVERY_MODE, beansXml != null ? beansXml.getBeanDiscoveryMode().toString() : BeanDiscoveryMode.ANNOTATED.toString());
            // Enablement - interceptors, decorators, alternatives
            JsonObjectBuilder enablementBuilder = Json.objectBuilder(true);
            ModuleEnablement enablement = beanDeploymentArchivesMap.get(bda).getEnabled();
            JsonArrayBuilder interceptors = Json.arrayBuilder();
            for (Class<?> interceptor : Components.getSortedProbeComponetCandidates(enablement.getInterceptors())) {
                interceptors.add(decorateProbeComponent(interceptor, createSimpleBeanJson(findEnabledBean(interceptor, BeanKind.INTERCEPTOR, probe), probe)));
            }
            enablementBuilder.add(INTERCEPTORS, interceptors);
            JsonArrayBuilder decorators = Json.arrayBuilder();
            for (Class<?> decorator : enablement.getDecorators()) {
                decorators.add(createSimpleBeanJson(findEnabledBean(decorator, BeanKind.DECORATOR, probe), probe));
            }
            enablementBuilder.add(DECORATORS, decorators);
            JsonArrayBuilder alternatives = Json.arrayBuilder();
            for (Class<?> clazz : Sets.union(enablement.getAlternativeClasses(), enablement.getGlobalAlternatives())) {
                alternatives.add(createSimpleBeanJson(findAlternativeBean(clazz, probe), probe));
            }
            for (Class<? extends Annotation> stereotype : enablement.getAlternativeStereotypes()) {
                Set<Bean<?>> beans = findAlternativeStereotypeBeans(stereotype, probe);
                if (!beans.isEmpty()) {
                    for (Bean<?> bean : beans) {
                        alternatives.add(createSimpleBeanJson(bean, probe));
                    }
                }
            }
            enablementBuilder.add(ALTERNATIVES, alternatives);
            bdaBuilder.add(ENABLEMENT, enablementBuilder);
            // Accessible BDAs
            BeanManagerImpl manager = beanDeploymentArchivesMap.get(bda);
            JsonArrayBuilder accesibleBdasBuilder = Json.arrayBuilder();
            for (BeanManagerImpl accesible : manager.getAccessibleManagers()) {
                accesibleBdasBuilder.add(Components.getId(accesible.getId()));
            }
            bdaBuilder.add(ACCESSIBLE_BDAS, accesibleBdasBuilder);
            bdaBuilder.add(BEANS, Components.getNumberOfEnabledBeans(manager));
            bdasBuilder.add(bdaBuilder);
        }
        deploymentBuilder.add(BDAS, bdasBuilder);

        // CONFIGURATION
        JsonArrayBuilder configBuilder = Json.arrayBuilder();
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        for (ConfigurationKey key : ConfigurationKey.values()) {
            Object defaultValue = key.getDefaultValue();
            Object value;
            if (defaultValue instanceof Boolean) {
                value = configuration.getBooleanProperty(key);
            } else if (defaultValue instanceof Long) {
                value = configuration.getLongProperty(key);
            } else if (defaultValue instanceof Integer) {
                value = configuration.getIntegerProperty(key);
            } else if (defaultValue instanceof String) {
                value = configuration.getStringProperty(key);
            } else {
                // Unsupported property type
                continue;
            }
            configBuilder.add(Json.objectBuilder().add(NAME, key.get()).add(DEFAULT_VALUE, defaultValue.toString()).add(VALUE, value.toString()));
        }
        deploymentBuilder.add(CONFIGURATION, configBuilder);

        // INSPECTABLE CONTEXTS
        deploymentBuilder.add(CONTEXTS, createContextsJson(beanManager, probe));

        return deploymentBuilder.build();
    }

    static Bean<?> findEnabledBean(Class<?> beanClass, BeanKind kind, Probe probe) {
        for (Bean<?> bean : probe.getBeans()) {
            if (kind.equals(BeanKind.from(bean)) && beanClass.equals(bean.getBeanClass())) {
                return bean;
            }
        }
        return null;
    }

    static Bean<?> findAlternativeBean(Class<?> beanClass, Probe probe) {
        for (Bean<?> bean : probe.getBeans()) {
            if (bean.isAlternative() && beanClass.equals(bean.getBeanClass())) {
                return bean;
            }
        }
        return null;
    }

    static Set<Bean<?>> findAlternativeStereotypeBeans(Class<? extends Annotation> stereotype, Probe probe) {
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        for (Bean<?> bean : probe.getBeans()) {
            if (bean.isAlternative() && bean.getStereotypes().contains(stereotype)) {
                beans.add(bean);
            }
        }
        return beans;
    }

    /**
     * <ul>
     * <li>{@value Representation#SIMPLE} - simple plus dependencies (non-transient)</li>
     * <li>{@value Representation#BASIC} - basic</li>
     * <li>{@value Representation#FULL} - full plus dependencies (non-transient)</li>
     * </ul>
     *
     * @param page
     * @param probe
     * @param representation
     * @return the collection of all beans of the given page, using the given representation
     */
    static String createBeansJson(Page<Bean<?>> page, Probe probe, BeanManagerImpl beanManager, Representation representation) {
        JsonArrayBuilder beansBuilder = Json.arrayBuilder();
        for (Bean<?> bean : page.getData()) {
            switch (representation) {
                case SIMPLE:
                    beansBuilder.add(createSimpleBeanJsonWithDependencies(bean, probe));
                    break;
                case FULL:
                    beansBuilder.add(createFullBeanJson(bean, false, false, beanManager, probe));
                    break;
                default:
                    beansBuilder.add(createBasicBeanJson(bean, probe));
                    break;
            }
        }
        return createPageJson(page, beansBuilder);
    }

    /**
     * The basic representation consists of the simple representation plus the scope, bean types and qualifiers.
     *
     * @param bean
     * @param probe
     * @return the basic bean representation
     */
    static JsonObjectBuilder createBasicBeanJson(Bean<?> bean, Probe probe) {
        JsonObjectBuilder beanBuilder = createSimpleBeanJson(bean, probe);
        // SCOPE
        beanBuilder.add(SCOPE, simplifiedScope(bean.getScope()));
        // BEAN TYPES
        JsonArrayBuilder typesBuilder = Json.arrayBuilder();
        for (Type type : sortTypes(bean.getTypes())) {
            // Omit java.lang.Object
            if (Object.class.equals(type)) {
                continue;
            }
            typesBuilder.add(Formats.formatType(type, false));
        }
        beanBuilder.add(TYPES, typesBuilder);
        // QUALIFIERS
        if (bean.getQualifiers() != null && !bean.getQualifiers().isEmpty()) {
            beanBuilder.add(QUALIFIERS, createQualifiers(bean.getQualifiers(), true));
        }
        // ALTERNATIVE
        if (bean.isAlternative()) {
            beanBuilder.add(IS_ALTERNATIVE, true);
        }
        return beanBuilder;
    }

    /**
     *
     * @param bean
     * @param transientDependencies
     * @param transientDependents
     * @param probe
     * @return the full bean representation
     */
    static String createFullBeanJson(Bean<?> bean, boolean transientDependencies, boolean transientDependents, BeanManagerImpl beanManager, Probe probe) {
        JsonObjectBuilder beanBuilder = createBasicBeanJson(bean, probe);
        // NAME
        if (bean.getName() != null) {
            beanBuilder.add(NAME, bean.getName());
        }
        // STEREOTYPES
        if (bean.getStereotypes() != null && !bean.getStereotypes().isEmpty()) {
            JsonArrayBuilder stereotypesBuilder = Json.arrayBuilder();
            for (Class<?> stereotype : Components.getSortedProbeComponetCandidates(bean.getStereotypes())) {
                stereotypesBuilder.add(Json.objectBuilder().add(CLASS, stereotype.getName()).add(PROBE_COMPONENT, Components.isProbeComponent(stereotype)));
            }
            beanBuilder.add(STEREOTYPES, stereotypesBuilder);
        }
        if (bean instanceof SessionBean) {
            // SESSION BEAN
            SessionBean<?> sessionBean = (SessionBean<?>) bean;
            if (sessionBean.getEjbDescriptor().getEjbName() != null) {
                beanBuilder.add(EJB_NAME, true);
            }
            beanBuilder.add(SESSION_BEAN_TYPE, Components.getSessionBeanType(sessionBean.getEjbDescriptor()).toString());
        } else if (bean instanceof AbstractProducerBean) {
            // PRODUCERS
            AbstractProducerBean<?, ?, ?> producerBean = (AbstractProducerBean<?, ?, ?>) bean;
            if (producerBean.getDeclaringBean() != null) {
                beanBuilder.add(DECLARING_BEAN, createSimpleBeanJson(producerBean.getDeclaringBean(), probe));
            }
            if (producerBean.getProducer() instanceof ProducerMethodProducer) {
                ProducerMethodProducer<?, ?> producer = (ProducerMethodProducer<?, ?>) producerBean.getProducer();
                if (producer.getDisposalMethod() != null) {
                    beanBuilder.add(DISPOSAL_METHOD, annotatedMethodToString(producer.getDisposalMethod().getAnnotated(), bean.getBeanClass()));
                }
                beanBuilder.add(PRODUCER_METHOD, annotatedMethodToString((AnnotatedMethod<?>) producer.getAnnotated(), bean.getBeanClass()));
            } else if (producerBean.getProducer() instanceof ProducerFieldProducer) {
                ProducerFieldProducer<?, ?> producer = (ProducerFieldProducer<?, ?>) producerBean.getProducer();
                if (producer.getDisposalMethod() != null) {
                    beanBuilder.add(DISPOSAL_METHOD, annotatedMethodToString(producer.getDisposalMethod().getAnnotated(), bean.getBeanClass()));
                }
                beanBuilder.add(PRODUCER_FIELD, annotatedFieldToString(producer.getAnnotated(), bean.getBeanClass()));
            }
        }
        // DEPENDENCIES
        JsonArrayBuilder dependencies = createDependencies(null, bean, probe, transientDependencies);
        if (dependencies != null) {
            beanBuilder.add(DEPENDENCIES, dependencies);
        }
        // DEPENDENTS
        JsonArrayBuilder dependents = createDependents(null, bean, probe, transientDependents);
        if (dependents != null) {
            beanBuilder.add(DEPENDENTS, dependents);
        }
        // DECLARED OBSERVERS
        JsonArrayBuilder declaredObservers = Json.arrayBuilder();
        for (ObserverMethod<?> observerMethod : probe.getObservers()) {
            if (observerMethod instanceof ObserverMethodImpl) {
                ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
                if (bean.equals(observerMethodImpl.getDeclaringBean())) {
                    JsonObjectBuilder observerBuilder = createSimpleObserverJson(observerMethodImpl, probe);
                    observerBuilder.add(RECEPTION, observerMethodImpl.getReception().toString());
                    observerBuilder.add(TX_PHASE, observerMethodImpl.getTransactionPhase().toString());
                    // observerBuilder.add(ANNOTATED_METHOD, annotatedMethodToString(observerMethodImpl.getMethod().getAnnotated(), bean.getBeanClass()));
                    declaredObservers.add(observerBuilder);
                }
            }
        }
        beanBuilder.add(DECLARED_OBSERVERS, declaredObservers);

        // DECLARED PRODUCERS
        JsonArrayBuilder declaredProducers = Json.arrayBuilder();
        for (Bean<?> candidate : probe.getBeans()) {
            BeanKind kind = BeanKind.from(candidate);
            if ((BeanKind.PRODUCER_FIELD.equals(kind) || BeanKind.PRODUCER_METHOD.equals(kind) || BeanKind.RESOURCE.equals(kind))
                    && candidate instanceof AbstractProducerBean) {
                AbstractProducerBean<?, ?, ?> producerBean = (AbstractProducerBean<?, ?, ?>) candidate;
                // The declaring bean of a candidate must equal to the current bean
                if (bean.equals(producerBean.getDeclaringBean())) {
                    JsonObjectBuilder producerBuilder = createSimpleBeanJson(candidate, probe);
                    if (producerBean.getProducer() instanceof ProducerMethodProducer) {
                        ProducerMethodProducer<?, ?> producer = (ProducerMethodProducer<?, ?>) producerBean.getProducer();
                        producerBuilder.add(PRODUCER_INFO, annotatedMethodToString((AnnotatedMethod<?>) producer.getAnnotated(), bean.getBeanClass()));
                    } else if (producerBean.getProducer() instanceof ProducerFieldProducer) {
                        ProducerFieldProducer<?, ?> producer = (ProducerFieldProducer<?, ?>) producerBean.getProducer();
                        producerBuilder.add(PRODUCER_INFO, annotatedFieldToString(producer.getAnnotated(), bean.getBeanClass()));
                    }
                    declaredProducers.add(producerBuilder);
                }
            }
        }
        beanBuilder.add(DECLARED_PRODUCERS, declaredProducers);

        // ENABLEMENT
        BeanKind kind = BeanKind.from(bean);
        if (BeanKind.INTERCEPTOR.equals(kind) || BeanKind.DECORATOR.equals(kind) || bean.isAlternative()) {
            JsonObjectBuilder enablementBuilder = Json.objectBuilder();
            AnnotationApiAbstraction annotationApi = beanManager.getServices().get(AnnotationApiAbstraction.class);
            Object priority = bean.getBeanClass().getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
            if (priority != null) {
                int priorityValue = annotationApi.getPriority(priority);
                enablementBuilder.add(PRIORITY, priorityValue);
                enablementBuilder.add(PRIORITY_RANGE, Components.PriorityRange.of(priorityValue).toString());
            } else {
                JsonArrayBuilder bdasBuilder = Json.arrayBuilder();
                Collection<BeanManagerImpl> beanManagers = Container.instance(beanManager).beanDeploymentArchives().values();
                for (BeanManagerImpl manager : beanManagers) {
                    ModuleEnablement enablement = manager.getEnabled();
                    if ((BeanKind.INTERCEPTOR.equals(kind) && enablement.isInterceptorEnabled(bean.getBeanClass()))
                            || (BeanKind.DECORATOR.equals(kind) && enablement.isDecoratorEnabled(bean.getBeanClass()))
                            || isSelectedAlternative(enablement, bean)) {
                        bdasBuilder.add(createSimpleBdaJson(manager.getId()));
                    }
                }
                enablementBuilder.add(BDAS, bdasBuilder);
            }
            beanBuilder.add(ENABLEMENT, enablementBuilder);
        }
        return beanBuilder.build();
    }

    private static boolean isSelectedAlternative(ModuleEnablement enablement, Bean<?> bean) {
        if (bean.isAlternative()) {
            if (enablement.isEnabledAlternativeClass(bean.getBeanClass())) {
                return true;
            }
            for (Class<? extends Annotation> stereotype : bean.getStereotypes()) {
                if (enablement.isEnabledAlternativeStereotype(stereotype)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The simple representation consists of the generated id, {@link Components.BeanKind}, {@link Bean#getBeanClass()} and bean archive id (if not a built-in
     * bean).
     *
     * @param bean
     * @param probe
     * @return the simple bean representation
     */
    static JsonObjectBuilder createSimpleBeanJson(Bean<?> bean, Probe probe) {
        JsonObjectBuilder builder = Json.objectBuilder(true);
        builder.add(ID, probe.getBeanId(bean));
        builder.add(KIND, BeanKind.from(bean).toString());
        builder.add(BEAN_CLASS, bean.getBeanClass().getName());
        // BDA
        BeanManagerImpl beanManager = probe.getBeanManager(bean);
        if (beanManager != null) {
            builder.add(BDA_ID, Components.getId(beanManager.getId()));
        }
        return builder;
    }

    /**
     * The simple representation plus dependencies (non-transient).
     *
     * @param bean
     * @param probe
     * @return the simple representation plus dependencies/dependents (non-transient)
     */
    static JsonObjectBuilder createSimpleBeanJsonWithDependencies(Bean<?> bean, Probe probe) {
        JsonObjectBuilder builder = createSimpleBeanJson(bean, probe);
        // DEPENDENCIES
        JsonArrayBuilder dependencies = createDependencies(null, bean, probe, false);
        if (dependencies != null) {
            builder.add(DEPENDENCIES, dependencies);
        }
        // DEPENDENTS
        JsonArrayBuilder dependents = createDependents(null, bean, probe, false);
        if (dependents != null) {
            builder.add(DEPENDENTS, dependents);
        }
        return builder;
    }

    /**
     *
     * @param parent
     * @param bean
     * @param probe
     * @param isTransient
     * @return
     */
    static JsonArrayBuilder createDependencies(Bean<?> parent, Bean<?> bean, Probe probe, boolean isTransient) {

        final BeanManagerImpl beanManager = probe.getBeanManager(bean);
        // Don't create dependencies for built-in beans
        if (beanManager == null) {
            return null;
        }
        JsonArrayBuilder dependenciesBuilder = Json.arrayBuilder(true);

        for (Dependency dep : Components.getDependencies(bean, beanManager, probe)) {
            // Workaround for built-in beans - these are identified by the set of types
            if (Components.isBuiltinBeanButNotExtension(dep.getBean())) {
                dependenciesBuilder.add(createDependency(probe.getBean(Components.getBuiltinBeanId((AbstractBuiltInBean<?>) dep.getBean())), dep, probe));
                continue;
            }
            // Handle circular dependencies
            if (dep.getBean().equals(parent)) {
                dependenciesBuilder.add(createDependency(dep.getBean(), dep, probe));
                continue;
            }

            JsonObjectBuilder dependency = createDependency(dep.getBean(), dep, probe);
            dependenciesBuilder.add(dependency);

            if (isTransient) {
                dependency.add(DEPENDENCIES, createDependencies(bean, dep.getBean(), probe, true));
            }
        }
        return dependenciesBuilder.isEmpty() ? null : dependenciesBuilder;
    }

    /**
     *
     * @param parent
     * @param bean
     * @param probe
     * @param isTransient
     * @return
     */
    static JsonArrayBuilder createDependents(Bean<?> parent, Bean<?> bean, Probe probe, boolean isTransient) {

        JsonArrayBuilder dependentsBuilder = Json.arrayBuilder(true);

        for (Dependency dependent : Components.getDependents(bean, probe)) {
            // Workaround for built-in beans - these are identified by the set of types
            if (Components.isBuiltinBeanButNotExtension(dependent.getBean())) {
                dependentsBuilder.add(createDependency(probe.getBean(Components.getBuiltinBeanId((AbstractBuiltInBean<?>) dependent.getBean())), dependent,
                        probe));
                continue;
            }
            // Handle circular dependencies
            if (dependent.getBean().equals(parent)) {
                dependentsBuilder.add(createDependency(dependent.getBean(), dependent, probe));
                continue;
            }

            JsonObjectBuilder dependency = createDependency(dependent.getBean(), dependent, probe);
            dependentsBuilder.add(dependency);

            if (isTransient) {
                dependency.add(DEPENDENTS, createDependents(bean, dependent.getBean(), probe, true));
            }
        }
        return dependentsBuilder.isEmpty() ? null : dependentsBuilder;
    }

    /**
     *
     * @param probe
     * @return the collection of all observer methods, using basic representation
     */
    static String createInvocationsJson(Page<Invocation> page, Probe probe) {
        JsonArrayBuilder invocationsBuilder = Json.arrayBuilder();
        for (Invocation invocation : page.getData()) {
            invocationsBuilder.add(createBasicInvocationJson(invocation, probe));
        }
        return createPageJson(page, invocationsBuilder);
    }

    static JsonObjectBuilder createBasicInvocationJson(Invocation invocation, Probe probe) {
        JsonObjectBuilder invocationBuilder = Json.objectBuilder();
        if (invocation.getEntryPointIdx() != null) {
            invocationBuilder.add(ID, invocation.getEntryPointIdx());
        }
        if (invocation.getInterceptedBean() != null) {
            invocationBuilder.add(INTERCEPTED_BEAN, createSimpleBeanJson(invocation.getInterceptedBean(), probe));
        } else {
            invocationBuilder.add(DECLARING_CLASS, invocation.getDeclaringClassName());
        }
        if (invocation.getDescription() != null) {
            invocationBuilder.add(DESCRIPTION, invocation.getDescription());
        }
        invocationBuilder.add(METHOD_NAME, invocation.getMethodName());
        invocationBuilder.add(START, invocation.getStart());
        invocationBuilder.add(TIME, invocation.getDuration());
        return invocationBuilder;
    }

    /**
     *
     * @param invocation
     * @param probe
     * @return
     */
    static JsonObjectBuilder createFullInvocationJson(Invocation invocation, Probe probe) {
        JsonObjectBuilder invocationBuilder = createBasicInvocationJson(invocation, probe);
        invocationBuilder.add(TYPE, invocation.getType().toString());
        if (invocation.hasChildren()) {
            JsonArrayBuilder childrenBuilder = Json.arrayBuilder();
            for (Invocation child : invocation.getChildren()) {
                childrenBuilder.add(createFullInvocationJson(child, probe));
            }
            invocationBuilder.add(CHILDREN, childrenBuilder);
        }
        return invocationBuilder;
    }

    /**
     *
     * @param probe
     * @return the collection of all observer methods, using basic representation
     */
    static String createObserversJson(Page<ObserverMethod<?>> page, Probe probe) {
        JsonArrayBuilder observersBuilder = Json.arrayBuilder();
        for (ObserverMethod<?> observerMethod : page.getData()) {
            observersBuilder.add(createBasicObserverJson(observerMethod, probe));
        }
        return createPageJson(page, observersBuilder);
    }

    /**
     *
     * @param observerMethod
     * @param probe
     * @return
     */
    static String createFullObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = createBasicObserverJson(observerMethod, probe);
        if (observerMethod instanceof ObserverMethodImpl) {
            ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
            observerBuilder.add(ANNOTATED_METHOD, annotatedMethodToString(observerMethodImpl.getMethod().getAnnotated(), observerMethodImpl.getBeanClass()));
        }
        return observerBuilder.build();
    }

    static JsonObjectBuilder createBasicObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = createSimpleObserverJson(observerMethod, probe);
        observerBuilder.add(RECEPTION, observerMethod.getReception().toString());
        observerBuilder.add(TX_PHASE, observerMethod.getTransactionPhase().toString());
        if (!observerMethod.getObservedQualifiers().isEmpty()) {
            JsonArrayBuilder qualifiersBuilder = Json.arrayBuilder();
            for (Annotation qualifier : observerMethod.getObservedQualifiers()) {
                qualifiersBuilder.add(qualifier.toString());
            }
            observerBuilder.add(QUALIFIERS, qualifiersBuilder);
        }
        if (observerMethod instanceof ObserverMethodImpl) {
            ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
            observerBuilder.add(DECLARING_BEAN, createSimpleBeanJson(observerMethodImpl.getDeclaringBean(), probe));
        }
        if (observerMethod instanceof Prioritized) {
            final int priority = Prioritized.class.cast(observerMethod).getPriority();
            observerBuilder.add(PRIORITY, priority);
            observerBuilder.add(PRIORITY_RANGE, Components.PriorityRange.of(priority).toString());
        }
        return observerBuilder;
    }

    static JsonObjectBuilder createSimpleObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = Json.objectBuilder();
        observerBuilder.add(ID, probe.getObserverId(observerMethod));
        observerBuilder.add(BEAN_CLASS, observerMethod.getBeanClass().getName());
        observerBuilder.add(OBSERVED_TYPE, Formats.formatType(observerMethod.getObservedType(), false));
        return observerBuilder;
    }

    static String createContextualInstanceJson(Bean<?> bean, Object contextualInstance, Probe probe) {
        try {
            JsonObjectBuilder builder = createSimpleBeanJson(bean, probe);
            builder.add(SCOPE, simplifiedScope(bean.getScope()));
            builder.add(CONTEXT_ID, Components.getInspectableScopeId(bean.getScope()));

            JsonArrayBuilder propertiesBuilder = Json.arrayBuilder();
            Class<?> definingClass = contextualInstance.getClass();
            if (ProxyObject.class.isAssignableFrom(definingClass)) {
                definingClass = definingClass.getSuperclass();
            }
            BeanInfo bi = java.beans.Introspector.getBeanInfo(definingClass);
            PropertyDescriptor[] properties = bi.getPropertyDescriptors();

            for (PropertyDescriptor propertyDescriptor : properties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod == null) {
                    continue;
                }
                Object value;
                try {
                    value = readMethod.invoke(contextualInstance);
                } catch (IllegalAccessException e) {
                    value = toString(e);
                } catch (IllegalArgumentException e) {
                    value = toString(e);
                } catch (InvocationTargetException e) {
                    value = toString(e);
                }
                propertiesBuilder
                        .add(Json.objectBuilder().add(NAME, propertyDescriptor.getDisplayName()).add(VALUE, value != null ? value.toString() : "null"));
            }
            builder.add(PROPERTIES, propertiesBuilder);
            return builder.build();

        } catch (IntrospectionException e) {
            ProbeLogger.LOG.introspectionProblem(bean, e);
            return null;
        }
    }

    static JsonArrayBuilder createContextsJson(BeanManagerImpl beanManager, Probe probe) {
        JsonArrayBuilder contexts = Json.arrayBuilder();
        for (Entry<String, Class<? extends Annotation>> entry : Components.INSPECTABLE_SCOPES.entrySet()) {
            contexts.add(createSimpleContextJson(entry.getKey(), entry.getValue()));
        }
        return contexts;
    }

    static JsonObjectBuilder createSimpleContextJson(String id, Class<? extends Annotation> scope) {
        JsonObjectBuilder builder = Json.objectBuilder(true);
        builder.add(SCOPE, scope.getName());
        builder.add(ID, id);
        return builder;
    }

    static JsonObjectBuilder createContextJson(String id, Class<? extends Annotation> scope, BeanManagerImpl beanManager, Probe probe, HttpServletRequest req) {
        JsonObjectBuilder builder = createSimpleContextJson(id, scope);

        builder.add(INSTANCES, inspectContext(scope, beanManager, probe));

        if (ConversationScoped.class.equals(scope)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                // Get all available conversation ids
                Object conversationsAttribute = session.getAttribute(AbstractConversationContext.CONVERSATIONS_ATTRIBUTE_NAME);
                if (conversationsAttribute != null && conversationsAttribute instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, ManagedConversation> conversationsMap = (Map<String, ManagedConversation>) conversationsAttribute;
                    if (!conversationsMap.isEmpty()) {
                        JsonArrayBuilder cidsBuilder = Json.arrayBuilder();
                        for (String cid : conversationsMap.keySet()) {
                            cidsBuilder.add(cid);
                        }
                        builder.add(CIDS, cidsBuilder);
                    }
                }
            }
        }
        return builder;
    }

    private static JsonArrayBuilder inspectContext(Class<? extends Annotation> scope, BeanManagerImpl beanManager, Probe probe) {
        Context context;
        try {
            context = beanManager.getContext(scope);
        } catch (ContextNotActiveException e) {
            return null;
        }
        JsonArrayBuilder builder = Json.arrayBuilder();
        for (Bean<?> bean : probe.getBeans()) {
            if (bean.getScope().equals(scope)) {
                Object contextualInstance = context.get(bean);
                if (contextualInstance != null) {
                    JsonObjectBuilder instanceBuilder = createSimpleBeanJson(bean, probe);
                    instanceBuilder.add(AS_STRING, contextualInstance.toString());
                    builder.add(instanceBuilder);
                }
            }
        }
        return builder;
    }

    private static String toString(Throwable e) {
        return e.getClass().getName() + '[' + e.getMessage() + ']';
    }

    private static JsonObjectBuilder createDependency(Bean<?> bean, Dependency dependency, Probe probe) {
        JsonObjectBuilder builder = createSimpleBeanJson(bean, probe);
        if (bean != null && dependency != null) {
            builder.add(REQUIRED_TYPE, Formats.formatType(dependency.getInjectionPoint().getType(), false)).add(QUALIFIERS,
                    createQualifiers(dependency.getInjectionPoint().getQualifiers(), false));
        }
        return builder;
    }

    static List<Type> sortTypes(Set<Type> types) {
        List<Type> sorted = new ArrayList<Type>(types);
        Collections.sort(sorted, new Comparator<Type>() {
            @Override
            public int compare(Type o1, Type o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return sorted;
    }

    static JsonArrayBuilder createQualifiers(Set<Annotation> qualifiers, boolean skipAny) {
        JsonArrayBuilder builder = Json.arrayBuilder();
        for (Annotation qualifier : qualifiers) {
            if (Any.class.equals(qualifier.annotationType())) {
                if (skipAny) {
                    // Omit javax.enterprise.inject.Any
                    continue;
                }
                // Remove package from @Any
                builder.add("@" + qualifier.annotationType().getSimpleName());
            } else if (Default.class.equals(qualifier.annotationType())) {
                // Remove package from @Default
                builder.add("@" + qualifier.annotationType().getSimpleName());
            } else {
                builder.add(qualifier.toString());
            }
        }
        return builder;
    }

    static String createPageJson(Page<?> page, JsonArrayBuilder data) {
        return Json.objectBuilder().add(PAGE, page.getIdx()).add(LAST_PAGE, page.getLastIdx()).add(TOTAL, page.getTotal()).add(DATA, data).build();
    }

    static String annotatedMethodToString(AnnotatedMethod<?> method, Class<?> beanClass) {
        StringBuilder builder = new StringBuilder();
        builder.append(Formats.addSpaceIfNeeded(Formats.formatAnnotations(method.getAnnotations())));
        builder.append(Formats.formatModifiers(method.getJavaMember().getModifiers()));
        builder.append(' ');
        builder.append(method.getJavaMember().getReturnType().getName());
        builder.append(' ');
        if (!beanClass.getName().equals(method.getDeclaringType().getJavaClass().getName())) {
            builder.append(method.getDeclaringType().getJavaClass().getName());
            builder.append('.');
        }
        builder.append(method.getJavaMember().getName());
        builder.append(Formats.formatAsFormalParameterList(method.getParameters()));
        return builder.toString();
    }

    static String annotatedFieldToString(AnnotatedField<?> field, Class<?> beanClass) {
        StringBuilder builder = new StringBuilder();
        builder.append(Formats.addSpaceIfNeeded(Formats.formatAnnotations(field.getAnnotations())));
        builder.append(Formats.formatModifiers(field.getJavaMember().getModifiers()));
        builder.append(' ');
        builder.append(field.getJavaMember().getType().getName());
        builder.append(' ');
        if (!beanClass.getName().equals(field.getDeclaringType().getJavaClass().getName())) {
            builder.append(field.getDeclaringType().getJavaClass().getName());
            builder.append('.');
        }
        builder.append(field.getJavaMember().getName());
        return builder.toString();
    }

    static JsonObjectBuilder createEventJson(EventInfo event, Probe probe) {
        JsonObjectBuilder builder = Json.objectBuilder();
        builder.add(TYPE, Formats.formatType(event.type, false));
        builder.add(QUALIFIERS, createQualifiers(event.qualifiers, true));
        builder.add(EVENT_INFO, event.eventString);
        builder.add(KIND, (event.containerEvent ? CONTAINER : APPLICATION).toUpperCase());
        builder.add(TIMESTAMP, event.timestamp);
        JsonArrayBuilder observersBuilder = Json.arrayBuilder();
        for (ObserverMethod<?> observer : event.observers) {
            JsonObjectBuilder b = createSimpleObserverJson(observer, probe);
            if (observer instanceof ObserverMethodImpl<?, ?>) {
                ObserverMethodImpl<?, ?> weldObserver = (ObserverMethodImpl<?, ?>) observer;
                AnnotatedMethod<?> method = weldObserver.getMethod().getAnnotated();
                b.add(METHOD, method.getJavaMember().getName() + Formats.formatAsFormalParameterList(method.getParameters()));
            }
            observersBuilder.add(b);
        }
        builder.add(OBSERVERS, observersBuilder);
        return builder;
    }

    static String createEventsJson(Page<EventInfo> page, Probe probe) {
        JsonArrayBuilder eventsBuilder = Json.arrayBuilder();
        for (EventInfo event : page.getData()) {
            eventsBuilder.add(createEventJson(event, probe));
        }
        return createPageJson(page, eventsBuilder);
    }

    static JsonObjectBuilder createSimpleBdaJson(String bdaId) {
        JsonObjectBuilder bdaBuilder = Json.objectBuilder(true);
        bdaBuilder.add(BDA_ID, bdaId);
        bdaBuilder.add(ID, Components.getId(bdaId));
        return bdaBuilder;
    }

    private static String simplifiedScope(Class<? extends Annotation> scope) {
        return "@" + (Components.isBuiltinScope(scope) ? scope.getSimpleName() : scope.getName());
    }

    private static JsonObjectBuilder decorateProbeComponent(Class<?> clazz, JsonObjectBuilder builder) {
        return Components.isProbeComponent(clazz) ? builder.add(PROBE_COMPONENT, true) : builder;
    }

}
