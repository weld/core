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

public class SimpleInjectionPoint implements InjectionPoint
{
   
   public static final InjectionPoint EMPTY_INJECTION_POINT = new SimpleInjectionPoint(false, false, Object.class, Collections.<Annotation>emptySet(), null, null, null); 
   
   private final boolean _transient;
   private final boolean delegate;
   private final Type type;
   private final Set<Annotation> qualifiers;
   private final Member member;
   private final Bean<?> bean;
   private final Annotated annotated;
   
   private SimpleInjectionPoint(boolean _transient, boolean delegate, Type type, Set<Annotation> qualifiers, Member member, Bean<?> bean, Annotated annotated)
   {
      this._transient = _transient;
      this.delegate = delegate;
      this.type = type;
      this.qualifiers = qualifiers;
      this.member = member;
      this.bean = bean;
      this.annotated = annotated;
   }

   public boolean isTransient()
   {
      return _transient;
   }

   public boolean isDelegate()
   {
      return delegate;
   }

   public Type getType()
   {
      return type;
   }

   public Set<Annotation> getQualifiers()
   {
      return qualifiers;
   }

   public Member getMember()
   {
      return member;
   }

   public Bean<?> getBean()
   {
      return bean;
   }

   public Annotated getAnnotated()
   {
      return annotated;
   }
   
}
