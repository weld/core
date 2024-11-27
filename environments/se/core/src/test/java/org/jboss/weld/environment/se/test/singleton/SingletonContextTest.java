/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class SingletonContextTest {

    @Test
    public void testSingletonBeanLifecycle() {
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        assertEquals("bar", container.select().select(Translator.class).get().translate("hello"));
        assertTrue(Translator.isInitCallbackInvoked);
        assertTrue(Dictionary.isInitCallbackInvoked);
        weld.shutdown();
        assertTrue(Translator.isDestroyCallbackInvoked);
        assertTrue(Dictionary.isDestroyCallbackInvoked);
    }

}
