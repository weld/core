/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * DiscoveryStrategy defines a strategy for discovering the Weld SE archive and processing the separate archives.
 *
 * @author Matej Briškár
 */
public abstract class DiscoveryStrategy {

    private final ResourceLoader resourceLoader;
    private final Bootstrap bootstrap;
    private Collection<BeanArchiveBuilder> builders;
    public static final String[] RESOURCES = { AbstractWeldSEDeployment.BEANS_XML };
    private final Set<WeldSEBeanDeploymentArchive> deploymentArchives = new HashSet<WeldSEBeanDeploymentArchive>();

    public DiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    /**
     * Discovers and return all the BeanDeploymentArchives found using the URLScanner.scan() method.
     */
    public Set<WeldSEBeanDeploymentArchive> discoverArchives() {
        builders = new URLScanner(resourceLoader, bootstrap, AbstractWeldSEDeployment.RESOURCES).scan();
        initialize();
        for (BeanArchiveBuilder builder : builders) {
            BeansXml beansXml = builder.parseBeansXml();
            switch (beansXml.getBeanDiscoveryMode()) {
                case ALL:
                    addToArchives(processAllDiscovery(builder));
                    break;
                case ANNOTATED:
                    addToArchives(processAnnotatedDiscovery(builder));
                    break;
                case NONE:
                    addToArchives(processNoneDiscovery(builder));
                    break;
                default:
                    throw new IllegalStateException("beans.xml has undefined bean discovery value:" + beansXml.getBeanDiscoveryMode());
            }
        }
        assignVisibility(deploymentArchives);
        return deploymentArchives;
    }

    private void assignVisibility(Set<WeldSEBeanDeploymentArchive> deploymentArchives) {
        for (WeldSEBeanDeploymentArchive archive : deploymentArchives) {
            archive.setBeanDeploymentArchives(deploymentArchives);
        }

    }

    public Collection<BeanArchiveBuilder> getBuilders() {
        return builders;
    }

    protected void addToArchives(WeldSEBeanDeploymentArchive bda) {
        if (bda != null) {
            deploymentArchives.add(bda);
        }
    }


    /**
     * Initialize the strategy before accessing found BeanArchiveBuilder builders. Best used for saving some information before the process method for each
     * builder is called.
     */
    protected void initialize() {
        // method for overriding
    }

    /**
     * Processes archive with none-bean-discovery mode and returns the builded BeanDeploymentArchive. Should be overridden by the subclasses.
     */
    protected WeldSEBeanDeploymentArchive processNoneDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Processes archive with annotated-bean-discovery mode and returns the builded BeanDeploymentArchive. Should be overridden by the subclasses.
     */
    protected WeldSEBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Processes archive with all-bean-discovery mode and returns the builded BeanDeploymentArchive.
     */
    protected WeldSEBeanDeploymentArchive processAllDiscovery(BeanArchiveBuilder builder) {
        WeldSEBeanDeploymentArchive bda = builder.build();
        return bda;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }


}
