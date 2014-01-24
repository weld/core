/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Named;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.security.SetAccessibleAction;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Optimized representation of a qualifier. JDK annotation proxies are slooow, this class provides significantly
 * faster equals/hashCode methods, that also correctly handle non binding attributes.
 *
 * @author Stuart Douglas
 * @author Martin Kouba
 */
public class QualifierInstance {

    public static final QualifierInstance ANY = new QualifierInstance(Any.class);

    private final Class<? extends Annotation> annotationClass;
    private final Map<AnnotatedMethod<?>, Object> values;
    private final int hashCode;

    /**
     *
     * @param annotation
     * @param store
     * @return a qualifier instance for the given annotation
     */
    public static QualifierInstance of(Annotation annotation, MetaAnnotationStore store) {
        return getValue(annotation, store, true);
    }

    /**
    *
    * @param annotation
    * @param store
    * @param useQualifierInstanceCache
    * @return a qualifier instance for the given annotation
    */
   public static QualifierInstance of(Annotation annotation, MetaAnnotationStore store, boolean useQualifierInstanceCache) {
       return getValue(annotation, store, useQualifierInstanceCache);
   }

    /**
     *
     * @param beanManager
     * @param annotations
     * @return an immutable set of qualifier instances for the given annotations
     */
    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, final Set<Annotation> annotations) {
        return qualifiers(beanManager.getServices().get(MetaAnnotationStore.class), beanManager.getServices().get(SharedObjectCache.class), annotations);
    }

    /**
     *
     *
     * @param store
     * @param sharedObjectCache
     * @param reflectionCache
     * @param annotations
     * @return an immutable set of qualifier instances for the given annotations
     */
    public static Set<QualifierInstance> qualifiers(MetaAnnotationStore store, SharedObjectCache sharedObjectCache,
            Set<Annotation> annotations) {

        if (annotations == null || annotations.isEmpty()) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<QualifierInstance> builder = ImmutableSet.builder();
        boolean useSharedCache = (sharedObjectCache != null);

        for (Annotation annotation : annotations) {
            boolean useQualifierInstanceCache = useQualifierInstanceCache(annotation);
            if (!useQualifierInstanceCache) {
                // Don't use shared object cache if there's some qualifier instance which should not be cached
                useSharedCache = false;
            }
            builder.add(getValue(annotation, store, useQualifierInstanceCache));
        }
        return useSharedCache ? sharedObjectCache.getSharedSet(builder.build()) : builder.build();
    }

    /**
     *
     * @param beanManager
     * @param bean
     * @return an immutable set of qualifier instances for the given bean
     */
    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, Bean<?> bean) {
        if(bean instanceof RIBean) {
            return ((RIBean<?>) bean).getQualifierInstances();
        }
        return qualifiers(beanManager, bean.getQualifiers());
    }

    private QualifierInstance(final Class<? extends Annotation> annotationClass) {
        this(annotationClass, Collections.<AnnotatedMethod<?>, Object>emptyMap());
    }

    private QualifierInstance(Class<? extends Annotation> annotationClass, Map<AnnotatedMethod<?>, Object> values) {
        this.annotationClass = annotationClass;
        this.values = values;
        this.hashCode = Objects.hashCode(annotationClass, values);
    }

    private static Map<AnnotatedMethod<?>, Object> createValues(final Annotation instance, final MetaAnnotationStore store) {

        final Class<? extends Annotation> annotationClass = instance.annotationType();
        final QualifierModel<? extends Annotation> model = store.getBindingTypeModel(annotationClass);

        if(model.getAnnotatedAnnotation().getMethods().size() == 0) {
            return Collections.emptyMap();
        }

        final ImmutableMap.Builder<AnnotatedMethod<?>, Object> builder = ImmutableMap.builder();

        for (final AnnotatedMethod<?> method : model.getAnnotatedAnnotation().getMethods()) {
            if(!model.getNonBindingMembers().contains(method)) {
                try {
                    if (System.getSecurityManager() != null) {
                        AccessController.doPrivileged(SetAccessibleAction.of(method.getJavaMember()));
                    } else {
                        method.getJavaMember().setAccessible(true);
                    }
                    builder.put(method, method.getJavaMember().invoke(instance));
                } catch (IllegalAccessException e) {
                    throw new WeldException(e);
                } catch (InvocationTargetException e) {
                    throw new WeldException(e);
                }
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QualifierInstance that = (QualifierInstance) o;

        if (!annotationClass.equals(that.annotationClass)) {
            return false;
        }
        if (!values.equals(that.values)) {
            return false;
        }

        return true;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "QualifierInstance{" +
                "annotationClass=" + annotationClass +
                ", values=" + values +
                ", hashCode=" + hashCode +
                '}';
    }

    /**
     *
     * @param annotation
     * @param store
     * @param useQualifierInstanceCache
     * @return a qualifier instance for the given annotation instance
     */
    private static QualifierInstance getValue(Annotation annotation, MetaAnnotationStore store, boolean useQualifierInstanceCache) {
        return useQualifierInstanceCache ? store.getCachedQualifierInstance(annotation)
                : new QualifierInstance(annotation.annotationType(), createValues(annotation, store));
    }

    private static boolean useQualifierInstanceCache(Annotation annotation) {
        if (annotation.annotationType().equals(Named.class)) {
            // Don't cache @Named with non-default value.
            Named named = (Named) annotation;
            return named.value().equals("");
        }
        return true;
    }
}
