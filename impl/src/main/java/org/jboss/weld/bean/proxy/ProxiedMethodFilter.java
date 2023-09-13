/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import java.lang.reflect.Method;

/**
 * Allows certain bean class methods to be ignored when creating a proxy / subclass. An example would be Groovy integration
 * where each groovy object implements the GroovyObject interface. However, when the methods defined by this interface are
 * implemented by Weld proxy / subclass, Groovy runtime does not work correctly any longer.
 *
 * An implementation of ProxiedMethodFilter may be used to filter out bean class methods that should not be implemented by
 * a proxy / subclass.
 *
 * @see GroovyMethodFilter
 * @see WELD-840
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
@FunctionalInterface
public interface ProxiedMethodFilter {

    /**
     * Determines whether this filter is enabled. E.g. GroovyMethodFilter is only enabled in Groovy environment
     *
     * @return true if this method filter should be used
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Determines, whether the given method should be implemented by the proxy / subclass or not.
     *
     * @param method the given method
     * @param proxySuperclass the class the proxy extends directly
     * @return true iff the method filter does not ban the given method from being implemented
     */
    boolean accept(Method method, Class<?> proxySuperclass);
}
