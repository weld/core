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
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint.modify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.literal.NewLiteral;

public class ModifyingExtension implements Extension {

    public void overrideFieldInjectionPoint(@Observes ProcessInjectionPoint<InjectingBean, Dog> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        event.setInjectionPoint(new ForwardingInjectionPoint() {

            @Override
            protected InjectionPoint delegate() {
                return delegate;
            }

            @Override
            public boolean isTransient() {
                return true;
            }

            @Override
            public Type getType() {
                return Hound.class;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.<Annotation> singleton(Fast.Literal.INSTANCE);
            }
        });
    }

    public void overrideDelegateInjectionPoint(@Observes ProcessInjectionPoint<AnimalDecorator, Object> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        event.setInjectionPoint(new ForwardingInjectionPoint() {

            @Override
            protected InjectionPoint delegate() {
                return delegate;
            }

            @Override
            public boolean isDelegate() {
                return true;
            }

            @Override
            public Type getType() {
                return Animal.class;
            }
        });
    }

    public void overrideFieldInjectionPointToTriggerNewBeanCreation(@Observes ProcessInjectionPoint<InjectingBean, Cat> event) {
        final InjectionPoint delegate = event.getInjectionPoint();
        event.setInjectionPoint(new ForwardingInjectionPoint() {

            @Override
            protected InjectionPoint delegate() {
                return delegate;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.<Annotation> singleton(NewLiteral.DEFAULT_INSTANCE);
            }
        });
    }

}
