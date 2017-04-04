/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.container.provider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.environment.se.WeldContainer;

public class TestExtension implements Extension {

    static AtomicReference<BeanManager> beanManagerReference = new AtomicReference<BeanManager>(null);

    static AtomicReference<Bean<?>> fooBeanReference = new AtomicReference<Bean<?>>(null);

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        CDI<Object> cdi = CDI.current();
        @SuppressWarnings("resource")
        WeldContainer container = (WeldContainer) cdi;
        assertFalse(container.isRunning());
        try {
            cdi.select(Object.class);
            fail();
        } catch (IllegalStateException expected) {
        }
        beanManagerReference.set(cdi.getBeanManager());
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        try {
            CDI.current().select(Foo.class);
            fail();
        } catch (IllegalStateException expected) {
        }
        BeanManager beanManager = beanManagerReference.get();
        if (beanManager != null) {
            fooBeanReference.set(beanManager.resolve(beanManager.getBeans(Foo.class)));
        }
    }

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        CDI.current().select(Foo.class).get().getCurrent().equals(CDI.current());
    }

    static void reset() {
        beanManagerReference.set(null);
        fooBeanReference.set(null);
    }

}
