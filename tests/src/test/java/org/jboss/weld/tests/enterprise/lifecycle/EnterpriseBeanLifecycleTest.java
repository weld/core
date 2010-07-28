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
package org.jboss.weld.tests.enterprise.lifecycle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * Sections
 * 
 * 6.5. Lifecycle of stateful session beans 
 * 6.6. Lifecycle of stateless session and singleton beans 
 * 6.11. Lifecycle of EJBs
 * 
 * Mostly overlapping with other tests...
 * 
 * @author Nicklas Karlsson
 * @author David Allen
 * 
 * Spec version: Public Release Draft 2
 * 
 */
@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
public class EnterpriseBeanLifecycleTest extends AbstractWeldTest
{

   /**
    * When the create() method of a Bean object that represents a stateful
    * session bean that is called, the container creates and returns a session
    * bean proxy, as defined in Section 3.3.9, "Session bean proxies".
    */
   @Test(groups = { "enterpriseBeans", "clientProxy", "lifecycle", "integration" })
   public void testCreateSFSB()
   {
      GrossStadt frankfurt = getReference(GrossStadt.class);
      Bean<KleinStadt> stadtBean = getBean(KleinStadt.class);
      assert stadtBean != null : "Expected a bean for stateful session bean Kassel";
      CreationalContext<KleinStadt> creationalContext = new MockCreationalContext<KleinStadt>();
      KleinStadt stadtInstance = stadtBean.create(creationalContext);
      assert stadtInstance != null : "Expected instance to be created by container";
      assert frankfurt.isKleinStadtCreated() : "PostConstruct should be invoked when bean instance is created";
      frankfurt.resetCreatedFlags();
      
      // Create a second one to make sure create always does create a new session bean
      KleinStadt anotherStadtInstance = stadtBean.create(creationalContext);
      assert anotherStadtInstance != null : "Expected second instance of session bean";
      assert frankfurt.isKleinStadtCreated();
      assert anotherStadtInstance != stadtInstance : "create() should not return same bean as before";
      
      // Verify that the instance returned is a proxy by checking for all local interfaces
      assert stadtInstance instanceof KleinStadt;
      assert stadtInstance instanceof SchoeneStadt;
   }

   @Test(groups = { "enterpriseBeans", "clientProxy", "lifecycle", "integration" })
   public void testDestroyRemovesSFSB() throws Exception
   {
      GrossStadt frankfurt = getReference(GrossStadt.class);
      Bean<KleinStadt> stadtBean = getBean(KleinStadt.class);
      assert stadtBean != null : "Expected a bean for stateful session bean Kassel";
      RequestContext requestContext = Container.instance().services().get(ContextLifecycle.class).getRequestContext();
      CreationalContext<KleinStadt> creationalContext = new MockCreationalContext<KleinStadt>();
      KleinStadt kassel = requestContext.get(stadtBean, creationalContext);
      stadtBean.destroy(kassel, creationalContext);
      
      assert frankfurt.isKleinStadtDestroyed() : "Expected SFSB bean to be destroyed";
      requestContext.destroy();
      kassel = requestContext.get(stadtBean);
      assert kassel == null : "SFSB bean should not exist after being destroyed";
   }
   
   @Test
   public void testDestroyDoesntTryToRemoveSLSB()
   {
      Bean<BeanLocal> bean = getBean(BeanLocal.class);
      assert bean != null : "Expected a bean for stateless session bean BeanLocal";
      CreationalContext<BeanLocal> creationalContext = getCurrentManager().createCreationalContext(bean);
      BeanLocal instance = bean.create(creationalContext);
      bean.destroy(instance, creationalContext);
   }
 
}
