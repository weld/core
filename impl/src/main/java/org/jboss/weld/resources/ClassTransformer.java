/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.introspector.ForwardingAnnotatedType;
import org.jboss.weld.introspector.WeldAnnotation;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldAnnotationImpl;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.logging.Category;
import org.jboss.weld.logging.LoggerFactory;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

/**
 * @author Pete Muir
 * @author Stuart Douglas
 * @author JBoss Weld Community
 * @author Ales Justin
 */
public class ClassTransformer implements Service {
    private static Logger log = LoggerFactory.loggerFactory().getLogger(Category.CLASS_LOADING);

    private static class TransformTypeToWeldClass extends CacheLoader<TypeHolder<?>, WeldClass<?>> {

        private final ClassTransformer classTransformer;

        private TransformTypeToWeldClass(ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public WeldClass<?> load(TypeHolder<?> from) {
            return WeldClassImpl.of(from.getRawType(), from.getBaseType(), classTransformer);
        }

    }

    private static class TransformClassToWeldAnnotation extends CacheLoader<Class<? extends Annotation>, WeldAnnotation<?>> {

        private final ClassTransformer classTransformer;

        private TransformClassToWeldAnnotation(ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public WeldAnnotation<?> load(Class<? extends Annotation> from) {
            return WeldAnnotationImpl.create(from, classTransformer);
        }

    }

    private static class TransformAnnotatedTypeToWeldClass extends CacheLoader<AnnotatedType<?>, WeldClass<?>> {

        private final ClassTransformer classTransformer;

        private TransformAnnotatedTypeToWeldClass(ClassTransformer classTransformer) {
            super();
            this.classTransformer = classTransformer;
        }

        public WeldClass<?> load(AnnotatedType<?> from) {
            return WeldClassImpl.of(from, classTransformer);
        }

    }

    private static final class TypeHolder<T> {
        private final Class<T> rawType;
        private final Type baseType;

        private TypeHolder(Class<T> rawType, Type baseType) {
            this.rawType = rawType;
            this.baseType = baseType;
        }

        public Type getBaseType() {
            return baseType;
        }

        public Class<T> getRawType() {
            return rawType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeHolder<?>) {
                TypeHolder<?> that = (TypeHolder<?>) obj;
                return this.getBaseType().equals(that.getBaseType());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return getBaseType().hashCode();
        }

        @Override
        public String toString() {
            return getBaseType().toString();
        }
    }

    private final LoadingCache<TypeHolder<?>, WeldClass<?>> classes;
    private final LoadingCache<AnnotatedType<?>, WeldClass<?>> annotatedTypes;
    private final LoadingCache<Class<? extends Annotation>, WeldAnnotation<?>> annotations;
    private final TypeStore typeStore;

    public ClassTransformer(TypeStore typeStore) {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        this.classes = builder.build(new TransformTypeToWeldClass(this));
        this.annotatedTypes = builder.build(new TransformAnnotatedTypeToWeldClass(this));
        this.annotations = builder.build(new TransformClassToWeldAnnotation(this));
        this.typeStore = typeStore;
    }

    @SuppressWarnings("unchecked")
    public <T> WeldClass<T> loadClass(final Class<T> rawType, final Type baseType) {
        try {
            return getCastCacheValue(classes, new TypeHolder<T>(rawType, baseType));
        } catch (UncheckedExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof NoClassDefFoundError || cause instanceof TypeNotPresentException || cause instanceof ResourceLoadingException || cause instanceof LinkageError) {
                throw new ResourceLoadingException("Error loading class " + rawType.getName(), cause);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Error loading class '" + rawType.getName() + "' : " + cause);
                }
                throw e;
            }
        }
    }

    public <T> WeldClass<T> loadClass(final Class<T> clazz) {
        try {
            return getCastCacheValue(classes, new TypeHolder<T>(clazz, clazz));
        } catch (UncheckedExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof TypeNotPresentException || cause instanceof ResourceLoadingException) {
                throw new ResourceLoadingException("Error loading class " + clazz.getName(), cause);
            }
            log.trace("Exception while loading class '{}' : {}", clazz.getName(), cause);
            throw e;
        } catch (ExecutionError e) {
            // LoadingCache throws ExecutionError if an error was thrown while loading the value
            final Throwable cause = e.getCause();
            if (cause instanceof NoClassDefFoundError || cause instanceof LinkageError) {
                throw new ResourceLoadingException("Error while loading class " + clazz.getName(), cause);
            }
            log.trace("Error while loading class '{}' : {}", clazz.getName(), cause);
            throw e;
        }
    }

    public void clearAnnotationData(Class<? extends Annotation> annotationClass) {
        annotations.invalidate(annotationClass);
    }

    @SuppressWarnings("unchecked")
    public <T> WeldClass<T> loadClass(final AnnotatedType<T> clazz) {
        // don't wrap existing weld class, dup instances!
        if (clazz instanceof WeldClass) {
            return (WeldClass<T>) clazz;
        } else if (clazz instanceof ForwardingAnnotatedType && ((ForwardingAnnotatedType) clazz).delegate() instanceof WeldClass) {
            ForwardingAnnotatedType fat = (ForwardingAnnotatedType) clazz;
            return (WeldClass<T>) fat.delegate();
        } else {
            return getCastCacheValue(annotatedTypes, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> WeldAnnotation<T> loadAnnotation(final Class<T> clazz) {
        return getCastCacheValue(annotations, clazz);
    }

    public TypeStore getTypeStore() {
        return typeStore;
    }

    public void cleanup() {
        this.annotatedTypes.invalidateAll();
        this.annotations.invalidateAll();
        this.classes.invalidateAll();
    }

}
