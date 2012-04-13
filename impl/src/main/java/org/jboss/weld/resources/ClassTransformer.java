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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedTypeImpl;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotationImpl;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.logging.Category;
import org.jboss.weld.logging.LoggerFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;

/**
 * @author Pete Muir
 * @author Stuart Douglas
 * @author JBoss Weld Community
 * @author Ales Justin
 */
public class ClassTransformer implements Service {

    public static ClassTransformer instance(BeanManagerImpl manager) {
        return manager.getServices().get(ClassTransformer.class);
    }

    private static Logger log = LoggerFactory.loggerFactory().getLogger(Category.CLASS_LOADING);

    private class TransformClassToWeldAnnotation implements Function<Class<? extends Annotation>, EnhancedAnnotation<?>> {
        @Override
        public EnhancedAnnotation<?> apply(Class<? extends Annotation> from) {
            return EnhancedAnnotationImpl.create(getAnnotatedType(from), ClassTransformer.this);
        }
    }

    private class TransformClassToSlimAnnotatedType implements Function<TypeHolder<?>, SlimAnnotatedType<?>> {
        @Override
        public SlimAnnotatedType<?> apply(TypeHolder<?> typeHolder) {
            return BackedAnnotatedType.of(typeHolder.getRawType(), typeHolder.getBaseType(), ClassTransformer.this);
        }
    }

    private class TransformExternalAnnotatedTypeToSlimAnnotatedType implements Function<AnnotatedType<?>, SlimAnnotatedType<?>> {
        @Override
        public SlimAnnotatedType<?> apply(AnnotatedType<?> input) {
            UnbackedAnnotatedType<?> type = UnbackedAnnotatedType.of(input);
            externalSlimAnnotatedTypesById.put(AnnotatedTypes.createTypeId(type), type);
            return type;
        }
    }

    private class TransformSlimAnnotatedTypeToEnhancedAnnotatedType implements Function<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> {
        @Override
        public EnhancedAnnotatedType<?> apply(SlimAnnotatedType<?> annotatedType) {
            return EnhancedAnnotatedTypeImpl.of(annotatedType, ClassTransformer.this);
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

    private final ConcurrentMap<TypeHolder<?>, SlimAnnotatedType<?>> discoveredSlimAnnotatedTypes;
    private final ConcurrentMap<AnnotatedType<?>, SlimAnnotatedType<?>> externalSlimAnnotatedTypes;
    private final ConcurrentMap<String, UnbackedAnnotatedType<?>> externalSlimAnnotatedTypesById;
    private final ConcurrentMap<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> enhancedAnnotatedTypes;
    private final ConcurrentMap<Class<? extends Annotation>, EnhancedAnnotation<?>> annotations;
    private final TypeStore typeStore;

    public ClassTransformer(TypeStore typeStore) {
        MapMaker defaultMaker = new MapMaker();
        MapMaker weakValuesMaker = new MapMaker().weakValues();
        // if an AnnotatedType reference is not retained by a Bean we are not going to need it at runtime and can therefore drop it immediately
        this.discoveredSlimAnnotatedTypes = weakValuesMaker.makeComputingMap(new TransformClassToSlimAnnotatedType());
        this.externalSlimAnnotatedTypes = weakValuesMaker.makeComputingMap(new TransformExternalAnnotatedTypeToSlimAnnotatedType());
        this.externalSlimAnnotatedTypesById = new ConcurrentHashMap<String, UnbackedAnnotatedType<?>>();
        this.enhancedAnnotatedTypes = defaultMaker.makeComputingMap(new TransformSlimAnnotatedTypeToEnhancedAnnotatedType());
        this.annotations = defaultMaker.makeComputingMap(new TransformClassToWeldAnnotation());
        this.typeStore = typeStore;
    }

    // Slim AnnotatedTypes

    public <T> BackedAnnotatedType<T> getAnnotatedType(final Class<T> rawType, final Type baseType) {
        try {
            return cast(discoveredSlimAnnotatedTypes.get(new TypeHolder<T>(rawType, baseType)));
        } catch (ComputationException e) {
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

    public <T> BackedAnnotatedType<T> getAnnotatedType(final Class<T> rawType) {
        return getAnnotatedType(rawType, rawType);
    }

    public <T> SlimAnnotatedType<T> getAnnotatedType(final AnnotatedType<T> type) {
        if (type instanceof SlimAnnotatedType<?>) {
            return cast(type);
        }
        if (type instanceof EnhancedAnnotatedType<?>) {
            return cast(Reflections.<EnhancedAnnotatedType<T>>cast(type).slim());
        }
        return cast(externalSlimAnnotatedTypes.get(type));
    }

    public UnbackedAnnotatedType<?> getUnbackedAnnotatedType(String id) {
        return externalSlimAnnotatedTypesById.get(id);
    }

    // Enhanced AnnotatedTypes

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(Class<T> rawType) {
        return getEnhancedAnnotatedType(getAnnotatedType(rawType));
    }

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(Class<T> rawType, Type baseType) {
        return getEnhancedAnnotatedType(getAnnotatedType(rawType, baseType));
    }

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(AnnotatedType<T> annotatedType) {
        if (annotatedType instanceof EnhancedAnnotatedType<?>) {
            return cast(annotatedType);
        }
        if (annotatedType instanceof SlimAnnotatedType<?>) {
            return cast(enhancedAnnotatedTypes.get(annotatedType));
        }
        return cast(enhancedAnnotatedTypes.get(getAnnotatedType(annotatedType)));
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> EnhancedAnnotation<T> getEnhancedAnnotation(final Class<T> clazz) {
        return (EnhancedAnnotation<T>) annotations.get(clazz);
    }

    public void clearAnnotationData(Class<? extends Annotation> annotationClass) {
        annotations.remove(annotationClass);
    }

    public TypeStore getTypeStore() {
        return typeStore;
    }

    public void cleanupAfterBoot() {
        this.enhancedAnnotatedTypes.clear();
        this.annotations.clear();
    }

    public void cleanup() {
        cleanupAfterBoot();
        this.discoveredSlimAnnotatedTypes.clear();
        this.externalSlimAnnotatedTypes.clear();
        this.externalSlimAnnotatedTypesById.clear();
    }
}
