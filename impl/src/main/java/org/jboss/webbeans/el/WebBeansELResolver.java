/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.webbeans.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * An EL-resolver against the named beans
 *  
 * @author Pete Muir
 */
public class WebBeansELResolver extends ELResolver
{

   private static final Namespace ROOT = new Namespace(null);
   
   /**
    * @see javax.el.ELResolver#getCommonPropertyType(ELContext, Object)
    */
   @Override
   public Class<?> getCommonPropertyType(ELContext context, Object base)
   {
      return null;
   }

   /**
    * @see javax.el.ELResolver#getFeatureDescriptors(ELContext, Object)
    */
   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
   {
      return null;
   }

   /**
    * @see javax.el.ELResolver#getType(ELContext, Object, Object)
    */
   @Override
   public Class<?> getType(ELContext context, Object base, Object property)
   {
      return null;
   }

   /**
    * @see javax.el.ELResolver#getValue(ELContext, Object, Object)
    */
   @Override
   public Object getValue(ELContext context, Object base, Object property)
   {
      if (property != null)
      {
         if (base == null) 
         {
            return new NamespacedResolver(context, ROOT, property.toString()).run().getValue();
         }
         else if (base instanceof Namespace) 
         {
            return new NamespacedResolver(context, (Namespace) base, property.toString()).run().getValue();
         }
      }
      return null;
   }

   /**
    * @see javax.el.ELResolver#isReadOnly(ELContext, Object, Object)
    */
   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property)
   {
      return false;
   }

   /**
    * @see javax.el.ELResolver#setValue(ELContext, Object, Object, Object)
    */
   @Override
   public void setValue(ELContext context, Object base, Object property, Object value)
   {
   }

}

