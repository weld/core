/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014-2019, Red Hat, Inc. and/or its affiliates, and individual
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import jakarta.annotation.Priority;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveScanner.ScanResult;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ServiceLoader;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Jozef Hartinger
 * @author <a href="https://about.me/lairdnelson"
 *         target="_parent">Laird Nelson</a>
 */
public abstract class AbstractDiscoveryStrategy implements DiscoveryStrategy {

    protected ResourceLoader resourceLoader;

    protected Bootstrap bootstrap;

    protected Set<Class<? extends Annotation>> initialBeanDefiningAnnotations;

    protected BeanArchiveScanner scanner;

    private final List<BeanArchiveHandler> handlers;

    private final BeanDiscoveryMode emptyBeansXmlDiscoveryMode;

    protected AbstractDiscoveryStrategy() {
        handlers = new LinkedList<BeanArchiveHandler>();
        this.emptyBeansXmlDiscoveryMode = BeanDiscoveryMode.ANNOTATED;
    }

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param initialBeanDefiningAnnotations
     * @param emptyBeansXmlDiscoveryMode
     */
    public AbstractDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<Class<? extends Annotation>> initialBeanDefiningAnnotations,
            BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        handlers = new LinkedList<BeanArchiveHandler>();
        setResourceLoader(resourceLoader);
        setBootstrap(bootstrap);
        setInitialBeanDefiningAnnotations(initialBeanDefiningAnnotations);
        this.emptyBeansXmlDiscoveryMode = emptyBeansXmlDiscoveryMode;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void setScanner(BeanArchiveScanner scanner) {
        this.scanner = scanner;
    }

    public void setInitialBeanDefiningAnnotations(Set<Class<? extends Annotation>> initialBeanDefiningAnnotations) {
        this.initialBeanDefiningAnnotations = initialBeanDefiningAnnotations;
    }

    @Override
    public Set<WeldBeanDeploymentArchive> performDiscovery() {

        if (scanner == null) {
            scanner = new DefaultBeanArchiveScanner(resourceLoader, bootstrap, emptyBeansXmlDiscoveryMode);
        }

        final List<BeanArchiveBuilder> beanArchiveBuilders = new ArrayList<BeanArchiveBuilder>();
        final Set<String> processedRefs = new HashSet<String>();

        List<BeanArchiveHandler> beanArchiveHandlers = initBeanArchiveHandlers();

        for (ScanResult scanResult : scanner.scan()) {
            final String ref = scanResult.getBeanArchiveRef();
            if (processedRefs.contains(ref)) {
                throw CommonLogger.LOG.invalidScanningResult(ref);
            }
            CommonLogger.LOG.processingBeanArchiveReference(ref);
            processedRefs.add(ref);
            BeanArchiveBuilder builder = null;
            for (BeanArchiveHandler handler : beanArchiveHandlers) {
                builder = handler.handle(ref);
                if (builder != null) {
                    CommonLogger.LOG.beanArchiveReferenceHandled(ref, handler);
                    builder.setId(scanResult.getBeanArchiveId());
                    builder.setBeansXml(scanResult.getBeansXml());
                    beanArchiveBuilders.add(builder);
                    break;
                }
            }
            if (builder == null) {
                CommonLogger.LOG.beanArchiveReferenceCannotBeHandled(ref, beanArchiveHandlers);
            }
        }

        beforeDiscovery(beanArchiveBuilders);
        Set<WeldBeanDeploymentArchive> archives = new HashSet<WeldBeanDeploymentArchive>();

        for (Iterator<BeanArchiveBuilder> iterator = beanArchiveBuilders.iterator(); iterator.hasNext();) {
            BeanArchiveBuilder builder = iterator.next();
            BeansXml beansXml = builder.getBeansXml();
            if (beansXml != null) {
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
                        throw CommonLogger.LOG.undefinedBeanDiscoveryValue(beansXml.getBeanDiscoveryMode());
                }
            } else {
                // A candidate for an implicit bean archive with no beans.xml
                addToArchives(archives, processAnnotatedDiscovery(builder));
            }
        }
        for (WeldBeanDeploymentArchive archive : archives) {
            archive.getServices().add(ResourceLoader.class, resourceLoader);
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
        if (bda == null) {
            return;
        }
        if (bda.isEmpty()) {
            // Most probably an unsuccessful candidate for an implicit bean archive with no beans.xml
            CommonLogger.LOG.debugv("Empty bean deployment archive ignored: {0}", bda.getId());
            return;
        }
        deploymentArchives.add(bda);
    }

    /**
     * Initialize the strategy before accessing found BeanArchiveBuilder builders. Best used for saving some information before
     * the process method for each
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

    @Override
    public void registerHandler(BeanArchiveHandler handler) {
        handlers.add(handler);
    }

    List<BeanArchiveHandler> initBeanArchiveHandlers() {
        List<SimpleEntry<Integer, BeanArchiveHandler>> entries = new ArrayList<>();

        // Add programatically added handlers
        for (ListIterator<BeanArchiveHandler> iterator = handlers.listIterator(); iterator.hasNext();) {
            entries.add(new SimpleEntry<>(handlers.size() - iterator.nextIndex(), iterator.next()));
        }

        // Load additional bean archive handlers - use Weld ServiceLoader so that we can use the given ResourceLoader
        for (Metadata<BeanArchiveHandler> meta : ServiceLoader.load(BeanArchiveHandler.class, resourceLoader)) {
            BeanArchiveHandler handler = meta.getValue();
            CommonLogger.LOG.debugv("Additional BeanArchiveHandler loaded: {0}", handler.getClass());
            entries.add(new SimpleEntry<>(getPriority(handler), handler));
        }

        Collections.sort(entries, new Comparator<SimpleEntry<Integer, BeanArchiveHandler>>() {
            @Override
            public int compare(SimpleEntry<Integer, BeanArchiveHandler> o1, SimpleEntry<Integer, BeanArchiveHandler> o2) {
                return Integer.compare(o2.getKey(), o1.getKey());
            }
        });

        List<BeanArchiveHandler> beanArchiveHandlers = new ArrayList<>(entries.size());
        for (SimpleEntry<Integer, BeanArchiveHandler> entry : entries) {
            beanArchiveHandlers.add(entry.getValue());
        }
        return beanArchiveHandlers;
    }

    private static int getPriority(BeanArchiveHandler handler) {
        Priority priority = handler.getClass().getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        return 0;
    }

}
