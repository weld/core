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
package org.jboss.weld.manager;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.Service;

public class InjectionTargetValidator implements Service {

    private final Validator validator;
    private final Collection<InjectionTarget<?>> injectionTargets;
    private final Container container;
    private final BeanManagerImpl beanManager;

    public InjectionTargetValidator(BeanManagerImpl beanManager) {
        this.validator = new Validator();
        this.injectionTargets = new ConcurrentLinkedQueue<InjectionTarget<?>>();
        this.container = Container.instance();
        this.beanManager = beanManager;
    }

    public void addInjectionTarget(InjectionTarget<?> injectionTarget) {
        if (container.getState().equals(ContainerState.VALIDATED)) {
            // Validate now and don't store for later validation as this has been created at runtime
            validator.validateInjectionTarget(injectionTarget, beanManager);
        } else {
            injectionTargets.add(injectionTarget);
        }
    }

    public void validate() {
        for (InjectionTarget<?> injectionTarget : injectionTargets) {
            validator.validateInjectionTarget(injectionTarget, beanManager);
        }
        injectionTargets.clear();
    }

    public void cleanup() {

    }

}
