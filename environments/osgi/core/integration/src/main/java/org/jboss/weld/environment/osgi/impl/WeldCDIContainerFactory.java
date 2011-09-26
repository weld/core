/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.impl;

import org.jboss.weld.environment.osgi.impl.integration.Weld;
import org.jboss.weld.environment.osgi.spi.CDIContainer;
import org.jboss.weld.environment.osgi.spi.CDIContainerFactory;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is the {@link CDIContainerFactory} implementation using Weld.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class WeldCDIContainerFactory implements CDIContainerFactory {
    private Logger logger = LoggerFactory.getLogger(WeldCDIContainerFactory.class);

    private final Set<String> blackList;

    private Map<Long, CDIContainer> containers = new HashMap<Long, CDIContainer>();

    public WeldCDIContainerFactory() {
        logger.debug("Creation of a new Weld CDI container factory");
        blackList = new HashSet<String>();
        blackList.add("java.io.Serializable");
        blackList.add("org.jboss.interceptor.proxy.LifecycleMixin");
        blackList.add("org.jboss.interceptor.util.proxy.TargetInstanceProxy");
        blackList.add("javassist.util.proxy.ProxyObject");
    }

    @Override
    public CDIContainer createContainer(Bundle bundle) {
        return new WeldCDIContainer(bundle);
    }

    @Override
    public CDIContainer container(Bundle bundle) {
        if (!containers.containsKey(bundle.getBundleId())) {
            return null;
        }
        return containers.get(bundle.getBundleId());
    }

    @Override
    public Collection<CDIContainer> containers() {
        return containers.values();
    }

    @Override
    public void removeContainer(Bundle bundle) {
        containers.remove(bundle.getBundleId());
    }

    @Override
    public void addContainer(CDIContainer container) {
        containers.put(container.getBundle().getBundleId(), container);
    }

    @Override
    public String getID() {
        return Weld.class.getName();
    }

    @Override
    public Set<String> getContractBlacklist() {
        return blackList;
    }

}
