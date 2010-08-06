/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.observer.transactional;

import java.math.BigInteger;

import javax.ejb.Local;

@Local
public interface PomeranianInterface
{
   /**
    * Observes a String event only after the transaction is completed.
    * 
    * @param someEvent
    */
   public void observeStringEvent(String someEvent);

   /**
    * Observes an Integer event if the transaction is successfully completed.
    * 
    * @param event
    */
   public void observeIntegerEvent(Integer event);

   /**
    * Observes a Float event only if the transaction failed.
    * 
    * @param event
    */
   public void observeFloatEvent(Float event);

   public void observeBigIntegerEvent(BigInteger event);
   
   public void observeExceptionEvent(RuntimeException event);
   
   public void observeCharEvent(Character event);

   public boolean isCorrectContext();

   public void setCorrectContext(boolean correctContext);

   public boolean isCorrectTransactionState();

   public void setCorrectTransactionState(boolean correctTransactionState);
   
   public void removeSessionBean();
}
