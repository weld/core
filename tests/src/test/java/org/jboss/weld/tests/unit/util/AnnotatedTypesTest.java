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
package org.jboss.weld.tests.unit.util;

import java.util.Iterator;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedTypeImpl;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;
import org.jboss.weld.util.AnnotatedTypes;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test comparison and id creation for AnnotatedTypes
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
public class AnnotatedTypesTest {
    /**
     * tests the AnnotatedTypes.compareAnnotatedTypes
     */
    @Test
    public void testComparison() throws SecurityException, NoSuchFieldException, NoSuchMethodException {
        //check that two weld classes on the same underlying are equal
        TypeStore ts = new TypeStore();
        ClassTransformer ct = new ClassTransformer(ts, new SharedObjectCache(), ReflectionCacheFactory.newInstance(ts),
                RegistrySingletonProvider.STATIC_INSTANCE);
        EnhancedAnnotatedType<Chair> chair1 = EnhancedAnnotatedTypeImpl
                .of(BackedAnnotatedType.of(Chair.class, ct.getSharedObjectCache(), ct.getReflectionCache(),
                        RegistrySingletonProvider.STATIC_INSTANCE, AnnotatedTypeIdentifier.NULL_BDA_ID), ct);
        EnhancedAnnotatedType<Chair> chair2 = EnhancedAnnotatedTypeImpl
                .of(BackedAnnotatedType.of(Chair.class, ct.getSharedObjectCache(), ct.getReflectionCache(),
                        RegistrySingletonProvider.STATIC_INSTANCE, AnnotatedTypeIdentifier.NULL_BDA_ID), ct);
        Assert.assertTrue(AnnotatedTypes.compareAnnotatedTypes(chair1, chair2));

        //check that a different implementation of annotated type is equal to the weld implementation
        TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToClass(new DefaultLiteral());
        builder.addToField(Chair.class.getField("legs"), new ProducesLiteral());
        builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
        AnnotatedType<Chair> chair3 = builder.create();
        Assert.assertTrue(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));

        //check that the implementation returns false if a field annotation changes
        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToClass(new DefaultLiteral());
        builder.addToField(Chair.class.getField("legs"), new DefaultLiteral());
        builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
        chair3 = builder.create();
        Assert.assertFalse(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));

        //check that the implementation returns false if a class level annotation changes
        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToClass(new ProducesLiteral());
        builder.addToField(Chair.class.getField("legs"), new DefaultLiteral());
        builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
        chair3 = builder.create();
        Assert.assertFalse(AnnotatedTypes.compareAnnotatedTypes(chair1, chair3));

    }

    @Test
    public void testFieldId() throws SecurityException, NoSuchFieldException {
        TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToField(Chair.class.getField("legs"), new ProducesLiteral());
        AnnotatedType<Chair> chair3 = builder.create();
        AnnotatedField<? super Chair> field = chair3.getFields().iterator().next();
        String id = AnnotatedTypes.createFieldId(field);
        Assert.assertEquals("org.jboss.weld.tests.unit.util.Chair.legs[@jakarta.enterprise.inject.Produces()]", id,
                "wrong id for field :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        chair3 = builder.create();
        field = chair3.getFields().iterator().next();
        id = AnnotatedTypes.createFieldId(field);
        Assert.assertEquals("org.jboss.weld.tests.unit.util.Chair.legs", id, "wrong id for field :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToField(Chair.class.getField("legs"), new ComfyChairLiteral());
        chair3 = builder.create();
        field = chair3.getFields().iterator().next();
        id = AnnotatedTypes.createFieldId(field);
        Assert.assertEquals("org.jboss.weld.tests.unit.util.Chair.legs[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]",
                id, "wrong id for field :" + id);
    }

    @Test
    public void testMethodId() throws SecurityException, NoSuchMethodException {
        TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
        AnnotatedType<Chair> chair3 = builder.create();
        Iterator<AnnotatedMethod<? super Chair>> it = chair3.getMethods().iterator();
        AnnotatedMethod<? super Chair> method = it.next();
        while (!method.getJavaMember().getName().equals("sit"))
            method = it.next();
        String id = AnnotatedTypes.createCallableId(method);
        Assert.assertEquals("org.jboss.weld.tests.unit.util.Chair.sit[@jakarta.enterprise.inject.Produces()]()", id,
                "wrong id for method :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        chair3 = builder.create();
        it = chair3.getMethods().iterator();
        method = it.next();
        while (!method.getJavaMember().getName().equals("sit"))
            method = it.next();
        id = AnnotatedTypes.createCallableId(method);
        Assert.assertEquals("org.jboss.weld.tests.unit.util.Chair.sit()", id, "wrong id for method :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToMethod(Chair.class.getMethod("sit"), new ComfyChairLiteral());
        chair3 = builder.create();
        it = chair3.getMethods().iterator();
        method = it.next();
        while (!method.getJavaMember().getName().equals("sit"))
            method = it.next();
        id = AnnotatedTypes.createCallableId(method);
        Assert.assertEquals(
                "org.jboss.weld.tests.unit.util.Chair.sit[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]()", id,
                "wrong id for method :" + id);
    }

    @Test
    public void testTypeId() throws SecurityException, NoSuchMethodException {
        TestAnnotatedTypeBuilder<Chair> builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToMethod(Chair.class.getMethod("sit"), new ProducesLiteral());
        AnnotatedType<Chair> chair3 = builder.create();
        String id = AnnotatedTypes.createTypeId(chair3);
        Assert.assertEquals(id, AnnotatedTypes.hash(
                "org.jboss.weld.tests.unit.util.Chair{org.jboss.weld.tests.unit.util.Chair.sit[@jakarta.enterprise.inject.Produces()]();}"),
                "wrong id for type :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        chair3 = builder.create();
        id = AnnotatedTypes.createTypeId(chair3);
        Assert.assertEquals(id, AnnotatedTypes.hash("org.jboss.weld.tests.unit.util.Chair{}"), "wrong id for type :" + id);

        builder = new TestAnnotatedTypeBuilder<Chair>(Chair.class);
        builder.addToMethod(Chair.class.getMethod("sit"), new ComfyChairLiteral());
        chair3 = builder.create();
        id = AnnotatedTypes.createTypeId(chair3);
        Assert.assertEquals(id, AnnotatedTypes.hash(
                "org.jboss.weld.tests.unit.util.Chair{org.jboss.weld.tests.unit.util.Chair.sit[@org.jboss.weld.tests.unit.util.ComfyChair(softness=1)]();}"),
                "wrong id for type :" + id);
    }

    private static class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
    }

    private static class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces {
    }

    private static class ComfyChairLiteral extends AnnotationLiteral<ComfyChair> implements ComfyChair {
        public int softness() {
            return 1;
        }

    }

}
