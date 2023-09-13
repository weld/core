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
package org.jboss.weld.annotated.enhanced;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface MethodSignature extends Serializable {

    String getMethodName();

    String[] getParameterTypes();

    /**
     * Determines whether this method signature matches the signature of the given method
     *
     * @param method the given method
     * @return true iff the method signature represented by this object matches the signature of the given method
     */
    boolean matches(Method method);

}
