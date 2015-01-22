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
import static org.jboss.weld.probe.Strings.ADDITIONAL_BDA_SUFFIX;
import static org.jboss.weld.probe.Strings.ALTERNATIVES;
import static org.jboss.weld.probe.Strings.ANNOTATED_METHOD;
import static org.jboss.weld.probe.Strings.AS_STRING;
import static org.jboss.weld.probe.Strings.BDAS;
import static org.jboss.weld.probe.Strings.BDA_ID;
import static org.jboss.weld.probe.Strings.BEANS;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.BEAN_DISCOVERY_MODE;
import static org.jboss.weld.probe.Strings.CHILDREN;
import static org.jboss.weld.probe.Strings.CONFIGURATION;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DECLARED_OBSERVERS;
import static org.jboss.weld.probe.Strings.DECLARED_PRODUCERS;
import static org.jboss.weld.probe.Strings.DECLARING_BEAN;
import static org.jboss.weld.probe.Strings.DECORATORS;
import static org.jboss.weld.probe.Strings.DEPENDENCIES;
import static org.jboss.weld.probe.Strings.DEPENDENTS;
import static org.jboss.weld.probe.Strings.DISPOSAL_METHOD;
import static org.jboss.weld.probe.Strings.EJB_NAME;
import static org.jboss.weld.probe.Strings.ENABLEMENT;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.INSTANCES;
import static org.jboss.weld.probe.Strings.INTERCEPTED_BEAN;
import static org.jboss.weld.probe.Strings.INTERCEPTORS;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.LAST_PAGE;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.NAME;
import static org.jboss.weld.probe.Strings.OBSERVED_TYPE;
import static org.jboss.weld.probe.Strings.PAGE;
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
import static org.jboss.weld.probe.Strings.TOTAL;
import static org.jboss.weld.probe.Strings.TX_PHASE;
import static org.jboss.weld.probe.Strings.TYPE;
import static org.jboss.weld.probe.Strings.TYPES;
import static org.jboss.weld.probe.Strings.VALUE;
import static org.jboss.weld.probe.Strings.WELD_VERSION;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.injection.producer.ProducerFieldProducer;
import org.jboss.weld.injection.producer.ProducerMethodProducer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.Components.Dependency;
import org.jboss.weld.probe.Json.JsonArrayBuilder;
import org.jboss.weld.probe.Json.JsonObjectBuilder;
import org.jboss.weld.probe.Queries.Page;
import org.jboss.weld.util.reflection.Formats;

/**
 * Lots of utility methods for creating JSON data.
 *
 * @author Martin Kouba
 */
final class JsonObjects {

    private static final Comparator<BeanDeploymentArchive> bdaComparator = new Comparator<BeanDeploymentArchive>() {
        @Override
        public int compare(BeanDeploymentArchive bda1, BeanDeploymentArchive bda2) {
            // Additional bean archive should have the lowest priority when sorting
            // This suffix is supported by WildFly and Weld Servlet
            int result = Boolean.compare(bda1.getId().endsWith(ADDITIONAL_BDA_SUFFIX), bda2.getId().endsWith(ADDITIONAL_BDA_SUFFIX));
            return result == 0 ? bda1.getId().compareTo(bda2.getId()) : result;
        }
    };

    private JsonObjects() {
    }

    /**
     *
     * @return
     */
    static String createRootJson() {
        return Json.newObjectBuilder().add(WELD_VERSION, Formats.version(WeldBootstrap.class.getPackage())).build();
    }

    /**
     *
     * @param beanManager
     * @return the root resource representation
     */
    static String createDeploymentJson(BeanManagerImpl beanManager) {

        Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchivesMap = Container.instance(beanManager).beanDeploymentArchives();
        JsonObjectBuilder deploymentBuilder = Json.newObjectBuilder();

        // BEAN DEPLOYMENT ARCHIVES
        JsonArrayBuilder bdasBuilder = Json.newArrayBuilder();
        List<BeanDeploymentArchive> bdas = new ArrayList<BeanDeploymentArchive>(beanDeploymentArchivesMap.keySet());
        Collections.sort(bdas, bdaComparator);
        for (BeanDeploymentArchive bda : bdas) {
            JsonObjectBuilder bdaBuilder = Json.newObjectBuilder().setIgnoreEmptyBuilders(true);
            String id = bda.getId();
            bdaBuilder.add(BDA_ID, id);
            bdaBuilder.add(ID, Components.getId(id));
            BeansXml beansXml = bda.getBeansXml();
            if (beansXml != null) {
                bdaBuilder.add(BEAN_DISCOVERY_MODE, beansXml.getBeanDiscoveryMode().toString());
            }
            // Enablement - interceptors, decorators, alternatives
            JsonObjectBuilder enablementBuilder = Json.newObjectBuilder().setIgnoreEmptyBuilders(true);
            ModuleEnablement enablement = beanDeploymentArchivesMap.get(bda).getEnabled();
            JsonArrayBuilder interceptors = Json.newArrayBuilder();
            for (Class<?> interceptor : enablement.getInterceptors()) {
                interceptors.add(interceptor.getName());
            }
            enablementBuilder.add(INTERCEPTORS, interceptors);
            JsonArrayBuilder decorators = Json.newArrayBuilder();
            for (Class<?> decorator : enablement.getDecorators()) {
                decorators.add(decorator.getName());
            }
            enablementBuilder.add(DECORATORS, decorators);
            JsonArrayBuilder alternatives = Json.newArrayBuilder();
            for (Class<?> alternative : enablement.getAllAlternatives()) {
                alternatives.add(alternative.getName());
            }
            enablementBuilder.add(ALTERNATIVES, alternatives);
            bdaBuilder.add(ENABLEMENT, enablementBuilder);
            // Accessible BDAs
            BeanManagerImpl manager = beanDeploymentArchivesMap.get(bda);
            JsonArrayBuilder accesibleBdasBuilder = Json.newArrayBuilder();
            for (BeanManagerImpl accesible : manager.getAccessibleManagers()) {
                accesibleBdasBuilder.add(Components.getId(accesible.getId()));
            }
            bdaBuilder.add(ACCESSIBLE_BDAS, accesibleBdasBuilder);
            List<Bean<?>> enabledBeans = manager.getBeans();
            int count = 0;
            for (Bean<?> bean : enabledBeans) {
                if (!Components.isBuiltinBeanButNotExtension(bean)) {
                    count++;
                }
            }
            bdaBuilder.add(BEANS, count);
            bdasBuilder.add(bdaBuilder);
        }
        deploymentBuilder.add(BDAS, bdasBuilder);

        // CONFIGURATION
        JsonArrayBuilder configBuilder = Json.newArrayBuilder();
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
            configBuilder.add(Json.newObjectBuilder().add(NAME, key.get()).add(VALUE, value.toString()));
        }
        deploymentBuilder.add(CONFIGURATION, configBuilder);

        return deploymentBuilder.build();
    }

    /**
     *
     * @param probe
     * @return the collection of all beans, using basic representation
     */
    static String createBeansJson(Page<Bean<?>> page, Probe probe) {
        JsonArrayBuilder beansBuilder = Json.newArrayBuilder();
        for (Bean<?> bean : page.getData()) {
            beansBuilder.add(createBasicBeanJson(bean, probe));
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
        beanBuilder.add(SCOPE, "@" + (Components.isBuiltinScope(bean.getScope()) ? bean.getScope().getSimpleName() : bean.getScope().getName()));
        // BEAN TYPES
        JsonArrayBuilder typesBuilder = Json.newArrayBuilder();
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
        // BDA
        BeanManagerImpl beanManager = probe.getBeanManager(bean);
        if (beanManager != null) {
            beanBuilder.add(BDA_ID, Components.getId(beanManager.getId()));
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
    static String createFullBeanJson(Bean<?> bean, boolean transientDependencies, boolean transientDependents, Probe probe) {
        JsonObjectBuilder beanBuilder = createBasicBeanJson(bean, probe).setIgnoreEmptyBuilders(true);
        // NAME
        if (bean.getName() != null) {
            beanBuilder.add(NAME, bean.getName());
        }
        // STEREOTYPES
        if (bean.getStereotypes() != null && !bean.getStereotypes().isEmpty()) {
            JsonArrayBuilder stereotypesBuilder = Json.newArrayBuilder();
            for (Class<?> stereotype : bean.getStereotypes()) {
                stereotypesBuilder.add(stereotype.getName());
            }
            beanBuilder.add(STEREOTYPES, stereotypesBuilder);
        }
        // ALTERNATIVE
        if (bean.isAlternative()) {
            beanBuilder.add(IS_ALTERNATIVE, true);
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
        JsonArrayBuilder dependents = createDependents(null, bean, probe, transientDependencies);
        if (dependents != null) {
            beanBuilder.add(DEPENDENTS, dependents);
        }
        // DECLARED OBSERVERS
        JsonArrayBuilder declaredObservers = Json.newArrayBuilder();
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
        JsonArrayBuilder declaredProducers = Json.newArrayBuilder();
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

        return beanBuilder.build();
    }

    /**
     * The simple representation consists of the generated id, {@link Components.BeanKind} and {@link Bean#getBeanClass()}.
     *
     * @param bean
     * @param probe
     * @return the simple bean representation
     */
    static JsonObjectBuilder createSimpleBeanJson(Bean<?> bean, Probe probe) {
        JsonObjectBuilder builder = Json.newObjectBuilder();
        builder.add(ID, probe.getBeanId(bean));
        builder.add(KIND, Components.BeanKind.from(bean).toString());
        builder.add(BEAN_CLASS, bean.getBeanClass().getName());
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
        JsonArrayBuilder dependenciesBuilder = Json.newArrayBuilder().setIgnoreEmptyBuilders(true);

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
            if (isTransient) {
                dependenciesBuilder.add(createDependencies(bean, dep.getBean(), probe, true));
            } else {
                dependenciesBuilder.add(createDependency(dep.getBean(), dep, probe));
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

        JsonArrayBuilder dependentsBuilder = Json.newArrayBuilder().setIgnoreEmptyBuilders(true);

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
            if (isTransient) {
                dependentsBuilder.add(createDependents(bean, dependent.getBean(), probe, true));
            } else {
                dependentsBuilder.add(createDependency(dependent.getBean(), dependent, probe));
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
        JsonArrayBuilder invocationsBuilder = Json.newArrayBuilder();
        for (Invocation invocation : page.getData()) {
            invocationsBuilder.add(createBasicInvocationJson(invocation, probe));
        }
        return createPageJson(page, invocationsBuilder);
    }

    static JsonObjectBuilder createBasicInvocationJson(Invocation invocation, Probe probe) {
        JsonObjectBuilder invocationBuilder = Json.newObjectBuilder();
        if (invocation.getEntryPointId() != null) {
            invocationBuilder.add(ID, invocation.getEntryPointId());
        }
        invocationBuilder.add(INTERCEPTED_BEAN, createSimpleBeanJson(invocation.getInterceptedBean(), probe));
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
            JsonArrayBuilder childrenBuilder = Json.newArrayBuilder();
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
        JsonArrayBuilder observersBuilder = Json.newArrayBuilder();
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
            JsonArrayBuilder qualifiersBuilder = Json.newArrayBuilder();
            for (Annotation qualifier : observerMethod.getObservedQualifiers()) {
                qualifiersBuilder.add(qualifier.toString());
            }
            observerBuilder.add(QUALIFIERS, qualifiersBuilder);
        }
        if (observerMethod instanceof ObserverMethodImpl) {
            ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
            observerBuilder.add(DECLARING_BEAN, createSimpleBeanJson(observerMethodImpl.getDeclaringBean(), probe));
        }
        return observerBuilder;
    }

    static JsonObjectBuilder createSimpleObserverJson(ObserverMethod<?> observerMethod, Probe probe) {
        JsonObjectBuilder observerBuilder = Json.newObjectBuilder();
        observerBuilder.add(ID, probe.getObserverId(observerMethod));
        observerBuilder.add(BEAN_CLASS, observerMethod.getBeanClass().getName());
        observerBuilder.add(OBSERVED_TYPE, Formats.formatType(observerMethod.getObservedType(), false));
        return observerBuilder;
    }

    static String createContextualInstanceJson(Bean<?> bean, Object contextualInstance, Probe probe) {
        try {
            JsonObjectBuilder builder = createSimpleBeanJson(bean, probe);

            JsonArrayBuilder propertiesBuilder = Json.newArrayBuilder();
            BeanInfo bi = java.beans.Introspector.getBeanInfo(contextualInstance.getClass());
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
                propertiesBuilder.add(Json.newObjectBuilder().add(NAME, propertyDescriptor.getDisplayName())
                        .add(VALUE, value != null ? value.toString() : "null"));
            }
            builder.add(PROPERTIES, propertiesBuilder);
            return builder.build();

        } catch (IntrospectionException e) {
            ProbeLogger.LOG.introspectionProblem(bean, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static String createContextsJson(BeanManagerImpl beanManager, Probe probe) {
        JsonArrayBuilder contexts = Json.newArrayBuilder();
        for (int i = 0; i < Components.INSPECTABLE_SCOPES.length; i++) {
            contexts.add(createContextJson(Components.INSPECTABLE_SCOPES[i], beanManager, probe));
        }
        return contexts.build();
    }

    static JsonObjectBuilder createContextJson(Class<? extends Annotation> scope, BeanManagerImpl beanManager, Probe probe) {
        JsonObjectBuilder builder = Json.newObjectBuilder();
        builder.add(SCOPE, scope.getName());
        builder.add(INSTANCES, inspectContext(scope, beanManager, probe));
        return builder;
    }

    private static JsonArrayBuilder inspectContext(Class<? extends Annotation> scope, BeanManagerImpl beanManager, Probe probe) {
        Context context;
        try {
            context = beanManager.getContext(scope);
        } catch (ContextNotActiveException e) {
            return null;
        }
        JsonArrayBuilder builder = Json.newArrayBuilder();
        for (Bean<?> bean : probe.getBeans()) {
            if (bean.getScope().equals(scope)) {
                Object contextualInstance = context.get(bean);
                if (contextualInstance != null) {
                    JsonObjectBuilder instanceBuilder = Json.newObjectBuilder();
                    instanceBuilder.add(ID, probe.getBeanId(bean));
                    instanceBuilder.add(BEAN_CLASS, bean.getBeanClass().getName());
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
            builder.add(REQUIRED_TYPE, dependency.getInjectionPoint().getType().toString()).add(QUALIFIERS,
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
        JsonArrayBuilder builder = Json.newArrayBuilder();
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
        return Json.newObjectBuilder().add(PAGE, page.getIdx()).add(LAST_PAGE, page.getLastIdx()).add(TOTAL, page.getTotal()).add(DATA, data).build();
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

}
