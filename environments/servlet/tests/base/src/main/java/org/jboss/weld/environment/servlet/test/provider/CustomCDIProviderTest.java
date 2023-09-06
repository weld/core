/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.servlet.test.provider;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.CDI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CustomCDIProviderTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addPackage(CustomCDIProviderTest.class.getPackage());
    }

    @Test
    public void testCustomCDIProvider() {
        // Other tests running prior to this one might have set the provider already
        // repeated set invocation leads to an exception, so we do a cautious unset
        TestCDI.unsetCDIProvider();
        try {
            CustomCDIProvider.reset();
            CDI.setCDIProvider(new CustomCDIProvider());
            CDI<Object> current = CDI.current();
            assertNotNull(current);
            assertTrue(current instanceof CustomCDIProvider.CustomCdi);
            assertTrue(CustomCDIProvider.isCalled);
        } finally {
            // Unset the CDIProvider so that other tests are not affected
            TestCDI.unsetCDIProvider();
        }
    }
}
