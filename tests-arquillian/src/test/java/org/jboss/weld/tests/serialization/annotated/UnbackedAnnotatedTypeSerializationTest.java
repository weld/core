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

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UnbackedAnnotatedTypeSerializationTest extends BackedAnnotatedTypeSerializationTest {

    @Inject
    private BeanManager manager;
    
    @Override
    public AnnotatedType<Foo> getAnnotatedType() {
        ClassTransformer transformer = Reflections.<BeanManagerImpl>cast(manager).getServices().get(ClassTransformer.class);
        final AnnotatedType<Foo> delegate = transformer.getAnnotatedType(Foo.class);
        final AnnotatedType<Foo> wrapped = new ForwardingAnnotatedType<Foo>(delegate);
        AnnotatedType<Foo> unbackedAnnotatedType = transformer.getAnnotatedType(wrapped);
        return unbackedAnnotatedType;
    }

}
