/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.annotated.slim;

import java.util.Set;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.resources.spi.ClassFileInfo;
import org.jboss.weld.resources.spi.ClassFileServices;

/**
 * Holds {@link SlimAnnotatedType} and additional information attached to the type.
 *
 * This is a boot-time construct and should be released after bootstrap.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type
 */
public class SlimAnnotatedTypeContext<T> {

    public static <T> SlimAnnotatedTypeContext<T> of(SlimAnnotatedType<T> type, ClassFileInfo classInfo,
            Set<ContainerLifecycleEventObserverMethod<?>> resolvedProcessAnnotatedTypeObservers) {
        return new SlimAnnotatedTypeContext<T>(type, classInfo, resolvedProcessAnnotatedTypeObservers, null);
    }

    public static <T> SlimAnnotatedTypeContext<T> of(SlimAnnotatedType<T> type) {
        return new SlimAnnotatedTypeContext<T>(type, null, null, null);
    }

    public static <T> SlimAnnotatedTypeContext<T> of(SlimAnnotatedType<T> type, Extension extension) {
        return new SlimAnnotatedTypeContext<T>(type, null, null, extension);
    }

    private final SlimAnnotatedType<T> type;
    private final ClassFileInfo classInfo;
    private final Set<ContainerLifecycleEventObserverMethod<?>> resolvedProcessAnnotatedTypeObservers;
    private final Extension extension;

    private SlimAnnotatedTypeContext(SlimAnnotatedType<T> type, ClassFileInfo classInfo,
            Set<ContainerLifecycleEventObserverMethod<?>> resolvedProcessAnnotatedTypeObservers, Extension extension) {
        this.type = type;
        this.classInfo = classInfo;
        this.resolvedProcessAnnotatedTypeObservers = resolvedProcessAnnotatedTypeObservers;
        this.extension = extension;
    }

    /**
     * @return the annotated type
     */
    public SlimAnnotatedType<T> getAnnotatedType() {
        return type;
    }

    /**
     * @return {@link ClassFileInfo} describing the underlying class. This attribute is only available if the integrator
     *         provided {@link ClassFileServices} and
     *         the underlying type is comes from scanning (not registered by an extension).
     */
    public ClassFileInfo getClassInfo() {
        return classInfo;
    }

    /**
     * @return the set of ProcessAnnotatedType observer method to which the ProcessAnnotatedType event for this type is
     *         assignable. This
     *         attribute is only available if the integrator provided {@link ClassFileServices} and the underlying type is comes
     *         from scanning (not registered
     *         by an extension).
     */
    public Set<ContainerLifecycleEventObserverMethod<?>> getResolvedProcessAnnotatedTypeObservers() {
        return resolvedProcessAnnotatedTypeObservers;
    }

    /**
     * @return the extension that registered this annotated type or null if the type comes from scanning
     */
    public Extension getExtension() {
        return extension;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SlimAnnotatedTypeContext<?>) {
            return type.equals(SlimAnnotatedTypeContext.class.cast(obj).type);
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + type.toString();
    }
}
