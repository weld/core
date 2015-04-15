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
package org.jboss.weld.environment.deployment.discovery;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveScanner.ScanResult;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Jozef Hartinger
 */
public abstract class AbstractDiscoveryStrategy implements DiscoveryStrategy {

    protected final ResourceLoader resourceLoader;

    protected final Bootstrap bootstrap;

    protected final Set<Class<? extends Annotation>> initialBeanDefiningAnnotations;

    protected BeanArchiveScanner scanner;

    private final List<BeanArchiveHandler> handlers;

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param initialBeanDefiningAnnotations
     */
    public AbstractDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap, Set<Class<? extends Annotation>> initialBeanDefiningAnnotations) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
        this.handlers = new LinkedList<BeanArchiveHandler>();
        this.initialBeanDefiningAnnotations = initialBeanDefiningAnnotations;
    }

    @Override
    public void setScanner(BeanArchiveScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Set<WeldBeanDeploymentArchive> performDiscovery() {

        if (scanner == null) {
            scanner = new DefaultBeanArchiveScanner(resourceLoader, bootstrap);
        }

        final Collection<BeanArchiveBuilder> beanArchiveBuilders = new ArrayList<BeanArchiveBuilder>();
        final Set<String> processedRefs = new HashSet<String>();

        for (ScanResult scanResult : scanner.scan().values()) {
            final String ref = scanResult.getBeanArchiveRef();
            if(processedRefs.contains(ref)) {
                throw CommonLogger.LOG.invalidScanningResult(ref);
            }
            CommonLogger.LOG.processingBeanArchiveReference(ref);
            processedRefs.add(ref);
            BeanArchiveBuilder builder = null;
            for (BeanArchiveHandler handler : handlers) {
                builder = handler.handle(ref);
                if (builder != null) {
                    builder.setId(scanResult.getBeanArchiveId());
                    builder.setBeansXml(scanResult.getBeansXml());
                    beanArchiveBuilders.add(builder);
                    break;
                }
            }
            if (builder == null) {
                CommonLogger.LOG.beanArchiveReferenceCannotBeHandled(ref, handlers);
            }
        }

        beforeDiscovery(beanArchiveBuilders);
        Set<WeldBeanDeploymentArchive> archives = new HashSet<WeldBeanDeploymentArchive>();

        for (BeanArchiveBuilder builder : beanArchiveBuilders) {
            BeansXml beansXml = builder.getBeansXml();
            switch (beansXml.getBeanDiscoveryMode()) {
                case ALL:
                    addToArchives(archives, processAllDiscovery(builder));
                    break;
                case ANNOTATED:
                    addToArchives(archives, processAnnotatedDiscovery(builder));
                    break;
                case NONE:
                    addToArchives(archives, processNoneDiscovery(builder));
                    break;
                default:
                    CommonLogger.LOG.undefinedBeanDiscoveryValue(beansXml.getBeanDiscoveryMode());
            }
        }
        afterDiscovery(archives);
        return archives;
    }

    @Override
    public ClassFileServices getClassFileServices() {
        // By default no bytecode scanning facility available
        return null;
    }

    protected void addToArchives(Set<WeldBeanDeploymentArchive> deploymentArchives, WeldBeanDeploymentArchive bda) {
        if (bda != null) {
            deploymentArchives.add(bda);
        }
    }

    /**
     * Initialize the strategy before accessing found BeanArchiveBuilder builders. Best used for saving some information before the process method for each
     * builder is called.
     */
    protected void beforeDiscovery(Collection<BeanArchiveBuilder> builders) {
        // No-op
    }

    protected void afterDiscovery(Set<WeldBeanDeploymentArchive> archives) {
        // No-op
    }

    /**
     * Process the bean archive with bean-discovery-mode of none. The archive is ignored by default.
     */
    protected WeldBeanDeploymentArchive processNoneDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Process the bean archive with bean-discovery-mode of annotated.
     */
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        throw new UnsupportedOperationException();
    }

    /**
     * Process the bean archive with bean-discovery-mode of all.
     */
    protected WeldBeanDeploymentArchive processAllDiscovery(BeanArchiveBuilder builder) {
        return builder.build();
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public void registerHandler(BeanArchiveHandler handler) {
        handlers.add(handler);
    }

}
