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

import java.util.Locale;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

/**
 * A forwarding class that delegates to an ELContext
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingELContext extends ELContext
{
   
   protected abstract ELContext delgate();
   
   @Override
   public ELResolver getELResolver()
   {
      return delgate().getELResolver();
   }
   
   @Override
   public FunctionMapper getFunctionMapper()
   {
      return delgate().getFunctionMapper();
   }
   
   @Override
   public VariableMapper getVariableMapper()
   {
      return delgate().getVariableMapper();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delgate().equals(obj);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public Object getContext(Class key)
   {
      return delgate().getContext(key);
   }
   
   @Override
   public Locale getLocale()
   {
      return delgate().getLocale();
   }
   
   @Override
   public int hashCode()
   {
      return delgate().hashCode();
   }
   
   @Override
   public boolean isPropertyResolved()
   {
      return delgate().isPropertyResolved();
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void putContext(Class key, Object contextObject)
   {
      delgate().putContext(key, contextObject);
   }
   
   @Override
   public void setLocale(Locale locale)
   {
      delgate().setLocale(locale);
   }
   
   @Override
   public void setPropertyResolved(boolean resolved)
   {
      delgate().setPropertyResolved(resolved);
   }
   
   @Override
   public String toString()
   {
      return delgate().toString();
   }
}
