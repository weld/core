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

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ArraySet;

import java.lang.annotation.Annotation;
import java.util.Set;

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

    private ArraySet<Class<? extends Annotation>> stereotypes;

    private final BeanManagerImpl manager;

    /**
     * Constructor
     *
     * @param stereotypeAnnotations The stereotypes to merge
     */
    public MergedStereotypes(Set<Annotation> stereotypeAnnotations, BeanManagerImpl manager) {
        this.possibleScopeTypes = new ArraySet<Annotation>();
        this.stereotypes = new ArraySet<Class<? extends Annotation>>();
        this.manager = manager;
        merge(stereotypeAnnotations);
        this.possibleScopeTypes.trimToSize();
        this.stereotypes.trimToSize();
    }

    /**
     * Perform the merge
     *
     * @param stereotypeAnnotations The stereotype annotations
     */
    protected void merge(Set<Annotation> stereotypeAnnotations) {
        for (Annotation stereotypeAnnotation : stereotypeAnnotations) {
            // Retrieve and merge all metadata from stereotypes
            StereotypeModel<?> stereotype = manager.getServices().get(MetaAnnotationStore.class).getStereotype(stereotypeAnnotation.annotationType());
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
