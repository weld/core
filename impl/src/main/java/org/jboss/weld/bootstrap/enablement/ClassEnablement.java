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
package org.jboss.weld.bootstrap.enablement;

public class ClassEnablement {

    private final Class<?> enabledClass;
    private final String location;
    private final Integer explicitPriority;

    public ClassEnablement(Class<?> javaClass, String location, Integer explicitPriority) {
        this.enabledClass = javaClass;
        this.location = location;
        this.explicitPriority = explicitPriority;
    }

    public Class<?> getEnabledClass() {
        return enabledClass;
    }

    public String getLocation() {
        return location;
    }

    /**
     * Returns the priority of the enabled class or null if there is no explicit priority associated with the given enablement.
     *
     * @return
     */
    public Integer getPriority() {
        return explicitPriority;
    }
}
