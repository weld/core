/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.attributes;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

/**
 * Representation of properties of an {@link InjectionPoint}, which can be modified by an extension in the
 * {@link ProcessInjectionPoint} phase.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <S>
 */
public interface WeldInjectionPointAttributes<T, S> extends InjectionPoint {

    /**
     * Returns an instance of a given qualifier annotation or null if a given qualifier is not present on the injection point.
     */
    <X extends Annotation> X getQualifier(Class<X> annotationType);
}
