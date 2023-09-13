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
package org.jboss.weld.annotated.enhanced.jlr;

import static org.jboss.weld.util.collections.WeldCollections.immutableListView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Represents an annotated constructor
 * <p/>
 * This class is immutable, and therefore threadsafe
 *
 * @param <T> exact type
 * @author Pete Muir
 * @author Ales Justin
 */
public class EnhancedAnnotatedConstructorImpl<T> extends AbstractEnhancedAnnotatedCallable<T, T, Constructor<T>>
        implements EnhancedAnnotatedConstructor<T> {

    // The list of parameter abstractions
    private final List<EnhancedAnnotatedParameter<?, T>> parameters;

    private final ConstructorSignature signature;

    private final AnnotatedConstructor<T> slim;

    public static <T> EnhancedAnnotatedConstructor<T> of(AnnotatedConstructor<T> annotatedConstructor,
            EnhancedAnnotatedType<T> declaringClass, ClassTransformer classTransformer) {
        return new EnhancedAnnotatedConstructorImpl<T>(annotatedConstructor,
                buildAnnotationMap(annotatedConstructor.getAnnotations()),
                buildAnnotationMap(annotatedConstructor.getAnnotations()), declaringClass, classTransformer);
    }

    /**
     * Constructor
     * <p/>
     * Initializes the superclass with the build annotations map
     *
     * @param constructor The constructor method
     * @param declaringClass The declaring class
     */
    private EnhancedAnnotatedConstructorImpl(AnnotatedConstructor<T> annotatedConstructor,
            Map<Class<? extends Annotation>, Annotation> annotationMap,
            Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, EnhancedAnnotatedType<T> declaringClass,
            ClassTransformer classTransformer) {
        super(annotatedConstructor, annotationMap, declaredAnnotationMap, classTransformer, declaringClass);
        this.slim = annotatedConstructor;

        ArrayList<EnhancedAnnotatedParameter<?, T>> parameters = new ArrayList<EnhancedAnnotatedParameter<?, T>>();
        validateParameterCount(annotatedConstructor);
        for (AnnotatedParameter<T> annotatedParameter : annotatedConstructor.getParameters()) {
            EnhancedAnnotatedParameter<?, T> parameter = EnhancedAnnotatedParameterImpl.of(annotatedParameter, this,
                    classTransformer);
            parameters.add(parameter);
        }
        this.parameters = immutableListView(parameters);
        this.signature = new ConstructorSignatureImpl(this);
    }

    /**
     * Gets the constructor
     *
     * @return The constructor
     */
    public Constructor<T> getAnnotatedConstructor() {
        return slim.getJavaMember();
    }

    /**
     * Gets the delegate (constructor)
     *
     * @return The delegate
     */
    @Override
    public Constructor<T> getDelegate() {
        return slim.getJavaMember();
    }

    /**
     * Gets the abstracted parameters
     * <p/>
     * If the parameters are null, initialize them first
     *
     * @return A list of annotated parameter abstractions
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor#getEnhancedParameters()
     */
    public List<EnhancedAnnotatedParameter<?, T>> getEnhancedParameters() {
        return parameters;
    }

    /**
     * Gets parameter abstractions with a given annotation type.
     * <p/>
     * If the parameters are null, they are initializes first.
     * <p/>
     * The results of the method are not cached, as it is not called at runtime
     *
     * @param annotationType The annotation type to match
     * @return A list of matching parameter abstractions. An empty list is
     *         returned if there are no matches.
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor#getEnhancedParameters(Class)
     */
    public List<EnhancedAnnotatedParameter<?, T>> getEnhancedParameters(Class<? extends Annotation> annotationType) {
        List<EnhancedAnnotatedParameter<?, T>> ret = new ArrayList<EnhancedAnnotatedParameter<?, T>>();
        for (EnhancedAnnotatedParameter<?, T> parameter : parameters) {
            if (parameter.isAnnotationPresent(annotationType)) {
                ret.add(parameter);
            }
        }
        return ret;
    }

    /**
     * The overridden equals operation
     *
     * @param other The instance to compare to
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {

        if (super.equals(other) && other instanceof EnhancedAnnotatedConstructor<?>) {
            EnhancedAnnotatedConstructor<?> that = (EnhancedAnnotatedConstructor<?>) other;
            return this.getJavaMember().equals(that.getJavaMember())
                    && this.getEnhancedParameters().equals(that.getEnhancedParameters());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getJavaMember().hashCode();
        hash = hash * 31 + getEnhancedParameters().hashCode();
        return hash;
    }

    /**
     * Gets a string representation of the constructor
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotatedConstructor(this);
    }

    public ConstructorSignature getSignature() {
        return signature;
    }

    public List<AnnotatedParameter<T>> getParameters() {
        return Reflections.cast(parameters);
    }

    public boolean isGeneric() {
        return getJavaMember().getTypeParameters().length > 0;
    }

    @Override
    public AnnotatedConstructor<T> slim() {
        return slim;
    }

}
