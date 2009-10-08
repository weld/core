package org.jboss.weld.test.unit.implementation;

import java.util.Set;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.introspector.WBAnnotated;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class NewEnterpriseBeanTest extends AbstractWeldTest
{
   
   private static final New NEW_LITERAL = new NewLiteral()
   {
      
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
      Set<? extends WBAnnotated<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getAnnotatedInjectionPoints();
      Set<? extends WBAnnotated<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getAnnotatedInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
}
