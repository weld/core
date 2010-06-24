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
package org.jboss.weld.tests.extensions.injectionTarget;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@IntegrationTest
@BeansXml("beans.xml")
@Packaging(PackagingType.EAR)
@Extension("javax.enterprise.inject.spi.Extension")
public class InjectionTargetTest extends AbstractWeldTest
{
   @Test(description="WELD-557")
   public void testActualInstanceAndNotProxyPassedToInject()
   {
      InjectionTargetWrapper.clear();
      Spitfire aircraft = getReference(Spitfire.class);
      aircraft.isFlying();
      assert aircraft.isTheSameInstance(InjectionTargetWrapper.injectInstance);
   }
   
   @Test(description="WELD-557")
   public void testActualInstanceAndNotProxyPassedToPostConstruct()
   {
      InjectionTargetWrapper.clear();
      Spitfire aircraft = getReference(Spitfire.class);
      aircraft.isFlying();
      assert aircraft.isTheSameInstance(InjectionTargetWrapper.postConstructInstance);
   }
   
   @Test(description="WELD-557")
   public void testActualInstanceAndNotProxyPassedToPreDestroy()
   {
      // prepare instance
      InjectionTargetWrapper.clear();
      Bean<Spitfire> bean = getBean(Spitfire.class);
      CreationalContext<Spitfire> ctx = getCurrentManager().createCreationalContext(bean);
      Spitfire aircraft = (Spitfire) getCurrentManager().getReference(bean, Spitfire.class, ctx);
      // invoke business method
      aircraft.isFlying();
      // destroy instance
      bean.destroy(aircraft, ctx);
      
      assert aircraft.isTheSameInstance(InjectionTargetWrapper.preDestroyInstance);
   }
}
