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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;


public class JandexEnabledDiscoveryStrategy extends DiscoveryStrategy {

    private final Set<DotName> beanDefiningAnnotations = new HashSet<DotName>();
    private CompositeIndex cindex;

    public JandexEnabledDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap, TypeDiscoveryConfiguration typeDiscoveryConfiguration) {
        super(resourceLoader, bootstrap);
        Set<Class<? extends Annotation>> knownBeanDefiningAnnotations = typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations();
        for (Class<? extends Annotation> annotation : knownBeanDefiningAnnotations) {
            DotName annotationDotName = DotName.createSimple(annotation.getName());
            beanDefiningAnnotations.add(annotationDotName);
        }
    }

    @Override
    protected void initialize() {
        List<IndexView> indexes = new ArrayList<IndexView>();
        for (BeanArchiveBuilder builder : getBuilders()) {
            IndexView index = (IndexView) builder.getIndex();
            indexes.add(index);
        }
        cindex = CompositeIndex.create(indexes);
    }

    @Override
    protected WeldSEBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        Iterator<String> classIterator = builder.getClassIterator();
        while (classIterator.hasNext()) {
            String className = classIterator.next();
            ClassInfo cinfo = cindex.getClassByName(DotName.createSimple(className));
            if (!containsBeanDefiningAnnotation(cinfo.annotations().keySet())) {
                classIterator.remove();
            }
        }
        WeldSEBeanDeploymentArchive bda = builder.build();
        return bda;
    }

    private boolean containsBeanDefiningAnnotation(Set<DotName> annotations) {
        for (DotName name : annotations) {
            if (beanDefiningAnnotations.contains(name)) {
                return true;
            }
        }
        return false;
    }

    public CompositeIndex getCompositeJandexIndex() {
        return cindex;
    }

}
