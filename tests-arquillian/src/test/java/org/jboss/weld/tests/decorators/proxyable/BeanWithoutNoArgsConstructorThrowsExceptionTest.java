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
package org.jboss.weld.tests.decorators.proxyable;

import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.proxy.DefaultProxyInstantiator;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that the correct exception is thrown for a decorated bean that
 * has no public default constructor.
 *
 * Addresses WELD-1436
 */
@RunWith(Arquillian.class)
public class BeanWithoutNoArgsConstructorThrowsExceptionTest {

    @ShouldThrowException(DeploymentException.class)
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(BeanWithoutNoArgsConstructorThrowsExceptionTest.class))
                .decorate(DecoratorBean.class)
                .addClass(DecoratedBean.class)
                .addClass(Foo.class)
                .addClass(BeanWithoutNoArgsConstructor.class)
                .addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.PROXY_INSTANTIATOR.get(), DefaultProxyInstantiator.class.getName())
                                .build(),
                        "weld.properties");
    }

    @Test
    public void testDeploymentWithoutNoArgsConstructor() {
        // should throw deployment exception
    }
}