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

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.Utils;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Integration tests for Web Bean events.
 * 
 * @author David Allen
 * 
 */
@Category(Integration.class)
@RunWith(Arquillian.class) 
public class TransactionalObserversTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
                  .addPackage(TransactionalObserversTest.class.getPackage())
                  .addClass(Utils.class);
   }

   @Inject @Tame
   private PomeranianInterface dog;

   @Inject
   private Agent               dogAgent;

   @Test
   @Category(Broken.class)
   public void testTransactionalObserverNotifiedImmediatelyWhenNoTransactionInProgress()
   {
      dog.setCorrectContext(false);
      dog.setCorrectTransactionState(false);
      assert dogAgent != null;
      dogAgent.sendOutsideTransaction(BigInteger.TEN);
      assert dog.isCorrectTransactionState();
   }

   @Test
   public void testAfterTransactionCompletionObserver() throws InterruptedException
   {
      dog.setCorrectContext(false);
      dog.setCorrectTransactionState(false);
      dogAgent.sendInTransaction("event");
      Thread.sleep(100);
      assert dog.isCorrectTransactionState();
   }

   @Test
   public void testAfterTransactionSuccessObserver() throws InterruptedException
   {
      dog.setCorrectContext(false);
      dog.setCorrectTransactionState(false);
      dogAgent.sendInTransaction(new Integer(4));
      Thread.sleep(100);
      assert dog.isCorrectTransactionState();
   }

   @Test
   public void testAfterTransactionFailureObserver() throws Exception
   {
      dog.setCorrectContext(false);
      dog.setCorrectTransactionState(false);
      try
      {
         dogAgent.sendInTransactionAndFail(new Float(4.0));
      }
      catch (Exception e)
      {
         if (!isThrowablePresent(e))
         {
            throw e;
         }
      }
      Thread.sleep(100);
      assert dog.isCorrectTransactionState();
   }

   @Test
   @Category(Broken.class)
   public void testBeforeTransactionCompletionObserver()
   {
      dog.setCorrectContext(false);
      dog.setCorrectTransactionState(false);
      dogAgent.sendInTransaction(new RuntimeException("test event"));
      assert dog.isCorrectTransactionState();
   }
   
   private boolean isThrowablePresent(Exception exception)
   {
      boolean present = false;
      Throwable t = exception;
      while (t != null)
      {
         if (t instanceof FooException)
         {
            present = true;
         }
         t = t.getCause();
      }
      return present;
   }
}
