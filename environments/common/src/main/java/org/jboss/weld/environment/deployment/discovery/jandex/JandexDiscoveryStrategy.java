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
package org.jboss.weld.environment.deployment.discovery.jandex;

import static org.jboss.weld.environment.util.Reflections.hasBeanDefiningMetaAnnotationSpecified;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.discovery.AbstractDiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.util.Reflections;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * An implementation of {@link DiscoveryStrategy} that is used when the jandex is available.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class JandexDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private static final int ANNOTATION = 0x00002000;

    private Set<DotName> beanDefiningAnnotations;

    private CompositeIndex cindex;

    private JandexClassFileServices classFileServices;

    public JandexDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap,
                                   Set<Class<? extends Annotation>> initialBeanDefiningAnnotations,
                                   BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        super(resourceLoader, bootstrap, initialBeanDefiningAnnotations, emptyBeansXmlDiscoveryMode);
        registerHandler(new JandexIndexBeanArchiveHandler());
        registerHandler(new JandexFileSystemBeanArchiveHandler());
    }

    @Override
    public ClassFileServices getClassFileServices() {
        return classFileServices;
    }

    @Override
    protected void beforeDiscovery(Collection<BeanArchiveBuilder> builders) {
        List<IndexView> indexes = new ArrayList<IndexView>();
        for (BeanArchiveBuilder builder : builders) {
            IndexView index = (IndexView) builder.getAttribute(Jandex.INDEX_ATTRIBUTE_NAME);
            if (index != null) {
                indexes.add(index);
            }
        }
        cindex = CompositeIndex.create(indexes);
        beanDefiningAnnotations = buildBeanDefiningAnnotationSet(initialBeanDefiningAnnotations, cindex);
        classFileServices = new JandexClassFileServices(this);
    }

    @Override
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        Iterator<String> classIterator = builder.getClassIterator();
        while (classIterator.hasNext()) {
            String className = classIterator.next();
            ClassInfo cinfo = cindex.getClassByName(DotName.createSimple(className));
            if (cinfo != null) {
                if (!containsBeanDefiningAnnotation(cinfo, className)) {
                    classIterator.remove();
                }
            } else {
                //if ClassInfo is not available (e.g for WEB-INF/lib/jars) then fallback to reflection
                Class<?> clazz = Reflections.loadClass(resourceLoader, className);
                if (clazz == null || !Reflections.hasBeanDefiningAnnotation(clazz, initialBeanDefiningAnnotations)) {
                    classIterator.remove();
                }
            }
        }
        return builder.build();
    }

    private Set<DotName> buildBeanDefiningAnnotationSet(Set<Class<? extends Annotation>> initialBeanDefiningAnnotations, CompositeIndex index) {
        ImmutableSet.Builder<DotName> beanDefiningAnnotations = ImmutableSet.builder();
        for (Class<? extends Annotation> annotation : initialBeanDefiningAnnotations) {
            final DotName annotationDotName = DotName.createSimple(annotation.getName());
            if (isMetaAnnotation(annotation)) {
                // find annotations annotated with this meta-annotation
                for (AnnotationInstance instance : index.getAnnotations(annotationDotName)) {
                    if (instance.target() instanceof ClassInfo) {
                        ClassInfo classInfo = instance.target().asClass();
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

    private boolean containsBeanDefiningAnnotation(ClassInfo cinfo, String className) {
        for (Entry<DotName, List<AnnotationInstance>> entry : cinfo.annotationsMap().entrySet()) {
            if (beanDefiningAnnotations.contains(entry.getKey())) {
                return true;
            }
            if (isDeclaredOnBeanClass(entry, cinfo) && cindex.getClassByName(entry.getKey()) == null) {
                // Annotation not found in the composite index - falling back to reflection
                Class<?> clazz = Reflections.loadClass(resourceLoader, className);
                if (clazz != null) {
                    for (Class<? extends Annotation> metaAnnotation : Reflections.META_ANNOTATIONS) {
                        if (hasBeanDefiningMetaAnnotationSpecified(clazz.getAnnotations(), metaAnnotation)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public CompositeIndex getCompositeJandexIndex() {
        return cindex;
    }

    private boolean isDeclaredOnBeanClass(Entry<DotName, List<AnnotationInstance>> entry, ClassInfo cinfo) {
        for (AnnotationInstance annotationInstance : entry.getValue()) {
            if (annotationInstance.target().equals(cinfo)) {
                return true;
            }
        }
        return false;
    }

}
