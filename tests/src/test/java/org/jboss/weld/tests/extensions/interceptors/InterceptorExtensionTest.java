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
package org.jboss.weld.tests.extensions.interceptors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * Tests that interceptors registered via the SPI work correctly
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
@Artifact
@IntegrationTest
@Packaging(PackagingType.EAR)
@Extension("javax.enterprise.inject.spi.Extension")
@BeansXml("beans.xml")
@Classes(packages = { "org.jboss.weld.tests.util.annotated" })
public class InterceptorExtensionTest extends AbstractWeldTest
{
   @Test(groups={"broken"})
   public void testInterceptorCalled()
   {
      NumberSource ng = getReference(NumberSource.class);
      assert ng.value() == 2;
      assert IncrementingInterceptor.isDoAroundCalled();
   }

   @Test(groups={"broken"})
   @SuppressWarnings("unchecked")
   public void testLifecycleInterceptor()
   {
      Bean bean = getCurrentManager().getBeans(Marathon.class).iterator().next();
      CreationalContext creationalContext = getCurrentManager().createCreationalContext(bean);
      Marathon m = (Marathon)bean.create(creationalContext);
      
      assert LifecycleInterceptor.isPostConstructCalled();
      assert m.getLength()==42;
      bean.destroy(m, creationalContext);
      assert LifecycleInterceptor.isPreDestroyCalled();
   }

}
