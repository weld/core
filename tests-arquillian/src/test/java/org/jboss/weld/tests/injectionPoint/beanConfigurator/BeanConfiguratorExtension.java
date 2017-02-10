/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.injectionPoint.beanConfigurator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.exceptions.IllegalArgumentException;

public class BeanConfiguratorExtension implements Extension {

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        event.addBean().scope(Dependent.class).addType(String.class)
                .addQualifier(Juicy.Literal.INSTANCE).produceWith((i) -> {
                    InjectionPoint ip = i.select(InjectionPoint.class).get();
                    assertNotNull(ip);
                    assertNotNull(ip.getBean());
                    return ip.getBean().getBeanClass().getName();
                });
        event.addBean().scope(ApplicationScoped.class).addType(Map.class)
                .addQualifier(Juicy.Literal.INSTANCE).produceWith((i) -> {
                    try {
                        i.select(InjectionPoint.class).get();
                        fail("Cannot inject injection point metadata into non-dependent bean");
                    } catch (IllegalArgumentException expected) {
                    }
                    return new HashMap<>();
                });
    }

}
