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

package javax.webbeans.manager;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.TypeLiteral;

/**
 * 
 * @author Pete Muir
 */

public interface Manager
{

   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes);

   public <T> T getInstanceByType(TypeLiteral<T> type,
         Annotation... bindingTypes);

   public <T> T resolveByType(Class<T> apiType, Annotation... bindingTypes);

   public <T> T resolveByType(TypeLiteral<T> apiType,
         Annotation... bindingTypes);

   public Object getInstanceByName(String name);
   
   public <T> T getInstance(Bean<T> bean);

   public Set<Bean<?>> resolveByName(String name);

   public void fireEvent(Object event, Annotation... bindings);
   
   public <T> void addObserver(Observer<T> observer);
   
   public <T> void removeObserver(Observer<T> observer);
   
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings);
   
   public void addContext(Context context);
   
   public Context getContext(Class<Annotation> scopeType);
   
   public Manager addBean(Bean<?> component);

}
