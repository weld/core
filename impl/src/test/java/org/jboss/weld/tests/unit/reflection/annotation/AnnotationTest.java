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
package org.jboss.weld.tests.unit.reflection.annotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;

@Synchronous
public class AnnotationTest {

    @Test
    public void testSerializability() throws Throwable {
        Synchronous synchronous = AnnotationTest.class.getAnnotation(Synchronous.class);
        deserialize(serialize(synchronous));
    }

    @Test
    public void testGetAnnotationDefaults() throws Throwable {
        Method method = Quality.class.getMethod("value");
        Object value = method.getDefaultValue();
        Assert.assertEquals("very", value);
    }

    protected byte[] serialize(Object instance) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(instance);
        return bytes.toByteArray();
    }

    protected <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return Reflections.<T> cast(in.readObject());
    }

}
