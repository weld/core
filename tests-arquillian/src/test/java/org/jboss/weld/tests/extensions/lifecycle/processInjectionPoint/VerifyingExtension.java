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
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint;

import static org.junit.Assert.assertNull;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessProducerMethod;

public class VerifyingExtension implements Extension {

    private InjectionPoint alpha;
    private InjectionPoint bravo;
    private InjectionPoint charlie;
    private InjectionPoint producerAlpha;
    private InjectionPoint producerBravo;

    private Bean<?> injectingBean;
    private Bean<?> producingBean;

    public void observeAlpha(@Observes ProcessInjectionPoint<InjectingBean, Alpha<String>> event) {
        assertNull(alpha);
        alpha = event.getInjectionPoint();
    }

    public void observeBravo(@Observes ProcessInjectionPoint<InjectingBean, Bravo<String>> event) {
        assertNull(bravo);
        bravo = event.getInjectionPoint();
    }

    public void observeCharlie(@Observes ProcessInjectionPoint<InjectingBean, Charlie> event) {
        assertNull(charlie);
        charlie = event.getInjectionPoint();
    }

    public void observeProducerAlpha(@Observes ProcessInjectionPoint<InjectingBean, Alpha<Integer>> event) {
        assertNull(producerAlpha);
        producerAlpha = event.getInjectionPoint();
    }

    public void observeProducerBravo(@Observes ProcessInjectionPoint<InjectingBean, Bravo<Integer>> event) {
        assertNull(producerBravo);
        producerBravo = event.getInjectionPoint();
    }

    public void observeInjectingManagerBean(@Observes ProcessManagedBean<InjectingBean> event) {
        assertNull(injectingBean);
        injectingBean = event.getBean();
    }

    public void observeProducingBean(@Observes ProcessProducerMethod<ProducedBean, InjectingBean> event) {
        assertNull(producingBean);
        producingBean = event.getBean();
    }

    public InjectionPoint getAlpha() {
        return alpha;
    }

    public InjectionPoint getBravo() {
        return bravo;
    }

    public InjectionPoint getCharlie() {
        return charlie;
    }

    public InjectionPoint getProducerAlpha() {
        return producerAlpha;
    }

    public InjectionPoint getProducerBravo() {
        return producerBravo;
    }

    public Bean<?> getInjectingBean() {
        return injectingBean;
    }

    public Bean<?> getProducingBean() {
        return producingBean;
    }
}
