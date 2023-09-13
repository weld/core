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
package org.jboss.weld.tests.beanManager.extension;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * Verifies that we there's only one instance of a given extension.
 *
 * @author Jozef Hartinger
 *
 */
public class VerifyingExtension implements Extension {

    public static final String STATE = "getExtension()";

    public void bbd(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        manager.getExtension(AlphaExtension.class).setState(STATE);
    }

    public void abd(@Observes AfterBeanDiscovery event, BeanManager manager) {
        assertEquals(STATE, manager.getExtension(AlphaExtension.class).getState());
    }

    public void adv(@Observes AfterDeploymentValidation event, BeanManager manager) {
        assertEquals(STATE, manager.getExtension(AlphaExtension.class).getState());
    }
}
