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
package org.jboss.weld.introspector.jlr;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.jboss.weld.introspector.TypeClosureLazyValueHolder;
import org.jboss.weld.introspector.WeldAnnotation;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.HashSetSupplier;
import org.jboss.weld.util.reflection.SecureReflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an annotated annotation
 * <p/>
 * This class is immutable and therefore threadsafe
 *
 * @param <T>
 * @author Pete Muir
 */
public class WeldAnnotationImpl<T extends Annotation> extends WeldClassImpl<T> implements WeldAnnotation<T> {

    // The annotated members map (annotation -> member with annotation)
    private final SetMultimap<Class<? extends Annotation>, WeldMethod<?, ?>> annotatedMembers;
    // The implementation class of the annotation
    private final Class<T> clazz;
    // The set of abstracted members
    private final Set<WeldMethod<?, ?>> members;

    //we can't call this method 'of', cause it won't compile on JDK7
    public static <A extends Annotation> WeldAnnotation<A> create(String contextId, Class<A> annotationType, ClassTransformer classTransformer) {
        Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
        annotationMap.putAll(buildAnnotationMap(annotationType.getAnnotations()));
        annotationMap.putAll(buildAnnotationMap(classTransformer.getTypeStore().get(annotationType)));

        Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
        declaredAnnotationMap.putAll(buildAnnotationMap(annotationType.getDeclaredAnnotations()));
        declaredAnnotationMap.putAll(buildAnnotationMap(classTransformer.getTypeStore().get(annotationType)));
        return new WeldAnnotationImpl<A>(contextId, annotationType, annotationMap, declaredAnnotationMap, classTransformer);
    }

    /**
     * Constructor
     * <p/>
     * Initializes the superclass with the built annotation map
     *
     * @param annotationType The annotation type
     */
    protected WeldAnnotationImpl(String contextId, Class<T> annotationType, Map<Class<? extends Annotation>, Annotation> annotationMap, Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap, ClassTransformer classTransformer) {
        super(contextId, annotationType, annotationType, null, new TypeClosureLazyValueHolder(contextId, annotationType), annotationMap, declaredAnnotationMap, classTransformer);
        this.clazz = annotationType;
        members = new HashSet<WeldMethod<?, ?>>();
        annotatedMembers = Multimaps.newSetMultimap(new HashMap<Class<? extends Annotation>, Collection<WeldMethod<?, ?>>>(), HashSetSupplier.<WeldMethod<?, ?>>instance());
        for (Method member : SecureReflections.getDeclaredMethods(clazz)) {
            WeldMethod<?, ?> annotatedMethod = WeldMethodImpl.of(contextId, member, this, classTransformer);
            members.add(annotatedMethod);
            for (Annotation annotation : annotatedMethod.getAnnotations()) {
                annotatedMembers.put(annotation.annotationType(), annotatedMethod);
            }
        }
    }

    /**
     * Gets all members of the annotation
     * <p/>
     * Initializes the members first if they are null
     *
     * @return The set of abstracted members
     * @see org.jboss.weld.introspector.WeldAnnotation#getMembers()
     */
    public Set<WeldMethod<?, ?>> getMembers() {
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
     * @see org.jboss.weld.introspector.WeldAnnotation#getMembers(Class)
     */
    public Set<WeldMethod<?, ?>> getMembers(Class<? extends Annotation> annotationType) {
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
}
