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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization.broken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

/**
 * The spec says:
 * 
 * "If X does not have some bean type of Y, the container automatically detects the problem and treats it as a definition error."
 * 
 * This test verifies, that such problem is detected if the set of types of X is altered by a portable extension.
 * 
 * @author Jozef Hartinger
 * 
 */
public class TypeExtension implements Extension {

    void modifySpecializingBean(@Observes ProcessBeanAttributes<Specializing> event) {
        final BeanAttributes<Specializing> delegate = event.getBeanAttributes();
        event.setBeanAttributes(new ForwardingBeanAttributes<Specializing>() {

            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(Object.class);
                types.add(Specializing.class);
                return Collections.unmodifiableSet(types);
            }

            @Override
            protected BeanAttributes<Specializing> attributes() {
                return delegate;
            }
        });
    }
}
