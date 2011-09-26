/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.osgi.impl.integration;

import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.environment.osgi.impl.extension.service.WeldOSGiExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 */
public class BundleSingletonProvider extends SingletonProvider {

    @Override
    public <T> Singleton<T> create(Class<? extends T> expectedType) {
        return new BundleSingleton<T>(expectedType);
    }

    private static class BundleSingleton<T> implements Singleton<T> {

        private static Map<String, Bundle> classes = new HashMap<String, Bundle>();
        private final Map<Long, T> store = new HashMap<Long, T>();
        private final Class<? extends T> clazz;

        public BundleSingleton(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        private Long getId() {
            Long value = WeldOSGiExtension.currentBundle.get();
            // fix with a patched version of weld ProxyMethodHandler
//            if (value == null) {
//                return FrameworkUtil.getBundle(ProxyMethodHandler.currentCaller.get()).getBundleId();
//            }
            return value;
        }

        @Override
        public T get(String id) {
            if (!store.containsKey(getId())) {
                T maybeObject = null;
                Throwable t = new Throwable();
                for (StackTraceElement element : t.getStackTrace()) {
                    String className = element.getClassName();
                    if (!className.startsWith("org.jboss.weld")
                            && !className.startsWith("java")
                            && !className.startsWith("org.apache.felix.framework")) {

                        if (!classes.containsKey(className)) {
                            //System.out.println("\u001b[1;31mAnalyzing stacktrace for class " + clazz.getName() + ": \u001b[m");
                            //System.out.println("\u001b[0;31m" + className + "." + element.getMethodName() + "\u001b[m");
                            Class<?> maybe = null;
                            try {
                                maybe = this.getClass().getClassLoader()
                                        .loadClass(className);
                            } catch (ClassNotFoundException ex) {
                                //System.out.println("CNFE " + element.getClassName());
                                // Ignore
                            }
                            if (maybe != null) {
                                Bundle bundle = FrameworkUtil.getBundle(maybe);
                                if (bundle != null) {
                                    classes.put(className, bundle);
                                }
                            }
                        }
                        Bundle maybeBundle = classes.get(className);
                        if (maybeBundle != null) {
                            if (!maybeBundle.getSymbolicName()
                                    .equals("org.jboss.weld.osgi.weld-osgi")) {
                                WeldOSGiExtension.currentBundle.
                                        set(maybeBundle.getBundleId());
                                maybeObject = get(null);
                                WeldOSGiExtension.currentBundle.remove();
                                if (maybeObject != null) {
                                    return maybeObject;
                                }
                                break;
                            }
                        }
                    }
                }
                throw new IllegalStateException("Singleton is not set for "
                        + "bundle "
                        + getId());
            }
            return store.get(getId());
        }

        @Override
        public void set(String id, T object) {
            store.put(getId(), object);
        }

        @Override
        public void clear(String id) {
            store.remove(getId());
        }

        @Override
        public boolean isSet(String id) {
            return store.containsKey(getId());
        }
    }
}
