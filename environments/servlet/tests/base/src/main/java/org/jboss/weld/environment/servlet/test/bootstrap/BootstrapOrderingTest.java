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
package org.jboss.weld.environment.servlet.test.bootstrap;

import static org.jboss.weld.environment.servlet.test.bootstrap.EventHolder.events;
import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.jboss.weld.environment.servlet.test.util.Deployments.extendDefaultWebXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.servlet.ServletContextEvent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BootstrapOrderingTest {

    public static final Asset WEB_XML = new ByteArrayAsset(extendDefaultWebXml(
            "<listener><listener-class>" + MyServletContextListener.class.getName() + "</listener-class></listener>")
            .getBytes());
    public static final Asset EXTENSION = new ByteArrayAsset(MyExtension.class.getName().getBytes());

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment(WEB_XML).addPackage(BootstrapOrderingTest.class.getPackage()).addAsWebInfResource(EXTENSION,
                "classes/META-INF/services/" + Extension.class.getName());
    }

    @Test
    public void testContextInitializedCalledBeforeBeanValidation() {
        assertEquals(4, events.size());
        assertTrue(events.get(0) instanceof BeforeBeanDiscovery);
        assertTrue(events.get(1) instanceof AfterBeanDiscovery);
        assertTrue(events.get(2) instanceof AfterDeploymentValidation);
        assertTrue(events.get(3) instanceof ServletContextEvent);
    }

}
