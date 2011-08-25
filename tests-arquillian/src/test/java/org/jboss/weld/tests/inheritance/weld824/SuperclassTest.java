/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.inheritance.weld824;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class SuperclassTest
{
   @Inject
   private Bar bar;
   
   /**
    * Webapp with beans.xml and no classes
    */
   @Deployment
   public static WebArchive createWebArchive()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
      war.addAsLibrary(createJavaArchive());
      war.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
      return war;
   }

   /**
    * Java library with two classes (Foo, Bar) and no beans.xml - thus the classes are not beans implicitly. 
    * However, the Bar class is registered through the SimpleExtension.
    */
   public static JavaArchive createJavaArchive()
   {
      JavaArchive war = ShrinkWrap.create(JavaArchive.class, "test.jar");
      war.addClasses(SimpleExtension.class, Foo.class, Bar.class);
      war.addAsManifestResource("org/jboss/weld/tests/inheritance/weld824/SimpleExtension", "services/javax.enterprise.inject.spi.Extension");
      return war;
   }
   
   @Test
   public void testSubclassInitialized()
   {
      assertTrue(bar.isSubclassInitialized());
      assertTrue(bar.isSuperclassInitialized());
   }
}
