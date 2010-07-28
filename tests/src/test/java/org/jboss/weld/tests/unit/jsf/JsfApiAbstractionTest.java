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
package org.jboss.weld.tests.unit.jsf;

import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;

import org.jboss.weld.jsf.JsfApiAbstraction;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.ApiAbstraction.Dummy;
import org.testng.annotations.Test;

/**
 * @author Dan Allen
 */
public class JsfApiAbstractionTest
{
   
   @Test
   public void testDetectsJsf12Version()
   {
      JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoaderHidingJsf20Classes());
      assert abstraction.MINIMUM_API_VERSION == 1.2;
      assert abstraction.isApiVersionCompatibleWith(2.0) == false;
   }

   @Test
   public void testLoadsJsf12Classes()
   {
      JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoaderHidingJsf20Classes());
      assert FacesContext.class.equals(abstraction.FACES_CONTEXT);
      assert Dummy.class.equals(abstraction.BEHAVIOR_CLASS);
   }

   @Test
   public void testDetectsJsf20Version()
   {
      JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoader());
      assert abstraction.MINIMUM_API_VERSION == 2.0;
      assert abstraction.isApiVersionCompatibleWith(2.0);
   }

   @Test
   public void testLoadsJsf20Classes()
   {
      JsfApiAbstraction abstraction = new JsfApiAbstraction(getResourceLoader());
      assert FacesContext.class.equals(abstraction.FACES_CONTEXT);
      assert Behavior.class.equals(abstraction.BEHAVIOR_CLASS);
   }

   private ResourceLoader getResourceLoader()
   {
      return new DefaultResourceLoader();
   }

   private ResourceLoader getResourceLoaderHidingJsf20Classes()
   {
      return new DefaultResourceLoader()
      {

         @Override
         public Class<?> classForName(String name)
         {
            if ("javax.faces.component.behavior.Behavior".equals(name))
            {
               throw new ResourceLoadingException("Hidden class");
            }
            return super.classForName(name);
         }

      };
   }
}
