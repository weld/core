/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.atd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

/**
 *
 * @author Martin Kouba
 */
public class AfterTypeDiscoveryObserver implements Extension {

    private List<Class<?>> initialInterceptors = null;
    private List<Class<?>> initialAlternatives = null;
    private List<Class<?>> initialDecorators = null;

    public void observeAfterTypeDiscovery(@Observes AfterTypeDiscovery event, BeanManager beanManager) {

        initialInterceptors = Collections.unmodifiableList(new ArrayList<Class<?>>(event.getInterceptors()));
        initialAlternatives = Collections.unmodifiableList(new ArrayList<Class<?>>(event.getAlternatives()));
        initialDecorators = Collections.unmodifiableList(new ArrayList<Class<?>>(event.getDecorators()));

        // Bravo interceptor removed
        for (Iterator<Class<?>> iterator = event.getInterceptors().iterator(); iterator.hasNext();) {
            if (BravoInterceptor.class.equals(iterator.next())) {
                iterator.remove();
            }
        }
        // Enable CharlieInterceptor globally
        event.getInterceptors().add(0, CharlieInterceptor.class);

        // Revert the order of decorators
        Collections.reverse(event.getDecorators());
        // Enable CharlieDecorator globally
        event.getDecorators().add(CharlieDecorator.class);

        // Remove AlphaAlternative
        for (Iterator<Class<?>> iterator = event.getAlternatives().iterator(); iterator.hasNext();) {
            if (AlphaAlternative.class.equals(iterator.next())) {
                iterator.remove();
            }
        }
        // Enable CharlieAlternative globally
        event.getAlternatives().add(0, CharlieAlternative.class);

        // Remove alternative, interceptor and decorator via List.remove(Object)
        event.getAlternatives().remove(EchoAlternative.class);
        event.getDecorators().remove(EchoDecorator.class);
        event.getInterceptors().remove(EchoInterceptor.class);
    }

    public List<Class<?>> getInitialInterceptors() {
        return initialInterceptors;
    }

    public List<Class<?>> getInitialAlternatives() {
        return initialAlternatives;
    }

    public List<Class<?>> getInitialDecorators() {
        return initialDecorators;
    }

}
