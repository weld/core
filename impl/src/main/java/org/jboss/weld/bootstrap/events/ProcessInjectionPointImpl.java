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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingFieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingParameterInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Jozef Hartinger
 */
public class ProcessInjectionPointImpl<T, X> extends AbstractDefinitionContainerEvent implements ProcessInjectionPoint<T, X> {

    protected static <T, X> FieldInjectionPointAttributes<T, X> fire(FieldInjectionPointAttributes<T, X> attributes, Class<?> declaringComponentClass, BeanManagerImpl manager) {
        ProcessInjectionPointImpl<T, X> event = new ProcessInjectionPointImpl<T, X>(attributes, declaringComponentClass, manager, attributes.getAnnotated().getBaseType()) {
        };
        event.fire();
        if (!event.isDirty()) {
            return attributes;
        } else {
            return ForwardingFieldInjectionPointAttributes.of(event.getInjectionPoint());
        }
    }

    public static <T, X> ParameterInjectionPointAttributes<T, X> fire(ParameterInjectionPointAttributes<T, X> attributes, Class<?> declaringComponentClass, BeanManagerImpl manager) {
        ProcessInjectionPointImpl<T, X> event = new ProcessInjectionPointImpl<T, X>(attributes, declaringComponentClass, manager, attributes.getAnnotated().getBaseType()) {
        };
        event.fire();
        if (!event.isDirty()) {
            return attributes;
        } else {
            return ForwardingParameterInjectionPointAttributes.of(event.getInjectionPoint());
        }
    }

    protected ProcessInjectionPointImpl(InjectionPoint ip, Class<?> declaringComponentClass, BeanManagerImpl beanManager, Type injectionPointType) {
        super(beanManager, ProcessInjectionPoint.class, new Type[] { (ip.getBean() == null ? declaringComponentClass : ip.getBean().getBeanClass()), injectionPointType });
        this.ip = ip;
    }

    private InjectionPoint ip;
    private boolean dirty;

    @Override
    public InjectionPoint getInjectionPoint() {
        return ip;
    }

    @Override
    public void setInjectionPoint(InjectionPoint injectionPoint) {
        ip = injectionPoint;
        dirty = true;
    }

    @Override
    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    public boolean isDirty() {
        return dirty;
    }
}
