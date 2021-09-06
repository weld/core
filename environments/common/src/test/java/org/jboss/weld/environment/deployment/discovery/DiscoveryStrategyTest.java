/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.deployment.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class DiscoveryStrategyTest {

    @Test
    public void testBeanArchiveHandlers() {
        AbstractDiscoveryStrategy strategy = (AbstractDiscoveryStrategy) DiscoveryStrategyFactory
                .create(new ClassLoaderResourceLoader(getClass().getClassLoader()), null, Collections.emptySet(), true, BeanDiscoveryMode.ANNOTATED);
        strategy.registerHandler(new TestHandler2());
        List<BeanArchiveHandler> handlers = strategy.initBeanArchiveHandlers();
        assertEquals(3, handlers.size());
        assertTrue(handlers.get(0) instanceof TestHandler);
        assertTrue(handlers.get(1) instanceof FileSystemBeanArchiveHandler);
        assertTrue(handlers.get(2) instanceof TestHandler2);
    }

    static class TestHandler2 implements BeanArchiveHandler {

        @Override
        public BeanArchiveBuilder handle(String beanArchiveReference) {
            return null;
        }

    }

}
