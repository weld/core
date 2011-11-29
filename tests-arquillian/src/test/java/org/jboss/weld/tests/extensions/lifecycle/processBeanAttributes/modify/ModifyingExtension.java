/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.modify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessModule;

import org.jboss.weld.literal.AnyLiteral;

public class ModifyingExtension implements Extension {

    void enableAlternative(@Observes ProcessModule event) {
        event.getAlternatives().add(Cat.class);
    }

    void modify(@Observes final ProcessBeanAttributes<Cat> event) {
        event.setBeanAttributes(new BeanAttributes<Cat>() {

            @SuppressWarnings("unchecked")
            public Set<Type> getTypes() {
                return Collections.unmodifiableSet(new HashSet<Type>(Arrays.asList(Object.class, Cat.class)));
            }

            public Set<Annotation> getQualifiers() {
                return Collections.unmodifiableSet(new HashSet<Annotation>(Arrays.asList(new Cute.Literal(), new Wild.Literal(true), AnyLiteral.INSTANCE)));
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

            public boolean isNullable() {
                return true;
            }
        });
    }
}
