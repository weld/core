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
package org.jboss.weld.tests.unit.environments.servlet;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A set of tests that validates that the contexts are properly created and
 * destroyed from the perspective of a servlet environment.
 * 
 * TODO PLM this test is severely broken in design due to a lack of
 * understanding of how lifecycle is built (it is an application singleton, it
 * can't just be replaced). If it starts to fail, it needs rewriting to be an
 * i/c test that runs in a servlet container OR without using the deployment
 * arch
 * 
 * TODO Rewrite correctly
 * 
 * @author Dan Allen
 */
@Artifact
public class ServletLifecycleTest extends AbstractWeldTest
{

   @Test(groups = "broken")
   public void testEndSessionWithActiveRequestAndSessionContexts()
   {
//      ServletLifecycle servletLifecycle = new ServletLifecycle(new ContextLifecycle());
//      BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
//      RequestContext.instance().setBeanStore(requestBeanStore);
//      RequestContext.instance().setActive(true);
//
//      BeanStore sessionBeanStore = new ConcurrentHashMapBeanStore();
//      SessionContext.instance().setBeanStore(sessionBeanStore);
//      SessionContext.instance().setActive(true);
//
//      HttpSession session = new MockHttpSession("99");
//      servletLifecycle.endSession(session);
//      assert Boolean.FALSE.equals(SessionContext.instance().isActive()) : "Session context should no longer be active";
//      assert Boolean.TRUE.equals(RequestContext.instance().isActive()) : "Request context should still be active";
   }

   @Test(groups = "broken")
   public void testEndSessionWithActiveRequestContextOnly()
   {
//      ServletLifecycle servletLifecycle = new ServletLifecycle(new ContextLifecycle());
//      BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
//      RequestContext.instance().setBeanStore(requestBeanStore);
//      RequestContext.instance().setActive(true);
//
//      HttpSession session = new MockHttpSession("99");
//      servletLifecycle.endSession(session);
//      assert Boolean.FALSE.equals(SessionContext.instance().isActive()) : "Session context should no longer be active";
//      assert Boolean.TRUE.equals(RequestContext.instance().isActive()) : "Request context should still be active";
   }

   @Test(groups = "broken")
   public void testEndSessionWithNoActiveRequestOrSessionContexts()
   {
//      ServletLifecycle servletLifecycle = new ServletLifecycle(new ContextLifecycle());
//
//      HttpSession session = new MockHttpSession("99");
//      servletLifecycle.endSession(session);
//      assert Boolean.FALSE.equals(SessionContext.instance().isActive()) : "Session context should no longer be active";
//      assert Boolean.FALSE.equals(RequestContext.instance().isActive()) : "Temporary request context should have been deactivated";
   }

   @BeforeMethod(groups = "broken")
   public void beforeMethod()
   {
//      RequestContext.instance().setBeanStore(null);
//      RequestContext.instance().setActive(false);
//      SessionContext.instance().setBeanStore(null);
//      SessionContext.instance().setActive(false);
   }
}
