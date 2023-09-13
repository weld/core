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
package org.jboss.weld.tests.interceptors.hierarchical;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.util.Beans;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class SimpleWeldClassTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SimpleWeldClassTest.class))
                .addPackage(InterceptorsWithHierarchyTest.class.getPackage());
    }

    /*
     * description = "WELD-568"
     */
    @Category(Broken.class)
    @Test
    public void testWeldClassForCovariantReturnType() {
        TypeStore typeStore = new TypeStore();
        EnhancedAnnotatedType<Attacker> weldClass = new ClassTransformer(typeStore, new SharedObjectCache(),
                ReflectionCacheFactory.newInstance(typeStore), RegistrySingletonProvider.STATIC_INSTANCE)
                .getEnhancedAnnotatedType(Attacker.class, AnnotatedTypeIdentifier.NULL_BDA_ID);
        Collection<EnhancedAnnotatedMethod<?, ? super Attacker>> methods = weldClass.getEnhancedMethods();
        Assert.assertEquals(4, methods.size());
        List<EnhancedAnnotatedMethod<?, ?>> interceptableMethods = Beans.getInterceptableMethods(weldClass);
        Assert.assertEquals(4, interceptableMethods.size());
    }
}
