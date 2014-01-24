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
package org.jboss.weld.metadata.cache;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import java.lang.annotation.Annotation;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resources.ClassTransformer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Metadata singleton for holding EJB metadata, scope models etc.
 *
 * @author Pete Muir
 */
public class MetaAnnotationStore implements Service {

    private abstract static class AbstractMetaAnnotationFunction<M extends AnnotationModel<Annotation>> extends
            CacheLoader<Class<Annotation>, M> {

        private final ClassTransformer classTransformer;

        private AbstractMetaAnnotationFunction(ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public ClassTransformer getClassTransformer() {
            return classTransformer;
        }

    }

    private static class StereotypeFunction extends AbstractMetaAnnotationFunction<StereotypeModel<Annotation>> {

        public StereotypeFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        public StereotypeModel<Annotation> load(Class<Annotation> from) {
            return new StereotypeModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class ScopeFunction extends AbstractMetaAnnotationFunction<ScopeModel<Annotation>> {

        public ScopeFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        public ScopeModel<Annotation> load(Class<Annotation> from) {
            return new ScopeModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class QualifierFunction extends AbstractMetaAnnotationFunction<QualifierModel<Annotation>> {

        public QualifierFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        public QualifierModel<Annotation> load(Class<Annotation> from) {
            return new QualifierModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class InterceptorBindingFunction extends AbstractMetaAnnotationFunction<InterceptorBindingModel<Annotation>> {

        public InterceptorBindingFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        public InterceptorBindingModel<Annotation> load(Class<Annotation> from) {
            return new InterceptorBindingModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class QualifierInstanceFunction extends CacheLoader<Annotation, QualifierInstance> {

        private final MetaAnnotationStore metaAnnotationStore;

        private QualifierInstanceFunction(MetaAnnotationStore metaAnnotationStore) {
            super();
            this.metaAnnotationStore = metaAnnotationStore;
        }

        @Override
        public QualifierInstance load(Annotation key) throws Exception {
            return QualifierInstance.of(key, metaAnnotationStore, false);
        }

    }

    // The stereotype models
    private final LoadingCache<Class<Annotation>, StereotypeModel<Annotation>> stereotypes;
    // The scope models
    private final LoadingCache<Class<Annotation>, ScopeModel<Annotation>> scopes;
    // The binding type models
    private final LoadingCache<Class<Annotation>, QualifierModel<Annotation>> qualifiers;
    // the interceptor bindings
    private final LoadingCache<Class<Annotation>, InterceptorBindingModel<Annotation>> interceptorBindings;

    private final LoadingCache<Annotation, QualifierInstance> qualifierInstanceCache;

    public MetaAnnotationStore(ClassTransformer classTransformer) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        this.stereotypes = cacheBuilder.build(new StereotypeFunction(classTransformer));
        this.scopes = cacheBuilder.build(new ScopeFunction(classTransformer));
        this.qualifiers = cacheBuilder.build(new QualifierFunction(classTransformer));
        this.interceptorBindings = cacheBuilder.build(new InterceptorBindingFunction(classTransformer));
        this.qualifierInstanceCache = cacheBuilder.build(new QualifierInstanceFunction(this));
    }

    /**
     * removes all data for an annotation class. This should be called after an
     * annotation has been modified through the SPI
     */
    public void clearAnnotationData(Class<? extends Annotation> annotationClass) {
        stereotypes.invalidate(annotationClass);
        scopes.invalidate(annotationClass);
        qualifiers.invalidate(annotationClass);
        interceptorBindings.invalidate(annotationClass);
    }

    /**
     * Gets a stereotype model
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T>        The type
     * @param stereotype The stereotype
     * @return The stereotype model
     */
    public <T extends Annotation> StereotypeModel<T> getStereotype(final Class<T> stereotype) {
        return getCastCacheValue(stereotypes, stereotype);
    }

    /**
     * Gets a scope model
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T>   The type
     * @param scope The scope type
     * @return The scope type model
     */
    public <T extends Annotation> ScopeModel<T> getScopeModel(final Class<T> scope) {
        return getCastCacheValue(scopes, scope);
    }

    /**
     * Gets a binding type model.
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T>         The type
     * @param bindingType The binding type
     * @return The binding type model
     */
    public <T extends Annotation> QualifierModel<T> getBindingTypeModel(final Class<T> bindingType) {
        return getCastCacheValue(qualifiers, bindingType);
    }

    /**
     *
     * @param interceptorBinding
     * @return
     */
    public <T extends Annotation> InterceptorBindingModel<T> getInterceptorBindingModel(final Class<T> interceptorBinding) {
        return getCastCacheValue(interceptorBindings, interceptorBinding);
    }

    /**
    *
    * @param annotation
    * @return the cached qualifier instance for the given annotation instance
    */
    public QualifierInstance getCachedQualifierInstance(Annotation annotation) {
        return getCacheValue(qualifierInstanceCache, annotation);
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        final String newLine = "\n";
        StringBuilder buffer = new StringBuilder();
        buffer.append("Metadata cache").append(newLine);
        buffer.append("Registered binding type models: ").append(qualifiers.size()).append(newLine);
        buffer.append("Registered scope type models: ").append(scopes.size()).append(newLine);
        buffer.append("Registered stereotype models: ").append(stereotypes.size()).append(newLine);
        buffer.append("Registered interceptor binding models: ").append(interceptorBindings.size()).append(newLine);
        buffer.append("Cached qualifier instances: ").append(qualifierInstanceCache.size()).append(newLine);
        return buffer.toString();
    }

    public void cleanup() {
        this.qualifiers.invalidateAll();
        this.scopes.invalidateAll();
        this.stereotypes.invalidateAll();
        this.interceptorBindings.invalidateAll();
        this.qualifierInstanceCache.invalidateAll();
    }

}
