/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.specialization.weld802;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.annotated.ForwardingWeldClass;

/**
 * @author Ales Justin
 */
public class CustomExtension implements Extension {
    public void registerBeans(@Observes BeforeBeanDiscovery event, final BeanManager manager) {
        final EnhancedAnnotatedType<Foo> foo = getEnhancedAnnotatedType(manager, Foo.class);
        final EnhancedAnnotatedType<Bar> bar = new ForwardingWeldClass<Bar>() {
            @Override
            protected EnhancedAnnotatedType<Bar> delegate() {
                return getEnhancedAnnotatedType(manager, Bar.class);
            }

            @Override
            public EnhancedAnnotatedType<? super Bar> getEnhancedSuperclass() {
                return foo;
            }
        };
        event.addAnnotatedType(foo);
        event.addAnnotatedType(bar);
    }

    protected <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(BeanManager manager, Class<T> javaClass) {
        if (manager instanceof BeanManagerImpl) {
            BeanManagerImpl bmi = (BeanManagerImpl) manager;
            ClassTransformer ct = bmi.getServices().get(ClassTransformer.class);
            return ct.getEnhancedAnnotatedType(javaClass);
        } else {
            throw new IllegalStateException();
        }
    }
}
