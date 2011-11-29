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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization.modify;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class ModifyingExtension implements Extension {

    <T> void alterName(@Observes ProcessBeanAttributes<Specialized> event) {
        if (!event.getBeanAttributes().getTypes().contains(Specializing.class)) {
            final BeanAttributes<Specialized> delegate = event.getBeanAttributes();
            event.setBeanAttributes(new ForwardingBeanAttributes<Specialized>() {

                @Override
                public Set<Annotation> getQualifiers() {
                    Set<Annotation> qualifiers = new HashSet<Annotation>(delegate.getQualifiers());
                    qualifiers.add(Foo.Literal.INSTANCE);
                    return Collections.unmodifiableSet(qualifiers);
                }

                @Override
                protected BeanAttributes<Specialized> attributes() {
                    return delegate;
                }

                @Override
                public String getName() {
                    return "extension";
                }
            });
        }
    }
}
