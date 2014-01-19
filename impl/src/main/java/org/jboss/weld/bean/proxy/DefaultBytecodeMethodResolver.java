/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.weld.util.bytecode.BytecodeUtils;

/**
 * A {@link BytecodeMethodResolver} that looks up the method using the
 * reflection API.
 * <p/>
 *
 * @author Stuart Douglas
 */
public class DefaultBytecodeMethodResolver implements BytecodeMethodResolver {

    private static final AtomicLong METHOD_COUNT = new AtomicLong();

    private static final String FIELD_NAME = "weld_proxy_field$$$";
    public static final String LJAVA_LANG_REFLECT_METHOD = "Ljava/lang/reflect/Method;";

    @Override
    public void getDeclaredMethod(final ClassMethod classMethod, final String declaringClass, final String methodName, final String[] parameterTypes, ClassMethod staticConstructor) {

        String fieldName = FIELD_NAME + METHOD_COUNT.incrementAndGet();
        staticConstructor.getClassFile().addField(AccessFlag.PRIVATE | AccessFlag.STATIC, fieldName, LJAVA_LANG_REFLECT_METHOD);
        final CodeAttribute code = staticConstructor.getCodeAttribute();
        BytecodeUtils.pushClassType(code, declaringClass);
        // now we have the class on the stack
        code.ldc(methodName);
        // now we need to load the parameter types into an array
        code.iconst(parameterTypes.length);
        code.anewarray(Class.class.getName());
        for (int i = 0; i < parameterTypes.length; ++i) {
            code.dup(); // duplicate the array reference
            code.iconst(i);
            // now load the class object
            String type = parameterTypes[i];
            BytecodeUtils.pushClassType(code, type);
            // and store it in the array
            code.aastore();
        }
        code.invokevirtual(Class.class.getName(), "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
        code.putstatic(classMethod.getClassFile().getName(), fieldName, LJAVA_LANG_REFLECT_METHOD);

        CodeAttribute methodCode = classMethod.getCodeAttribute();
        methodCode.getstatic(classMethod.getClassFile().getName(), fieldName, LJAVA_LANG_REFLECT_METHOD);

    }
}
