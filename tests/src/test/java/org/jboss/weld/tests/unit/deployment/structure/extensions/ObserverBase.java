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
package org.jboss.weld.tests.unit.deployment.structure.extensions;

import javax.enterprise.inject.spi.BeanManager;

public class ObserverBase {

    protected boolean afterBeanDiscoveryCalled;
    protected boolean beforeBeanDiscoveryCalled;
    protected boolean afterDeploymentValidationCalled;
    protected boolean processProducerCalled;
    protected boolean processInjectionTargetCalled;
    protected boolean processManagedBeanCalled;

    protected BeanManager beforeBeanDiscoveryBeanManager;

    public ObserverBase() {
        super();
    }

    public boolean isAfterBeanDiscoveryCalled() {
        return afterBeanDiscoveryCalled;
    }

    public boolean isBeforeBeanDiscoveryCalled() {
        return beforeBeanDiscoveryCalled;
    }

    public boolean isAfterDeploymentValidationCalled() {
        return afterDeploymentValidationCalled;
    }

    public boolean isProcessProducerCalled() {
        return processProducerCalled;
    }

    public boolean isProcessInjectionTargetCalled() {
        return processInjectionTargetCalled;
    }

    public boolean isProcessManagedBeanCalled() {
        return processManagedBeanCalled;
    }

    public BeanManager getBeforeBeanDiscoveryBeanManager() {
        return beforeBeanDiscoveryBeanManager;
    }

}
