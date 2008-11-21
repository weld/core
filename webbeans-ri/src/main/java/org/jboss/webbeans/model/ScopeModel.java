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


package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;

import javax.webbeans.ScopeType;

/**
 * 
 * Model of a scope
 * 
 * @author Pete Muir
 *
 */
public class ScopeModel<T extends Annotation> extends AnnotationModel<T>
{
   
   public ScopeModel(Class<T> scope)
   {
      super(scope);
   }
   
   public boolean isNormal()
   {
      return getAnnotatedAnnotation().getAnnotation(ScopeType.class).normal();
   }
   
   public boolean isPassivating()
   {
      return getAnnotatedAnnotation().getAnnotation(ScopeType.class).passivating();
   }
   
   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return ScopeType.class;
   }
   
   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Scope model\n");
      buffer.append("Valid : " + isValid() + "\n");
      buffer.append("Annotated type " + getAnnotatedAnnotation().toString());
      return buffer.toString();
   }
   
}
