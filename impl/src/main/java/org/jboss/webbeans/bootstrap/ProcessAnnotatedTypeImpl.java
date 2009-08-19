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
package org.jboss.webbeans.bootstrap;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Container lifecycle event for each Java class or interface discovered by
 * the container.
 * 
 * @author pmuir
 * @author David Allen
 *
 */
public class ProcessAnnotatedTypeImpl<X> implements ProcessAnnotatedType<X>
{
   
   private AnnotatedType<X> annotatedType;
   private boolean veto;
   private boolean annotatedTypeReplaced = false;

   public ProcessAnnotatedTypeImpl(AnnotatedType<X> annotatedType)
   {
      this.annotatedType = annotatedType;
   }

   public AnnotatedType<X> getAnnotatedType()
   {
      return annotatedType;
   }

   public void setAnnotatedType(AnnotatedType<X> type)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("Cannot set the type to null (if you want to stop the type being used, call veto()) " + this);
      }
      this.annotatedType = type;
      this.annotatedTypeReplaced = true;
   }

   public void veto()
   {
      this.veto = true;
   }
   
   public boolean isVeto()
   {
      return veto;
   }
   
   public boolean isAnnotatedTypeReplaced()
   {
      return annotatedTypeReplaced;
   }

}
