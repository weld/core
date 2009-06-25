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
package javax.enterprise.event;

/**
 * The enumeration {@link TransactionPhase} identifies the kind
 * of transactional observer method
 * 
 * Transactional observer methods are observer methods which receive event
 * notifications during the before or after completion phase of the
 * transaction in which the event was fired. If no transaction is in progress
 * when the event is fired, they are notified at the same time as other
 * observers.
 * 
 * 
 * @author Pete Muir
 * 
 */
public enum TransactionPhase
{

   IN_PROGRESS,

   /**
    * A before completion observer method is called during the before completion
    * phase of the transaction.
    */
   BEFORE_COMPLETION,

   /**
    * An after completion observer method is called during the after completion
    * phase of the transaction.
    */
   AFTER_COMPLETION,

   /**
    * An after failure observer method is called during the after completion
    * phase of the transaction, only when the transaction fails.
    */
   AFTER_FAILURE,

   /**
    * A before completion observer method is called during the before completion
    * phase of the transaction.
    */
   AFTER_SUCCESS

}
