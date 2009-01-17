package org.jboss.webbeans.test.unit.implementation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.util.Proxies.TypeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpecVersion("20081222")
public class NewEnterpriseBeanTest extends AbstractTest
{
   private EnterpriseBean<WrappedEnterpriseBean> wrappedEnterpriseBean;
   private NewEnterpriseBean<WrappedEnterpriseBean> newEnterpriseBean;
   
   @BeforeMethod
   public void initNewBean() {
      addToEjbCache(WrappedEnterpriseBean.class);
      wrappedEnterpriseBean = EnterpriseBean.of(WrappedEnterpriseBean.class, manager);
      manager.addBean(wrappedEnterpriseBean);
      newEnterpriseBean = NewEnterpriseBean.of(WrappedEnterpriseBean.class, manager);
      manager.addBean(newEnterpriseBean);
   }
   
   @Test(groups = { "new", "broken" })
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      assert newEnterpriseBean.getType().equals(WrappedEnterpriseBean.class);
   }

   @Test(groups = { "new" })
   public void testNewBeanIsEnterpriseWebBeanIfParameterTypeIsEnterpriseWebBean()
   {
      assert wrappedEnterpriseBean.getType().equals(newEnterpriseBean.getType());
      assert manager.getEjbDescriptorCache().containsKey(newEnterpriseBean.getType());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      assert newEnterpriseBean.getInitializerMethods().equals(wrappedEnterpriseBean.getInitializerMethods());
   }

   @Test(groups = { "new" })
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      Set<AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getAnnotatedInjectionPoints();
      Set<AnnotatedItem<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getAnnotatedInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
   @Test(groups = { "new" })
   public void testNewBeanHasNoDisposalMethods()
   {
      Class<?> type = TypeInfo.ofTypes(newEnterpriseBean.getTypes()).getSuperClass();
      assert manager.resolveDisposalMethods(type, newEnterpriseBean.getBindings().toArray(new Annotation[0])).isEmpty();
   }   
   
}
