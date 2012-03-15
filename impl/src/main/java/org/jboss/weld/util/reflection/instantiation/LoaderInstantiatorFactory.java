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

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Instantiator factory per loader.
 *
 * @author Ales Justin
 */
public class LoaderInstantiatorFactory extends AbstractInstantiatorFactory implements Function<ClassLoader, Boolean> {

    private volatile Boolean enabled;
    @SuppressWarnings("deprecation")
    private final Map<ClassLoader, Boolean> cached = new MapMaker().makeComputingMap(this);

    public boolean useInstantiators() {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        if (tccl == null) {
            if (enabled == null) {
                synchronized (this) {
                    if (enabled == null) {
                        boolean tmp = (getClass().getResource(MARKER) != null);
                        if (tmp) {
                            tmp = checkInstantiator();
                        }
                        enabled = tmp;
                    }
                }
            }
            return enabled;
        }

        return cached.get(tccl);
    }

    public void cleanup() {
        cached.clear();
    }

    public Boolean apply(ClassLoader tccl) {
        return (tccl.getResource(MARKER) != null) && checkInstantiator();
    }
}
