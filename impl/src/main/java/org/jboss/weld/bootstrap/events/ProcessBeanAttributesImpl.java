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

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.configurator.BeanAttributesConfigurator;

import org.jboss.weld.bootstrap.events.configurator.BeanAttributesConfiguratorImpl;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Container lifecycle event that allows bean metadata ({@link BeanAttributes}) to be changed before the bean is registered.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of bean
 */
public class ProcessBeanAttributesImpl<T> extends AbstractDefinitionContainerEvent implements ProcessBeanAttributes<T> {

    protected static <T> ProcessBeanAttributesImpl<T> fire(BeanManagerImpl beanManager, BeanAttributes<T> attributes,
            Annotated annotated, Type type) {
        ProcessBeanAttributesImpl<T> event = new ProcessBeanAttributesImpl<T>(beanManager, attributes, annotated, type) {
        };
        event.fire();
        return event;
    }

    private ProcessBeanAttributesImpl(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated,
            Type type) {
        super(beanManager, ProcessBeanAttributes.class, new Type[] { type });
        this.attributes = attributes;
        this.annotated = annotated;
    }

    private final Annotated annotated;
    private BeanAttributes<T> attributes;
    private BeanAttributesConfiguratorImpl<T> configurator;
    private boolean veto;
    private boolean dirty;
    private boolean ignoreFinalMethods;

    // we need this to ensure that configurator and set method are not invoked within one observer
    private boolean beanAttributesSet;

    @Override
    public Annotated getAnnotated() {
        checkWithinObserverNotification();
        return annotated;
    }

    @Override
    public BeanAttributes<T> getBeanAttributes() {
        checkWithinObserverNotification();
        return attributes;
    }

    public BeanAttributes<T> getBeanAttributesInternal() {
        return attributes;
    }

    @Override
    public void setBeanAttributes(BeanAttributes<T> beanAttributes) {
        if (configurator != null) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessBeanAttributes.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        BootstrapLogger.LOG.setBeanAttributesCalled(getReceiver(), attributes, beanAttributes);
        attributes = beanAttributes;
        dirty = true;
        beanAttributesSet = true;
    }

    @Override
    public BeanAttributesConfigurator<T> configureBeanAttributes() {
        if (beanAttributesSet) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessBeanAttributes.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        if (configurator == null) {
            configurator = new BeanAttributesConfiguratorImpl<>(attributes, getBeanManager());
        }
        BootstrapLogger.LOG.configureBeanAttributesCalled(getReceiver(), attributes);
        return configurator;
    }

    @Override
    public void veto() {
        checkWithinObserverNotification();
        veto = true;
        BootstrapLogger.LOG.beanAttributesVetoed(getReceiver(), attributes);
    }

    @Override
    public void ignoreFinalMethods() {
        BootstrapLogger.LOG.ignoreFinalMethodsCalled(getReceiver(), attributes);
        ignoreFinalMethods = true;
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isIgnoreFinalMethods() {
        return ignoreFinalMethods;
    }

    @Override
    public void postNotify(Extension extension) {
        super.postNotify(extension);
        if (configurator != null) {
            attributes = configurator.complete();
            configurator = null;
            dirty = true;
        }
        beanAttributesSet = false;
    }
}
