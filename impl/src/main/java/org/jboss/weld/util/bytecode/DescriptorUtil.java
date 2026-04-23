/*
 * JBoss, Home of Professional Open Source
 * Copyright 2025, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.bytecode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Utility class for generating JVM type descriptors.
 * Provides lightweight replacements for org.jboss.classfilewriter.util.DescriptorUtils functionality.
 *
 * @author Claude (Gizmo 2 migration)
 */
public class DescriptorUtil {

    private DescriptorUtil() {
    }

    /**
     * Creates a JVM type descriptor for a class.
     *
     * @param clazz the class
     * @return the JVM type descriptor (e.g., "Ljava/lang/String;" or "I" for primitives)
     */
    public static String makeDescriptor(Class<?> clazz) {
        if (clazz == void.class) {
            return "V";
        }
        if (clazz == boolean.class) {
            return "Z";
        }
        if (clazz == byte.class) {
            return "B";
        }
        if (clazz == char.class) {
            return "C";
        }
        if (clazz == short.class) {
            return "S";
        }
        if (clazz == int.class) {
            return "I";
        }
        if (clazz == long.class) {
            return "J";
        }
        if (clazz == float.class) {
            return "F";
        }
        if (clazz == double.class) {
            return "D";
        }
        if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        }
        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    /**
     * Creates parameter descriptors for an array of parameter types.
     *
     * @param parameterTypes the parameter types
     * @return array of type descriptors
     */
    public static String[] parameterDescriptors(Class<?>[] parameterTypes) {
        String[] result = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            result[i] = makeDescriptor(parameterTypes[i]);
        }
        return result;
    }

    /**
     * Creates parameter descriptors for a method.
     *
     * @param method the method
     * @return array of type descriptors
     */
    public static String[] parameterDescriptors(Method method) {
        return parameterDescriptors(method.getParameterTypes());
    }

    /**
     * Creates a method descriptor string.
     *
     * @param parameterDescriptors the parameter type descriptors
     * @param returnTypeDescriptor the return type descriptor
     * @return the method descriptor (e.g., "(Ljava/lang/String;I)V")
     */
    public static String methodDescriptor(String[] parameterDescriptors, String returnTypeDescriptor) {
        StringBuilder sb = new StringBuilder("(");
        for (String param : parameterDescriptors) {
            sb.append(param);
        }
        sb.append(")");
        sb.append(returnTypeDescriptor);
        return sb.toString();
    }

    /**
     * Creates a method descriptor for a method.
     *
     * @param method the method
     * @return the method descriptor
     */
    public static String methodDescriptor(Method method) {
        return methodDescriptor(parameterDescriptors(method), makeDescriptor(method.getReturnType()));
    }

    /**
     * Creates a constructor descriptor for a constructor.
     *
     * @param constructor the constructor
     * @return the constructor descriptor
     */
    public static String makeDescriptor(Constructor<?> constructor) {
        return methodDescriptor(parameterDescriptors(constructor.getParameterTypes()), "V");
    }
}
