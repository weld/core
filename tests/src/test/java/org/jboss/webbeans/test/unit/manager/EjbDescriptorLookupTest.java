/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.test.unit.manager;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * @author pmuir
 *
 */
@Artifact
@Packaging(PackagingType.EAR)
public class EjbDescriptorLookupTest extends AbstractWebBeansTest
{
   
   @Test
   public void testCorrectSubType()
   {
      EjbDescriptor<CatLocal> descriptor = getCurrentManager().getEjbDescriptor("Cat");
      assert descriptor.getClass().equals(InternalEjbDescriptor.class);
      Bean<CatLocal> bean = getCurrentManager().getBean(descriptor);
      assert bean != null;
      assert bean instanceof EnterpriseBean;
      assert bean.getBeanClass().equals(Cat.class);
      InjectionTarget<CatLocal> it = getCurrentManager().createInjectionTarget(descriptor);
      assert it != null;
      assert it instanceof EnterpriseBean;
      assert it == bean;
   }

}
