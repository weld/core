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
package org.jboss.weld.tests.unit.bootstrap.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.logging.messages.XmlMessage;
import org.jboss.weld.mock.MockEELifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.xml.WeldXmlException;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class BeansXmlTest
{
   private class FailedDeployment
   {
      private List<Class<?>> beans = Collections.emptyList();
      private List<URL> beansXml = Collections.emptyList();

      public FailedDeployment(List<Class<?>> beans, List<URL> beansXml)
      {
         this.beans = beans;
         this.beansXml = beansXml;
      }

      public void run()
      {
         TestContainer container = null;
         try
         {
            container = new TestContainer(new MockEELifecycle(), beans, beansXml).startContainer().ensureRequestActive();
         }
         finally
         {
            if (container != null)
            {
               container.stopContainer();
            }
         }
      }

      public void runAndExpect(WeldXmlException expected)
      {
         String errorCode = expected.getMessage().substring(0, 12);
         try
         {
            run();
         }
         catch (WeldXmlException e)
         {
            if (e.getMessage().startsWith(errorCode))
            {
               return;
            }
         }
         assert false;
      }

   }

   private void testWithBeansXmlAndExpectException(String beansXml, WeldXmlException e)
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, Plain.class, IntBind.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource(beansXml));
      new FailedDeployment(beans, beansXmls).runAndExpect(e);
   }

   // Multiple XML blocks

   @Test
   public void multipleAlternativeBlocksFail()
   {
      testWithBeansXmlAndExpectException("multipleAlternativeBlocks.xml", new WeldXmlException(XmlMessage.MULTIPLE_ALTERNATIVES));
   }

   @Test
   public void multipleDecoratorBlocksFail()
   {
      testWithBeansXmlAndExpectException("multipleDecoratorBlocks.xml", new WeldXmlException(XmlMessage.MULTIPLE_DECORATORS));
   }

   @Test
   public void multipleInterceptorBlocksFail()
   {
      testWithBeansXmlAndExpectException("multipleInterceptorsBlocks.xml", new WeldXmlException(XmlMessage.MULTIPLE_INTERCEPTORS));
   }

   @Test
   public void alternativesEnabled()
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, IntBind.class, Plain.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource("alternative.xml"));
      TestContainer container = new TestContainer(new MockEELifecycle(), beans, beansXmls).startContainer().ensureRequestActive();
      assert container.getBeanManager().getEnabledAlternativeClasses().size() == 1;
      assert container.getBeanManager().getEnabledAlternativeClasses().iterator().next() == Alt.class;
      container.stopContainer();
   }

   @Test
   public void decoratorsEnabled()
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, IntBind.class, Plain.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource("decorator.xml"));
      TestContainer container = new TestContainer(new MockEELifecycle(), beans, beansXmls).startContainer().ensureRequestActive();
      assert container.getBeanManager().getEnabledDecoratorClasses().size() == 1;
      assert container.getBeanManager().getEnabledDecoratorClasses().iterator().next() == Dec.class;
      container.stopContainer();
   }

   @Test
   public void interceptorsEnabled()
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, IntBind.class, Plain.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource("interceptor.xml"));
      TestContainer container = new TestContainer(new MockEELifecycle(), beans, beansXmls).startContainer().ensureRequestActive();
      assert container.getBeanManager().getEnabledInterceptorClasses().size() == 1;
      assert container.getBeanManager().getEnabledInterceptorClasses().iterator().next() == Int.class;
      container.stopContainer();
   }

   @Test
   public void testMergeBeansXmls()
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, IntBind.class, Plain.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource("alternative.xml"), getClass().getResource("decorator.xml"), getClass().getResource("interceptor.xml"));
      TestContainer container = new TestContainer(new MockEELifecycle(), beans, beansXmls).startContainer().ensureRequestActive();
      assert container.getBeanManager().getEnabledAlternativeClasses().size() == 1;
      assert container.getBeanManager().getEnabledAlternativeClasses().iterator().next() == Alt.class;
      assert container.getBeanManager().getEnabledInterceptorClasses().size() == 1;
      assert container.getBeanManager().getEnabledInterceptorClasses().iterator().next() == Int.class;
      assert container.getBeanManager().getEnabledDecoratorClasses().size() == 1;
      assert container.getBeanManager().getEnabledDecoratorClasses().iterator().next() == Dec.class;
      container.stopContainer();
   }

   @Test
   public void testBeansXmlDoesntExist()
   {
      testWithBeansXmlAndExpectException("nope.xml", new WeldXmlException(XmlMessage.LOAD_ERROR));
   }

   @Test(groups="stub")
   public void testCannotGetDocumentBuilder()
   {
   }
   /*
    * https://jira.jboss.org/jira/browse/WELD-362
    */
   @Test
   public void testNonPrettyPrintedXML()
   {
      List<Class<?>> beans = Arrays.asList(Alt.class, Dec.class, Int.class, IntBind.class, Plain.class);
      List<URL> beansXmls = Arrays.asList(getClass().getResource("nonPrettyPrinted.xml"));
      TestContainer container = new TestContainer(new MockEELifecycle(), beans, beansXmls).startContainer().ensureRequestActive();
      assert container.getBeanManager().getEnabledAlternativeClasses().size() == 1;
      assert container.getBeanManager().getEnabledAlternativeClasses().iterator().next() == Alt.class;
      container.stopContainer();
   }

   @Test
   public void testCannotLoadFile() throws MalformedURLException
   {
      List<Class<?>> beans = Collections.emptyList();
      new FailedDeployment(beans, Arrays.asList(new URL("http://foo.bar/beans.xml"))).runAndExpect(new WeldXmlException(XmlMessage.LOAD_ERROR));
   }

   @Test
   public void testParsingError()
   {
      testWithBeansXmlAndExpectException("unparseable.xml", new WeldXmlException(XmlMessage.PARSING_ERROR));
   }

   @Test
   public void testCannotLoadClass()
   {
      testWithBeansXmlAndExpectException("unloadable.xml", new WeldXmlException(XmlMessage.CANNOT_LOAD_CLASS));
   }

}
