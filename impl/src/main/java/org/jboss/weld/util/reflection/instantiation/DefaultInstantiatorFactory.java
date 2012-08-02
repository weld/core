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
package org.jboss.weld.util.reflection.instantiation;

import org.jboss.weld.Container;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * A factory class for obtaining the first available instantiator
 *
 * @author Nicklas Karlsson
 * @author Ales Justin
 */
public class DefaultInstantiatorFactory extends AbstractInstantiatorFactory {
    private volatile Boolean enabled;
    private final String id;

    public DefaultInstantiatorFactory(String id) {
        this.id = id;
    }

    public boolean useInstantiators() {
        if (enabled == null) {
            synchronized (this) {
                if (enabled == null) {
                    ResourceLoader loader = Container.instance(id).services().get(ResourceLoader.class);
                    if (loader == null)
                        loader = DefaultResourceLoader.INSTANCE;

                    boolean tmp = loader.getResource(MARKER) != null;

                    if (tmp) {
                        tmp = checkInstantiator();
                    }

                    enabled = tmp;
                }
            }
        }
        return enabled;
    }

    public void cleanup() {
    }
}
