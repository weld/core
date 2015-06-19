/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.manager;

import java.util.Comparator;

import org.jboss.weld.manager.api.WeldManager;

import com.google.common.base.Function;


public final class BeanManagers {

    public static final Comparator<WeldManager> ID_COMPARATOR = new Comparator<WeldManager>() {
        @Override
        public int compare(WeldManager m1, WeldManager m2) {
            return m1.getId().compareTo(m2.getId());
        }
    };

    public static final Function<BeanManagerImpl, String> BEAN_MANAGER_TO_ID = new Function<BeanManagerImpl, String>() {
        @Override
        public String apply(BeanManagerImpl manager) {
            if (manager == null) {
                throw new IllegalArgumentException("manager"); // findbugs
            }
            return manager.getId();
        }
    };

    private BeanManagers() {
    }
}
