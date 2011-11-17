/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet.util;

import org.jboss.weld.bootstrap.spi.Metadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract away service loader usage.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServiceLoader {

    private static final String JDK_SERVICE_LOADER = "java.util.ServiceLoader";
    private static final String WELD_SERVICE_LOADER = "org.jboss.weld.util.ServiceLoader";
    private static final Method loadMethod;
    private static boolean weldSL = false;

    static {
        ClassLoader cl = ServiceLoader.class.getClassLoader();
        Class<?> clazz = null;

        try {
            clazz = cl.loadClass(JDK_SERVICE_LOADER);
        } catch (Throwable ignored) {
        }

        if (clazz == null) {
            try {
                clazz = cl.loadClass(WELD_SERVICE_LOADER);
                weldSL = true;
            } catch (Throwable ignored) {
            }
        }

        if (clazz == null)
            throw new IllegalArgumentException("No ServiceLoader class available!");

        try {
            loadMethod = clazz.getDeclaredMethod("load", Class.class, ClassLoader.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("No load class available on ServiceLoader.", e);
        }
    }

    private static Iterable adapt(Iterable iterable) {
        if (weldSL) {
            List<Object> list = new ArrayList<Object>();
            for (Object o : iterable) {
                Metadata md = (Metadata) o;
                list.add(md.getValue());
            }
            return list;
        } else {
            return iterable;
        }
    }

    public static <S> Iterable<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> Iterable<S> load(Class<S> service, ClassLoader loader) {
        if (loader == null) {
            loader = service.getClassLoader();
        }
        try {
            //noinspection unchecked
            return adapt((Iterable) loadMethod.invoke(null, service, loader));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
