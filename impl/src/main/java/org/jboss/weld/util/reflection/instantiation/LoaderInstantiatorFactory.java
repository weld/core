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

import java.util.HashMap;
import java.util.Map;

/**
 * Instantiator factory per loader.
 *
 * @author Ales Justin
 */
public class LoaderInstantiatorFactory extends AbstractInstantiatorFactory {

    private final Map<ClassLoader, Boolean> enabled = new HashMap<ClassLoader, Boolean>();

    public boolean useInstantiators() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // TODO
        synchronized (enabled) {
            Boolean existing = enabled.get(cl);
            if (existing == null) {
                boolean tmp = (cl.getResource(MARKER) != null);
                if (tmp) {
                    tmp = checkInstantiator();
                }
                existing = tmp;
                enabled.put(cl, existing);
            }
            return existing;
        }
    }

    public void cleanup() {
    }
}
