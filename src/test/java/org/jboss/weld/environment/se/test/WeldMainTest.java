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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.events.Shutdown;
import org.jboss.weld.environment.se.test.beans.CustomEvent;
import org.jboss.weld.environment.se.test.beans.InitObserverTestBean;
import org.jboss.weld.environment.se.test.beans.MainTestBean;
import org.jboss.weld.environment.se.test.beans.ObserverTestBean;
import org.jboss.weld.environment.se.test.beans.ParametersTestBean;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author Peter Royle
 */
public class WeldMainTest
{

   /**
    * Test the alternate API for boting Weld from an SE app.
    */
   @Test
   public void testInitialize()
   {

      Weld weld = new Weld().initialize();

      MainTestBean mainTestBean = weld.instance().select(MainTestBean.class).get();
      Assert.assertNotNull(mainTestBean);

      ParametersTestBean paramsBean = mainTestBean.getParametersTestBean();
      Assert.assertNotNull(paramsBean);
      Assert.assertNotNull(paramsBean.getParameters());

      shutdownManager(weld.getBeanManager());
   }

   /**
    * Test the firing of observers using the alternate API for boting Weld from an SE app.
    */
   @Test
   public void testObservers()
   {
      InitObserverTestBean.reset();
      ObserverTestBean.reset();

      Weld weld = new Weld().initialize();
      weld.event().select(CustomEvent.class).fire(new CustomEvent());

      Assert.assertTrue(ObserverTestBean.isBuiltInObserved());
      Assert.assertTrue(ObserverTestBean.isCustomObserved());
      Assert.assertFalse(ObserverTestBean.isInitObserved());

      Assert.assertFalse(InitObserverTestBean.isInitObserved());
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
