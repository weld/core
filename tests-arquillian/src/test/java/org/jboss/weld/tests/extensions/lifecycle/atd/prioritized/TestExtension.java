/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.atd.prioritized;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.tests.extensions.lifecycle.atd.prioritized.Monitored.MonitoredLiteral;

/**
 *
 * @author Martin Kouba
 */
public class TestExtension implements Extension {

    private List<Class<?>> initialInterceptors = null;

    public void observeAfterTypeDiscovery(@Observes AfterTypeDiscovery event, BeanManager beanManager) {

        initialInterceptors = Collections.unmodifiableList(new ArrayList<Class<?>>(event.getInterceptors()));

        // Bravo interceptor removed
        for (Iterator<Class<?>> iterator = event.getInterceptors().iterator(); iterator.hasNext();) {
            if (BravoInterceptor.class.equals(iterator.next())) {
                iterator.remove();
            }
        }
        // Enable CharlieInterceptor globally
        event.getInterceptors().add(0, CharlieInterceptor.class);
    }

    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        // We have to use a negative priority value because there might be some interceptors registered automatically and we
        // want to be sure PrioritizedInterceptor will have lower priority than CharlieInterceptor
        event.addBean(new PrioritizedInterceptor(-1000, Collections.<Annotation> singleton(MonitoredLiteral.INSTANCE)));
        event.addBean(new LegacyPrioritizedInterceptor(4000, Collections.<Annotation> singleton(MonitoredLiteral.INSTANCE)));
    }

    public List<Class<?>> getInitialInterceptors() {
        return initialInterceptors;
    }

}
