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

package org.jboss.weld.tests.unit.serialization;

import java.lang.reflect.Method;

import org.jboss.weld.serialization.MethodHolder;
import org.jboss.weld.test.util.Utils;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MethodHolderSerializationTest {

    @Test
    public void testPrimitiveParameters() throws Exception {
        Method method = getMethod("methodWithPrimitiveParameters", byte.class, short.class, int.class, long.class, float.class,
                double.class, boolean.class, char.class);
        assertReferenceSerializable(method);
    }

    @Test
    public void testObjectArrayParameter() throws Exception {
        Method method = getMethod("methodWithObjectArrayParameter", String[].class);
        assertReferenceSerializable(method);
    }

    @Test
    public void testPrimitiveArrayParameter() throws Exception {
        Method method = getMethod("methodWithPrimitiveArrayParameter", int[].class);
        assertReferenceSerializable(method);
    }

    @Test
    public void testInnerClassParameter() throws Exception {
        Method method = getMethod("methodWithInnerClassParameter", InnerClass.class);
        assertReferenceSerializable(method);
    }

    private Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return getClass().getDeclaredMethod(name, parameterTypes);
    }

    private void assertReferenceSerializable(Method method) throws Exception {
        MethodHolder reference = MethodHolder.of(method);
        Utils.deserialize(Utils.serialize(reference));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void methodWithPrimitiveParameters(byte b, short s, int i, long l, float f, double d, boolean bool, char ch) {
    }

    @SuppressWarnings("UnusedDeclaration")
    public void methodWithObjectArrayParameter(String[] strArray) {
    }

    @SuppressWarnings("UnusedDeclaration")
    public void methodWithPrimitiveArrayParameter(int[] intArray) {
    }

    @SuppressWarnings("UnusedDeclaration")
    public void methodWithInnerClassParameter(InnerClass innerClass) {
    }

    public static class InnerClass {

    }
}
