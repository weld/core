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
package org.jboss.weld.tests.resolution;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Map;

import static org.jboss.weld.test.util.Utils.getReference;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class ResolutionTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(ResolutionTest.class.getPackage())
         .addClass(Utils.class);
   }

   @Inject
   private BeanManagerImpl beanManager;

   @Inject Wibble wibble;

    @Test
    // WELD-711
    public void testResolveWithAnonymousAnnotationLiteral() throws Exception {
        Annotation defaultQualifier = new AnnotationLiteral<Default>() {
        };
        assertNotNull(getReference(beanManager, Foo.class, defaultQualifier));
        TypeSafeBeanResolver<?> resolver = beanManager.getBeanResolver();
        assertFalse(resolver.isCached(new ResolvableBuilder(beanManager).addType(Foo.class).addQualifier(defaultQualifier).create()));
    }

   // WELD-873
   @Test
   public void testCallingUserMethod()
   {
      assertNull(wibble.get("bleh"));
   }

   // WELD-873
   @Test
   public void testCallingBridgeMethod()
   {
      assertNull(((Map)wibble).get("bleh"));
   }

}
