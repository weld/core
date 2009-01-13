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

package org.jboss.webbeans.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Initializer;
import javax.webbeans.InjectionPoint;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.introspector.AnnotatedParameter;

/**
 * The container provided implementation for InjectionPoint beans
 * 
 * @author David Allen
 */
@Standard
@Dependent
public class InjectionPointImpl implements InjectionPoint
{
   private final AnnotatedMember<?, ?> memberInjectionPoint;
   private final AnnotatedParameter<?> parameterInjectionPoint;
   private final Bean<?> bean;

   /**
    * Creates a new metadata bean for the given injection point information.
    * 
    * @param injectedMember The member of the bean being injected
    * @param bean The bean being injected
    * @param beanInstance The instance of the bean being injected
    */
   public InjectionPointImpl(AnnotatedField<?> injectedMember, Bean<?> bean)
   {
      this.memberInjectionPoint = injectedMember;
      this.parameterInjectionPoint = null;
      this.bean = bean;
   }

   /**
    * Creates a new injection point representing a parameter to a constructor or method.
    * 
    * @param injectedMember The constructor or method member
    * @param parameterInjectionPoint The parameter
    * @param bean The bean owning the injectedMember
    */
   public InjectionPointImpl(AnnotatedMember<?, ?> injectedMember, AnnotatedParameter<?> parameterInjectionPoint, Bean<?> bean)
   {
      this.memberInjectionPoint = injectedMember;
      this.parameterInjectionPoint = parameterInjectionPoint;
      this.bean = bean;
   }

   /**
    * Returns a new injection point of any type.  If this is a parameter, the
    * information about the parameter is null.
    * 
    * @param member The member being injected
    * @param bean The bean
    * @return a new injection point metadata bean
    */
   public static InjectionPointImpl of(AnnotatedMember<?, ?> member, Bean<?> bean)
   {
      if (member instanceof AnnotatedField)
         return new InjectionPointImpl((AnnotatedField<?>) member, bean);
      else
         return new InjectionPointImpl(member, null, bean);
   }

   public boolean isField()
   {
      return getMember() instanceof Field;
   }

   public boolean isMethod()
   {
      return getMember() instanceof Method;
   }

   public boolean isConstructor()
   {
      return getMember() instanceof Constructor;
   }

   public boolean isInitializer()
   {
      return isMethod() && isAnnotationPresent(Initializer.class);
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return this.memberInjectionPoint.getAnnotation(annotationType);
   }

   public Annotation[] getAnnotations()
   {
      return this.memberInjectionPoint.getAnnotations().toArray(new Annotation[0]);
   }

   public Bean<?> getBean()
   {
      return this.bean;
   }

   public Set<Annotation> getBindings()
   {
      if (isField())
         return this.memberInjectionPoint.getBindingTypes();
      else
         return this.parameterInjectionPoint.getBindingTypes();
   }

   public Member getMember()
   {
      return this.memberInjectionPoint.getMember();
   }

   public Type getType()
   {
      if (isField())
         return this.memberInjectionPoint.getType();
      else
         return this.parameterInjectionPoint.getType();
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return this.memberInjectionPoint.isAnnotationPresent(annotationType);
   }

   @Override
   public String toString()
   {
      return memberInjectionPoint.toString();
   }
}
