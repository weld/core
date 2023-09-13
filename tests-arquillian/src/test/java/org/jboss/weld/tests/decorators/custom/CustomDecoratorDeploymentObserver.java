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

package org.jboss.weld.tests.decorators.custom;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class CustomDecoratorDeploymentObserver implements Extension {
    public void addDecorators(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.addBean(new CustomDecorator(beanManager));
    }

    /**
     * Must veto the custom decorator class, otherwise a bean will be created
     */
    @SuppressWarnings("rawtypes") // the raw observed type is intentional
    public void vetoCustomDecorator(@Observes ProcessAnnotatedType event, BeanManager beanManager) {
        if (event.getAnnotatedType().getJavaClass().equals(CustomWindowFrame.class))
            event.veto();
    }
}
