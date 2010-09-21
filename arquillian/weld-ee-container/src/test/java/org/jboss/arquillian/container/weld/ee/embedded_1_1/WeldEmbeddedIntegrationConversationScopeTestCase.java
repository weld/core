/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.container.weld.ee.embedded_1_1.beans.TalkingChicken;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WeldEmbeddedIntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WeldEmbeddedIntegrationConversationScopeTestCase
{
   @Deployment
   public static JavaArchive createdeployment() 
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
                  .addClasses(
                        WeldEmbeddedIntegrationConversationScopeTestCase.class,
                        TalkingChicken.class)
                  .addManifestResource(
                        new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"));
   }
   
   @Inject
   private TalkingChicken chicken;
   
   @Inject Conversation conversation;
   
   @Test
   public void shouldBeAbleToSetAgeWithoutStartingAConversation() throws Exception 
   {
      Assert.assertNotNull(
            "Verify that the Bean has been injected",
            chicken);
      
      chicken.setAge(10);
      assertEquals(new Integer(10), chicken.getAge());
   }

   @Test
   public void shouldNotBeAbleToReadAgeConversationNotStarted() throws Exception 
   {
      assertNotNull(
            "Verify that the Bean has been injected",
            chicken);
      
      assertEquals(new Integer(-1), chicken.getAge());
   }

   @Test
   public void shouldBeAbleToSetAgeAndStartAConversation() throws Exception 
   {
      assertNotNull(
            "Verify that the Bean has been injected",
            chicken);
      
      chicken.setAge(10);
      conversation.begin();
   }

   // This works most of the time, you can uncomment to test manually
   @Test @Ignore // Can't do dependent methods in JUnit
   public void shouldBeAbleToReadAgeAfterConversationWasStarted() throws Exception 
   {
      assertNotNull(
            "Verify that the Bean has been injected",
            chicken);
      
      assertEquals(new Integer(10), chicken.getAge());
   }
}
