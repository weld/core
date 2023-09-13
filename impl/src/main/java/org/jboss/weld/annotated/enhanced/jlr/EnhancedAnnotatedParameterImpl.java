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

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Formats;

/**
 * Represents a parameter
 * <p/>
 * This class is immutable and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class EnhancedAnnotatedParameterImpl<T, X> extends AbstractEnhancedAnnotated<T, Object>
        implements EnhancedAnnotatedParameter<T, X> {

    public static <T, X> EnhancedAnnotatedParameter<T, X> of(AnnotatedParameter<X> annotatedParameter,
            EnhancedAnnotatedCallable<?, X, ?> declaringMember, ClassTransformer classTransformer) {
        return new EnhancedAnnotatedParameterImpl<T, X>(annotatedParameter, declaringMember,
                buildAnnotationMap(annotatedParameter.getAnnotations()), classTransformer);
    }

    private final EnhancedAnnotatedCallable<?, X, ?> declaringMember;
    private final AnnotatedParameter<X> slim;

    /**
     * Constructor
     *
     * @param annotations The annotations array
     * @param type The type of the parameter
     */
    protected EnhancedAnnotatedParameterImpl(AnnotatedParameter<X> annotatedParameter,
            EnhancedAnnotatedCallable<?, X, ?> declaringMember, Map<Class<? extends Annotation>, Annotation> annotationMap,
            ClassTransformer classTransformer) {
        super(annotatedParameter, annotationMap, annotationMap, classTransformer);
        this.slim = annotatedParameter;
        this.declaringMember = declaringMember;
    }

    /**
     * Indicates if the parameter is final
     *
     * @return True if final, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isFinal()
     */
    public boolean isFinal() {
        return false;
    }

    /**
     * Indicates if the parameter is static
     *
     * @return True if static, false otherwise
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#isStatic()
     */
    public boolean isStatic() {
        return false;
    }

    public boolean isPublic() {
        return false;
    }

    public boolean isPrivate() {
        return false;
    }

    public boolean isPackagePrivate() {
        return false;
    }

    public boolean isGeneric() {
        return false;
    }

    public Package getPackage() {
        return declaringMember.getPackage();
    }

    /**
     * Gets the name of the parameter
     *
     * @throws IllegalArgumentException (not supported)
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotated#getName()
     */
    public String getName() {
        throw ReflectionLogger.LOG.unableToGetParameterName();
    }

    /**
     * Gets a string representation of the parameter
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotatedParameter(this);
    }

    public AnnotatedCallable<X> getDeclaringCallable() {
        return declaringMember;
    }

    public EnhancedAnnotatedCallable<?, X, ?> getDeclaringEnhancedCallable() {
        return declaringMember;
    }

    public int getPosition() {
        return slim.getPosition();
    }

    @Override
    public Object getDelegate() {
        return null;
    }

    public EnhancedAnnotatedType<X> getDeclaringType() {
        return getDeclaringEnhancedCallable().getDeclaringType();
    }

    @Override
    public AnnotatedParameter<X> slim() {
        return slim;
    }
}
