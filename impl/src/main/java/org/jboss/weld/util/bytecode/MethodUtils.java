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
package org.jboss.weld.util.bytecode;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility class for working with methods
 *
 * @author Stuart Douglas
 */
public class MethodUtils {

    private MethodUtils() {

    }

    /**
     * Creates a MethodInfo from the given information. This method must be added
     * to the ClassFile manually
     *
     * @param methodInfo The method information
     * @param exceptions checked exceptions thrown by the method
     * @param body       the method bytecode. This must have the correct value for
     *                   maxLocals already set
     * @param pool       the const pool
     * @return the created method
     */
    public static MethodInfo makeMethod(MethodInformation methodInfo, Class<?>[] exceptions, Bytecode body, ConstPool pool) {
        MethodInfo meth = new MethodInfo(pool, methodInfo.getName(), methodInfo.getDescriptor());
        meth.setAccessFlags(methodInfo.getModifiers());
        String[] ex = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; ++i) {
            ex[i] = exceptions[i].getName().replace('.', '/');
        }
        ExceptionsAttribute exAt = new ExceptionsAttribute(pool);
        exAt.setExceptions(ex);
        meth.setExceptionsAttribute(exAt);
        CodeAttribute ca = body.toCodeAttribute();
        meth.setCodeAttribute(ca);
        try {
            ca.computeMaxStack();
        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }
        return meth;
    }

    /**
     * Calculates maxLocals required to hold all parameters and this, assuming
     * that user code does not require any extra variables
     */
    public static int calculateMaxLocals(Method method) {
        int ret = 0;
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            ret = 1;
        }
        ret += method.getParameterTypes().length;
        for (Class<?> i : method.getParameterTypes()) {
            if (i == double.class || i == long.class) {
                ret++;
            }
        }
        return ret;
    }
}
