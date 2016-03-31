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

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import javax.decorator.Decorator;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.interceptor.Interceptor;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeEventResolvable;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.bootstrap.events.RequiredAnnotationDiscovery;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.event.ResolvedObservers;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.probe.BootstrapStats.EventType;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ImmutableSet;

/**
 * This extension adds {@link AnnotatedType}s needed for monitoring. Furthermore, {@link BeanAttributes} of all suitable beans are modified so that a stereotype
 * with applied interceptor binding is declared. Finally, an initialization of the {@link Probe} component (mapping data) is triggered.
 *
 * <p>
 * An integrator is required to register this extension for every application which should be a subject of inspection.
 * </p>
 *
 * @author Martin Kouba
 */
public class ProbeExtension implements Extension {

    private final Probe probe;

    private volatile JsonDataProvider jsonDataProvider;

    private volatile Pattern invocationMonitorExcludePattern;

    private volatile boolean eventMonitorContainerLifecycleEvents;

    public ProbeExtension() {
        this.probe = new Probe();
    }

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        ProbeLogger.LOG.developmentModeEnabled();
        BeanManagerImpl manager = BeanManagerProxy.unwrap(beanManager);
        event.addAnnotatedType(manager.createAnnotatedType(Monitored.class), Monitored.class.getName());
        event.addAnnotatedType(manager.createAnnotatedType(MonitoredComponent.class), MonitoredComponent.class.getName());
        event.addAnnotatedType(manager.createAnnotatedType(InvocationMonitor.class), InvocationMonitor.class.getName());
        WeldConfiguration configuration = manager.getServices().get(WeldConfiguration.class);
        String exclude = configuration.getStringProperty(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE);
        this.invocationMonitorExcludePattern = exclude.isEmpty() ? null : Pattern.compile(exclude);
        this.jsonDataProvider = new DefaultJsonDataProvider(probe, manager);
        this.eventMonitorContainerLifecycleEvents = configuration.getBooleanProperty(ConfigurationKey.PROBE_EVENT_MONITOR_CONTAINER_LIFECYCLE_EVENTS);
        addContainerLifecycleEvent(event, null, beanManager);
    }

    public <T> void processBeanAttributes(@Observes ProcessBeanAttributes<T> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PBA);
        final BeanAttributes<T> beanAttributes = event.getBeanAttributes();
        final WeldManager weldManager = (WeldManager) beanManager;
        if (isMonitored(event.getAnnotated(), beanAttributes, weldManager)) {
            event.setBeanAttributes(new ForwardingBeanAttributes<T>() {
                @Override
                public Set<Class<? extends Annotation>> getStereotypes() {
                    return ImmutableSet.<Class<? extends Annotation>> builder().addAll(attributes().getStereotypes()).add(MonitoredComponent.class).build();
                }

                @Override
                protected BeanAttributes<T> attributes() {
                    return beanAttributes;
                }

                @Override
                public String toString() {
                    return beanAttributes.toString();
                }
            });
            ProbeLogger.LOG.monitoringStereotypeAdded(event.getAnnotated());
        }
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event, "Types: [" + Formats.formatTypes(event.getBeanAttributes().getTypes()) + "], qualifiers: ["
                    + Formats.formatAnnotations(event.getBeanAttributes().getQualifiers()) + "]", beanManager);
        }
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        BeanManagerImpl weldManager = BeanManagerProxy.unwrap(beanManager);
        String exclude = weldManager.getServices().get(WeldConfiguration.class).getStringProperty(ConfigurationKey.PROBE_EVENT_MONITOR_EXCLUDE_TYPE);
        event.addObserverMethod(new ProbeObserver(weldManager, exclude.isEmpty() ? null : Pattern.compile(exclude), probe));
        addContainerLifecycleEvent(event, null, beanManager);
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        BeanManagerImpl manager = BeanManagerProxy.unwrap(beanManager);
        probe.init(manager);
        if (isJMXSupportEnabled(manager)) {
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean(new ProbeDynamicMBean(jsonDataProvider, JsonDataProvider.class), constructProbeJsonDataMBeanName(manager, probe));
            } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                event.addDeploymentProblem(ProbeLogger.LOG.unableToRegisterMBean(JsonDataProvider.class, manager.getContextId(), e));
            }
        }
        addContainerLifecycleEvent(event, null, beanManager);
    }

    public void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
        BeanManagerImpl manager = BeanManagerProxy.unwrap(beanManager);
        if (isJMXSupportEnabled(manager)) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName name = constructProbeJsonDataMBeanName(manager, probe);
                if (mbs.isRegistered(name)) {
                    mbs.unregisterMBean(name);
                }
            } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e) {
                throw ProbeLogger.LOG.unableToUnregisterMBean(JsonDataProvider.class, manager.getContextId(), e);
            }
        }
    }

    public void processAnnotatedTypes(@Observes ProcessAnnotatedType<?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PAT);
        addContainerLifecycleEvent(event, null, beanManager);
    }

    public void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PIP);
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event, formatMember(event.getInjectionPoint().getMember()), beanManager);
        }
    }

    public void processInjectionTargets(@Observes ProcessInjectionTarget<?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PIT);
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event, Formats.formatType(event.getAnnotatedType().getBaseType(), false), beanManager);
        }
    }

    public void afterTypeDiscovery(@Observes AfterTypeDiscovery event, BeanManager beanManager) {
        addContainerLifecycleEvent(event, null, beanManager);
    }

    public void processObserverMethods(@Observes ProcessObserverMethod<?, ?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.POM);
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event,
                    event.getAnnotatedMethod() != null ? formatMember(event.getAnnotatedMethod().getJavaMember()) : event.getObserverMethod().toString(),
                    beanManager);
        }
    }

    public void processProducers(@Observes ProcessProducer<?, ?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PP);
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event, formatMember(event.getAnnotatedMember().getJavaMember()), beanManager);
        }
    }

    public void processBeans(@Observes ProcessBean<?> event, BeanManager beanManager) {
        probe.getBootstrapStats().increment(EventType.PB);
        if (eventMonitorContainerLifecycleEvents) {
            Object info;
            if (event instanceof ProcessProducerMethod) {
                info = formatMember(((ProcessProducerMethod<?, ?>) event).getAnnotatedProducerMethod().getJavaMember());
            } else if (event instanceof ProcessProducerField) {
                info = formatMember(((ProcessProducerField<?, ?>) event).getAnnotatedProducerField().getJavaMember());
            } else {
                info = Formats.formatType(event.getBean().getBeanClass(), false);
            }
            addContainerLifecycleEvent(event, info, beanManager);
        }
    }

    Probe getProbe() {
        return probe;
    }

    JsonDataProvider getJsonDataProvider() {
        return jsonDataProvider;
    }

    private boolean isJMXSupportEnabled(BeanManagerImpl manager) {
        return manager.getServices().get(WeldConfiguration.class).getBooleanProperty(ConfigurationKey.PROBE_JMX_SUPPORT);
    }

    private ObjectName constructProbeJsonDataMBeanName(BeanManagerImpl manager, Probe probe) throws MalformedObjectNameException {
        return new ObjectName(
                Probe.class.getPackage().getName() + ":type=JsonData,context=" + ObjectName.quote(manager.getContextId() + "_" + probe.getInitTs()));
    }

    private <T> boolean isMonitored(Annotated annotated, BeanAttributes<T> beanAttributes, WeldManager weldManager) {
        if (annotated.isAnnotationPresent(Interceptor.class) || annotated.isAnnotationPresent(Decorator.class)) {
            // Omit interceptors and decorators
            return false;
        }
        final Type type;
        if (annotated instanceof AnnotatedMember) {
            // AnnotatedField or AnnotatedMethod
            type = ((AnnotatedMember<?>) annotated).getDeclaringType().getBaseType();
        } else {
            type = annotated.getBaseType();
        }
        UnproxyableResolutionException unproxyableException = Proxies.getUnproxyableTypeException(type, weldManager.getServices());
        if (unproxyableException != null) {
            // A bean with an interceptor must be a proxyable
            ProbeLogger.LOG.invocationMonitorNotAssociatedNonProxyableType(type);
            ProbeLogger.LOG.catchingTrace(unproxyableException);
            return false;
        }
        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            if (invocationMonitorExcludePattern != null && invocationMonitorExcludePattern.matcher(clazz.getName()).matches()) {
                ProbeLogger.LOG.invocationMonitorNotAssociatedExcluded(clazz.getName());
                return false;
            }
        }
        return true;
    }

    private <T> void addContainerLifecycleEvent(T event, Object info, BeanManagerImpl beanManagerImpl) {
        ResolvedObservers<?> resolvedObservers = null;
        Type eventType = null;
        if (event instanceof AbstractContainerEvent) {
            AbstractContainerEvent containerEvent = (AbstractContainerEvent) event;
            eventType = containerEvent.getEventType();
            resolvedObservers = beanManagerImpl.getGlobalLenientObserverNotifier().resolveObserverMethods(eventType);
        } else if (event instanceof ProcessAnnotatedTypeImpl) {
            ProcessAnnotatedTypeImpl<?> processAnnotatedTypeEvent = (ProcessAnnotatedTypeImpl<?>) event;
            eventType = ProcessAnnotatedType.class;
            info = Formats.formatType(processAnnotatedTypeEvent.getOriginalAnnotatedType().getBaseType(), false);
            resolvedObservers = beanManagerImpl.getGlobalLenientObserverNotifier().resolveObserverMethods(
                    ProcessAnnotatedTypeEventResolvable.of(processAnnotatedTypeEvent, beanManagerImpl.getServices().get(RequiredAnnotationDiscovery.class)));
        }
        if (resolvedObservers != null && eventType != null) {
            Iterable<ObserverMethod<?>> observerMethods = Reflections.cast(resolvedObservers.getAllObservers());
            probe.addEvent(new EventInfo(eventType, Collections.<Annotation>emptySet(), info, null, observerMethods, true, System.currentTimeMillis(), false));
        }
    }

    private <T> void addContainerLifecycleEvent(T event, Object payloadInfo, BeanManager beanManager) {
        if (eventMonitorContainerLifecycleEvents) {
            addContainerLifecycleEvent(event, payloadInfo, BeanManagerProxy.unwrap(beanManager));
        }
    }

    private String formatMember(Member member) {
        StringBuilder format = new StringBuilder();
        format.append(member.getDeclaringClass().getName());
        format.append(".");
        format.append(member.getName());
        if (member instanceof Method) {
            format.append("()");
        }
        return format.toString();
    }

}
