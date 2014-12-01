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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

/**
 * Something that is resolvable by the resolver. A resolvable is defined by it's
 * bindings and type closure
 *
 * @author pmuir
 */
public interface Resolvable {

    /**
     * Get the bindings to use for resolution. @Default will be returned if no
     * bindings were specified
     *
     * @return the bindings
     */
    Set<QualifierInstance> getQualifiers();

    /**
     * The types that this resolvable may be assigned to
     *
     * @return
     */
    Set<Type> getTypes();

    /**
     * Get the underlying java class used to generate this resolvable, or null
     * if no java class was used
     *
     * @return the java class
     */
    Class<?> getJavaClass();

    /**
     * Get the declaring the injection point, or null if there is none
     *
     * @return
     */
    Bean<?> getDeclaringBean();

    /**
     * Returns true if the resolvable represents a delegate injection point, which requires specific rules (8.3.1) to be used
     * during resolution.
     */
    boolean isDelegate();

}
