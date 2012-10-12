/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * {@link ResourceLoader} implementation that uses the classloader that loaded this class to load other resources. This is not
 * very useful except for cases where we need to decide whether optional parts of weld-core should be registered or not.
 *
 * These optional parts depend on certain artifacts that may not be available (e.g. servlet API). We cannot use the
 * {@link DefaultResourceLoader} for the decision making since in a modular environment the required artifact may be available
 * to the application (loadable via TCCL) but may not be available to Weld. This class is helpful for detecting such situations.
 *
 * @author Jozef Hartinger
 *
 * @see https://issues.jboss.org/browse/WELD-1208
 *
 */
public class WeldClassLoaderResourceLoader extends AbstractClassLoaderResourceLoader {

    public static final WeldClassLoaderResourceLoader INSTANCE = new WeldClassLoaderResourceLoader();

    protected WeldClassLoaderResourceLoader() {
    }

    @Override
    protected ClassLoader classLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public void cleanup() {
        // noop
    }
}
