/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
