/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.beanManager.annotatedType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.ProtectionDomain;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bean.proxy.ByteArrayClassOutput;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.EmbeddedContainer;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import io.quarkus.gizmo2.Gizmo;

/**
 *
 * @author Martin Kouba
 * @see WELD-2062
 */
@Category(EmbeddedContainer.class)
@RunWith(Arquillian.class)
public class CreateAnnotatedTypeWithIdTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(CreateAnnotatedTypeWithIdTest.class))
                .addClass(Component.class);
    }

    @Test
    public void testCreateAnnotatedTypeWithId(BeanManagerImpl beanManager) {
        AnnotatedType<Component> annotatedType = beanManager.createAnnotatedType(Component.class);
        assertTrue(annotatedType.isAnnotationPresent(Dependent.class));
        assertFalse(hasPongMethod(annotatedType));

        // Create a different class with the same name
        // we need to define this class in a new CL to avoid duplicate declaration - hence we use Gizmo to generate it
        ByteArrayClassOutput classOutput = new ByteArrayClassOutput();
        Gizmo.create(classOutput).class_(Component.class.getName(), cc -> {
            cc.public_();

            // Add void pong()
            cc.method("pong", m -> {
                m.public_();
                m.synthetic();
                m.returning(void.class);

                m.body(b -> {
                    b.return_();
                });
            });
        });

        byte[] bytecode = classOutput.getBytes();
        SimpleClassLoader loader = new SimpleClassLoader(Component.class.getClassLoader());
        Class<?> componentClass = loader.publicDefineClass(Component.class.getName(), bytecode, 0, bytecode.length, null);

        @SuppressWarnings("unchecked")
        BackedAnnotatedType<Component> newAnnotatedType = (BackedAnnotatedType<Component>) beanManager.createAnnotatedType(
                componentClass,
                componentClass.getName() + componentClass.getClassLoader().hashCode());
        assertFalse(newAnnotatedType.isAnnotationPresent(Dependent.class));
        assertTrue(hasPongMethod(newAnnotatedType));
        // Dispose annotated type
        AnnotatedTypeIdentifier identifier = newAnnotatedType.getIdentifier();
        beanManager.disposeAnnotatedType(componentClass, componentClass.getName() + componentClass.getClassLoader().hashCode());
        assertNull(beanManager.getServices().get(ClassTransformer.class).getSlimAnnotatedTypeById(identifier));
    }

    private boolean hasPongMethod(AnnotatedType<?> annotatedType) {
        for (AnnotatedMethod<?> annotatedMethod : annotatedType.getMethods()) {
            if (annotatedMethod.getJavaMember().getName().equals("pong")) {
                return true;
            }
        }
        return false;
    }

    private static class SimpleClassLoader extends ClassLoader {

        SimpleClassLoader(ClassLoader parent) {
            super(parent);
        }

        public final Class<?> publicDefineClass(String name, byte[] b, int off, int len, ProtectionDomain pd) {
            return super.defineClass(name, b, off, len, pd);
        }
    }

}
