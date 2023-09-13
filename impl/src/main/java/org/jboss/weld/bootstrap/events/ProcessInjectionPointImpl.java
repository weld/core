/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.configurator.InjectionPointConfigurator;

import org.jboss.weld.bootstrap.events.configurator.InjectionPointConfiguratorImpl;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public class ProcessInjectionPointImpl<T, X> extends AbstractDefinitionContainerEvent implements ProcessInjectionPoint<T, X> {

    protected static <T, X> FieldInjectionPointAttributes<T, X> fire(FieldInjectionPointAttributes<T, X> attributes,
            Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        ProcessInjectionPointImpl<T, X> event = new ProcessInjectionPointImpl<T, X>(attributes, declaringComponentClass,
                manager,
                attributes.getAnnotated().getBaseType()) {
        };
        event.fire();
        if (!event.isDirty()) {
            return attributes;
        } else {
            return ForwardingFieldInjectionPointAttributes.of(event.getInjectionPointInternal());
        }
    }

    public static <T, X> ParameterInjectionPointAttributes<T, X> fire(ParameterInjectionPointAttributes<T, X> attributes,
            Class<?> declaringComponentClass,
            BeanManagerImpl manager) {
        ProcessInjectionPointImpl<T, X> event = new ProcessInjectionPointImpl<T, X>(attributes, declaringComponentClass,
                manager,
                attributes.getAnnotated().getBaseType()) {
        };
        event.fire();
        if (!event.isDirty()) {
            return attributes;
        } else {
            return ForwardingParameterInjectionPointAttributes.of(event.getInjectionPointInternal());
        }
    }

    protected ProcessInjectionPointImpl(InjectionPoint ip, Class<?> declaringComponentClass, BeanManagerImpl beanManager,
            Type injectionPointType) {
        super(beanManager, ProcessInjectionPoint.class,
                new Type[] { (ip.getBean() == null ? declaringComponentClass : ip.getBean().getBeanClass()),
                        injectionPointType });
        this.ip = ip;
    }

    private InjectionPoint ip;
    private InjectionPointConfiguratorImpl configurator;
    private boolean dirty;

    // we need this to ensure that configurator and set method are not invoked within one observer
    private boolean injectionPointSet;

    @Override
    public InjectionPoint getInjectionPoint() {
        checkWithinObserverNotification();
        return ip;
    }

    InjectionPoint getInjectionPointInternal() {
        return ip;
    }

    @Override
    public void setInjectionPoint(InjectionPoint injectionPoint) {
        if (configurator != null) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessInjectionPoint.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        BootstrapLogger.LOG.setInjectionPointCalled(getReceiver(), ip, injectionPoint);
        ip = injectionPoint;
        dirty = true;
        injectionPointSet = true;
    }

    @Override
    public InjectionPointConfigurator configureInjectionPoint() {
        if (injectionPointSet) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessInjectionPoint.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        if (configurator == null) {
            configurator = new InjectionPointConfiguratorImpl(ip);
        }
        BootstrapLogger.LOG.configureInjectionPointCalled(getReceiver(), ip);
        return configurator;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void postNotify(Extension extension) {
        super.postNotify(extension);
        if (configurator != null) {
            ip = configurator.complete();
            configurator = null;
            dirty = true;
        }
        injectionPointSet = false;
    }

}
