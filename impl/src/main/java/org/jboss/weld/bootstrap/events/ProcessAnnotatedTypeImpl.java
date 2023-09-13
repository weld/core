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
package org.jboss.weld.bootstrap.events;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Container lifecycle event for each Java class or interface discovered by the container.
 *
 * @author pmuir
 * @author David Allen
 * @author Jozef Hartinger
 */
@SuppressWarnings("rawtypes")
public class ProcessAnnotatedTypeImpl<X> extends ContainerEvent implements ProcessAnnotatedType<X> {

    private final SlimAnnotatedType<X> originalAnnotatedType;
    private final BeanManagerImpl manager;
    private AnnotatedType<X> annotatedType;
    private AnnotatedTypeConfiguratorImpl<X> configurator;
    private boolean veto;

    // we need this to ensure that configurator and set method are not invoked within one observer
    private boolean annotatedTypeSet;

    public ProcessAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<X> annotatedType) {
        this(beanManager, annotatedType, ProcessAnnotatedType.class);
    }

    protected ProcessAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<X> annotatedType,
            Class<? extends ProcessAnnotatedType> rawType) {
        this.manager = beanManager;
        this.annotatedType = annotatedType;
        this.originalAnnotatedType = annotatedType;
    }

    @Override
    public AnnotatedType<X> getAnnotatedType() {
        checkWithinObserverNotification();
        return annotatedType;
    }

    /**
     * Call this method after all observer methods of this event have been invoked to get the final value of this
     * {@link AnnotatedType}.
     *
     * @return the resulting annotated type
     */
    public SlimAnnotatedType<X> getResultingAnnotatedType() {
        if (isDirty()) {
            return ClassTransformer.instance(manager).getUnbackedAnnotatedType(originalAnnotatedType, annotatedType);
        } else {
            return originalAnnotatedType;
        }
    }

    public SlimAnnotatedType<X> getOriginalAnnotatedType() {
        return originalAnnotatedType;
    }

    @Override
    public void setAnnotatedType(AnnotatedType<X> type) {
        if (configurator != null) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessAnnotatedType.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        if (type == null) {
            throw BootstrapLogger.LOG.annotationTypeNull(this);
        }
        replaceAnnotatedType(type);
        annotatedTypeSet = true;
        BootstrapLogger.LOG.setAnnotatedTypeCalled(getReceiver(), annotatedType, type);
    }

    @Override
    public AnnotatedTypeConfigurator<X> configureAnnotatedType() {
        if (annotatedTypeSet) {
            throw BootstrapLogger.LOG.configuratorAndSetMethodBothCalled(ProcessAnnotatedType.class.getSimpleName(),
                    getReceiver());
        }
        checkWithinObserverNotification();
        if (configurator == null) {
            configurator = new AnnotatedTypeConfiguratorImpl<>(annotatedType);
        }
        BootstrapLogger.LOG.configureAnnotatedTypeCalled(getReceiver(), annotatedType);
        return configurator;
    }

    @Override
    public void veto() {
        checkWithinObserverNotification();
        this.veto = true;
        BootstrapLogger.LOG.annotatedTypeVetoed(getReceiver(), annotatedType);
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return originalAnnotatedType != annotatedType;
    }

    @Override
    public void postNotify(Extension extension) {
        super.postNotify(extension);
        if (configurator != null) {
            replaceAnnotatedType(configurator.complete());
            configurator = null;
        }
        annotatedTypeSet = false;
    }

    @Override
    public String toString() {
        return annotatedType.toString();
    }

    private void replaceAnnotatedType(AnnotatedType<X> type) {
        if (!this.originalAnnotatedType.getJavaClass().equals(type.getJavaClass())) {
            throw BootstrapLogger.LOG.annotatedTypeJavaClassMismatch(this.annotatedType.getJavaClass(), type.getJavaClass());
        }
        AnnotatedTypeValidator.validateAnnotatedType(type);
        this.annotatedType = type;
    }

}
