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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * Represents an annotated annotation
 * <p/>
 * This class is immutable and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class EnhancedAnnotationImpl<T extends Annotation> extends EnhancedAnnotatedTypeImpl<T>
        implements EnhancedAnnotation<T> {

    // The annotated members map (annotation -> member with annotation)
    private final SetMultimap<Class<? extends Annotation>, EnhancedAnnotatedMethod<?, ?>> annotatedMembers;
    // The implementation class of the annotation
    private final Class<T> clazz;
    // The set of abstracted members
    private final Set<EnhancedAnnotatedMethod<?, ?>> members;

    //we can't call this method 'of', cause it won't compile on JDK7
    public static <A extends Annotation> EnhancedAnnotation<A> create(SlimAnnotatedType<A> annotatedType,
            ClassTransformer classTransformer) {

        Class<A> annotationType = annotatedType.getJavaClass();

        Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
        annotationMap.putAll(buildAnnotationMap(annotatedType.getAnnotations()));
        annotationMap.putAll(buildAnnotationMap(classTransformer.getTypeStore().get(annotationType)));
        // Annotations and declared annotations are the same for annotation type
        return new EnhancedAnnotationImpl<A>(annotatedType, annotationMap, annotationMap, classTransformer);
    }

    /**
     * Constructor
     * <p/>
     * Initializes the superclass with the built annotation map
     *
     * @param annotationType The annotation type
     */
    protected EnhancedAnnotationImpl(SlimAnnotatedType<T> annotatedType,
            Map<Class<? extends Annotation>, Annotation> annotationMap,
            Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer) {
        super(annotatedType, annotationMap, declaredAnnotationMap, classTransformer);
        this.clazz = annotatedType.getJavaClass();
        members = new HashSet<EnhancedAnnotatedMethod<?, ?>>();
        annotatedMembers = SetMultimap.newSetMultimap();
        for (AnnotatedMethod<? super T> annotatedMethod : annotatedType.getMethods()) {
            EnhancedAnnotatedMethod<?, ? super T> enhancedAnnotatedMethod = EnhancedAnnotatedMethodImpl.of(annotatedMethod,
                    this, classTransformer);
            members.add(enhancedAnnotatedMethod);
            for (Annotation annotation : enhancedAnnotatedMethod.getAnnotations()) {
                annotatedMembers.put(annotation.annotationType(), enhancedAnnotatedMethod);
            }
        }
    }

    @Override
    protected Set<EnhancedAnnotatedMethod<?, ? super T>> getOverriddenMethods(EnhancedAnnotatedType<T> annotatedType,
            Set<EnhancedAnnotatedMethod<?, ? super T>> methods, boolean skipOverridingBridgeMethods) {
        return Collections.emptySet();
    }

    /**
     * Gets all members of the annotation
     * <p/>
     * Initializes the members first if they are null
     *
     * @return The set of abstracted members
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotation#getMembers()
     */
    public Set<EnhancedAnnotatedMethod<?, ?>> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Returns the annotated members with a given annotation type
     * <p/>
     * If the annotated members are null, they are initialized first.
     *
     * @param annotationType The annotation type to match
     * @return The set of abstracted members with the given annotation type
     *         present. An empty set is returned if no matches are found
     * @see org.jboss.weld.annotated.enhanced.EnhancedAnnotation#getMembers(Class)
     */
    public Set<EnhancedAnnotatedMethod<?, ?>> getMembers(Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableSet(annotatedMembers.get(annotationType));
    }

    /**
     * Gets a string representation of the annotation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return getJavaClass().toString();
    }

    @Override
    public Class<T> getDelegate() {
        return clazz;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EnhancedAnnotationImpl<?> that = cast(obj);
        return super.equals(that);
    }
}
