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

import static org.jboss.weld.util.reflection.Reflections.EMPTY_TYPES;

import javax.enterprise.inject.spi.AfterDeploymentValidation;

import org.jboss.weld.manager.BeanManagerImpl;

public class AfterDeploymentValidationImpl extends AbstractDeploymentContainerEvent implements AfterDeploymentValidation {

    public static void fire(BeanManagerImpl beanManager) {
        new AfterDeploymentValidationImpl(beanManager).fire();
    }

    protected AfterDeploymentValidationImpl(BeanManagerImpl beanManager) {
        super(beanManager, AfterDeploymentValidation.class, EMPTY_TYPES);
    }

    public void addDeploymentProblem(Throwable t) {
        checkWithinObserverNotification();
        getErrors().add(t);
    }

}
