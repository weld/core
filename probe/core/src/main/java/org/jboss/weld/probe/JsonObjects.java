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
import static org.jboss.weld.probe.Strings.ACTIVATIONS;
import static org.jboss.weld.probe.Strings.ALTERNATIVES;
import static org.jboss.weld.probe.Strings.ANNOTATED_METHOD;
import static org.jboss.weld.probe.Strings.APPLICATION;
import static org.jboss.weld.probe.Strings.ASSOCIATED_TO;
import static org.jboss.weld.probe.Strings.AS_STRING;
import static org.jboss.weld.probe.Strings.BDAS;
import static org.jboss.weld.probe.Strings.BDA_ID;
import static org.jboss.weld.probe.Strings.BEANS;
import static org.jboss.weld.probe.Strings.BEANS_XML;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.BEAN_DISCOVERY_MODE;
import static org.jboss.weld.probe.Strings.BINDINGS;
import static org.jboss.weld.probe.Strings.BOOSTRAP_STATS;
import static org.jboss.weld.probe.Strings.CHILDREN;
import static org.jboss.weld.probe.Strings.CIDS;
import static org.jboss.weld.probe.Strings.CLASS;
import static org.jboss.weld.probe.Strings.CLASS_AVAILABILITY;
import static org.jboss.weld.probe.Strings.CLASS_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.probe.Strings.CONFIGURATION;
import static org.jboss.weld.probe.Strings.CONFLICTS;
import static org.jboss.weld.probe.Strings.CONTAINER;
import static org.jboss.weld.probe.Strings.CONTEXTS;
import static org.jboss.weld.probe.Strings.CONTEXT_ID;
import static org.jboss.weld.probe.Strings.DASHBOARD;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DECLARED_OBSERVERS;
import static org.jboss.weld.probe.Strings.DECLARED_PRODUCERS;
import static org.jboss.weld.probe.Strings.DECLARING_BEAN;
import static org.jboss.weld.probe.Strings.DECLARING_CLASS;
import static org.jboss.weld.probe.Strings.DECORATED_TYPES;
import static org.jboss.weld.probe.Strings.DECORATORS;
import static org.jboss.weld.probe.Strings.DEFAULT_VALUE;
import static org.jboss.weld.probe.Strings.DELEGATE_QUALIFIERS;
import static org.jboss.weld.probe.Strings.DELEGATE_TYPE;
import static org.jboss.weld.probe.Strings.DEPENDENCIES;
import static org.jboss.weld.probe.Strings.DEPENDENTS;
import static org.jboss.weld.probe.Strings.DESCRIPTION;
import static org.jboss.weld.probe.Strings.DISPOSAL_METHOD;
import static org.jboss.weld.probe.Strings.EJB_NAME;
import static org.jboss.weld.probe.Strings.EMPTY;
import static org.jboss.weld.probe.Strings.ENABLEMENT;
import static org.jboss.weld.probe.Strings.EVENT_INFO;
import static org.jboss.weld.probe.Strings.EXCLUDE;
import static org.jboss.weld.probe.Strings.FIRED;
import static org.jboss.weld.probe.Strings.HASH;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.INCLUDE;
import static org.jboss.weld.probe.Strings.INFO;
import static org.jboss.weld.probe.Strings.INFO_FETCHING_LAZILY;
import static org.jboss.weld.probe.Strings.INIT_TS;
import static org.jboss.weld.probe.Strings.INSTANCES;
import static org.jboss.weld.probe.Strings.INTERCEPTED_BEAN;
import static org.jboss.weld.probe.Strings.INTERCEPTORS;
import static org.jboss.weld.probe.Strings.INVERTED;
import static org.jboss.weld.probe.Strings.INVOCATIONS;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.IS_AMBIGUOUS;
import static org.jboss.weld.probe.Strings.IS_POTENTIAL;
import static org.jboss.weld.probe.Strings.IS_UNSATISFIED;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.LAST_PAGE;
import static org.jboss.weld.probe.Strings.MARKER;
import static org.jboss.weld.probe.Strings.METHOD;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.NAME;
import static org.jboss.weld.probe.Strings.OBJECT_TO_STRING;
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
import static org.jboss.weld.probe.Strings.SCAN;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.Strings.SESSION_BEAN_TYPE;
import static org.jboss.weld.probe.Strings.START;
import static org.jboss.weld.probe.Strings.STEREOTYPES;
import static org.jboss.weld.probe.Strings.SYS_PROPERTY;
import static org.jboss.weld.probe.Strings.TIME;
import static org.jboss.weld.probe.Strings.TIMESTAMP;
import static org.jboss.weld.probe.Strings.TOTAL;
import static org.jboss.weld.probe.Strings.TRIMMED;
import static org.jboss.weld.probe.Strings.TX_PHASE;
import static org.jboss.weld.probe.Strings.TYPE;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.Strings.UNUSED;
import static org.jboss.weld.probe.Strings.VALUE;
import static org.jboss.weld.probe.Strings.VERSION;
import static org.jboss.weld.probe.Strings.WARNING;
import static org.jboss.weld.probe.Strings.WARNING_CONFLICTING_ENABLEMENT;
import static org.jboss.weld.probe.Strings.WARNING_UNRESTRICTED_PAT_OBSERVER;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.Prioritized;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.InstanceImpl;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.contexts.AbstractConversationContext;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.injection.producer.ProducerFieldProducer;
import org.jboss.weld.injection.producer.ProducerMethodProducer;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.BootstrapStats.EventType;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.Components.Dependency;
import org.jboss.weld.probe.Json.JsonArrayBuilder;
import org.jboss.weld.probe.Json.JsonObjectBuilder;
import org.jboss.weld.probe.Queries.ObserverFilters;
import org.jboss.weld.probe.Queries.Page;
import org.jboss.weld.probe.Resource.Representation;
import org.jboss.weld.util.AnnotationApiAbstraction;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.collections.Sets;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Lots of utility methods for creating JSON data.
 *
 * @author Martin Kouba
 */
@Vetoed
final class JsonObjects {

    private static final int CONTEXTUAL_INSTANCE_TO_STRING_LIMIT = 100;

    private static final int CONTEXTUAL_INSTANCE_PROPERTY_VALUE_LIMIT = 500;

    private JsonObjects() {
    }

    /**
     *
     * @param beanManager
     * @return the root resource representation
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "We want to catch all exceptions, runtime included.")
    static String createDeploymentJson(BeanManagerImpl beanManager, Probe probe) {

        Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchivesMap = Container.instance(beanManager).beanDeploymentArchives();
        AnnotationApiAbstraction annotationApi = beanManager.getServices().get(AnnotationApiAbstraction.class);
        JsonObjectBuilder deploymentBuilder = Json.objectBuilder();

        // INIT TS
        deploymentBuilder.add(INIT_TS, probe.getInitTs());
        // CONTEXT ID
        deploymentBuilder.add(CONTEXT_ID, beanManager.getContextId());
        // WELD VERSION
        deploymentBuilder.add(VERSION, Formats.getSimpleVersion());

        // BEAN DEPLOYMENT ARCHIVES
        JsonArrayBuilder bdasBuilder = Json.arrayBuilder();
        List<BeanDeploymentArchive> bdas = new ArrayList<BeanDeploymentArchive>(beanDeploymentArchivesMap.keySet());
        Collections.sort(bdas, probe.getBdaComparator());
        for (BeanDeploymentArchive bda : bdas) {
            JsonObjectBuilder bdaBuilder = createSimpleBdaJson(bda.getId());
            // If beans.xml is not found it's likely an implicit bean archive
            BeansXml beansXml = bda.getBeansXml();
            bdaBuilder.add(BEAN_DISCOVERY_MODE, beansXml != null ? beansXml.getBeanDiscoveryMode().toString() : BeanDiscoveryMode.ANNOTATED.toString());

            // beans.xml
            if (beansXml != null) {
                JsonObjectBuilder beansXmlBuilder = Json.objectBuilder();
                if (beansXml.equals(BeansXml.EMPTY_BEANS_XML)) {
                    beansXmlBuilder.add(MARKER, Boolean.TRUE);
                } else {
                    beansXmlBuilder.add(Strings.URL, beansXml.getUrl() != null ? beansXml.getUrl().toString() : EMPTY);
                    beansXmlBuilder.add(VERSION, beansXml.getVersion() != null ? beansXml.getVersion().toString() : EMPTY);
                    beansXmlBuilder.add(TRIMMED, beansXml.isTrimmed());
                    if (beansXml.getScanning() != null && (!beansXml.getScanning().getExcludes().isEmpty() || !beansXml.getScanning().getExcludes().isEmpty())) {
                        JsonArrayBuilder scanBuilder = Json.arrayBuilder();
                        createMetadataArrayJson(scanBuilder, beansXml.getScanning().getExcludes(), EXCLUDE);
                        createMetadataArrayJson(scanBuilder, beansXml.getScanning().getIncludes(), INCLUDE);
                        beansXmlBuilder.add(SCAN, scanBuilder);
                    }
                }
                bdaBuilder.add(Strings.BEANS_XML, beansXmlBuilder);
            }

            // Enablement - interceptors, decorators, alternatives
            JsonObjectBuilder enablementBuilder = Json.objectBuilder(true);
            ModuleEnablement enablement = beanDeploymentArchivesMap.get(bda).getEnabled();
            JsonArrayBuilder interceptors = Json.arrayBuilder();
            for (Class<?> interceptor : Components.getSortedProbeComponetCandidates(enablement.getInterceptors())) {

                Bean<?> interceptorBean = findEnabledBean(interceptor, BeanKind.INTERCEPTOR, probe);
                if (interceptorBean != null) {
                    JsonObjectBuilder builder = decorateProbeComponent(interceptor, createSimpleBeanJson(interceptorBean, probe));
                    if (beansXml != null) {
                        for (Metadata<String> meta : beansXml.getEnabledInterceptors()) {
                            if (meta.getValue().equals(interceptorBean.getBeanClass().getName())) {
                                // Locally enabled
                                builder.add(BEANS_XML, true);
                            }
                        }
                    }
                    Object priority = interceptorBean.getBeanClass().getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
                    if (priority != null) {
                        builder.add(PRIORITY, annotationApi.getPriority(priority));
                    }
                    if (builder.has(PRIORITY) && builder.has(BEANS_XML)) {
                        builder.add(CONFLICTS, true);
                    }
                    interceptors.add(builder);
                }
            }
            enablementBuilder.add(INTERCEPTORS, interceptors);
            JsonArrayBuilder decorators = Json.arrayBuilder();
            for (Class<?> decorator : enablement.getDecorators()) {

                Bean<?> decoratorBean = findEnabledBean(decorator, BeanKind.DECORATOR, probe);
                if (decoratorBean != null) {
                    JsonObjectBuilder builder = createSimpleBeanJson(decoratorBean, probe);
                    if (beansXml != null) {
                        for (Metadata<String> meta : beansXml.getEnabledDecorators()) {
                            if (meta.getValue().equals(decoratorBean.getBeanClass().getName())) {
                                // Locally enabled
                                builder.add(BEANS_XML, true);
                            }
                        }
                    }
                    Object priority = decoratorBean.getBeanClass().getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
                    if (priority != null) {
                        builder.add(PRIORITY, annotationApi.getPriority(priority));
                    }
                    if (builder.has(PRIORITY) && builder.has(BEANS_XML)) {
                        builder.add(CONFLICTS, true);
                    }
                    decorators.add(builder);
                }
            }
            enablementBuilder.add(DECORATORS, decorators);
            JsonArrayBuilder alternatives = Json.arrayBuilder();
            for (Class<?> clazz : Sets.union(enablement.getAlternativeClasses(), enablement.getGlobalAlternatives())) {
                Bean<?> alternativeBean = findAlternativeBean(clazz, probe);
                if (alternativeBean != null) {
                    JsonObjectBuilder builder = createSimpleBeanJson(alternativeBean, probe);
                    if (enablement.getAlternativeClasses().contains(clazz)) {
                        builder.add(BEANS_XML, true);
                    }
                    if (enablement.getGlobalAlternatives().contains(clazz)) {
                        Object priority = clazz.getAnnotation(annotationApi.PRIORITY_ANNOTATION_CLASS);
                        if (priority != null) {
                            builder.add(PRIORITY, annotationApi.getPriority(priority));
                        }
                    }
                    alternatives.add(builder);
                }
            }
            for (Class<? extends Annotation> stereotype : enablement.getAlternativeStereotypes()) {
                Set<Bean<?>> beans = findAlternativeStereotypeBeans(stereotype, probe);
                if (!beans.isEmpty()) {
                    for (Bean<?> bean : beans) {
                        JsonObjectBuilder builder = createSimpleBeanJson(bean, probe);
                        builder.add(BEANS_XML, true);
                        alternatives.add(builder);
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
        for (ConfigurationKey key : Reports.getSortedConfigurationKeys()) {
            Object defaultValue = key.getDefaultValue();
            String desc = Reports.getDesc(key);
            if (desc == null) {
                // Don't show config options without description
                continue;
            }
            Object value = Reports.getValue(key, configuration);
            if (value == null) {
                // Unsupported property type
                continue;
            }
            configBuilder.add(
                Json.objectBuilder().add(NAME, key.get()).add(DEFAULT_VALUE, defaultValue.toString()).add(VALUE, value.toString()).add(DESCRIPTION, desc));
        }
        deploymentBuilder.add(CONFIGURATION, configBuilder);

        // INSPECTABLE CONTEXTS
        deploymentBuilder.add(CONTEXTS, createContextsJson(beanManager, probe));

        // DASHBOARD DATA
        JsonObjectBuilder dashboardBuilder = Json.objectBuilder();
        // Application
        JsonObjectBuilder appBuilder = Json.objectBuilder();
        appBuilder.add(BEANS, probe.getApplicationBeansCount());
        appBuilder.add(OBSERVERS, probe.getApplicationObserversCount());
        dashboardBuilder.add(APPLICATION, appBuilder);
        // Bootstrap
        dashboardBuilder.add(BOOSTRAP_STATS, createBootstrapStatsJson(probe));
        deploymentBuilder.add(DASHBOARD, dashboardBuilder);

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
     * <li>{@value Representation#FULL} - full plus dependencies (including transient)</li>
     * </ul>
     *
     * @param page
     * @param probe
     * @param representation
     * @return the collection of all beans of the given page, using the given representation
     */
    static String createBeansJson(Page<Bean<?>> page, Probe probe, BeanManagerImpl beanManager, Representation representation) {
        JsonArrayBuilder beansBuilder = Json.arrayBuilder();
        if (representation == null) {
            representation = Representation.BASIC;
        }
        for (Bean<?> bean : page.getData()) {
            switch (representation) {
                case SIMPLE:
                    beansBuilder.add(createSimpleBeanJsonWithDependencies(bean, probe));
                    break;
                case BASIC:
                    beansBuilder.add(createBasicBeanJson(bean, probe));
                    break;
                case FULL:
                    beansBuilder.add(createFullBeanJson(bean, true, true, beanManager, probe));
                    break;
                default:
                    throw new UnsupportedOperationException(representation.toString());
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
        // UNUSED
        if (probe.isUnused(bean)) {
            beanBuilder.add(UNUSED, true);
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
    static JsonObjectBuilder createFullBeanJson(Bean<?> bean, boolean transientDependencies, boolean transientDependents, BeanManagerImpl beanManager, Probe probe) {
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
                    observerBuilder.add(ANNOTATED_METHOD, annotatedMethodToString(observerMethodImpl.getMethod().getAnnotated(), bean.getBeanClass()));
                    declaredObservers.add(observerBuilder);
                }
            }
        }
        beanBuilder.add(DECLARED_OBSERVERS, declaredObservers);

        // DECLARED PRODUCERS
        beanBuilder.add(DECLARED_PRODUCERS, createDeclaredProducers(bean, probe));

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
                if (!probe.getLocalEnablementOfBean(bean.getBeanClass()).isEmpty()) {
                    enablementBuilder.add(WARNING, WARNING_CONFLICTING_ENABLEMENT);
                    JsonArrayBuilder conflictingBdas = Json.arrayBuilder();
                    for (String bdaId : probe.getLocalEnablementOfBean(bean.getBeanClass())) {
                        conflictingBdas.add(createSimpleBdaJson(bdaId));
                    }
                    enablementBuilder.add(CONFLICTS, conflictingBdas);
                }
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

        // INTERCEPTOR BINDINGS
        if (BeanKind.INTERCEPTOR.equals(kind)) {
            Interceptor<?> interceptor = (Interceptor<?>) bean;
            JsonArrayBuilder bindings = Json.arrayBuilder(true);
            for (Annotation binding : interceptor.getInterceptorBindings()) {
                bindings.add(annotationToString(binding));
            }
            beanBuilder.add(BINDINGS, bindings);
        }

        // DECORATOR
        if (BeanKind.DECORATOR.equals(kind)) {
            Decorator<?> decorator = (Decorator<?>) bean;
            beanBuilder.add(DELEGATE_TYPE, Formats.formatType(decorator.getDelegateType(), false));
            beanBuilder.add(DELEGATE_QUALIFIERS, createQualifiers(decorator.getDelegateQualifiers(), false));
            JsonArrayBuilder decoratedTypes = Json.arrayBuilder(true);
            for (Type type : decorator.getDecoratedTypes()) {
                decoratedTypes.add(Formats.formatType(type, false));
            }
            beanBuilder.add(DECORATED_TYPES, decoratedTypes);
            // ASSOCIATED TO BEANS
            Set<Bean<?>> decoratedBeans = findDecoratedBeans(decorator, beanManager, probe);
            if (!decoratedBeans.isEmpty()) {
                JsonArrayBuilder decoratedBeansBuilder = Json.arrayBuilder();
                for (Bean<?> decoratedBean : decoratedBeans) {
                    decoratedBeansBuilder.add(createSimpleBeanJson(decoratedBean, probe));
                }
                beanBuilder.add(ASSOCIATED_TO, decoratedBeansBuilder);
            }
        }

        if (bean instanceof AbstractClassBean) {
            AbstractClassBean<?> abstractClassBean = (AbstractClassBean<?>) bean;
            InterceptionModel interceptionModel = abstractClassBean.getInterceptors();
            // CLASS-LEVEL BINDINGS
            if (interceptionModel != null) {
                Set<Annotation> classInterceptorBindings = interceptionModel.getClassInterceptorBindings();
                if (!classInterceptorBindings.isEmpty()) {
                    JsonArrayBuilder bindingsBuilder = Json.arrayBuilder();
                    for (Annotation binding : Components.getSortedProbeComponetAnnotationCandidates(classInterceptorBindings)) {
                        bindingsBuilder.add(Json.objectBuilder().add(VALUE, annotationToString(binding)).add(PROBE_COMPONENT, Components.isProbeAnnotation(binding)));
                    }
                    beanBuilder.add(CLASS_INTERCEPTOR_BINDINGS, bindingsBuilder);
                }
            }
            // ASSOCIATED DECORATORS
            List<Decorator<?>> decorators = abstractClassBean.getDecorators();
            if (!decorators.isEmpty()) {
                JsonArrayBuilder decoratorsBuilder = Json.arrayBuilder();
                for (Decorator<?> decorator : decorators) {
                    decoratorsBuilder.add(createSimpleBeanJson(decorator, probe));
                }
                beanBuilder.add(DECORATORS, decoratorsBuilder);
            }
        }

        return beanBuilder;
    }

    private static Set<Bean<?>> findDecoratedBeans(Decorator<?> decorator, BeanManagerImpl beanManager, Probe probe) {
        Set<Bean<?>> beans = new HashSet<Bean<?>>();
        for (Bean<?> bean : probe.getBeans()) {
            List<?> decorators;
            if (bean instanceof AbstractClassBean) {
                decorators = ((AbstractClassBean<?>) bean).getDecorators();
            } else {
                decorators = beanManager.resolveDecorators(bean.getTypes(), bean.getQualifiers());
            }
            if (decorators.contains(decorator)) {
                beans.add(bean);
            }
        }
        return beans;
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
     * The simple representation consists of the generated id, {@link Components.BeanKind}, {@link Bean#getBeanClass()} and bean
     * archive id (if not a built-in bean).
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

    static void createMetadataArrayJson(JsonArrayBuilder scanBuilder, Collection<Metadata<Filter>> metadata, String type) {
        if (metadata != null && !metadata.isEmpty()) {
            for (Metadata<Filter> filterMetadata : metadata) {
                Filter filter = filterMetadata.getValue();
                scanBuilder.add(createFilterJsonArray(filter, type));
            }
        }
    }

    static JsonObjectBuilder createFilterJsonArray(Filter filter, String type) {
        JsonObjectBuilder filterBuilder = Json.objectBuilder(true);
        filterBuilder.add(TYPE, type);
        filterBuilder.add(NAME, filter.getName());
        JsonArrayBuilder activationsBuilder = Json.arrayBuilder(true);
        filterBuilder.add(ACTIVATIONS, activationsBuilder);
        if (filter.getClassAvailableActivations() != null && !filter.getClassAvailableActivations().isEmpty()) {
            for (Metadata<ClassAvailableActivation> metadata : filter.getClassAvailableActivations()) {
                activationsBuilder.add(Json.objectBuilder().add(INVERTED, metadata.getValue().isInverted()).add(CLASS_AVAILABILITY,
                    metadata.getValue().getClassName()));
            }
        }
        if (filter.getSystemPropertyActivations() != null && !filter.getSystemPropertyActivations().isEmpty()) {
            for (Metadata<SystemPropertyActivation> metadata : filter.getSystemPropertyActivations()) {
                JsonObjectBuilder sysPropBuilder = Json.objectBuilder().add(SYS_PROPERTY, metadata.getValue().getName());
                if (metadata.getValue().getValue() != null) {
                    sysPropBuilder.add(VALUE, metadata.getValue().getValue());
                }
                activationsBuilder.add(sysPropBuilder);
            }
        }
        return filterBuilder;
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
        builder.add(DECLARING_BEAN, createDeclaringBean(bean, probe));
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
     * @param bean
     * @param probe
     * @return the declaring bean if the specified bean is an implicit producer
     */
    static JsonObjectBuilder createDeclaringBean(Bean<?> bean, Probe probe) {
        if (bean instanceof AbstractProducerBean) {
            AbstractProducerBean<?, ?, ?> producerBean = (AbstractProducerBean<?, ?, ?>) bean;
            if (producerBean.getDeclaringBean() != null) {
                return createSimpleBeanJson(producerBean.getDeclaringBean(), probe);
            }
        }
        return null;
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
            if (dep.isUnsatisfied()) {
                dependenciesBuilder.add(createDependency(null, dep, probe).add(IS_UNSATISFIED, true));
                continue;
            }
            if (dep.isAmbiguous()) {
                dependenciesBuilder.add(createDependency(null, dep, probe).add(IS_AMBIGUOUS, true));
                continue;
            }
            if (Components.isBuiltinBeanButNotExtension(dep.getBean())) {
                dependenciesBuilder.add(createBuiltInDependency(dep, probe, beanManager, DEPENDENCIES));
                continue;
            }
            // Handle circular dependencies
            if (dep.getBean().equals(parent)) {
                dependenciesBuilder.add(createDependency(dep.getBean(), dep, probe));
                continue;
            }

            JsonObjectBuilder dependency = createDependency(dep.getBean(), dep, probe);
            dependency.add(DECLARING_BEAN, createDeclaringBean(dep.getBean(), probe));
            dependenciesBuilder.add(dependency);

            if (isTransient) {
                dependency.add(DEPENDENCIES, createDependencies(bean, dep.getBean(), probe, true));
            }
        }
        return dependenciesBuilder.isEmpty() ? null : dependenciesBuilder;
    }

    /**
     * Built-in beans are identified by the set of types. Moreover, each bean deployment archive has its own instance.
     *
     * @param dependency
     * @param probe
     * @param beanManager
     * @param type
     * @return
     */
    private static JsonObjectBuilder createBuiltInDependency(Dependency dependency, Probe probe, BeanManagerImpl beanManager, String type) {
        AbstractBuiltInBean<?> builtInBean = (AbstractBuiltInBean<?>) dependency.getBean();
        JsonObjectBuilder builtInDependency = createDependency(probe.getBean(Components.getBuiltinBeanId(builtInBean)), dependency, probe);
        if (builtInBean.getBeanClass().equals(InstanceImpl.class)) {
            // Special treatment of Instance<?>
            Bean<?> lazilyFetched = Components.getInstanceResolvedBean(beanManager, dependency.getInjectionPoint());
            if (lazilyFetched != null && !Components.isBuiltinBeanButNotExtension(lazilyFetched)) {
                JsonObjectBuilder lazilyFetchedDependency = createDependency(lazilyFetched, null, probe);
                lazilyFetchedDependency.add(REQUIRED_TYPE, Formats.formatType(Components.getFacadeType(dependency.getInjectionPoint()), false)).add(QUALIFIERS,
                    createQualifiers(dependency.getInjectionPoint().getQualifiers(), false));
                lazilyFetchedDependency.add(INFO, INFO_FETCHING_LAZILY);
                lazilyFetchedDependency.add(IS_POTENTIAL, true);
                builtInDependency.add(type, Json.arrayBuilder().add(lazilyFetchedDependency));
            }
        }
        return builtInDependency;
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
                dependentsBuilder
                    .add(createDependency(probe.getBean(Components.getBuiltinBeanId((AbstractBuiltInBean<?>) dependent.getBean())), dependent, probe));
                continue;
            }
            // Handle circular dependencies
            if (dependent.getBean().equals(parent)) {
                dependentsBuilder.add(createDependency(dependent.getBean(), dependent, probe));
                continue;
            }

            JsonObjectBuilder dependency = createDependency(dependent.getBean(), dependent, probe);
            if (dependent.getInfo() != null) {
                dependency.add(INFO, dependent.getInfo());
                if (dependent.isPotential()) {
                    dependency.add(IS_POTENTIAL, true);
                }
            }
            dependency.add(DECLARING_BEAN, createDeclaringBean(dependent.getBean(), probe));
            dependentsBuilder.add(dependency);

            if (isTransient) {
                dependency.add(DEPENDENTS, createDependents(bean, dependent.getBean(), probe, true));
            }
        }
        return dependentsBuilder.isEmpty() ? null : dependentsBuilder;
    }

    /**
     *
     * @param bean
     * @param probe
     * @return
     */
    static JsonArrayBuilder createDeclaredProducers(Bean<?> bean, Probe probe) {
        JsonArrayBuilder declaredProducers = Json.arrayBuilder();
        for (AbstractProducerBean<?, ?, ?> producerBean : probe.getDeclaredProducers(bean)) {
            JsonObjectBuilder producerBuilder = createSimpleBeanJson(producerBean, probe);
            if (producerBean.getProducer() instanceof ProducerMethodProducer) {
                ProducerMethodProducer<?, ?> producer = (ProducerMethodProducer<?, ?>) producerBean.getProducer();
                producerBuilder.add(PRODUCER_INFO, annotatedMethodToString((AnnotatedMethod<?>) producer.getAnnotated(), bean.getBeanClass()));
            } else if (producerBean.getProducer() instanceof ProducerFieldProducer) {
                ProducerFieldProducer<?, ?> producer = (ProducerFieldProducer<?, ?>) producerBean.getProducer();
                producerBuilder.add(PRODUCER_INFO, annotatedFieldToString(producer.getAnnotated(), bean.getBeanClass()));
            }
            declaredProducers.add(producerBuilder);
        }
        return declaredProducers;
    }

    /**
     *
     * @param probe
     * @return the collection of all observer methods, using basic representation
     */
    static String createInvocationsJson(Page<Invocation> page, Probe probe, Representation representation) {
        JsonArrayBuilder invocationsBuilder = Json.arrayBuilder();
        if (representation == null) {
            representation = Representation.BASIC;
        }
        for (Invocation invocation : page.getData()) {
            switch (representation) {
                case BASIC:
                    invocationsBuilder.add(createBasicInvocationJson(invocation, probe));
                    break;
                case FULL:
                    invocationsBuilder.add(createFullInvocationJson(invocation, probe));
                    break;
                default:
                    throw new UnsupportedOperationException(representation.toString());
            }

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
    static String createObserversJson(Page<ObserverMethod<?>> page, Probe probe, Representation representation) {
        JsonArrayBuilder observersBuilder = Json.arrayBuilder();
        if (representation == null) {
            representation = Representation.BASIC;
        }
        for (ObserverMethod<?> observerMethod : page.getData()) {
            switch (representation) {
                case BASIC:
                    observersBuilder.add(createBasicObserverJson(observerMethod, probe));
                    break;
                case FULL:
                    observersBuilder.add(createFullObserverJson(observerMethod, probe));
                    break;
                default:
                    throw new UnsupportedOperationException(representation.toString());
            }
        }
        return createPageJson(page, observersBuilder);
    }

    /**
     *
     * @param observerMethod
     * @param probe
     * @return
     */
    static JsonObjectBuilder createFullObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = createBasicObserverJson(observerMethod, probe);
        if (observerMethod instanceof ObserverMethodImpl) {
            ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
            observerBuilder.add(ANNOTATED_METHOD, annotatedMethodToString(observerMethodImpl.getMethod().getAnnotated(), observerMethodImpl.getBeanClass()));
        }
        return observerBuilder;
    }

    static JsonObjectBuilder createBasicObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = createSimpleObserverJson(observerMethod, probe);
        observerBuilder.add(RECEPTION, observerMethod.getReception().toString());
        observerBuilder.add(TX_PHASE, observerMethod.getTransactionPhase().toString());
        if (!observerMethod.getObservedQualifiers().isEmpty()) {
            JsonArrayBuilder qualifiersBuilder = Json.arrayBuilder();
            for (Annotation qualifier : observerMethod.getObservedQualifiers()) {
                qualifiersBuilder.add(annotationToString(qualifier));
            }
            observerBuilder.add(QUALIFIERS, qualifiersBuilder);
        }
        if (observerMethod instanceof ObserverMethodImpl) {
            ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
            observerBuilder.add(DECLARING_BEAN, createSimpleBeanJson(observerMethodImpl.getDeclaringBean(), probe));
        }
        if (isUnrestrictedProcessAnnotatedTypeObserver(observerMethod)) {
            observerBuilder.add(DESCRIPTION, WARNING_UNRESTRICTED_PAT_OBSERVER);
        }
        // Every OM is now instance of Prioritized
        final int priority = Prioritized.class.cast(observerMethod).getPriority();
        observerBuilder.add(PRIORITY, priority);
        observerBuilder.add(PRIORITY_RANGE, Components.PriorityRange.of(priority).toString());

        return observerBuilder;
    }

    private static boolean isUnrestrictedProcessAnnotatedTypeObserver(ObserverMethod<?> observerMethod) {
        if (observerMethod instanceof ContainerLifecycleEventObserverMethod) {
            ContainerLifecycleEventObserverMethod<?> containerLifecycleObserverMethod = (ContainerLifecycleEventObserverMethod<?>) observerMethod;
            Class<?> rawObserverType = Reflections.getRawType(containerLifecycleObserverMethod.getObservedType());
            if ((rawObserverType.equals(ProcessAnnotatedType.class) || rawObserverType.equals(ProcessSyntheticAnnotatedType.class))
                && containerLifecycleObserverMethod.getRequiredAnnotations().isEmpty()) {
                Type eventType = containerLifecycleObserverMethod.getObservedType();
                Type[] typeArguments;
                if (eventType instanceof ParameterizedType) {
                    typeArguments = ((ParameterizedType) eventType).getActualTypeArguments();
                } else {
                    typeArguments = Arrays2.EMPTY_TYPE_ARRAY;
                }
                if (typeArguments.length == 0 || Reflections.isUnboundedWildcard(typeArguments[0]) || Reflections.isUnboundedTypeVariable(typeArguments[0])) {
                    return true;
                }
            }
        }
        return false;
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
            builder.add(CLASS, contextualInstance.getClass().getName());
            builder.add(HASH, contextualInstance.hashCode());
            builder.add(AS_STRING, Strings.abbreviate(contextualInstance.toString(), CONTEXTUAL_INSTANCE_TO_STRING_LIMIT));

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
                propertiesBuilder.add(Json.objectBuilder().add(NAME, propertyDescriptor.getDisplayName()).add(VALUE,
                    value != null ? Strings.abbreviate(value.toString(), CONTEXTUAL_INSTANCE_PROPERTY_VALUE_LIMIT) : "null"));
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

        if (req != null && ConversationScoped.class.equals(scope)) {
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
                    instanceBuilder.add(OBJECT_TO_STRING, contextualInstance.getClass().getName() + "@" + Integer.toHexString(contextualInstance.hashCode()));
                    instanceBuilder.add(AS_STRING, Strings.abbreviate(contextualInstance.toString(), CONTEXTUAL_INSTANCE_TO_STRING_LIMIT));
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
        JsonObjectBuilder builder = null;
        if (bean != null) {
            builder = createSimpleBeanJson(bean, probe);
        }
        if (dependency != null) {
            if (builder == null) {
                builder = Json.objectBuilder(true);
            }
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
                builder.add(simplifiedAnnotation(qualifier));
            } else if (Default.class.equals(qualifier.annotationType())) {
                builder.add(simplifiedAnnotation(qualifier));
            } else {
                builder.add(annotationToString(qualifier));
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
        TypeVariable<Method>[] typeParams = method.getJavaMember().getTypeParameters();
        builder.append(' ');
        if (typeParams.length > 0) {
            builder.append(Formats.formatTypeParameters(typeParams));
            builder.append(' ');
        }
        builder.append(Formats.formatType(method.getJavaMember().getGenericReturnType()));
        builder.append(' ');
        if (!beanClass.getName().equals(method.getDeclaringType().getJavaClass().getName())) {
            builder.append(Formats.formatType(method.getDeclaringType().getJavaClass()));
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
        builder.append(Formats.formatType(field.getJavaMember().getType()));
        builder.append(' ');
        if (!beanClass.getName().equals(field.getDeclaringType().getJavaClass().getName())) {
            builder.append(Formats.formatType(field.getDeclaringType().getJavaClass()));
            builder.append('.');
        }
        builder.append(field.getJavaMember().getName());
        return builder.toString();
    }

    static JsonObjectBuilder createEventJson(EventInfo event, Probe probe) {
        JsonObjectBuilder builder = Json.objectBuilder();
        builder.add(TYPE, Formats.formatType(event.getType(), event.isContainerEvent()));
        builder.add(QUALIFIERS, createQualifiers(event.getQualifiers(), true));
        builder.add(EVENT_INFO, event.getEventString());
        builder.add(KIND, (event.isContainerEvent() ? CONTAINER : APPLICATION).toUpperCase());
        builder.add(TIMESTAMP, event.getTimestamp());
        JsonArrayBuilder observersBuilder = Json.arrayBuilder();
        for (ObserverMethod<?> observer : event.getObservers()) {
            JsonObjectBuilder b = createSimpleObserverJson(observer, probe);
            if (observer instanceof ObserverMethodImpl<?, ?>) {
                ObserverMethodImpl<?, ?> weldObserver = (ObserverMethodImpl<?, ?>) observer;
                AnnotatedMethod<?> method = weldObserver.getMethod().getAnnotated();
                b.add(METHOD, method.getJavaMember().getName());
                // b.add(METHOD, method.getJavaMember().getName() + Formats.formatAsFormalParameterList(method.getParameters()));
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

    static JsonArrayBuilder createBootstrapStatsJson(Probe probe) {
        JsonArrayBuilder builder = Json.arrayBuilder();
        Map<EventType, AtomicInteger> counts = probe.getBootstrapStats().getCounts();
        List<EventType> sortedKeys = new ArrayList<>(counts.keySet());
        Collections.sort(sortedKeys, new Comparator<EventType>() {
            @Override
            public int compare(EventType o1, EventType o2) {
                return Integer.compare(o1.getPriority(), o2.getPriority());
            }
        });
        for (EventType eventType : sortedKeys) {
            JsonObjectBuilder eventBuilder = Json.objectBuilder();
            eventBuilder.add(NAME, eventType.toString());
            eventBuilder.add(TYPE, eventType.getType());
            eventBuilder.add(FIRED, counts.get(eventType).get());
            eventBuilder.add(OBSERVERS, Queries.find(probe.getObservers(), 1, 0, new ObserverFilters(probe, eventType.getType(), null)).getTotal());
            builder.add(eventBuilder);
        }
        return builder;
    }

    static JsonObjectBuilder createMonitoringStatsJson(Probe probe) {
        JsonObjectBuilder builder = Json.objectBuilder();
        builder.add(FIRED, probe.getFiredEventsCount());
        builder.add(INVOCATIONS, probe.getInvocationsCount());
        return builder;
    }

    static String simplifiedScope(Class<? extends Annotation> scope) {
        return "@" + (Components.isBuiltinScope(scope) ? scope.getSimpleName() : scope.getName());
    }

    static String simplifiedAnnotation(Annotation annotation) {
        return "@" + annotation.annotationType().getSimpleName();
    }

    private static JsonObjectBuilder decorateProbeComponent(Class<?> clazz, JsonObjectBuilder builder) {
        return Components.isProbeComponent(clazz) ? builder.add(PROBE_COMPONENT, true) : builder;
    }

    /**
     * JDK version-agnostic Annotation -> String convertor. Used because of slight differences between JDK 8 and 9+ toString()
     * implementations. We are trying to achieve something like this:
     *
     * <pre>
     *   &#064;my.own.ann.NameAndInfo(first="Alfred", middle="E.", last="Neuman", age=5)
     * </pre>
     *
     * In case of any exception (Security, IllegalAccess,...) this method uses default Annotation.toString()
     */
    static String annotationToString(Annotation annotation) {
        StringBuilder string = new StringBuilder();
        string.append('@').append(annotation.annotationType().getName()).append('(');

        String classAffix = ".class";
        String quotationMark = "\"";
        try {
            List<Method> methods = Arrays.asList(annotation.annotationType().getDeclaredMethods());
            methods.sort(new Comparator<Method>() {

                @Override
                public int compare(Method o1, Method o2) {
                    return o1.toGenericString().compareTo(o2.toGenericString());
                }
            });
            for (int i = 0; i < methods.size(); i++) {
                string.append(methods.get(i).getName()).append('=');
                Object value = methods.get(i).invoke(annotation);
                if (value instanceof boolean[]) {
                    appendInBraces(string, Arrays.toString((boolean[]) value));
                } else if (value instanceof byte[]) {
                    appendInBraces(string, Arrays.toString((byte[]) value));
                } else if (value instanceof short[]) {
                    appendInBraces(string, Arrays.toString((short[]) value));
                } else if (value instanceof int[]) {
                    appendInBraces(string, Arrays.toString((int[]) value));
                } else if (value instanceof long[]) {
                    appendInBraces(string, Arrays.toString((long[]) value));
                } else if (value instanceof float[]) {
                    appendInBraces(string, Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    appendInBraces(string, Arrays.toString((double[]) value));
                } else if (value instanceof char[]) {
                    appendInBraces(string, Arrays.toString((char[]) value));
                } else if (value instanceof String[]) {
                    String[] strings = (String[]) value;
                    String[] quoted = new String[strings.length];
                    for (int j = 0; j < strings.length; j++) {
                        quoted[j] = quotationMark + strings[j] + quotationMark;
                    }
                    appendInBraces(string, Arrays.toString(quoted));
                } else if (value instanceof Class<?>[]) {
                    Class<?>[] classes = (Class<?>[]) value;
                    String[] names = new String[classes.length];
                    for (int j = 0; j < classes.length; j++) {
                        names[j] = classes[j].getName() + classAffix;
                    }
                    appendInBraces(string, Arrays.toString(names));
                } else if (value instanceof Object[]) {
                    appendInBraces(string, Arrays.toString((Object[]) value));
                } else if (value instanceof String) {
                    string.append('"').append(value).append('"');
                } else if (value instanceof Class<?>) {
                    string.append(((Class<?>) value).getName()).append(classAffix);
                } else {
                    string.append(value);
                }
                if (i < methods.size() - 1) {
                    string.append(", ");
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException ex) {
            // we cannot do it our way, revert to default Annotation.toString()
            ProbeLogger.LOG.cannotUseUnifiedAnnotationToStringConversion(ex);
            return annotation.toString();
        }
        return string.append(')').toString();
    }

    private static void appendInBraces(StringBuilder buf, String s) {
        buf.append('{').append(s.substring(1, s.length() - 1)).append('}');
    }
}
