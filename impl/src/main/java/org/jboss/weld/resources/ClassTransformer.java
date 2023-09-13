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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedTypeImpl;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotationImpl;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.reflection.Reflections;

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

    private class TransformClassToWeldAnnotation implements Function<Class<? extends Annotation>, EnhancedAnnotation<?>> {
        @Override
        public EnhancedAnnotation<?> apply(Class<? extends Annotation> from) {

            SlimAnnotatedType<? extends Annotation> slimAnnotatedType = syntheticAnnotationsAnnotatedTypes.get(from);

            if (slimAnnotatedType == null) {
                // We do not recognize the BDA that defined the annotation This could in theory cause problem is two
                // annotations with the same name but different definition are defined within the same application (different
                // BDAs)
                slimAnnotatedType = getBackedAnnotatedType(from, AnnotatedTypeIdentifier.NULL_BDA_ID);
            }
            return EnhancedAnnotationImpl.create(slimAnnotatedType, ClassTransformer.this);
        }
    }

    private class TransformClassToBackedAnnotatedType implements Function<TypeHolder<?>, BackedAnnotatedType<?>> {
        @Override
        public BackedAnnotatedType<?> apply(TypeHolder<?> typeHolder) {
            // make sure declaring class (if any) is loadable before loading this class
            Reflections.checkDeclaringClassLoadable(typeHolder.getRawType());
            BackedAnnotatedType<?> type = BackedAnnotatedType.of(typeHolder.getRawType(), typeHolder.getBaseType(), cache,
                    reflectionCache, contextId, typeHolder.getBdaId(), typeHolder.getSuffix());
            return updateLookupTable(type);
        }
    }

    private class TransformSlimAnnotatedTypeToEnhancedAnnotatedType
            implements Function<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> {
        @Override
        public EnhancedAnnotatedType<?> apply(SlimAnnotatedType<?> annotatedType) {
            return EnhancedAnnotatedTypeImpl.of(annotatedType, ClassTransformer.this);
        }
    }

    private static final class TypeHolder<T> {

        private final String bdaId;
        private final Class<T> rawType;
        private final Type baseType;
        private final String suffix;

        private TypeHolder(Class<T> rawType, Type baseType, String bdaId, String suffix) {
            this.rawType = rawType;
            this.baseType = baseType;
            this.bdaId = bdaId;
            this.suffix = suffix;
        }

        Type getBaseType() {
            return baseType;
        }

        Class<T> getRawType() {
            return rawType;
        }

        String getBdaId() {
            return bdaId;
        }

        String getSuffix() {
            return suffix;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeHolder<?>) {
                TypeHolder<?> that = (TypeHolder<?>) obj;
                return Objects.equals(this.getBaseType(), that.getBaseType())
                        && Objects.equals(this.getBdaId(), that.getBdaId())
                        && Objects.equals(this.getSuffix(), that.getSuffix());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getBaseType(), this.getBdaId(), this.getSuffix());
        }

        @Override
        public String toString() {
            return "TypeHolder [bdaId=" + bdaId + ", rawType=" + rawType + ", baseType=" + baseType + ", suffix=" + suffix
                    + "]";
        }
    }

    // The synthetic annotations map (annotation type -> annotated type)
    private final ConcurrentMap<Class<? extends Annotation>, UnbackedAnnotatedType<? extends Annotation>> syntheticAnnotationsAnnotatedTypes = new ConcurrentHashMap<Class<? extends Annotation>, UnbackedAnnotatedType<? extends Annotation>>();

    private final ConcurrentMap<AnnotatedTypeIdentifier, SlimAnnotatedType<?>> slimAnnotatedTypesById;

    private final ComputingCache<TypeHolder<?>, BackedAnnotatedType<?>> backedAnnotatedTypes;
    private final ComputingCache<SlimAnnotatedType<?>, EnhancedAnnotatedType<?>> enhancedAnnotatedTypes;
    private final ComputingCache<Class<? extends Annotation>, EnhancedAnnotation<?>> annotations;

    private final TypeStore typeStore;
    private final SharedObjectCache cache;
    private final ReflectionCache reflectionCache;

    private final String contextId;

    public ClassTransformer(TypeStore typeStore, SharedObjectCache cache, ReflectionCache reflectionCache, String contextId) {
        this.contextId = contextId;
        this.backedAnnotatedTypes = ComputingCacheBuilder.newBuilder().setWeakValues()
                .build(new TransformClassToBackedAnnotatedType());
        this.enhancedAnnotatedTypes = ComputingCacheBuilder.newBuilder()
                .build(new TransformSlimAnnotatedTypeToEnhancedAnnotatedType());
        this.annotations = ComputingCacheBuilder.newBuilder().build(new TransformClassToWeldAnnotation());
        this.typeStore = typeStore;
        this.cache = cache;
        this.reflectionCache = reflectionCache;
        this.slimAnnotatedTypesById = new ConcurrentHashMap<AnnotatedTypeIdentifier, SlimAnnotatedType<?>>();
    }

    // Slim AnnotatedTypes

    public <T> BackedAnnotatedType<T> getBackedAnnotatedType(final Class<T> rawType, final Type baseType, final String bdaId,
            final String suffix) {
        try {
            return backedAnnotatedTypes.getCastValue(new TypeHolder<T>(rawType, baseType, bdaId, suffix));
        } catch (RuntimeException e) {
            if (e instanceof TypeNotPresentException || e instanceof ResourceLoadingException) {
                BootstrapLogger.LOG.exceptionWhileLoadingClass(rawType.getName(), e);
                throw new ResourceLoadingException("Exception while loading class " + rawType.getName(), e);
            }
            throw e;
        } catch (Error e) {
            if (e instanceof NoClassDefFoundError || e instanceof LinkageError) {
                throw new ResourceLoadingException("Error while loading class " + rawType.getName(), e);
            }
            BootstrapLogger.LOG.errorWhileLoadingClass(rawType.getName(), e);
            throw e;
        }
    }

    public <T> BackedAnnotatedType<T> getBackedAnnotatedType(final Class<T> rawType, final String bdaId) {
        return getBackedAnnotatedType(rawType, rawType, bdaId, null);
    }

    public <T> BackedAnnotatedType<T> getBackedAnnotatedType(Class<T> rawType, String bdaId, String suffix) {
        return getBackedAnnotatedType(rawType, rawType, bdaId, suffix);
    }

    public <T> SlimAnnotatedType<T> getSlimAnnotatedTypeById(AnnotatedTypeIdentifier id) {
        return cast(slimAnnotatedTypesById.get(id));
    }

    public <T> UnbackedAnnotatedType<T> getUnbackedAnnotatedType(AnnotatedType<T> source, String bdaId, String suffix) {
        UnbackedAnnotatedType<T> type = UnbackedAnnotatedType.additionalAnnotatedType(contextId, source, bdaId, suffix, cache);
        return updateLookupTable(type);
    }

    public <T> UnbackedAnnotatedType<T> getUnbackedAnnotatedType(SlimAnnotatedType<T> originalType, AnnotatedType<T> source) {
        UnbackedAnnotatedType<T> type = UnbackedAnnotatedType.modifiedAnnotatedType(originalType, source, cache);
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
        return getEnhancedAnnotatedType(getBackedAnnotatedType(rawType, baseType, bdaId, null));
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
        return cast(enhancedAnnotatedTypes.getValue(annotatedType));
    }

    public <T extends Annotation> EnhancedAnnotation<T> getEnhancedAnnotation(final Class<T> clazz) {
        return annotations.getCastValue(clazz);
    }

    public void clearAnnotationData(Class<? extends Annotation> annotationClass) {
        annotations.invalidate(annotationClass);
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

    public <T> void disposeBackedAnnotatedType(Class<T> rawType, String bdaId, String suffix) {
        TypeHolder<T> typeHolder = new TypeHolder<>(rawType, rawType, bdaId, suffix);
        BackedAnnotatedType<T> annotatedType = cast(this.backedAnnotatedTypes.getValueIfPresent(typeHolder));
        if (annotatedType != null) {
            this.backedAnnotatedTypes.invalidate(typeHolder);
            this.slimAnnotatedTypesById.remove(annotatedType.getIdentifier());
            this.enhancedAnnotatedTypes.invalidate(annotatedType);
        }
    }

    @Override
    public void cleanupAfterBoot() {
        this.enhancedAnnotatedTypes.clear();
        this.annotations.clear();
        backedAnnotatedTypes.forEachValue((BackedAnnotatedType::clear));
        this.backedAnnotatedTypes.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
        slimAnnotatedTypesById.clear();
        syntheticAnnotationsAnnotatedTypes.clear();
    }

    public void removeAll(Set<Bean<?>> removable) {
        for (Bean<?> bean : removable) {
            if (bean instanceof AbstractClassBean) {
                slimAnnotatedTypesById.remove(((AbstractClassBean<?>) bean).getAnnotated().getIdentifier());
            }
        }
    }
}
