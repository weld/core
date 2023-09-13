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
package org.jboss.weld.environment.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

import org.jboss.weld.SimpleCDI;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagers;

/**
 *
 * @author Jozef Hartinger
 */
public class WeldProvider implements CDIProvider {

    private static class EnvironmentCDI extends SimpleCDI {

        /*
         * The BeanManager we are going to return when unable to precisely identify caller's BDA. We assume that all BDAs
         * share the same classloader and therefore visibility is not a concern. The only difference this can make is per-BDA
         * (CDI 1.0 style)
         * enablement of alternatives / interceptors and decorators. Nothing we can do about that.
         */
        private final BeanManagerImpl fallbackBeanManager;

        public EnvironmentCDI() {
            // sort the managers by their ID and use the first one as the fallback BeanManager
            // this guarantees that we consistently use the same BM
            List<BeanManagerImpl> managers = new ArrayList<BeanManagerImpl>(getContainer().beanDeploymentArchives().values());
            Collections.sort(managers, BeanManagers.ID_COMPARATOR);
            this.fallbackBeanManager = managers.get(0);
        }

        @Override
        protected BeanManagerImpl unsatisfiedBeanManager(String callerClassName) {
            return fallbackBeanManager;
        }
    }

    @Override
    public CDI<Object> getCDI() {
        return new EnvironmentCDI();
    }

}
