/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.manager;

import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.CDI11Deployment;

/**
 * Simple facade over {@link CDI11Deployment} that allows {@link BeanManager} for a given class to be looked up at runtime.
 *
 * @author Jozef Hartinger
 * @author Marko Luksa
 *
 */
public class BeanManagerLookupService implements Service {

    private final CDI11Deployment deployment;
    private final ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap;

    public BeanManagerLookupService(CDI11Deployment deployment,
            ConcurrentMap<BeanDeploymentArchive, BeanManagerImpl> bdaToBeanManagerMap) {
        this.deployment = deployment;
        this.bdaToBeanManagerMap = bdaToBeanManagerMap;
    }

    private BeanManagerImpl lookupBeanManager(Class<?> javaClass) {
        if (deployment == null) {
            return null;
        }
        BeanDeploymentArchive archive = deployment.getBeanDeploymentArchive(javaClass);
        if (archive == null) {
            return null;
        }
        return bdaToBeanManagerMap.get(archive);
    }

    public static BeanManagerImpl lookupBeanManager(Class<?> javaClass, BeanManagerImpl fallback) {
        BeanManagerLookupService lookup = fallback.getServices().get(BeanManagerLookupService.class);
        if (lookup == null) {
            return fallback;
        }
        BeanManagerImpl result = lookup.lookupBeanManager(javaClass);
        if (result == null) {
            return fallback;
        } else {
            return result;
        }
    }

    @Override
    public void cleanup() {
    }

}
