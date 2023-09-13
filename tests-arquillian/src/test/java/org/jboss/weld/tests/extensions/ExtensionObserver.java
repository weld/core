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
package org.jboss.weld.tests.extensions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSessionBean;

public class ExtensionObserver implements Extension {

    private boolean allBeforeBeanDiscovery;
    private boolean allAfterBeanDiscovery;
    private boolean allAfterDeploymentValidation;
    private boolean allProcessBean;
    private boolean allProcessInjectionTarget;
    private boolean allProcessManagedBean;
    private boolean allProcessObserverMethod;
    private boolean allProcessProducer;
    private boolean allProcessProducerField;
    private boolean allProcessProducerMethod;
    private boolean allProcessSessionBean;
    private boolean allProcessAnnnotatedType;

    private boolean beforeBeanDiscovery;
    private boolean afterBeanDiscovery;
    private boolean afterDeploymentValidation;
    private boolean processBean;
    private boolean processInjectionTarget;
    private boolean processManagedBean;
    private boolean processObserverMethod;
    private boolean processProducer;
    private boolean processProducerField;
    private boolean processProducerMethod;
    private boolean processSessionBean;
    private boolean processAnnotatedType;

    private AnnotatedParameter<?> producerMethodDisposerParameter;

    public void observeAll(@Observes Object event) {
        if (event instanceof BeforeBeanDiscovery) {
            allBeforeBeanDiscovery = true;
        }
        if (event instanceof AfterBeanDiscovery) {
            allAfterBeanDiscovery = true;
        }
        if (event instanceof AfterDeploymentValidation) {
            allAfterDeploymentValidation = true;
        }
        if (event instanceof ProcessBean<?>
                && !(event instanceof ProcessProducerField<?, ?> || event instanceof ProcessProducerMethod<?, ?>
                        || event instanceof ProcessManagedBean<?> || event instanceof ProcessSessionBean<?>)) {
            allProcessBean = true;
        }
        if (event instanceof ProcessInjectionTarget<?>) {
            allProcessInjectionTarget = true;
        }
        if (event instanceof ProcessManagedBean<?>) {
            allProcessManagedBean = true;
        }
        if (event instanceof ProcessObserverMethod<?, ?>) {
            allProcessObserverMethod = true;
        }
        if (event instanceof ProcessProducer<?, ?>) {
            allProcessProducer = true;
        }
        if (event instanceof ProcessProducerField<?, ?>) {
            allProcessProducerField = true;
        }
        if (event instanceof ProcessProducerMethod<?, ?>) {
            allProcessProducerMethod = true;
        }
        if (event instanceof ProcessSessionBean<?>) {
            allProcessSessionBean = true;
        }
        if (event instanceof ProcessAnnotatedType<?>) {
            allProcessAnnnotatedType = true;
        }
    }

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        beforeBeanDiscovery = true;
    }

    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        afterBeanDiscovery = true;
    }

    public void observeAfterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        afterDeploymentValidation = true;
    }

    public void observeProcessBean(@Observes ProcessBean<?> event) {
        processBean = true;
    }

    public void observeProcessInjectionTarget(@Observes ProcessInjectionTarget<?> event) {
        processInjectionTarget = true;
    }

    public void observeProcessProducer(@Observes ProcessProducer<?, ?> event) {
        processProducer = true;
    }

    public void observeProcessProducerMethod(@Observes ProcessProducerMethod<?, Stable> event) {
        processProducerMethod = true;
        this.producerMethodDisposerParameter = event.getAnnotatedDisposedParameter();
    }

    public void observeProcessProducerField(@Observes ProcessProducerField<?, ?> event) {
        processProducerField = true;
    }

    public void observeProcessObserverMethod(@Observes ProcessObserverMethod<?, ?> event) {
        processObserverMethod = true;
    }

    public void observeProcessManagedBean(@Observes ProcessManagedBean<?> event) {
        processManagedBean = true;
    }

    public void observeProcessSessionBean(@Observes ProcessSessionBean<?> event) {
        processSessionBean = true;
    }

    public void observeProcessAnnotatedType(@Observes ProcessAnnotatedType<?> event) {
        processAnnotatedType = true;
    }

    public boolean isAllBeforeBeanDiscovery() {
        return allBeforeBeanDiscovery;
    }

    public boolean isAllAfterBeanDiscovery() {
        return allAfterBeanDiscovery;
    }

    public boolean isAllAfterDeploymentValidation() {
        return allAfterDeploymentValidation;
    }

    public boolean isAllProcessBean() {
        return allProcessBean;
    }

    public boolean isAllProcessInjectionTarget() {
        return allProcessInjectionTarget;
    }

    public boolean isAllProcessManagedBean() {
        return allProcessManagedBean;
    }

    public boolean isAllProcessObserverMethod() {
        return allProcessObserverMethod;
    }

    public boolean isAllProcessProducer() {
        return allProcessProducer;
    }

    public boolean isAllProcessProducerField() {
        return allProcessProducerField;
    }

    public boolean isAllProcessProducerMethod() {
        return allProcessProducerMethod;
    }

    public boolean isAllProcessSessionBean() {
        return allProcessSessionBean;
    }

    public boolean isAllProcessAnnnotatedType() {
        return allProcessAnnnotatedType;
    }

    public boolean isBeforeBeanDiscovery() {
        return beforeBeanDiscovery;
    }

    public boolean isAfterBeanDiscovery() {
        return afterBeanDiscovery;
    }

    public boolean isAfterDeploymentValidation() {
        return afterDeploymentValidation;
    }

    public boolean isProcessBean() {
        return processBean;
    }

    public boolean isProcessInjectionTarget() {
        return processInjectionTarget;
    }

    public boolean isProcessManagedBean() {
        return processManagedBean;
    }

    public boolean isProcessObserverMethod() {
        return processObserverMethod;
    }

    public boolean isProcessProducer() {
        return processProducer;
    }

    public boolean isProcessProducerField() {
        return processProducerField;
    }

    public boolean isProcessProducerMethod() {
        return processProducerMethod;
    }

    public AnnotatedParameter<?> getProducerMethodDisposerParameter() {
        return producerMethodDisposerParameter;
    }

    public boolean isProcessSessionBean() {
        return processSessionBean;
    }

    public boolean isProcessAnnotatedType() {
        return processAnnotatedType;
    }

}
