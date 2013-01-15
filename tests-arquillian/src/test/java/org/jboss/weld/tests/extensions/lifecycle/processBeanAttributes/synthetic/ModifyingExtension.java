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
