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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.InjectionPoint;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.jlr.AbstractAnnotatedMember;

/**
 * The container provided implementation for InjectionPoint beans
 * 
 * @author David Allen
 */
@Standard
@Dependent
public class InjectionPointBean implements InjectionPoint
{
   private final AbstractAnnotatedMember<?, ? extends Member> memberInjectionPoint;
   private final Bean<?> bean;
   private final Object beanInstance;

   /**
    * Creates a new metadata bean for the given injection point information.
    * 
    * @param injectedMember The member of the bean being injected
    * @param bean The bean being injected
    * @param beanInstance The instance of the bean being injected
    */
   public InjectionPointBean(AbstractAnnotatedMember<?, ? extends Member> injectedMember, Bean<?> bean, Object beanInstance)
   {
      this.memberInjectionPoint = injectedMember;
      this.bean = bean;
      this.beanInstance = beanInstance;
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

   public Set<Annotation> getBindingTypes()
   {
      return this.memberInjectionPoint.getBindingTypes();
   }

   public Object getInstance()
   {
      return this.beanInstance;
   }

   public Member getMember()
   {
      return this.memberInjectionPoint.getDelegate();
   }

   public Type getType()
   {
      return this.memberInjectionPoint.getType();
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return this.memberInjectionPoint.isAnnotationPresent(annotationType);
   }
}
