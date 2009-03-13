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
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class NewEnterpriseBeanTest extends AbstractWebBeansTest
{
   
   private static final New NEW_LITERAL = new NewLiteral();
   
   private EnterpriseBean<WrappedEnterpriseBeanLocal> wrappedEnterpriseBean;
   private NewEnterpriseBean<WrappedEnterpriseBeanLocal> newEnterpriseBean;
   
   public void initNewBean() {
      
      assert manager.resolveByType(WrappedEnterpriseBeanLocal.class).size() == 1;
      assert manager.resolveByType(WrappedEnterpriseBeanLocal.class).iterator().next() instanceof EnterpriseBean;
      wrappedEnterpriseBean = (EnterpriseBean<WrappedEnterpriseBeanLocal>) manager.resolveByType(WrappedEnterpriseBeanLocal.class).iterator().next();
      
      assert manager.resolveByType(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).size() == 1;
      assert manager.resolveByType(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next() instanceof NewEnterpriseBean;
      newEnterpriseBean = (NewEnterpriseBean<WrappedEnterpriseBeanLocal>) manager.resolveByType(WrappedEnterpriseBeanLocal.class, NEW_LITERAL).iterator().next();
      
   }
   
   @Test(groups = { "new", "broken" })
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      initNewBean();
      assert newEnterpriseBean.getType().equals(WrappedEnterpriseBeanLocal.class);
   }

   @Test(groups = { "new" })
   public void testNewBeanIsEnterpriseWebBeanIfParameterTypeIsEnterpriseWebBean()
   {
      initNewBean();
      assert wrappedEnterpriseBean.getType().equals(newEnterpriseBean.getType());
      assert manager.getEjbDescriptorCache().containsKey(newEnterpriseBean.getType());
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
      Set<? extends AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getInjectionPoints();
      Set<? extends AnnotatedItem<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
   @Test(groups = { "new" })
   public void testNewBeanHasNoDisposalMethods()
   {
      initNewBean();
      Class<?> type = TypeInfo.ofTypes(newEnterpriseBean.getTypes()).getSuperClass();
      assert manager.resolveDisposalMethods(type, newEnterpriseBean.getBindings().toArray(new Annotation[0])).isEmpty();
   }   
   
}
