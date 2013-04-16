/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

    private ServiceLoader() {
    }

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

        if (clazz == null) {
            throw new IllegalArgumentException("No ServiceLoader class available!");
        }

        try {
            loadMethod = clazz.getDeclaredMethod("load", Class.class, ClassLoader.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("No load method available on ServiceLoader - " + clazz, e);
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
