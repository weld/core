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
package org.jboss.weld.tests.session.newBean;

import java.util.Set;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class NewEnterpriseBeanTest extends AbstractWeldTest
{
   
   private static final New NEW_LITERAL = new NewLiteral()
   {
      
      @Override
      public java.lang.Class<?> value() 
      {
         return WrappedEnterpriseBean.class;
      }
      
   };
   
   private SessionBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
   private NewSessionBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;
   
   public void initNewBean() 
   {
      Set<Bean<?>> beans = getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class);
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).size() == 1;
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof SessionBean;
      wrappedEnterpriseBean = (SessionBean<WrappedEnterpriseBeanLocal>) getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).iterator().next();
      
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewSessionBean;
      newEnterpriseBean = (NewSessionBean<WrappedEnterpriseBeanLocal>) getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
      
   }
   
   @Test(groups = { "new", "broken" })
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      initNewBean();
      assert newEnterpriseBean.getType().equals(WrappedEnterpriseBean.class);
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      initNewBean();
      assert newEnterpriseBean.getInitializerMethods().equals(wrappedEnterpriseBean.getInitializerMethods());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      initNewBean();
      Set<? extends WeldAnnotated<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getWeldInjectionPoints();
      Set<? extends WeldAnnotated<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getWeldInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
}
