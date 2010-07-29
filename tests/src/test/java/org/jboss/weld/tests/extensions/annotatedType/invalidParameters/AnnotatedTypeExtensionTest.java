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
package org.jboss.weld.tests.extensions.annotatedType.invalidParameters;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

//@ExpectedDeploymentException(Exception.class)
@Category(Integration.class)
@RunWith(Arquillian.class)
public class AnnotatedTypeExtensionTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
                  .addWebResource(EmptyAsset.INSTANCE, "beans.xml")
                  .addPackage(AnnotatedTypeExtensionTest.class.getPackage())
                  .addServiceProvider(Extension.class, AnnotatedTypeExtension.class);
   }

   /*
    * description = "WELD-371"
    */
   @Test
   public void testIncorrectlyOverridenParameters()
   {
      assert false;
   }

}
