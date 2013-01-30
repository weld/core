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
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.logging.Category;
import org.jboss.weld.logging.LoggerFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.AnnotatedTypes;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;

/**
 * @author Pete Muir
 * @author Stuart Douglas
 * @author JBoss Weld Community
 * @author Ales Justin
 */
public class ClassTransformer implements BootstrapService {

    public static ClassTransformer instance(BeanManagerImpl manager) {
        return manager.getServices().get(ClassTransformer.class);
    }

    private static Logger log = LoggerFactory.loggerFactory().getLogger(Category.CLASS_LOADING);

    private class TransformClassToWeldAnnotation implements Function<Class<? extends Annotation>, EnhancedAnnotation<?>> {
        @Override
        public EnhancedAnnotation<?> apply(Class<? extends Annotation> from) {

            SlimAnnotatedType<? extends Annotation> slimAnnotatedType = syntheticAnnotationsAnnotatedTypes.get(from);

            if (slimAnnotatedType == null) {
                /*
                 * TODO: we do not recognize the BDA that defined the annotation This could in theory cause problem is two
                 * annotations with the same name but different definition are defined within the same application (different
                 * BDAs)
                 */
                slimAnnotatedType = getBackedAnnotatedType(from, AnnotatedTypeIdentifier.NULL_BDA_ID);
            }
            return EnhancedAnnotationImpl.create(slimAnnotatedType, ClassTransformer.this);
        }
    }

    private class TransformClassToBackedAnnotatedType implements Function<TypeHolder<?>, BackedAnnotatedType<?>> {
        @Override
        public BackedAnnotatedType<?> apply(TypeHolder<?> typeHolder) {
            BackedAnnotatedType<?> type = BackedAnnotatedType.of(typeHolder.getRawType(), typeHolder.getBaseType(), cache,
                    reflectionCache, typeHolder.getBdaId());
            return updateLookupTable(type);
        }
    }

    private class TransformSlimAnnotatedTypeToEnhancedAnnotatedType implements
            Function<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> {
        @Override
        public EnhancedAnnotatedType<?> apply(SlimAnnotatedType<?> annotatedType) {
            return EnhancedAnnotatedTypeImpl.of(annotatedType, ClassTransformer.this);
        }
    }

    private static final class TypeHolder<T> {

        private final String bdaId;
        private final Class<T> rawType;
        private final Type baseType;

        private TypeHolder(Class<T> rawType, Type baseType, String bdaId) {
            this.rawType = rawType;
            this.baseType = baseType;
            this.bdaId = bdaId;
        }

        public Type getBaseType() {
            return baseType;
        }

        public Class<T> getRawType() {
            return rawType;
        }

        public String getBdaId() {
            return bdaId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeHolder<?>) {
                TypeHolder<?> that = (TypeHolder<?>) obj;
                return Objects.equal(this.getBaseType(), that.getBaseType()) && Objects.equal(this.getBdaId(), that.getBdaId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.getBaseType(), this.getBdaId());
        }

        @Override
        public String toString() {
            return getBaseType() + " from " + getBdaId();
        }
    }

    // The synthetic annotations map (annotation type -> annotated type)
    private final ConcurrentMap<Class<? extends Annotation>, UnbackedAnnotatedType<? extends Annotation>> syntheticAnnotationsAnnotatedTypes = new ConcurrentHashMap<Class<? extends Annotation>, UnbackedAnnotatedType<? extends Annotation>>();

    private final ConcurrentMap<AnnotatedTypeIdentifier, SlimAnnotatedType<?>> slimAnnotatedTypesById;

    private final ConcurrentMap<TypeHolder<?>, BackedAnnotatedType<?>> backedAnnotatedTypes;
    private final ConcurrentMap<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> enhancedAnnotatedTypes;
    private final ConcurrentMap<Class<? extends Annotation>, EnhancedAnnotation<?>> annotations;

    private final TypeStore typeStore;
    private final SharedObjectCache cache;
    private final ReflectionCache reflectionCache;

    public ClassTransformer(TypeStore typeStore, SharedObjectCache cache, ReflectionCache reflectionCache) {
        MapMaker defaultMaker = new MapMaker();
        MapMaker weakValuesMaker = new MapMaker().weakValues();
        // if an AnnotatedType reference is not retained by a Bean we are not going to need it at runtime and can therefore drop
        // it immediately
        this.backedAnnotatedTypes = weakValuesMaker.makeComputingMap(new TransformClassToBackedAnnotatedType());
        this.enhancedAnnotatedTypes = defaultMaker.makeComputingMap(new TransformSlimAnnotatedTypeToEnhancedAnnotatedType());
        this.annotations = defaultMaker.makeComputingMap(new TransformClassToWeldAnnotation());
        this.typeStore = typeStore;
        this.cache = cache;
        this.reflectionCache = reflectionCache;
        this.slimAnnotatedTypesById = new ConcurrentHashMap<AnnotatedTypeIdentifier, SlimAnnotatedType<?>>();
    }

    // Slim AnnotatedTypes

    public <T> BackedAnnotatedType<T> getBackedAnnotatedType(final Class<T> rawType, final Type baseType, final String bdaId) {
        try {
            return cast(backedAnnotatedTypes.get(new TypeHolder<T>(rawType, baseType, bdaId)));
        } catch (ComputationException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof NoClassDefFoundError || cause instanceof TypeNotPresentException
                    || cause instanceof ResourceLoadingException || cause instanceof LinkageError) {
                throw new ResourceLoadingException("Error loading class " + rawType.getName(), cause);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Error loading class '" + rawType.getName() + "' : " + cause);
                }
                throw e;
            }
        }
    }

    public <T> BackedAnnotatedType<T> getBackedAnnotatedType(final Class<T> rawType, final String bdaId) {
        return getBackedAnnotatedType(rawType, rawType, bdaId);
    }

    public <T> SlimAnnotatedType<T> getSlimAnnotatedTypeById(AnnotatedTypeIdentifier id) {
        return cast(slimAnnotatedTypesById.get(id));
    }

    public <T> UnbackedAnnotatedType<T> getUnbackedAnnotatedType(AnnotatedType<T> source, String bdaId, String suffix) {
        UnbackedAnnotatedType<T> type = UnbackedAnnotatedType.additionalAnnotatedType(source, bdaId, suffix);
        return updateLookupTable(type);
    }

    public <T> UnbackedAnnotatedType<T> getUnbackedAnnotatedType(SlimAnnotatedType<T> originalType, AnnotatedType<T> source) {
        UnbackedAnnotatedType<T> type = UnbackedAnnotatedType.modifiedAnnotatedType(originalType, source);
        return updateLookupTable(type);
    }

    public UnbackedAnnotatedType<? extends Annotation> getSyntheticAnnotationAnnotatedType(
            Class<? extends Annotation> annotationType) {
        return syntheticAnnotationsAnnotatedTypes.get(annotationType);
    }

    private <T, S extends SlimAnnotatedType<T>> S updateLookupTable(S annotatedType) {
        SlimAnnotatedType<?> previousValue = slimAnnotatedTypesById.putIfAbsent(annotatedType.getIdentifier(), annotatedType);
        if (previousValue == null) {
            return annotatedType;
        } else {
            return cast(previousValue);
        }
    }

    // Enhanced AnnotatedTypes

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(Class<T> rawType, String bdaId) {
        return getEnhancedAnnotatedType(getBackedAnnotatedType(rawType, bdaId));
    }

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(Class<T> rawType, Type baseType, String bdaId) {
        return getEnhancedAnnotatedType(getBackedAnnotatedType(rawType, baseType, bdaId));
    }

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(AnnotatedType<T> annotatedType, String bdaId) {
        if (annotatedType instanceof EnhancedAnnotatedType<?>) {
            return cast(annotatedType);
        }
        if (annotatedType instanceof SlimAnnotatedType<?>) {
            return cast(getEnhancedAnnotatedType((SlimAnnotatedType<?>) annotatedType));
        }
        return getEnhancedAnnotatedType(getUnbackedAnnotatedType(annotatedType, bdaId,
                AnnotatedTypes.createTypeId(annotatedType)));
    }

    public <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(SlimAnnotatedType<T> annotatedType) {
        return cast(enhancedAnnotatedTypes.get(annotatedType));
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

    public SharedObjectCache getSharedObjectCache() {
        return cache;
    }

    public ReflectionCache getReflectionCache() {
        return reflectionCache;
    }

    /**
     *
     * @param annotation
     */
    public void addSyntheticAnnotation(AnnotatedType<? extends Annotation> annotation, String bdaId) {
        syntheticAnnotationsAnnotatedTypes.put(annotation.getJavaClass(),
                getUnbackedAnnotatedType(annotation, bdaId, AnnotatedTypeIdentifier.SYNTHETIC_ANNOTATION_SUFFIX));
        clearAnnotationData(annotation.getJavaClass());
    }

    @Override
    public void cleanupAfterBoot() {
        this.enhancedAnnotatedTypes.clear();
        this.annotations.clear();
        for (BackedAnnotatedType<?> annotatedType : backedAnnotatedTypes.values()) {
            annotatedType.clear();
        }
        this.backedAnnotatedTypes.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
        slimAnnotatedTypesById.clear();
        syntheticAnnotationsAnnotatedTypes.clear();
    }
}
