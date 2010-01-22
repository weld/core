/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class DummyInjectionPoint implements InjectionPoint
{
   
   public static final DummyInjectionPoint INSTANCE = new DummyInjectionPoint();
   
   public boolean isTransient()
   {
      return true;
   }

   public boolean isDelegate()
   {
      return false;
   }

   public Type getType()
   {
      return InjectionPoint.class;
   }

   public Set<Annotation> getQualifiers()
   {
      return Collections.emptySet();
   }

   public Member getMember()
   {
      return null;
   }

   public Bean<?> getBean()
   {
      return null;
   }

   public Annotated getAnnotated()
   {
      return null;
   }
   
}
