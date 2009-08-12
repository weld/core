package org.jboss.webbeans.test.unit.implementation;

import java.util.Set;

import javax.enterprise.inject.New;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.literal.NewLiteral;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class NewEnterpriseBeanTest extends AbstractWebBeansTest
{
   
   private static final New NEW_LITERAL = new NewLiteral()
   {
      
      public java.lang.Class<?> value() 
      {
         return WrappedEnterpriseBean.class;
      }
      
   };
   
   private EnterpriseBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
   private NewEnterpriseBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;
   
   public void initNewBean() {
      
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).size() == 1;
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof EnterpriseBean;
      wrappedEnterpriseBean = (EnterpriseBean<WrappedEnterpriseBeanLocal>) getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class).iterator().next();
      
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewEnterpriseBean;
      newEnterpriseBean = (NewEnterpriseBean<WrappedEnterpriseBeanLocal>) getCurrentManager().getBeans(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
      
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
