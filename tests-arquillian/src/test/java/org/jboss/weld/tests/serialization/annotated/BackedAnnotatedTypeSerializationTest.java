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
package org.jboss.weld.tests.serialization.annotated;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BackedAnnotatedTypeSerializationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(BackedAnnotatedTypeSerializationTest.class.getPackage())
                .addClass(Utils.class);
    }

    public AnnotatedType<Foo> getAnnotatedType() {
        ClassTransformer transformer = new ClassTransformer(new TypeStore());
        return transformer.getAnnotatedType(Foo.class);
    }

    @Test
    public void testType() throws Exception {
        Utils.deserialize(Utils.serialize(getAnnotatedType()));
    }

    @Test
    public void testFields() throws Exception {
        for (AnnotatedField<?> field : getAnnotatedType().getFields()) {
            Utils.deserialize(Utils.serialize(field));
        }
    }

    @Test
    public void testMethods() throws Exception {
        for (AnnotatedMethod<?> method : getAnnotatedType().getMethods()) {
            Utils.deserialize(Utils.serialize(method));
        }
    }

    @Test
    public void testConstructors() throws Exception {
        for (AnnotatedConstructor<?> constructor : getAnnotatedType().getConstructors()) {
            Utils.deserialize(Utils.serialize(constructor));
        }
    }

    @Test
    public void testParameters() throws Exception {
        for (AnnotatedConstructor<?> constructor : getAnnotatedType().getConstructors()) {
            for (AnnotatedParameter<?> parameter : constructor.getParameters()) {
                testParameter(parameter);
            }
        }
        for (AnnotatedMethod<?> method : getAnnotatedType().getMethods()) {
            for (AnnotatedParameter<?> parameter : method.getParameters()) {
                testParameter(parameter);
            }
        }
    }

    private void testParameter(AnnotatedParameter<?> parameter) throws Exception {
        Utils.deserialize(Utils.serialize(parameter));
    }
}
