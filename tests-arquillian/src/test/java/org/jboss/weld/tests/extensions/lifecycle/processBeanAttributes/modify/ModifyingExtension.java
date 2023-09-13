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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.modify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;

public class ModifyingExtension implements Extension {

    void modify(@Observes final ProcessBeanAttributes<Cat> event) {
        event.setBeanAttributes(new BeanAttributes<Cat>() {

            public Set<Type> getTypes() {
                return Collections.unmodifiableSet(new HashSet<Type>(Arrays.asList(Object.class, Cat.class)));
            }

            public Set<Annotation> getQualifiers() {
                return Collections.unmodifiableSet(new HashSet<Annotation>(
                        Arrays.asList(new Cute.Literal(), new Wild.Literal(true), Any.Literal.INSTANCE)));
            }

            public Class<? extends Annotation> getScope() {
                return ApplicationScoped.class;
            }

            public String getName() {
                return "cat";
            }

            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.<Class<? extends Annotation>> singleton(PersianStereotype.class);
            }

            public boolean isAlternative() {
                return true;
            }
        });
    }
}
