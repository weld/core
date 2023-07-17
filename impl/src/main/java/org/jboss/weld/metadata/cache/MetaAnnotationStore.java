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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Named;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Metadata singleton for holding EJB metadata, scope models etc.
 *
 * @author Pete Muir
 */
public class MetaAnnotationStore implements Service {

    private abstract static class AbstractMetaAnnotationFunction<M extends AnnotationModel<Annotation>> implements
            Function<Class<Annotation>, M> {

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

        @Override
        public StereotypeModel<Annotation> apply(Class<Annotation> from) {
            return new StereotypeModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class ScopeFunction extends AbstractMetaAnnotationFunction<ScopeModel<Annotation>> {

        public ScopeFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        @Override
        public ScopeModel<Annotation> apply(Class<Annotation> from) {
            return new ScopeModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class QualifierFunction extends AbstractMetaAnnotationFunction<QualifierModel<Annotation>> {

        public QualifierFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        @Override
        public QualifierModel<Annotation> apply(Class<Annotation> from) {
            return new QualifierModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class InterceptorBindingFunction
            extends AbstractMetaAnnotationFunction<InterceptorBindingModel<Annotation>> {

        public InterceptorBindingFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        @Override
        public InterceptorBindingModel<Annotation> apply(Class<Annotation> from) {
            return new InterceptorBindingModel<Annotation>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    private static class QualifierInstanceFunction implements Function<Annotation, QualifierInstance> {

        private final MetaAnnotationStore metaAnnotationStore;

        private QualifierInstanceFunction(MetaAnnotationStore metaAnnotationStore) {
            super();
            this.metaAnnotationStore = metaAnnotationStore;
        }

        @Override
        public QualifierInstance apply(Annotation key) {
            return QualifierInstance.of(key, metaAnnotationStore);
        }

    }

    private static class InvokableFunction extends AbstractMetaAnnotationFunction<InvokableModel<Annotation>> {

        public InvokableFunction(ClassTransformer classTransformer) {
            super(classTransformer);
        }

        @Override
        public InvokableModel<Annotation> apply(Class<Annotation> from) {
            return new InvokableModel<>(getClassTransformer().getEnhancedAnnotation(from));
        }

    }

    // The stereotype models
    private final ComputingCache<Class<Annotation>, StereotypeModel<Annotation>> stereotypes;
    // The scope models
    private final ComputingCache<Class<Annotation>, ScopeModel<Annotation>> scopes;
    // The binding type models
    private final ComputingCache<Class<Annotation>, QualifierModel<Annotation>> qualifiers;
    // the interceptor bindings
    private final ComputingCache<Class<Annotation>, InterceptorBindingModel<Annotation>> interceptorBindings;
    // The invokable models
    private final ComputingCache<Class<Annotation>, InvokableModel<Annotation>> invokables;

    private final ComputingCache<Annotation, QualifierInstance> qualifierInstanceCache;

    private final SharedObjectCache sharedObjectCache;

    public MetaAnnotationStore(ClassTransformer classTransformer) {
        ComputingCacheBuilder cacheBuilder = ComputingCacheBuilder.newBuilder();
        this.stereotypes = cacheBuilder.build(new StereotypeFunction(classTransformer));
        this.scopes = cacheBuilder.build(new ScopeFunction(classTransformer));
        this.qualifiers = cacheBuilder.build(new QualifierFunction(classTransformer));
        this.interceptorBindings = cacheBuilder.build(new InterceptorBindingFunction(classTransformer));
        this.invokables = cacheBuilder.build(new InvokableFunction(classTransformer));
        this.qualifierInstanceCache = cacheBuilder.build(new QualifierInstanceFunction(this));
        this.sharedObjectCache = classTransformer.getSharedObjectCache();
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
     * @param <T> The type
     * @param stereotype The stereotype
     * @return The stereotype model
     */
    public <T extends Annotation> StereotypeModel<T> getStereotype(final Class<T> stereotype) {
        return stereotypes.getCastValue(stereotype);
    }

    /**
     * Gets a scope model
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T> The type
     * @param scope The scope type
     * @return The scope type model
     */
    public <T extends Annotation> ScopeModel<T> getScopeModel(final Class<T> scope) {
        return scopes.getCastValue(scope);
    }

    /**
     * Gets a binding type model.
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T> The type
     * @param bindingType The binding type
     * @return The binding type model
     */
    public <T extends Annotation> QualifierModel<T> getBindingTypeModel(final Class<T> bindingType) {
        return qualifiers.getCastValue(bindingType);
    }

    /**
     *
     * @param interceptorBinding
     * @return
     */
    public <T extends Annotation> InterceptorBindingModel<T> getInterceptorBindingModel(final Class<T> interceptorBinding) {
        return interceptorBindings.getCastValue(interceptorBinding);
    }

    /**
     * Gets an invokable model.
     * <p/>
     * Adds the model if it is not present.
     *
     * @param <T>         The type
     * @param annotation The annotation class
     * @return The invokable model
     */
    public <T extends Annotation> InvokableModel<T> getInvokableModel(final Class<T> annotation) {
        return invokables.getCastValue(annotation);
    }

    /**
     *
     * @param annotation
     * @return the qualifier instance for the given annotation, uses cache if possible
     */
    public QualifierInstance getQualifierInstance(final Annotation annotation) {
        return isCacheAllowed(annotation) ? qualifierInstanceCache.getValue(annotation)
                : QualifierInstance.of(annotation, this);
    }

    /**
     *
     * @param bean
     * @return the set of qualifier instances for the given bean, uses caches if possible
     */
    public Set<QualifierInstance> getQualifierInstances(final Bean<?> bean) {
        if (bean instanceof RIBean) {
            return ((RIBean<?>) bean).getQualifierInstances();
        }
        return getQualifierInstances(bean.getQualifiers());
    }

    /**
     *
     * @param annotations
     * @return the set of qualifier instances, uses caches if possible
     */
    public Set<QualifierInstance> getQualifierInstances(final Set<Annotation> annotations) {

        if (annotations == null || annotations.isEmpty()) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<QualifierInstance> builder = ImmutableSet.builder();
        boolean useSharedCache = true;

        for (Annotation annotation : annotations) {
            if (isCacheAllowed(annotation)) {
                builder.add(qualifierInstanceCache.getValue(annotation));
            } else {
                builder.add(QualifierInstance.of(annotation, this));
                // Don't use shared object cache if there's some qualifier instance which should not be cached
                useSharedCache = false;
            }
        }
        return useSharedCache ? sharedObjectCache.getSharedSet(builder.build()) : builder.build();
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

    @Override
    public void cleanup() {
        this.qualifiers.clear();
        this.scopes.clear();
        this.stereotypes.clear();
        this.interceptorBindings.clear();
        this.qualifierInstanceCache.clear();
    }

    private static boolean isCacheAllowed(Annotation annotation) {
        if (annotation.annotationType().equals(Named.class)) {
            // Don't cache @Named with non-default value.
            Named named = (Named) annotation;
            return named.value().equals("");
        }
        return true;
    }

}
