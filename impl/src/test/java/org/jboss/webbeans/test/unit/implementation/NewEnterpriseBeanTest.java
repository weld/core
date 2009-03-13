package org.jboss.webbeans.test.unit.implementation;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.inject.New;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.literal.NewLiteral;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.jboss.webbeans.util.Proxies.TypeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class NewEnterpriseBeanTest extends AbstractWebBeansTest
{
   
   private static final New NEW_LITERAL = new NewLiteral();
   
   private EnterpriseBean<WrappedEnterpriseBean> wrappedEnterpriseBean;
   private NewEnterpriseBean<WrappedEnterpriseBean> newEnterpriseBean;
   
   @BeforeMethod
   public void initNewBean() {
      
      assert manager.resolveByType(WrappedEnterpriseBean.class).size() == 1;
      assert manager.resolveByType(WrappedEnterpriseBean.class).iterator().next() instanceof EnterpriseBean;
      wrappedEnterpriseBean = (EnterpriseBean<WrappedEnterpriseBean>) manager.resolveByType(WrappedEnterpriseBean.class).iterator().next();
      
      assert manager.resolveByType(WrappedEnterpriseBean.class, NEW_LITERAL).size() == 1;
      assert manager.resolveByType(WrappedEnterpriseBean.class, NEW_LITERAL).iterator().next() instanceof NewEnterpriseBean;
      newEnterpriseBean = (NewEnterpriseBean<WrappedEnterpriseBean>) manager.resolveByType(WrappedEnterpriseBean.class, NEW_LITERAL).iterator().next();
      
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
      Set<? extends AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getInjectionPoints();
      Set<? extends AnnotatedItem<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
   @Test(groups = { "new" })
   public void testNewBeanHasNoDisposalMethods()
   {
      Class<?> type = TypeInfo.ofTypes(newEnterpriseBean.getTypes()).getSuperClass();
      assert manager.resolveDisposalMethods(type, newEnterpriseBean.getBindings().toArray(new Annotation[0])).isEmpty();
   }   
   
}
