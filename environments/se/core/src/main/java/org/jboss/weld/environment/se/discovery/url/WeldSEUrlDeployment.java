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
package org.jboss.weld.environment.se.discovery.url;

import java.util.Collection;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Weld Deployment for Java SE environment.
 *
 * @author Peter Royle
 */
public class WeldSEUrlDeployment extends AbstractWeldSEDeployment {

    private final Collection<BeanDeploymentArchive> beanDeploymentArchives;


    public WeldSEUrlDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap, Collection<BeanDeploymentArchive> beanDeploymentArchives,
            Iterable<Metadata<Extension>> extensions) {
        super(bootstrap, extensions);
        this.beanDeploymentArchives = beanDeploymentArchives;
        for(BeanDeploymentArchive archive : beanDeploymentArchives) {
            archive.getServices().add(ResourceLoader.class, resourceLoader);
        }
    }

    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return beanDeploymentArchives;
    }

    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        // TODO: Make it much better
        return beanDeploymentArchives.iterator().next();
    }

    @Override
    public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
        for (BeanDeploymentArchive bda : beanDeploymentArchives) {
            if (bda.getBeanClasses().contains(beanClass.getName())) {
                return bda;
            }
        }
        return null;
    }
}
