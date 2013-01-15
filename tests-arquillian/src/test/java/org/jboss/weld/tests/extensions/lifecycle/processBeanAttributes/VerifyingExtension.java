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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

public class VerifyingExtension implements Extension {

    private BeanAttributes<Alpha> alpha;
    private BeanAttributes<Bravo> bravo;
    private BeanAttributes<Charlie> charlie;
    private BeanAttributes<Mike> mike;

    void observeAlpha(@Observes ProcessBeanAttributes<Alpha> event) {
        alpha = event.getBeanAttributes();
    }

    void observeBravo(@Observes ProcessBeanAttributes<Bravo> event) {
        bravo = event.getBeanAttributes();
    }

    void observeCharlie(@Observes ProcessBeanAttributes<Charlie> event) {
        charlie = event.getBeanAttributes();
    }

    void observeMike(@Observes ProcessBeanAttributes<Mike> event) {
        mike = event.getBeanAttributes();
    }

    protected BeanAttributes<Alpha> getAlpha() {
        return alpha;
    }

    protected BeanAttributes<Bravo> getBravo() {
        return bravo;
    }

    protected BeanAttributes<Charlie> getCharlie() {
        return charlie;
    }

    void vetoClassLevelBravoBean(@Observes ProcessAnnotatedType<Bravo> event) {
        event.veto();
    }

    void vetoClassLevelCharlieBean(@Observes ProcessAnnotatedType<Charlie> event) {
        event.veto();
    }

    public BeanAttributes<Mike> getMike() {
        return mike;
    }
}
