/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.security;

import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;

/**
 * Returns a field from the class or any class/interface in the inheritance hierarchy
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public class FieldLookupAction extends GetDeclaredFieldAction implements PrivilegedExceptionAction<Field> {

    public FieldLookupAction(Class<?> javaClass, String fieldName) {
        super(javaClass, fieldName);
    }

    @Override
    public Field run() throws NoSuchFieldException {
        return lookupField(javaClass, fieldName);
    }

    public static Field lookupField(Class<?> javaClass, String fieldName) throws NoSuchFieldException {
        for (Class<?> inspectedClass = javaClass; inspectedClass != null; inspectedClass = inspectedClass.getSuperclass()) {
            for (Class<?> inspectedInterface : inspectedClass.getInterfaces()) {
                try {
                    return lookupField(inspectedInterface, fieldName);
                } catch (NoSuchFieldException e) {
                    // Expected, nothing to see here.
                }
            }
            try {
                return inspectedClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException nsme) {
                // Expected, nothing to see here.
            }
        }
        throw new NoSuchFieldException();
    }
}
