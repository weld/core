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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.google.common.collect.ImmutableSet;

/**
 * An implementation of {@link DiscoveryStrategy} that is used when the jandex is available. Thanks to the jandex. there is a posibility to support annotated
 * bean discovery mode.
 *
 * @author Matej Briškár
 */
public class JandexEnabledDiscoveryStrategy extends DiscoveryStrategy {

    private static final int ANNOTATION= 0x00002000;

    private final TypeDiscoveryConfiguration typeDiscoveryConfiguration;
    private Set<DotName> beanDefiningAnnotations;
    private CompositeIndex cindex;

    public JandexEnabledDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap, TypeDiscoveryConfiguration typeDiscoveryConfiguration) {
        super(resourceLoader, bootstrap);
        this.typeDiscoveryConfiguration = typeDiscoveryConfiguration;
    }

    @Override
    protected void initialize() {
        List<IndexView> indexes = new ArrayList<IndexView>();
        for (BeanArchiveBuilder builder : getBuilders()) {
            IndexView index = (IndexView) builder.getIndex();
            indexes.add(index);
        }
        cindex = CompositeIndex.create(indexes);
        beanDefiningAnnotations = buildBeanDefiningAnnotationSet(typeDiscoveryConfiguration, cindex);
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

    private Set<DotName> buildBeanDefiningAnnotationSet(TypeDiscoveryConfiguration typeDiscoveryConfiguration, CompositeIndex index) {
        ImmutableSet.Builder<DotName> beanDefiningAnnotations = ImmutableSet.builder();
        for (Class<? extends Annotation> annotation : typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations()) {
            final DotName annotationDotName = DotName.createSimple(annotation.getName());
            if (isMetaAnnotation(annotation)) {
                // find annotations annotated with this meta-annotation
                for (AnnotationInstance instance : index.getAnnotations(annotationDotName)) {
                    if (instance.target() instanceof ClassInfo) {
                        ClassInfo classInfo = (ClassInfo) instance.target();
                        if ((classInfo.flags() & ANNOTATION) != 0) {
                            beanDefiningAnnotations.add(classInfo.name());
                        }
                    }
                }
            } else {
                beanDefiningAnnotations.add(annotationDotName);
            }
        }
        return beanDefiningAnnotations.build();
    }

    private boolean isMetaAnnotation(Class<? extends Annotation> annotation) {
        Target target = annotation.getAnnotation(Target.class);
        if (target == null) {
            return false;
        }
        if (target.value() == null) {
            return false;
        }
        for (ElementType elementType : target.value()) {
            if (ElementType.ANNOTATION_TYPE.equals(elementType)) {
                return true;
            }
        }
        return false;
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
