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

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.Stereotype;

import static org.jboss.weld.logging.messages.MetadataMessage.STEREOTYPE_NOT_REGISTERED;

/**
 * Meta model for the merged stereotype for a bean
 *
 * @author Pete Muir
 */
public class MergedStereotypes<T, E> {
    // The possible scope types
    private final ArraySet<Annotation> possibleScopeTypes;
    // Is the bean name defaulted?
    private boolean beanNameDefaulted;
    // Are any of the sterotypes alternatives
    private boolean alternative;

    private Set<Class<? extends Annotation>> stereotypes;

    private final BeanManagerImpl manager;

    public static <T, E> MergedStereotypes<T, E> of(EnhancedAnnotated<T, E> annotated, BeanManagerImpl manager) {
        return of(annotated.getMetaAnnotations(Stereotype.class), manager);
    }

    public static <T, E> MergedStereotypes<T, E> of(Set<Annotation> stereotypeAnnotations, BeanManagerImpl manager) {
        return new MergedStereotypes<T, E>(stereotypeAnnotations, manager);
    }

    /**
     * Constructor
     *
     * @param stereotypeAnnotations The stereotypes to merge
     */
    protected MergedStereotypes(Set<Annotation> stereotypeAnnotations, BeanManagerImpl manager) {
        this.possibleScopeTypes = new ArraySet<Annotation>();
        this.stereotypes = new ArraySet<Class<? extends Annotation>>();
        this.manager = manager;
        merge(stereotypeAnnotations);
        this.possibleScopeTypes.trimToSize();
        Reflections.<ArraySet<?>>cast(this.stereotypes).trimToSize();
        if (stereotypes.isEmpty()) {
            this.stereotypes = Collections.emptySet();
        } else {
            this.stereotypes = SharedObjectFacade.wrap(stereotypes);
        }
    }

    /**
     * Perform the merge
     *
     * @param stereotypeAnnotations The stereotype annotations
     */
    protected void merge(Set<Annotation> stereotypeAnnotations) {
        final MetaAnnotationStore store = manager.getServices().get(MetaAnnotationStore.class);
        for (Annotation stereotypeAnnotation : stereotypeAnnotations) {
            // Retrieve and merge all metadata from stereotypes
            StereotypeModel<?> stereotype = store.getStereotype(stereotypeAnnotation.annotationType());
            if (stereotype == null) {
                throw new IllegalStateException(STEREOTYPE_NOT_REGISTERED, stereotypeAnnotation);
            }
            if (stereotype.isAlternative()) {
                alternative = true;
            }
            if (stereotype.getDefaultScopeType() != null) {
                possibleScopeTypes.add(stereotype.getDefaultScopeType());
            }
            if (stereotype.isBeanNameDefaulted()) {
                beanNameDefaulted = true;
            }
            this.stereotypes.add(stereotypeAnnotation.annotationType());
            // Merge in inherited stereotypes
            merge(stereotype.getInheritedSterotypes());
        }
    }

    public boolean isAlternative() {
        return alternative;
    }

    /**
     * Returns the possible scope types
     *
     * @return The scope types
     */
    public Set<Annotation> getPossibleScopes() {
        return possibleScopeTypes;
    }

    /**
     * Indicates if the name i defaulted
     *
     * @return True if defaulted, false if not
     */
    public boolean isBeanNameDefaulted() {
        return beanNameDefaulted;
    }

    /**
     * @return the stereotypes
     */
    public Set<Class<? extends Annotation>> getStereotypes() {
        return stereotypes;
    }

    /**
     * Gets a string representation of the merged stereotypes
     *
     * @return The string representation
     */
    @Override
    public String toString() {
        return "Merged stereotype model; Any of the sterotypes is an alternative: " +
                alternative + "; possible scopes " + possibleScopeTypes;
    }

}
