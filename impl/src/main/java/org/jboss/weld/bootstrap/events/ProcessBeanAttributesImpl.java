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

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.builder.BeanAttributesConfigurator;

import org.jboss.weld.bootstrap.events.builder.BeanAttributesBuilderImpl;
import org.jboss.weld.bootstrap.events.builder.BeanAttributesConfiguratorImpl;
import org.jboss.weld.exceptions.IllegalStateException;
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

    protected static <T> ProcessBeanAttributesImpl<T> fire(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        ProcessBeanAttributesImpl<T> event = new ProcessBeanAttributesImpl<T>(beanManager, attributes, annotated, type) {
        };
        event.fire();
        return event;
    }

    private ProcessBeanAttributesImpl(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        super(beanManager, ProcessBeanAttributes.class, new Type[] { type });
        this.attributes = attributes;
        this.annotated = annotated;
    }

    private final Annotated annotated;
    private BeanAttributes<T> attributes;
    private BeanAttributesConfiguratorImpl<T> configurator;
    private boolean veto;
    private boolean dirty;

    // TODO CDI-596
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
        // TODO CDI-596
        if (configurator != null) {
            throw new IllegalStateException("Configurator used");
        }
        checkWithinObserverNotification();
        BootstrapLogger.LOG.setBeanAttributesCalled(getReceiver(), attributes, beanAttributes);
        attributes = beanAttributes;
        dirty = true;
        beanAttributesSet = true;
    }

    @Override
    public BeanAttributesConfigurator<T> configureBeanAttributes() {
        // TODO CDI-596
        if (beanAttributesSet) {
            throw new IllegalStateException("setAnnotatedType() used");
        }
        checkWithinObserverNotification();
        if (configurator == null) {
            configurator = new BeanAttributesConfiguratorImpl<>(attributes);
        }
        return configurator;
    }

    @Override
    public void veto() {
        checkWithinObserverNotification();
        veto = true;
        BootstrapLogger.LOG.beanAttributesVetoed(getReceiver(), attributes);
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void postNotify(Extension extension) {
        super.postNotify(extension);
        if (configurator != null) {
            attributes = new BeanAttributesBuilderImpl<>(configurator).build();
            configurator = null;
            dirty = true;
        }
        beanAttributesSet = false;
    }
}
