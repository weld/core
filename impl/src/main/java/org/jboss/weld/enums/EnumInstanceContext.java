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
package org.jboss.weld.enums;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Contains everything the container needs to know to manage an instance of a Java enum.
 *
 * @author Jozef Hartinger
 *
 * @param <T> enum type
 */
public class EnumInstanceContext<T extends Enum<?>> {

    private final T instance;
    private final InjectionTarget<T> injectionTarget;
    private final CreationalContext<T> ctx;

    public EnumInstanceContext(T instance, InjectionTarget<T> injector, CreationalContext<T> ctx) {
        this.instance = instance;
        this.injectionTarget = injector;
        this.ctx = ctx;
    }

    public void inject() {
        injectionTarget.inject(instance, ctx);
    }

    public void destroy() {
        injectionTarget.dispose(instance);
        ctx.release();
    }
}
