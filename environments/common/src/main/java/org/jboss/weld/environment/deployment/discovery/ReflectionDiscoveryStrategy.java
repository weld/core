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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.environment.util.Reflections;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * This implementation supports bean-discovery-mode="annotated" and makes use of reflection to detect a class with a bean defining annotation.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class ReflectionDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private final AtomicBoolean annotatedDiscoveryProcessed;

    public ReflectionDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap,
                                       Set<Class<? extends Annotation>> initialBeanDefiningAnnotations,
                                       BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        super(resourceLoader, bootstrap, initialBeanDefiningAnnotations, emptyBeansXmlDiscoveryMode);
        this.annotatedDiscoveryProcessed = new AtomicBoolean(false);
        registerHandler(new FileSystemBeanArchiveHandler());
    }

    @Override
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        if (annotatedDiscoveryProcessed.compareAndSet(false, true)) {
            CommonLogger.LOG.reflectionFallback();
        }
        Iterator<String> classIterator = builder.getClassIterator();
        while (classIterator.hasNext()) {
            String className = classIterator.next();
            Class<?> clazz = Reflections.loadClass(resourceLoader, className);
            if (clazz == null || !Reflections.hasBeanDefiningAnnotation(clazz, initialBeanDefiningAnnotations)) {
                classIterator.remove();
            }
        }
        return builder.build();
    }

}
