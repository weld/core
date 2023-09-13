/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

/**
 * A simple resource loader.
 * <p/>
 * Uses {@link DefaultResourceLoader}'s classloader if the Thread Context
 * Classloader isn't available
 *
 * @author Pete Muir
 */
public class DefaultResourceLoader extends WeldClassLoaderResourceLoader {
    public static final DefaultResourceLoader INSTANCE = new DefaultResourceLoader();

    protected DefaultResourceLoader() {
    }

    public void cleanup() {
    }

    @Override
    protected ClassLoader classLoader() {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl == null) {
            return super.classLoader();
        } else {
            return tccl;
        }
    }
}
