/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.provider.custom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.CDI;

import org.jboss.weld.environment.se.test.WeldSETest;
import org.junit.Test;

/**
 * @author Martin Kouba
 */
public class CustomCDIProviderTest extends WeldSETest {

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
