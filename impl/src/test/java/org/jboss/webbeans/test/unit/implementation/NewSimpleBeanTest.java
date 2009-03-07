package org.jboss.webbeans.test.unit.implementation;

import java.util.Set;

import javax.inject.New;

import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.literal.NewLiteral;
import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NewSimpleBeanTest extends AbstractTest
{
   private SimpleBean<WrappedSimpleBean> wrappedSimpleBean;
   private NewSimpleBean<WrappedSimpleBean> newSimpleBean;
   
   private static final New NEW_LITERAL = new NewLiteral();
   
   @BeforeMethod
   public void initNewBean() {
      deployBeans(WrappedSimpleBean.class);
      
      assert manager.resolveByType(WrappedSimpleBean.class).size() == 1;
      assert manager.resolveByType(WrappedSimpleBean.class).iterator().next() instanceof SimpleBean;
      wrappedSimpleBean = (SimpleBean<WrappedSimpleBean>) manager.resolveByType(WrappedSimpleBean.class).iterator().next();
      
      assert manager.resolveByType(WrappedSimpleBean.class, NEW_LITERAL).size() == 1;
      assert manager.resolveByType(WrappedSimpleBean.class, NEW_LITERAL).iterator().next() instanceof NewSimpleBean;
      newSimpleBean = (NewSimpleBean<WrappedSimpleBean>) manager.resolveByType(WrappedSimpleBean.class, NEW_LITERAL).iterator().next();
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
      Set<? extends AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedSimpleBean.getInjectionPoints();
      Set<? extends AnnotatedItem<?, ?>> newBeanInjectionPoints = newSimpleBean.getInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
}
