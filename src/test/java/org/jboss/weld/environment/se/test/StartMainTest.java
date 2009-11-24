/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.events.Shutdown;
import org.jboss.weld.environment.se.test.beans.CustomEvent;
import org.jboss.weld.environment.se.test.beans.InitObserverTestBean;
import org.jboss.weld.environment.se.test.beans.MainTestBean;
import org.jboss.weld.environment.se.test.beans.ObserverTestBean;
import org.jboss.weld.environment.se.test.beans.ParametersTestBean;
import org.jboss.weld.environment.se.util.WeldManagerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Peter Royle
 */
public class StartMainTest
{

   public static String[] ARGS = new String[] { "arg1", "arg2", "arg3" };
   public static String[] ARGS_EMPTY = new String[] {};

   /**
    * Test of main method, of class StartMain. Checks that the beans found in
    * the org.jboss.weld.environment.se.beans package are initialised as
    * expected.
    */
   @Test
   public void testMain()
   {
      String[] args = ARGS;
      BeanManager manager = new StartMain(args).go();

      MainTestBean mainTestBean = WeldManagerUtils.getInstanceByType(manager, MainTestBean.class);
      Assert.assertNotNull(mainTestBean);

      ParametersTestBean paramsBean = mainTestBean.getParametersTestBean();
      Assert.assertNotNull(paramsBean);
      Assert.assertNotNull(paramsBean.getParameters());
      Assert.assertNotNull(paramsBean.getParameters().get(0));
      Assert.assertEquals(ARGS[0], paramsBean.getParameters().get(0));
      Assert.assertNotNull(paramsBean.getParameters().get(1));
      Assert.assertEquals(ARGS[1], paramsBean.getParameters().get(1));
      Assert.assertNotNull(paramsBean.getParameters().get(2));
      Assert.assertEquals(ARGS[2], paramsBean.getParameters().get(2));

      shutdownManager(manager);
   }

   /**
    * Test of main method, of class StartMain when no command-line args are
    * provided.
    */
   @Test
   public void testMainEmptyArgs()
   {
      BeanManager manager = new StartMain(ARGS_EMPTY).go();

      MainTestBean mainTestBean = WeldManagerUtils.getInstanceByType(manager, MainTestBean.class);
      Assert.assertNotNull(mainTestBean);

      ParametersTestBean paramsBean = mainTestBean.getParametersTestBean();
      Assert.assertNotNull(paramsBean);
      Assert.assertNotNull(paramsBean.getParameters());

      shutdownManager(manager);
   }

   @Test
   public void testObservers()
   {
      InitObserverTestBean.reset();
      ObserverTestBean.reset();

      BeanManager manager = new StartMain(ARGS_EMPTY).go();
      manager.fireEvent(new CustomEvent());

      Assert.assertTrue(ObserverTestBean.isBuiltInObserved());
      Assert.assertTrue(ObserverTestBean.isCustomObserved());
      Assert.assertTrue(ObserverTestBean.isInitObserved());

      Assert.assertTrue(InitObserverTestBean.isInitObserved());
   }

   private void shutdownManager(BeanManager manager)
   {
      manager.fireEvent(manager, new ShutdownAnnotation());
   }

   private static class ShutdownAnnotation extends AnnotationLiteral<Shutdown>
   {

      public ShutdownAnnotation()
      {
      }
   }

}
