package org.jboss.weld.test.managed.newBean;

import java.util.Set;

import javax.enterprise.inject.New;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class NewSimpleBeanTest extends AbstractWeldTest
{
   private ManagedBean<WrappedSimpleBean> wrappedSimpleBean;
   private NewManagedBean<WrappedSimpleBean> newSimpleBean;
   
   private static final New NEW_LITERAL = new NewLiteral();
   
   public void initNewBean() {
      
      assert getCurrentManager().getBeans(WrappedSimpleBean.class).size() == 1;
      assert getCurrentManager().getBeans(WrappedSimpleBean.class).iterator().next() instanceof ManagedBean;
      wrappedSimpleBean = (ManagedBean<WrappedSimpleBean>) getCurrentManager().getBeans(WrappedSimpleBean.class).iterator().next();
      
      assert getCurrentManager().getBeans(WrappedSimpleBean.class, NEW_LITERAL).size() == 1;
      assert getCurrentManager().getBeans(WrappedSimpleBean.class, NEW_LITERAL).iterator().next() instanceof NewManagedBean;
      newSimpleBean = (NewManagedBean<WrappedSimpleBean>) getCurrentManager().getBeans(WrappedSimpleBean.class, NEW_LITERAL).iterator().next();
   }

   @Test(groups = { "new" })
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      initNewBean();
      assert newSimpleBean.getType().equals(WrappedSimpleBean.class);
   }

   @Test(groups = { "new" })
   public void testNewBeanIsSimpleWebBeanIfParameterTypeIsSimpleWebBean()
   {
      initNewBean();
      assert newSimpleBean.getType().equals(wrappedSimpleBean.getType());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameConstructorAsWrappedBean()
   {
      initNewBean();
      assert wrappedSimpleBean.getConstructor().equals(newSimpleBean.getConstructor());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      initNewBean();
      assert newSimpleBean.getInitializerMethods().equals(wrappedSimpleBean.getInitializerMethods());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      initNewBean();
      Set<? extends WeldAnnotated<?, ?>> wrappedBeanInjectionPoints = wrappedSimpleBean.getAnnotatedInjectionPoints();
      Set<? extends WeldAnnotated<?, ?>> newBeanInjectionPoints = newSimpleBean.getAnnotatedInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
}
