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

package org.jboss.webbeans.event;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;

/**
 * Basic factory class that produces implicit observers for observer methods.
 * 
 * @author David Allen
 * 
 */
public class ObserverFactory
{
   /**
    * Creates an observer
    * 
    * @param method The observer method abstraction
    * @param declaringBean The declaring bean
    * @param manager The Web Beans manager
    * @return An observer implementation built from the method abstraction
    */
   public static <T> ObserverImpl<T> create(AnnotatedMethod<?> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      ObserverImpl<T> result = null;
      if (TransactionalObserverImpl.isObserverMethodTransactional(method))
      {
         result = new TransactionalObserverImpl<T>(method, declaringBean, manager);
      }
      else
      {
         result = new ObserverImpl<T>(method, declaringBean, manager);
      }
      return result;
   }
}
