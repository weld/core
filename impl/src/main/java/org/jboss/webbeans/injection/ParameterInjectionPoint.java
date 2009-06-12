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
import java.lang.reflect.Member;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.introspector.ForwardingWBParameter;

public class ParameterInjectionPoint<T> extends ForwardingWBParameter<T> implements WBInjectionPoint<T, Object>
{
   
   private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
   
   public static <T> ParameterInjectionPoint<T> of(Bean<?> declaringBean, WBParameter<T> parameter)
   {
      return new ParameterInjectionPoint<T>(declaringBean, parameter);
   }
   
   private final Bean<?> declaringBean;
   private final WBParameter<T> parameter;

   private ParameterInjectionPoint(Bean<?> declaringBean, WBParameter<T> parameter)
   {
      this.declaringBean = declaringBean;
      this.parameter = parameter;
   }

   @Override
   protected WBParameter<T> delegate()
   {
      return parameter;
   }

   public Bean<?> getBean()
   {
      return declaringBean;
   }

   public Set<Annotation> getBindings()
   {
      return delegate().getBindings();
   }

   public Member getMember()
   {
      return delegate().getDeclaringMember().getMember();
   }
   
   public void inject(Object declaringInstance, Object value)
   {
      throw new UnsupportedOperationException();
   }
   
   @SuppressWarnings("unchecked")
   public T getValueToInject(BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      return (T) manager.getInjectableReference(this, creationalContext);
   }

   public Annotated getAnnotated()
   {
      return new AnnotatedAdaptor(delegate());
   }

   public boolean isDelegate()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isTransient()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
