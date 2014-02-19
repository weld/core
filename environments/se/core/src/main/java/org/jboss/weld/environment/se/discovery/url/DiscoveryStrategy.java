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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
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

    private ResourceLoader resourceLoader;
    private Bootstrap bootstrap;
    private Collection<BeanArchiveBuilder> builders;
    public static final String[] RESOURCES = { AbstractWeldSEDeployment.BEANS_XML };
    private List<BeanDeploymentArchive> deploymentArchives = new ArrayList<BeanDeploymentArchive>();

    public DiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    /**
     * Discover and return all the BeanDeploymentArchives found using the URLScanner.scan() method.
     */
    public Collection<BeanDeploymentArchive> discoverArchives() {
        builders = new URLScanner(resourceLoader, bootstrap, AbstractWeldSEDeployment.RESOURCES).scan();
        initialize();
        for (BeanArchiveBuilder builder : builders) {
            BeansXml beansXml = builder.parseBeansXml();
            switch (beansXml.getBeanDiscoveryMode()) {
                case ALL:
                    BeanDeploymentArchive archive = processAllDiscovery(builder);
                    addToArchives(archive);
                    break;
                case ANNOTATED:
                    BeanDeploymentArchive annotatedArchive = processAnnotatedDiscovery(builder);
                    addToArchives(annotatedArchive);
                    break;
                case NONE:
                    BeanDeploymentArchive noneArchive = processNoneDiscovery(builder);
                    addToArchives(noneArchive);
                    break;
                default:
                    throw new IllegalStateException("beans.xml has undefined bean discovery value:" + beansXml.getBeanDiscoveryMode());
            }
        }
        assignVisibility(deploymentArchives);
        return deploymentArchives;
    }

    private void assignVisibility(List<BeanDeploymentArchive> deploymentArchives) {
        for (BeanDeploymentArchive archive : deploymentArchives) {
            ((WeldSEBeanDeploymentArchive) archive).setBeanDeploymentArchives(deploymentArchives);
        }

    }

    public Collection<BeanArchiveBuilder> getBuilders() {
        return builders;
    }

    protected void addToArchives(BeanDeploymentArchive bda) {
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
    protected BeanDeploymentArchive processNoneDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Processes archive with annotated-bean-discovery mode and returns the builded BeanDeploymentArchive. Should be overridden by the subclasses.
     */
    protected BeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Processes archive with all-bean-discovery mode and returns the builded BeanDeploymentArchive.
     */
    protected BeanDeploymentArchive processAllDiscovery(BeanArchiveBuilder builder) {
        WeldSEBeanDeploymentArchive bda = builder.build();
        return bda;
    }


}
