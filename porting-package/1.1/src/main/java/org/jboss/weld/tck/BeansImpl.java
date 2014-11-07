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
package org.jboss.weld.tck;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import org.jboss.cdi.tck.spi.Beans;

/**
 * Implements the Beans SPI for the TCK specifically for the JBoss RI.
 *
 * @author Shane Bryzak
 * @author Pete Muir
 * @author David Allen
 */
public class BeansImpl implements Beans {

    public boolean isProxy(Object instance) {
        return instance.getClass().getName().indexOf("_$$_Weld") > 0;
    }

    public byte[] passivate(Object instance) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        try {
            out.writeObject(instance);
            return bytes.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public Object activate(byte[] bytes) throws IOException, ClassNotFoundException {
        TCCLObjectInputStream in = new TCCLObjectInputStream(new ByteArrayInputStream(bytes));
        try {
            return in.readObject();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static class TCCLObjectInputStream extends ObjectInputStream {

        private final ClassLoader classLoader;

        public TCCLObjectInputStream(InputStream in) throws IOException {
            super(in);
            this.classLoader = Thread.currentThread().getContextClassLoader();
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                String name = desc.getName();
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException e) {
                return super.resolveClass(desc);
            }
        }
    }

}
