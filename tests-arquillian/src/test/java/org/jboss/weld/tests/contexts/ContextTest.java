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
package org.jboss.weld.tests.contexts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Conversation;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ContextTest 
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(ContextTest.class.getPackage())
         .addClass(Utils.class);
   }
   
   @Inject
   private BeanManagerImpl beanManager;
   
   /*
    * description = "WELD-348"
    */
   @Test
   public void testCallToConversationWithContextNotActive()
   {
      new WorkInInactiveConversationContext()
      {
         
         @Override
         protected void work()
         {
            try
            {
               Utils.getReference(beanManager, Conversation.class).getId();
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).getTimeout();
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).begin();
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).begin("foo");
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).end();
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).isTransient();
               Assert.fail();
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
            try
            {
               Utils.getReference(beanManager, Conversation.class).setTimeout(0);
               assert false;
            }
            catch (ContextNotActiveException e) 
            {
               // Expected
            }
            catch (Exception e) 
            {
               Assert.fail();
            }
         }
      }.run();
      
   } 
   
   @Inject
   private Event<Mouse> mouseEvent; 
   
   /*
    * description = "WELD-480"
    */
   @Test
   public void testConditionalObserverOnNonActiveContext(Cat cat, final House house)
   {
      new WorkInInactiveRequestContext()
      {
         
         @Override
         protected void work()
         {
            Mouse mouse = new Mouse("Jerry");
            house.setMouse(mouse);
            mouseEvent.fire(mouse);
         }
         
      }.run();
      assertNull(cat.getMouse());
      assertNotNull(house.getMouse());
   }
}
