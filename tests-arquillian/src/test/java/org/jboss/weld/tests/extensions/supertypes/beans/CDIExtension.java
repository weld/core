/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.supertypes.beans;

import java.util.Collections;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * @author robc
 * @author Pete Muir
 */
public class CDIExtension implements Extension {

    <T extends BeanOne> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        AnnotatedType<T> type = event.getAnnotatedType();

        AnnotatedTypeImpl<T> newType = new AnnotatedTypeImpl<T>(type);

        newType.setConstructors(type.getConstructors());

        // Clear all fields and methods
        newType.setFields(Collections.<AnnotatedField<? super T>> emptySet());
        newType.setMethods(Collections.<AnnotatedMethod<? super T>> emptySet());
        event.setAnnotatedType(newType);
    }

}
