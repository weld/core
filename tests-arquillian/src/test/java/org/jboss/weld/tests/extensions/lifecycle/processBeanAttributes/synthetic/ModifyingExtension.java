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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.synthetic;

import static org.junit.Assert.assertNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class ModifyingExtension implements Extension {

    private boolean modified;

    void modifyBicycle(@Observes ProcessBeanAttributes<Bicycle> event) {
        assertNull(event.getAnnotated());
        final BeanAttributes<Bicycle> delegate = event.getBeanAttributes();

        // validate what we got
        Validator.validateBeforeModification(delegate);

        event.setBeanAttributes(new ForwardingBeanAttributes<Bicycle>() {
            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(Object.class);
                types.add(Bicycle.class);
                return types;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return RequestScoped.class;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.<Class<? extends Annotation>> singleton(BarStereotype.class);
            }

            @Override
            public boolean isAlternative() {
                return true;
            }

            @Override
            protected BeanAttributes<Bicycle> attributes() {
                return delegate;
            }
        });
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

}
