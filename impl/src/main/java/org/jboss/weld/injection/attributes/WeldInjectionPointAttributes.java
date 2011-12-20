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
package org.jboss.weld.injection.attributes;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldAnnotated;

/**
 * Representation of properties of an {@link InjectionPoint}, which can be modified by an extension in the
 * {@link ProcessInjectionPoint} phase. After the phase, these data objects are wrapped within matching
 * {@link WeldInjectionPoint} implementation, which contain additional logic.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <X>
 */
public interface WeldInjectionPointAttributes<T, S> extends InjectionPoint {

    @Override
    WeldAnnotated<T, S> getAnnotated();

    /**
     * Returns an instance of a given qualifier annotation or null if a given qualifier is not present on the injection point.
     */
    <X extends Annotation> X getQualifier(Class<X> annotationType);
}
