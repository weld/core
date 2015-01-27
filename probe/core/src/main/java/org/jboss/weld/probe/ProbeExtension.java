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
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.regex.Pattern;

import javax.decorator.Decorator;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.interceptor.Interceptor;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This extension is needed for monitoring. In particular, it adds {@link AnnotatedType}s for interceptor, interceptor binding and stereotype. Furthermore,
 * {@link BeanAttributes} of all suitable beans are modified so that a stereotype with applied interceptor binding is declared.
 *
 * <p>
 * An integrator is required to register this extension if appropriate.
 * </p>
 *
 * @author Martin Kouba
 */
public class ProbeExtension implements Extension {

    private volatile Pattern invocationMonitorExcludePattern;

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        ProbeLogger.LOG.developmentModeEnabled();
        event.addAnnotatedType(beanManager.createAnnotatedType(Monitored.class), Monitored.class.getName());
        event.addAnnotatedType(beanManager.createAnnotatedType(MonitoredComponent.class), MonitoredComponent.class.getName());
        event.addAnnotatedType(beanManager.createAnnotatedType(InvocationMonitor.class), InvocationMonitor.class.getName());
        WeldManager weldManager = (WeldManager) beanManager;
        String exclude = weldManager.getServices().get(WeldConfiguration.class).getStringProperty(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE);
        invocationMonitorExcludePattern = exclude.isEmpty() ? null : Pattern.compile(exclude);
    }

    public <T> void processBeanAttributes(@Observes ProcessBeanAttributes<T> event) {
        final BeanAttributes<T> beanAttributes = event.getBeanAttributes();
        if (isMonitored(event.getAnnotated(), beanAttributes)) {
            event.setBeanAttributes(new ForwardingBeanAttributes<T>() {
                @Override
                public Set<Class<? extends Annotation>> getStereotypes() {
                    return ImmutableSet.<Class<? extends Annotation>> builder().addAll(attributes().getStereotypes()).add(MonitoredComponent.class).build();
                }

                @Override
                protected BeanAttributes<T> attributes() {
                    return beanAttributes;
                }
            });
            ProbeLogger.LOG.monitoringStereotypeAdded(event.getAnnotated());
        }
    }

    private <T> boolean isMonitored(Annotated annotated, BeanAttributes<T> beanAttributes) {
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
        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            if (Modifier.isFinal(clazz.getModifiers())) {
                // Final classes may not have an interceptor
                return false;
            }
            if (invocationMonitorExcludePattern != null && invocationMonitorExcludePattern.matcher(clazz.getName()).matches()) {
                ProbeLogger.LOG.invocationMonitorNotAssociated(clazz.getName());
                return false;
            }
        }
        return true;
    }

}
