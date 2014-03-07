/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.lifecycle.phase;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

public class VerifyingExtension implements Extension {

    private BeforeBeanDiscovery beforeBeanDiscovery;
    private AfterTypeDiscovery afterTypeDiscovery;
    private AfterBeanDiscovery afterBeanDiscovery;

    void o1(@Observes BeforeBeanDiscovery event) {
        this.beforeBeanDiscovery = event;
    }

    void o2(@Observes AfterTypeDiscovery event) {
        this.afterTypeDiscovery = event;
    }

    void o3(@Observes AfterBeanDiscovery event) {
        this.afterBeanDiscovery = event;
    }

    BeforeBeanDiscovery getBeforeBeanDiscovery() {
        return beforeBeanDiscovery;
    }

    AfterTypeDiscovery getAfterTypeDiscovery() {
        return afterTypeDiscovery;
    }

    AfterBeanDiscovery getAfterBeanDiscovery() {
        return afterBeanDiscovery;
    }

}
