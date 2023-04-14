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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

public class VerifyingExtension implements Extension {

    private BeanAttributes<Alpha> alpha;
    private BeanAttributes<Bravo> bravo;
    private BeanAttributes<Charlie> charlie;
    private int bravoPitInvocations = 0;

    public void alpha(@Observes ProcessBeanAttributes<Alpha> event) {
        Set<Type> types = event.getBeanAttributes().getTypes();
        if (!types.contains(Bravo.class) && !types.contains(Charlie.class)) {
            alpha = event.getBeanAttributes();
        }
    }

    public void bravo(@Observes ProcessBeanAttributes<Bravo> event) {
        Set<Type> types = event.getBeanAttributes().getTypes();
        if (!types.contains(Charlie.class)) {
            bravo = event.getBeanAttributes();
        }
    }

    public void charlie(@Observes ProcessBeanAttributes<Charlie> event) {
        if (event.getBeanAttributes().getTypes().contains(Charlie.class)) {
            charlie = event.getBeanAttributes();
        }
    }

    public void pitBravo(@Observes ProcessInjectionTarget<Bravo> bravoPit) {
        bravoPitInvocations++;
    }

    public BeanAttributes<Alpha> getAlpha() {
        return alpha;
    }

    public BeanAttributes<Bravo> getBravo() {
        return bravo;
    }

    public BeanAttributes<Charlie> getCharlie() {
        return charlie;
    }

    public int getBravoPitInvocations() {
        return bravoPitInvocations;
    }
}
