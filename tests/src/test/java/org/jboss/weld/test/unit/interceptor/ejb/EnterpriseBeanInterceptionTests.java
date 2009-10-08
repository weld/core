package org.jboss.weld.test.unit.interceptor.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.model.InterceptionModel;
import static org.jboss.interceptor.model.InterceptionType.AROUND_INVOKE;
import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.test.AbstractWeldTest;

import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
@BeansXml("beans.xml")
public class EnterpriseBeanInterceptionTests extends AbstractWeldTest
{

   @Test(groups = { "interceptors"})
   public void testEnterpriseBeanInterceptionMetadataAdded() throws Exception
   {
      SessionBean<Ball> ballSessionBean = (SessionBean<Ball>)getCurrentManager().getBeans(Ball.class).iterator().next();
      InterceptionModel<Class<?>, Interceptor<?>> interceptionModel = getCurrentManager().getBoundInterceptorsRegistry().getInterceptionModel(ballSessionBean.getType());
      List<javax.enterprise.inject.spi.Interceptor> interceptors =
            new ArrayList<javax.enterprise.inject.spi.Interceptor>(interceptionModel.getAllInterceptors());

      assert interceptors.size() == 2;
      assert interceptors.get(0).getBeanClass().equals(Goalkeeper.class);
      assert interceptors.get(1).getBeanClass().equals(Defender.class);

      assert interceptionModel.getInterceptors(AROUND_INVOKE, ballSessionBean.getType().getMethod("shoot")).size() == 1;
      assert interceptionModel.getInterceptors(AROUND_INVOKE, ballSessionBean.getType().getMethod("shoot")).get(0).getBeanClass().equals(Goalkeeper.class);
      
      assert interceptionModel.getInterceptors(AROUND_INVOKE, ballSessionBean.getType().getMethod("pass")).size() == 1;
      assert interceptionModel.getInterceptors(AROUND_INVOKE, ballSessionBean.getType().getMethod("pass")).get(0).getBeanClass().equals(Defender.class);

   }

}
