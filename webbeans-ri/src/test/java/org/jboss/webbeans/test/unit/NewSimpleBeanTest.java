package org.jboss.webbeans.test.unit;

import java.util.Set;

import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.newbean.valid.WrappedSimpleBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpecVersion("20081222")
public class NewSimpleBeanTest extends AbstractTest
{
   private SimpleBean<WrappedSimpleBean> wrappedSimpleBean;
   private NewSimpleBean<WrappedSimpleBean> newSimpleBean;
   
   @BeforeMethod
   public void initNewBean() {
      wrappedSimpleBean = SimpleBean.of(WrappedSimpleBean.class, manager);
      manager.addBean(wrappedSimpleBean);
      newSimpleBean = NewSimpleBean.of(WrappedSimpleBean.class, manager);
      manager.addBean(newSimpleBean);
   }

   @Test(groups = { "new" })
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      assert newSimpleBean.getType().equals(WrappedSimpleBean.class);
   }

   @Test(groups = { "new" })
   public void testNewBeanIsSimpleWebBeanIfParameterTypeIsSimpleWebBean()
   {
      assert newSimpleBean.getType().equals(wrappedSimpleBean.getType());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameConstructorAsWrappedBean()
   {
      assert wrappedSimpleBean.getConstructor().equals(newSimpleBean.getConstructor());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      assert newSimpleBean.getInitializerMethods().equals(wrappedSimpleBean.getInitializerMethods());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      Set<AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedSimpleBean.getAnnotatedInjectionPoints();
      Set<AnnotatedItem<?, ?>> newBeanInjectionPoints = newSimpleBean.getAnnotatedInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
}
