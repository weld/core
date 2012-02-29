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
package org.jboss.weld.introspector;

import javax.enterprise.inject.spi.AnnotatedType;
import java.util.Set;

/**
 * A wrapper for annotated types that are modified as part of the discovery process
 *
 * @author pmuir
 * @author alesj
 */
public class DiscoveredExternalAnnotatedType<X> extends ExternalAnnotatedType<X> {

    private AnnotatedType<?> original;

    public static <X> AnnotatedType<X> of(AnnotatedType<X> annotatedType, AnnotatedType<?> original) {
        return new DiscoveredExternalAnnotatedType<X>(annotatedType, original);
    }

    private DiscoveredExternalAnnotatedType(AnnotatedType<X> delegate, AnnotatedType<?> original) {
        super(delegate);
        this.original = original;
    }

    /**
     * Did we modify the original.
     *
     * @return true if modified, false otherwise
     */
    public boolean isModifed() {
        if (original.getJavaClass().equals(getJavaClass()) == false)
            return true;

        if (equals(original.getConstructors(), getConstructors()) == false)
            return true;

        if (equals(original.getFields(), getFields()) == false)
            return true;

        if (equals(original.getMethods(), getMethods()) == false)
            return true;

        return false;
    }

    // TODO -- how to diff between already empty set and explicity empty set
    // but I guess it doesn't matter, as there is no injection anyway then
    private static boolean equals(Set original, Set copy) {
        // original should not be null
        if (copy == null)
            return false;

        if (original.size() != copy.size())
            return false;

        return original.containsAll(copy);
    }
}
