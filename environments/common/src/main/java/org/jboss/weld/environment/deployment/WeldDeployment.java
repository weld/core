/*
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
package org.jboss.weld.environment.deployment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 *
 * @author Peter Royle
 * @author Martin Kouba
 */
public class WeldDeployment extends AbstractWeldDeployment {

    public static final String ADDITIONAL_BDA_ID = WeldDeployment.class.getName() + ".additionalClasses";

    public static final String SYNTHETIC_BDA_ID = WeldDeployment.class.getName() + ".synthetic";

    private final Set<WeldBeanDeploymentArchive> beanDeploymentArchives;

    private volatile WeldBeanDeploymentArchive additionalBeanDeploymentArchive;

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param beanDeploymentArchives The set should be mutable so that additional bean deployment archives can be eventually
     *        added
     * @param extensions
     */
    public WeldDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<WeldBeanDeploymentArchive> beanDeploymentArchives,
            Iterable<Metadata<Extension>> extensions) {
        super(bootstrap, extensions);
        this.beanDeploymentArchives = beanDeploymentArchives;
        setBeanDeploymentArchivesAccessibility();
        getServices().add(ResourceLoader.class, resourceLoader);
    }

    @Override
    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return Collections.<BeanDeploymentArchive> unmodifiableSet(beanDeploymentArchives);
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        WeldBeanDeploymentArchive beanDeploymentArchive = getBeanDeploymentArchive(beanClass);
        if (beanDeploymentArchive == null) {
            beanDeploymentArchive = getAndUpdateAdditionalBeanDeploymentArchive(beanClass);
        }
        return beanDeploymentArchive;
    }

    @Override
    public WeldBeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
        for (WeldBeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            if (beanDeploymentArchive.getKnownClasses().contains(beanClass.getName())) {
                return beanDeploymentArchive;
            }
        }
        return null;
    }

    protected WeldBeanDeploymentArchive getAndUpdateAdditionalBeanDeploymentArchive(Class<?> beanClass) {
        if (additionalBeanDeploymentArchive == null) {
            synchronized (this) {
                if (additionalBeanDeploymentArchive == null) {
                    additionalBeanDeploymentArchive = createAdditionalBeanDeploymentArchive();
                }
            }
        }
        additionalBeanDeploymentArchive.addBeanClass(beanClass.getName());
        return additionalBeanDeploymentArchive;
    }

    /**
     * Additional bean deployment archives are used for extentions, synthetic annotated types and beans which do not come from a
     * bean archive.
     *
     * @param beanClass
     * @return the additional bean deployment archive
     */
    protected WeldBeanDeploymentArchive createAdditionalBeanDeploymentArchive() {
        WeldBeanDeploymentArchive additionalBda = new WeldBeanDeploymentArchive(ADDITIONAL_BDA_ID,
                Collections.synchronizedSet(new HashSet<String>()), null);
        additionalBda.getServices().addAll(getServices().entrySet());
        beanDeploymentArchives.add(additionalBda);
        setBeanDeploymentArchivesAccessibility();
        return additionalBda;
    }

    /**
     * By default all bean archives see each other.
     */
    protected void setBeanDeploymentArchivesAccessibility() {
        for (WeldBeanDeploymentArchive beanDeploymentArchive : beanDeploymentArchives) {
            Set<WeldBeanDeploymentArchive> accessibleArchives = new HashSet<>();
            for (WeldBeanDeploymentArchive candidate : beanDeploymentArchives) {
                if (candidate.equals(beanDeploymentArchive)) {
                    continue;
                }
                accessibleArchives.add(candidate);
            }
            beanDeploymentArchive.setAccessibleBeanDeploymentArchives(accessibleArchives);
        }
    }

}